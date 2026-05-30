package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LamaStoreLocalNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode valueNode;

    private final int slot;

    public LamaStoreLocalNode(int slot, LamaExpressionNode valueNode) {
        this.slot = slot;
        this.valueNode = valueNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object value = valueNode.executeGeneric(frame);
        frame.setObject(slot, value);
        return value;
    }
}
