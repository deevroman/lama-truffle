package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LamaConstantNode extends LamaExpressionNode {
    private final Object value;

    public LamaConstantNode(Object value) {
        this.value = value;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return value;
    }
}
