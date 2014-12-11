/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.auraframework.test.TestExecutor;
import org.auraframework.test.TestExecutor.TestRun;
import org.auraframework.test.TestInventory;
import org.auraframework.test.TestInventory.Type;
import org.auraframework.test.perf.PerfUtil;
import org.auraframework.test.WebDriverProvider;
import org.auraframework.throwable.AuraRuntimeException;
import org.auraframework.util.AuraUtil;
import org.auraframework.util.ServiceLocator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Run all integration tests.
 * 
 * If the "testNameContains" system property is set, filter tests to run only those tests containing the provided string
 * (case-insensitive).
 * 
 * If the "runPerfTests" system property is set, it will run the tests that are annotated with @PerfTest
 */
public class AuraIntegrationTests extends TestSuite {
    public static final boolean TEST_SHUFFLE = Boolean.parseBoolean(System.getProperty("testShuffle", "false"));
    public static final int TEST_ITERATIONS;
    private static final Logger logger = Logger.getLogger(AuraIntegrationTests.class.getName());

    private final String nameFragment;
    private static final boolean RUN_PERF_TESTS = System.getProperty("runPerfTests") != null;

    private final List<String> skipTests;
    private final List<String> testsToRun;

    static {
        int numIterations = 0;
        try {
            numIterations = Integer.parseInt(System.getProperty("testIterations"));
        } catch (Throwable t) {
        }
        TEST_ITERATIONS = Math.max(1, numIterations);
    }

    public static TestSuite suite() {
        return new AuraIntegrationTests();
    }

    private AuraIntegrationTests() {
        String frag = System.getProperty("testNameContains");
        if (frag != null && !frag.trim().equals("")) {
            nameFragment = frag.toLowerCase();
        } else {
            nameFragment = null;
        }

        // comma-delimited list of fully qualified test classes (case-insensitive), e.g. my.test.TestClass
        skipTests = ImmutableList.copyOf(System.getProperty("skipIntTests", "").toLowerCase().trim().split("\\s,\\s"));
        // Ivan: experiment with test names list
        testsToRun = ImmutableList.copyOf(System.getProperty("testsToRun", "").toLowerCase().trim().split(","));
    }

    /**
     * Run integration tests in parallel.
     */
    @Override
    public void run(final TestResult masterResult) {
        logger.info("Building test inventories");
        if (nameFragment != null) {
            logger.info("Filtering by test names containing: " + nameFragment);
        }
        if (RUN_PERF_TESTS) {
            logger.info("Filtering only test annotated with @PerfTest");
        }
        Set<TestInventory> inventories = ServiceLocator.get().getAll(TestInventory.class);
        List<Callable<TestResult>> queue = Lists.newLinkedList();
        List<Callable<TestResult>> hostileQueue = Lists.newLinkedList();
        for (TestInventory inventory : inventories) {
            for (Type type : new Type[] { Type.INTEGRATION, Type.WEB }) {
                TestSuite child = inventory.getTestSuite(type);
                if (child != null) {
                    for (Enumeration<?> tests = child.tests(); tests.hasMoreElements();) {
                        queueTest((Test) tests.nextElement(), masterResult, queue, hostileQueue);
                    }
                }
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(TestExecutor.NUM_THREADS);
        try {
            executeIterations(executor, queue, "parallelizable");
            executeIterations(executor, hostileQueue, "thread hostile");
        } catch (InterruptedException e) {
            throw new AuraRuntimeException("TEST RUN INTERRUPTED", e);
        } finally {
            executor.shutdown();
            WebDriverProvider provider = AuraUtil.get(WebDriverProvider.class);
            if (provider != null) {
                logger.info("Releasing WebDriver resources");
                provider.release();
            }
        }
    }

    private void executeIterations(ExecutorService executor, List<Callable<TestResult>> tests, String testType)
            throws InterruptedException {
        logger.info(String.format("Executing %s tests with %s thread(s), %s iteration(s), %s", testType,
                TestExecutor.NUM_THREADS, TEST_ITERATIONS, TEST_SHUFFLE ? "random order" : "in order"));
        long start = System.currentTimeMillis();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            if (TEST_SHUFFLE) {
                Collections.shuffle(tests);
            }
            executor.invokeAll(tests);
        }
        logger.info(String.format("Completed %s tests in %s ms", tests.size() * TEST_ITERATIONS,
                System.currentTimeMillis() - start));
    }

    private void queueTest(final Test test, final TestResult result, Collection<Callable<TestResult>> queue,
            Collection<Callable<TestResult>> hostileQueue) {
        // queue up TestCases individually so they can be fully parallelized (vs. per-suite)
        if (test instanceof TestSuite) {
            TestSuite suite = (TestSuite) test;
            for (int i = 0; i < suite.testCount(); i++) {
                queueTest(suite.testAt(i), result, queue, hostileQueue);
            }
        } else if (test instanceof TestCase) {
            String caseName = ((TestCase) test).getName().toLowerCase();
            String testClassName = test.getClass().getName().toLowerCase();
            String testName = testClassName + "." + caseName;
            if (nameFragment != null) {
                if (!testName.contains(nameFragment)) {
                    return;
                }
            // IPL: The following block was added by Ivan
            } else if (!testsToRun.isEmpty()) {
                String suiteName = testClassName.substring(testClassName.lastIndexOf('.') + 1);
                if (!testsToRun.contains(suiteName)) {
                    return;
                }
            } else if (!skipTests.isEmpty() && skipTests.contains(testName)) {
                return;
            }
            if (RUN_PERF_TESTS) {
                if (!PerfUtil.hasPerfTestAnnotation((TestCase) test)) {
                    return;
                }
            }
            TestRun callable = new TestRun(test, result);
            if (TestExecutor.isThreadHostile(test)) {
                hostileQueue.add(callable);
            } else {
                queue.add(callable);
            }
        }
    }
}
