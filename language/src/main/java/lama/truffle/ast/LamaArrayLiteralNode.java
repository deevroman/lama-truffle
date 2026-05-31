package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import lama.truffle.runtime.LamaArray;

public final class LamaArrayLiteralNode extends LamaExpressionNode {
    @Children
    private final LamaExpressionNode[] elementNodes;

    public LamaArrayLiteralNode(LamaExpressionNode[] elementNodes) {
        this.elementNodes = elementNodes;
    }

    @Override
    @ExplodeLoop
    public Object executeGeneric(VirtualFrame frame) {
        Object[] elements = new Object[elementNodes.length];
        for (int i = 0; i < elementNodes.length; i++) {
            elements[i] = elementNodes[i].executeGeneric(frame);
        }
        return new LamaArray(elements);
    }
}
