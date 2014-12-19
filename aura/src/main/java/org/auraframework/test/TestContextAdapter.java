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
package org.auraframework.test;

import org.auraframework.ds.serviceloader.AuraServiceProvider;

/**
 * Provide TestContexts to persist the state of tests across multiple requests.
 */
public interface TestContextAdapter extends AuraServiceProvider {

	/**
	 * Get the current TestContext, or null if one hasn't been created.
	 * 
	 * @return the current TestContext, null if it doesn't exist
	 */
	TestContext getTestContext();

	/**
	 * Locate a TestContext by name, which should be unique across all tests in
	 * the system. If the TestContext is not found, create one with the given
	 * name.
	 * 
	 * @param name
	 *            the name of TestContext
	 * @return the TestContext identified by name
	 */
	TestContext getTestContext(String name);

	/**
	 * release current testContext, ALSO remove it from context map 
	 * this function calls clear().
	 */
	void release();

	/**
	 * clear current TestContext.
	 * Aura.get(TestContextAdapter.class).getTestContext() will return null after this.
	 * but testContext for the current test is still in the context map, which means we can get it by getTextContext(testname).
	 * if we want current testContex to be removed from the map, call release() instead.
	 */
	void clear();
}
