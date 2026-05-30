package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.runtime.LamaClosure;

public final class LamaStoreUpvalueNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode valueNode;

    private final int slot;

    public LamaStoreUpvalueNode(int slot, LamaExpressionNode valueNode) {
        this.valueNode = valueNode;
        this.slot = slot;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object value = valueNode.executeGeneric(frame);
        LamaClosure closure = (LamaClosure) frame.getArguments()[0];
        closure.captures()[slot] = value;
        return value;
    }
}
