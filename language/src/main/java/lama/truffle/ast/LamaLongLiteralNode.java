package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LamaLongLiteralNode extends LamaExpressionNode {
    private final long value;

    public LamaLongLiteralNode(long value) {
        this.value = value;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return value;
    }

    @Override
    public long executeLong(VirtualFrame frame) {
        return value;
    }
}
