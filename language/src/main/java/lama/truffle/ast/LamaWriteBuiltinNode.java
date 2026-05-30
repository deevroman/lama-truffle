package lama.truffle.ast;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.LamaContext;

public final class LamaWriteBuiltinNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode valueNode;

    public LamaWriteBuiltinNode(LamaExpressionNode valueNode) {
        this.valueNode = valueNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return executeLong(frame);
    }

    @Override
    public long executeLong(VirtualFrame frame) {
        long value = valueNode.executeLong(frame);
        write(LamaContext.get(this), value);
        return 0;
    }

    @CompilerDirectives.TruffleBoundary
    private static void write(LamaContext context, long value) {
        context.output.println(value);
    }
}
