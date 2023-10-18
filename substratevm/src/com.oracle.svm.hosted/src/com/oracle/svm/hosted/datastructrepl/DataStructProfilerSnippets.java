package com.oracle.svm.hosted.datastructrepl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.oracle.svm.core.snippets.SnippetRuntime;
import com.oracle.svm.core.snippets.SubstrateForeignCallTarget;

public class DataStructProfilerSnippets {

    static class AverageCalculator {

        private int samples;
        private long sum;

        public void update(int partial) {
            sum = sum + partial;
            samples = samples + 1;
        }

        public int average() {
            return samples == 0 ? 0 : (int) sum / samples;
        }

        @Override
        public String toString() {
            return String.format("%s", average());
        }
    }

    static class AverageCalculatorComparator implements Comparator<AverageCalculator> {

        @Override
        public int compare(AverageCalculator o1, AverageCalculator o2) {
            return o1.average() - o2.average();
        }

    }

    public static final SnippetRuntime.SubstrateForeignCallDescriptor PROFILE_NEW_INSTANCE = SnippetRuntime.findForeignCall(DataStructProfilerSnippets.class, "profileNewInstance", false);
    public static final SnippetRuntime.SubstrateForeignCallDescriptor PROFILE_FIELD_LOAD = SnippetRuntime.findForeignCall(DataStructProfilerSnippets.class, "profileFieldLoad", false);
    public static final SnippetRuntime.SubstrateForeignCallDescriptor[] FOREIGN_CALLS = new SnippetRuntime.SubstrateForeignCallDescriptor[]{PROFILE_NEW_INSTANCE, PROFILE_FIELD_LOAD};

    private static int newInstanceCounter = 0;
    private static Map<Integer, Integer> sizesPerObjectHashcode = new ConcurrentHashMap<>();
    private static Map<Integer, AverageCalculator> sizesPerObjectFieldId = new ConcurrentHashMap<>();

    @SubstrateForeignCallTarget(stubCallingConvention = false)
    public static void profileNewInstance() {
        newInstanceCounter += 1;
    }

    @SubstrateForeignCallTarget(stubCallingConvention = false)
    public static void profileFieldLoad(Object object, int fieldId) {

        if (object != null && (object instanceof HashMap || object instanceof ConcurrentHashMap)) {
            Integer hashcode = System.identityHashCode(object);
            Integer size = ((Map) object).size();
            sizesPerObjectHashcode.put(hashcode, size);
            if (!sizesPerObjectFieldId.containsKey(fieldId)) {
                sizesPerObjectFieldId.put(fieldId, new AverageCalculator());
            }
            sizesPerObjectFieldId.get(fieldId).update(size);
        }
    }

    private static void dumpNewInstanceCounter() {
        System.out.println(String.format("[DataStructProfilerSnippets:dumpNewInstanceCounter] Found %s instancesof Map.", newInstanceCounter));
    }

    private static void dumpSizesPerObjectHashcode() {
        String tag = "dumpSizesPerObjectHashcode";
        System.out.println(String.format("[DataStructProfilerSnippets:%s] Found %s Map objects.", tag, sizesPerObjectHashcode.size()));

        HashMap<Integer, Integer> cardinalityPerSize = new HashMap<>();
        for (Integer size : sizesPerObjectHashcode.values()) {
            if (!cardinalityPerSize.containsKey(size)) {
                cardinalityPerSize.put(size, 1);
            } else {
                cardinalityPerSize.put(size, cardinalityPerSize.get(size) + 1);
            }
        }

        List<Integer> sortedSizes = new ArrayList<>(cardinalityPerSize.keySet());
        Collections.sort(sortedSizes);
        for (Integer size : sortedSizes) {
            System.out.println(String.format("[DataStructProfilerSnippets:%s] Found %s Maps with %s elements.", tag, cardinalityPerSize.get(size), size));
        }
    }

    private static void dumpSizesPerObjectFieldId() {
        String tag = "dumpSizesPerObjectFieldId";
        System.out.println(String.format("[DataStructProfilerSnippets:%s] Found %s Map fields.", tag, sizesPerObjectFieldId.size()));

        HashMap<Integer, Integer> cardinalityPerSize = new HashMap<>();
        for (AverageCalculator size : sizesPerObjectFieldId.values()) {
            int average = size.average();
            if (!cardinalityPerSize.containsKey(average)) {
                cardinalityPerSize.put(average, 1);
            } else {
                cardinalityPerSize.put(average, cardinalityPerSize.get(average) + 1);
            }
        }

        List<Integer> sortedSizes = new ArrayList<>(cardinalityPerSize.keySet());
        Collections.sort(sortedSizes);
        for (Integer size : sortedSizes) {
            System.out.println(String.format("[DataStructProfilerSnippets:%s] Found %s Maps with %s elements.", tag, cardinalityPerSize.get(size), size));
        }
    }

    public static Runnable dumpProfileResults() {
        return () -> {
            dumpNewInstanceCounter();
            dumpSizesPerObjectHashcode();
            dumpSizesPerObjectFieldId();
        };
    }
}
