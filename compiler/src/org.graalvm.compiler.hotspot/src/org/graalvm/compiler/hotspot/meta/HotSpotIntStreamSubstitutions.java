package org.graalvm.compiler.hotspot.meta;

import java.lang.reflect.Field;
import java.util.Spliterator.OfInt;
import java.util.stream.IntStream;

import org.graalvm.compiler.api.replacements.ClassSubstitution;
import org.graalvm.compiler.api.replacements.MethodSubstitution;
import org.graalvm.compiler.core.common.spi.ForeignCallDescriptor;
import org.graalvm.compiler.graph.Node.ConstantNodeParameter;
import org.graalvm.compiler.graph.Node.NodeIntrinsic;
import org.graalvm.compiler.hotspot.replacements.IdentityHashCodeNode;
import org.graalvm.compiler.nodes.extended.ForeignCallNode;

@ClassSubstitution(className = "java.util.stream.IntPipeline")
public class HotSpotIntStreamSubstitutions {

    @SuppressWarnings("unused")
    @MethodSubstitution(isStatic = false)
    public static int sum(Object receiver) {
        try {
            // TODO - All non-recursive calls in the intrinsic sum(Object) must be inlined or
            // intrinsified: found call to IntStream.spliterator()
            OfInt spliterator = ((IntStream) receiver).spliterator();
            Field array = spliterator.getClass().getDeclaredField("array");
            array.setAccessible(true);
            return intStreamSum(HotSpotHostForeignCallsProvider.INT_STREAM_SUM, array.get(receiver));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return 0;
        }
    }

    @NodeIntrinsic(ForeignCallNode.class)
    public static native int intStreamSum(@ConstantNodeParameter ForeignCallDescriptor descriptor, Object o);
}