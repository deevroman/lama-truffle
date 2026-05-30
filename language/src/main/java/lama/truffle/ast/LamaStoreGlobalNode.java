package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.LamaContext;

public final class LamaStoreGlobalNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode valueNode;

    private final String name;

    public LamaStoreGlobalNode(String name, LamaExpressionNode valueNode) {
        this.name = name;
        this.valueNode = valueNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object value = valueNode.executeGeneric(frame);
        LamaContext.get(this).setGlobal(name, value);
        return value;
    }
}
