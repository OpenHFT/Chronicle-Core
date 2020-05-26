/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class MonitorProfileAnalyserMain {

    private static final int MAX_LINES = Integer.getInteger("st.maxlines", 8);
    private static final String PROFILE_OF_THE_THREAD = "profile of the thread";
    private static final String THREAD_HAS_BLOCKED_FOR = "thread has blocked for";

    /**
     * Reads one or more log files and looks for thread profiles to summarise
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0)
            System.err.println("No input file(s) provided");

        final String stIgnore = System.getProperty("st.ignore");
        List<String> ignoreSubStrings = stIgnore != null ? Arrays.asList(stIgnore.split(",")) : Collections.emptyList();
        int interval = Integer.getInteger("interval", 0);
        if (interval <= 0) {
            main0(ignoreSubStrings, args);
        } else {
            for (; ; ) {
                main0(ignoreSubStrings, args);
                JitterSampler.sleepSilently(interval * 1000);
                System.out.println("\n---\n");
            }
        }
    }

    public static void main0(List<String> ignoreSubStrings, String[] args) throws IOException {
        System.out.println("Grouped by line");
        Map<String, Integer> stackCount = new LinkedHashMap<>();

        for (String arg : args) {
            StringBuilder sb = new StringBuilder();
            int lineCount = -1;
            try (BufferedReader br = Files.newBufferedReader(Paths.get(arg))) {
                // TODO: PrintGCApplicationStoppedTime

                for (String line; (line = br.readLine()) != null; ) {
                    if (line.contains(PROFILE_OF_THE_THREAD) || line.contains(THREAD_HAS_BLOCKED_FOR)) {
                        if (sb.length() > 0) {
                            addToStackCount(ignoreSubStrings, stackCount, sb);
                        }
                        lineCount = 0;
                        sb.setLength(0);

                    } else if (partOfStackTrace(line) && lineCount >= 0) {
                        if (++lineCount <= MAX_LINES) {
                            sb.append(line).append("\n");
                        }
                    } else if (sb.length() > 0) {
                        addToStackCount(ignoreSubStrings, stackCount, sb);
                        sb.setLength(0);
                    }
                }
            }
        }
        List<Map.Entry<String, Integer>> stackSortedByCount =
                stackCount.entrySet().stream()
                        .filter(e -> e.getValue() > 2)
                        .sorted(Comparator.comparing(e -> -e.getValue())) // reversed
                        .limit(20)
                        .collect(Collectors.toList());
        stackSortedByCount
                .stream()
                .peek(e -> stackCount.remove(e.getKey()))
                .forEach(e -> System.out.println(e.getValue() + e.getKey()));

        System.out.println("Grouped by method.");
        Map<String, Integer> methodCount = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : stackCount.entrySet()) {
            String stack = entry.getKey().replaceFirst("\\(.*?\\)", "( * )");
            methodCount.compute(stack, (k, v) -> (v == null ? 0 : v) + entry.getValue());
        }

        List<Map.Entry<String, Integer>> methodSortedByCount =
                methodCount.entrySet().stream()
                        .filter(e -> e.getValue() > 2)
                        .sorted(Comparator.comparing(e -> -e.getValue())) // reversed
                        .limit(20)
                        .collect(Collectors.toList());
        methodSortedByCount
                .forEach(e -> System.out.println(e.getValue() + e.getKey()));
    }

    private static void addToStackCount(List<String> ignoreSubStrings, Map<String, Integer> stackCount, StringBuilder sb) {
        String lines = sb.toString();
        for (String ss : ignoreSubStrings)
            if (lines.contains(ss))
                return;
        stackCount.compute(lines, (k, v) -> v == null ? 1 : v + 1);
    }

    private static boolean partOfStackTrace(String line) {
        // make this more robust in the face of copy/pasting etc.
        return line.startsWith("\tat ") || line.matches("\\s+at .*");
    }
}
