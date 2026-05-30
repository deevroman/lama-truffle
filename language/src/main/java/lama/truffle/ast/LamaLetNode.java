package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.LamaException;
import lama.truffle.pattern.LamaPattern;

public final class LamaLetNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode initNode;

    @Child
    private LamaExpressionNode bodyNode;

    private final LamaPattern pattern;

    public LamaLetNode(LamaExpressionNode initNode, LamaPattern pattern, LamaExpressionNode bodyNode) {
        this.initNode = initNode;
        this.pattern = pattern;
        this.bodyNode = bodyNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object value = initNode.executeGeneric(frame);
        if (!pattern.matches(frame, value)) {
            throw LamaException.matchFailure(this);
        }
        return bodyNode.executeGeneric(frame);
    }
}
