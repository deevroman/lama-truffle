package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.LamaException;
import lama.truffle.pattern.LamaPattern;

public final class LamaMatchNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode valueNode;

    private final LamaPattern pattern;

    public LamaMatchNode(LamaExpressionNode valueNode, LamaPattern pattern) {
        this.valueNode = valueNode;
        this.pattern = pattern;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object value = valueNode.executeGeneric(frame);
        if (!pattern.matches(frame, value)) {
            throw LamaException.matchFailure(this);
        }
        return value;
    }
}
