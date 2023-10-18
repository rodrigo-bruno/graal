package com.oracle.svm.hosted.datastructrepl;

import org.graalvm.compiler.graph.Node;
import org.graalvm.compiler.nodes.ConstantNode;
import org.graalvm.compiler.nodes.FrameState;
import org.graalvm.compiler.nodes.PhiNode;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.ValueNode;
import org.graalvm.compiler.nodes.calc.IsNullNode;
import org.graalvm.compiler.nodes.extended.ForeignCallNode;
import org.graalvm.compiler.nodes.java.FinalFieldBarrierNode;
import org.graalvm.compiler.nodes.java.LoadFieldNode;
import org.graalvm.compiler.nodes.java.NewInstanceNode;
import org.graalvm.compiler.nodes.java.StoreFieldNode;
import org.graalvm.compiler.phases.Phase;

import com.oracle.graal.pointsto.meta.AnalysisField;
import com.oracle.svm.core.nodes.SubstrateMethodCallTargetNode;
import com.oracle.svm.hosted.meta.HostedField;

public class DataStructProfilerPhase extends Phase {

    private static boolean canBeHashMap(String typename) {
        switch (typename) {
            case "Ljava/util/HashMap;":
            case "Ljava/util/concurrent/ConcurrentHashMap;":
            case "Ljava/util/concurrent/ConcurrentMap;":
            case "Ljava/util/AbstractMap;":
            case "Ljava/util/Map;":
                return true;
            default:
                return false;
        }
    }

    /*-
     * We track NewInstanceNode. Objects can escape into:
     * - StoreField (as value)
     * - Return
     * - MethodCall (as argument
     *
     * We need call graph analysis to continue looking into Return and MethodCall
     *
     * We track LoadFieldNode. Objects can escape into:
     * - Phi...
     *
     * We need to
     */

    private static void printNewInstanceNodeUsage(NewInstanceNode nin, Node usage) {
        String prefix = "[DataStructProfiler] \t NewInstanceNode usage:";

        if (usage instanceof SubstrateMethodCallTargetNode) {
            SubstrateMethodCallTargetNode casted = (SubstrateMethodCallTargetNode) usage;
            System.out.println(String.format("%s method call %s (as %s)",
                            prefix,
                            casted.targetName(),
                            casted.arguments().first() == nin ? "target" : "argument"));
        } else if (usage instanceof StoreFieldNode) {
            StoreFieldNode casted = (StoreFieldNode) usage;
            System.out.println(String.format("%s field store %s:%s (as value)",
                            prefix,
                            casted.field().getDeclaringClass().getName(),
                            casted.field().getName()));
        } else {
            System.out.println(String.format("%s %s", prefix, usage));
        }
    }

    private static void printLoadFieldNodeUsage(LoadFieldNode lfn, Node usage) {
        String prefix = "[DataStructProfiler] \t LoadFieldNode usage:";

        if (usage instanceof PhiNode) {
            for (Node phiUsage : lfn.usages()) {
                System.out.println(String.format("%s (after Phi) %s", prefix, phiUsage));
            }
        }

        System.out.println(String.format("%s %s", prefix, usage));
    }

    @Override
    protected void run(StructuredGraph graph) {
        for (Node n : graph.getNodes()) {

            if (n instanceof NewInstanceNode) {
                NewInstanceNode nin = (NewInstanceNode) n;
                if (canBeHashMap(nin.instanceClass().getName())) {
                    System.out.println(String.format("[DataStructProfiler] NewInstanceNode in %s:%s",
                                    graph.method().getDeclaringClass().getName(),
                                    graph.method().getName()));
                    for (Node usage : nin.usages()) {
                        // Skip if node is used in FrameState or FinalFieldBarrierNode
                        if (usage instanceof FrameState || usage instanceof FinalFieldBarrierNode) {
                            continue;
                        }

                        // Skip if node is used in store but not as a value.
                        if (usage instanceof StoreFieldNode && ((StoreFieldNode) usage).value() != nin) {
                            continue;
                        }

                        printNewInstanceNodeUsage(nin, usage);
                    }
                    ForeignCallNode fcn = graph.add(new ForeignCallNode(DataStructProfilerSnippets.PROFILE_NEW_INSTANCE, new ValueNode[]{}));
                    graph.addBeforeFixed(nin, fcn);
                }
            }

            if (n instanceof LoadFieldNode) {
                LoadFieldNode lfn = (LoadFieldNode) n;
                if (canBeHashMap(lfn.field().getType().getName())) {
                    System.out.println(String.format("[DataStructProfiler] installing profiler for LoadFieldNode in %s:%s",
                                    lfn.field().getDeclaringClass().getName(),
                                    lfn.field().getName()));
                    for (Node usage : lfn.usages()) {
                        // Skip if node is used a null check
                        if (usage instanceof IsNullNode) {
                            continue;
                        }

                        printLoadFieldNodeUsage(lfn, usage);
                    }

                    AnalysisField af = ((HostedField) lfn.field()).wrapped;
                    ValueNode[] args = new ValueNode[]{lfn, ConstantNode.forInt(af.getId(), graph)};
                    ForeignCallNode fcn = graph.add(new ForeignCallNode(DataStructProfilerSnippets.PROFILE_FIELD_LOAD, args));
                    graph.addBeforeFixed(lfn, fcn);
                }
            }
        }
    }
}
