/*
 * Copyright (c) 2016, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.graalvm.compiler.hotspot.replacements;

import static org.graalvm.compiler.hotspot.meta.HotSpotForeignCallsProviderImpl.INT_STREAM_SUM;
import static org.graalvm.compiler.hotspot.replacements.HotSpotReplacementsUtil.intStreamSum;
import static org.graalvm.compiler.nodes.extended.BranchProbabilityNode.NOT_FREQUENT_PROBABILITY;
import static org.graalvm.compiler.nodes.extended.BranchProbabilityNode.probability;

import org.graalvm.compiler.api.replacements.Snippet;
import org.graalvm.compiler.debug.DebugHandlersFactory;
import org.graalvm.compiler.hotspot.meta.HotSpotProviders;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.spi.LoweringTool;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.replacements.SnippetTemplate;
import org.graalvm.compiler.replacements.SnippetTemplate.AbstractTemplates;
import org.graalvm.compiler.replacements.SnippetTemplate.Arguments;
import org.graalvm.compiler.replacements.SnippetTemplate.SnippetInfo;
import org.graalvm.compiler.replacements.Snippets;

import jdk.vm.ci.code.TargetDescription;

public class IntStreamSumSnippets implements Snippets {

    @Snippet
    public static int intStreamSumSnippet(final Object thisObj) {
        if (probability(NOT_FREQUENT_PROBABILITY, thisObj == null)) {
            return 0;
        }
        return computeIntStreamSum(thisObj);
    }

    static int computeIntStreamSum(final Object x) {
        return intStreamSum(INT_STREAM_SUM, x);
    }

    public static class Templates extends AbstractTemplates {

        private final SnippetInfo intStreamSumSnippet = snippet(IntStreamSumSnippets.class, "intStreamSumSnippet", HotSpotReplacementsUtil.MARK_WORD_LOCATION);

        public Templates(OptionValues options, Iterable<DebugHandlersFactory> factories, HotSpotProviders providers, TargetDescription target) {
            super(options, factories, providers, providers.getSnippetReflection(), target);
        }

        public void lower(IntStreamSumNode node, LoweringTool tool) {
            StructuredGraph graph = node.graph();
            Arguments args = new Arguments(intStreamSumSnippet, graph.getGuardsStage(), tool.getLoweringStage());
            args.add("thisObj", node.object);
            SnippetTemplate template = template(node, args);
            template.instantiate(providers.getMetaAccess(), node, SnippetTemplate.DEFAULT_REPLACER, args);
        }

    }

}
