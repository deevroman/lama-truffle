package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import lama.truffle.runtime.LamaSexp;

public final class LamaSexpNode extends LamaExpressionNode {
    private final String tag;

    @Children
    private final LamaExpressionNode[] elementNodes;

    public LamaSexpNode(String tag, LamaExpressionNode[] elementNodes) {
        this.tag = tag;
        this.elementNodes = elementNodes;
    }

    @Override
    @ExplodeLoop
    public Object executeGeneric(VirtualFrame frame) {
        Object[] elements = new Object[elementNodes.length];
        for (int i = 0; i < elementNodes.length; i++) {
            elements[i] = elementNodes[i].executeGeneric(frame);
        }
        return new LamaSexp(tag, elements);
    }
}
