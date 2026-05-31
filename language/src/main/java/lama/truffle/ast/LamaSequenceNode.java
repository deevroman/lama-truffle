package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public final class LamaSequenceNode extends LamaExpressionNode {
    @Children
    private final LamaExpressionNode[] expressionNodes;

    public LamaSequenceNode(LamaExpressionNode[] expressionNodes) {
        this.expressionNodes = expressionNodes;
    }

    @Override
    @ExplodeLoop
    public Object executeGeneric(VirtualFrame frame) {
        Object result = 0L;
        for (LamaExpressionNode expressionNode : expressionNodes) {
            result = expressionNode.executeGeneric(frame);
        }
        return result;
    }
}
