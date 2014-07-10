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
package org.auraframework.test.perf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.auraframework.test.perf.metrics.PerfMetrics;
import org.auraframework.util.IOUtil;
import org.auraframework.util.test.PerfGoldFilesUtil;
import org.json.JSONObject;

import com.google.common.base.Charsets;

/**
 * Utility methods related to the results generated by the perf runs.
 */
public final class PerfResultsUtil {

    private static final Logger LOG = Logger.getLogger(PerfResultsUtil.class.getSimpleName());

    public static final File RESULTS_DIR;

    static {
        RESULTS_DIR = new File(System.getProperty("aura.perf.results.dir", "target/perf/results"));
        LOG.info("perf results dir: " + RESULTS_DIR.getAbsolutePath());
        RESULTS_DIR.mkdirs();
    }

    /**
     * @return the written file
     */
    public static File writeGoldFile(PerfMetrics metrics, String fileName, boolean storeDetails) {
        File file = new File(RESULTS_DIR + "/goldfiles/" + fileName + ".json");
        return writeFile(file, PerfGoldFilesUtil.toGoldFileText(metrics, storeDetails), "goldfile");
    }

    /**
     * @return the written file
     */
    public static File writeAuraStats(String auraStatsContents, String fileName) {
        File file = new File(RESULTS_DIR + "/aurastats/" + fileName + "_aurastats.json");
        return writeFile(file, auraStatsContents, "Aura Stats");
    }

    private static File writeFile(File file, String contents, String what) {
        OutputStreamWriter writer = null;
        try {
            IOUtil.mkdirs(file.getParentFile());
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            writer.write(contents);
            LOG.info("wrote " + what + ": " + file.getAbsolutePath());
            return file;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "error writing " + file.getAbsolutePath(), e);
            return file;
        } finally {
            IOUtil.close(writer);
        }
    }

    /**
     * Writes the dev tools log for a perf test run to
     * System.getProperty("aura.perf.results.dir")/timelines/testName_timeline.json
     * 
     * @return the written file
     */
    public static File writeDevToolsLog(List<JSONObject> timeline, String fileName, String userAgent) {
        File file = new File(RESULTS_DIR + "/timelines/" + fileName + "_timeline.json");
        try {
            writeDevToolsLog(timeline, file, userAgent);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "error writing " + file.getAbsolutePath(), e);
        }
        return file;
    }

    private static void writeDevToolsLog(List<JSONObject> timeline, File file, String userAgent) throws Exception {
        BufferedWriter writer = null;
        try {
            file.getParentFile().mkdirs();
            writer = new BufferedWriter(new FileWriter(file));
            writer.write('[');
            writer.write(JSONObject.quote(userAgent));
            for (JSONObject entry : timeline) {
                writer.write(',');
                writer.newLine();
                writer.write(entry.toString());
            }
            writer.write("]");
            writer.newLine();
            LOG.info("wrote dev tools log: " + file.getAbsolutePath());
        } finally {
            IOUtil.close(writer);
        }
    }

    /**
     * Writes the JavaScript CPU profile data for a perf test run to
     * System.getProperty("aura.perf.results.dir")/profiles/testName_profile.cpuprofile
     * 
     * @return the written file
     */
    public static File writeJSProfilerData(Map<String, ?> jsProfilerData, String fileName) {
        File file = new File(RESULTS_DIR + "/profiles/" + fileName + "_profile.cpuprofile");
        try {
            file.getParentFile().mkdirs();
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(file));
                writer.write(new JSONObject(jsProfilerData).toString());
                LOG.info("wrote JavaScript CPU profile: " + file.getAbsolutePath());
            } finally {
                IOUtil.close(writer);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "error writing " + file.getAbsolutePath(), e);
        }
        return file;
    }

    // JS heap snapshot

    /**
     * Writes the heap snapshot into a file, this file can be loaded into chrome dev tools -> Profiles -> Load
     * 
     * @return the written file
     */
    @SuppressWarnings("unchecked")
    public static File writeHeapSnapshot(Map<String, ?> data, String fileName) throws Exception {
        File file = new File(RESULTS_DIR + "/heaps/" + fileName + "_heap.heapsnapshot");
        BufferedWriter writer = null;
        try {
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);

            // write using same format as CDT Save:
            // https://developers.google.com/chrome-developer-tools/docs/heap-profiling
            writer = new BufferedWriter(new OutputStreamWriter(out, Charsets.US_ASCII));
            writer.write('{');
            writer.write(JSONObject.quote("snapshot"));
            writer.write(':');
            new JSONObject((Map<String, ?>) data.get("snapshot")).write(writer);
            writer.write(',');
            writer.newLine();
            writeList(writer, "nodes", (List<?>) data.get("nodes"), 5, false);
            writeList(writer, "edges", (List<?>) data.get("edges"), 3, false);
            writeList(writer, "trace_function_infos", (List<?>) data.get("trace_function_infos"), 1, false);
            writeList(writer, "trace_tree", (List<?>) data.get("trace_tree"), 1, false);
            writeList(writer, "strings", (List<?>) data.get("strings"), 1, true);
            writer.write('}');

            LOG.info("wrote heap snapshot: " + file.getAbsolutePath());
        } finally {
            IOUtil.close(writer);
        }
        return file;
    }

    static void writeList(BufferedWriter writer, String key, List<?> list, int numPerLine, boolean last)
            throws IOException {
        writer.write(JSONObject.quote(key));
        writer.write(':');
        writer.write('[');
        for (int i = 0; i < list.size(); i++) {
            Object entry = list.get(i);
            if (i > 0) {
                if (numPerLine > 1) {
                    if (i % numPerLine == 0) {
                        writer.newLine();
                    }
                    writer.write(',');
                } else {
                    writer.write(',');
                    writer.newLine();
                }
            }
            if (entry instanceof String) {
                writer.write(JSONObject.quote((String) entry));
            } else {
                writer.write(entry.toString());
            }
        }
        if (numPerLine > 1) {
            writer.newLine();
        }
        writer.write("]");
        if (!last) {
            writer.write(',');
            writer.newLine();
        }
    }
}