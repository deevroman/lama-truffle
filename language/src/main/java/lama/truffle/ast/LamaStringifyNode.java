package lama.truffle.ast;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.runtime.LamaString;
import lama.truffle.runtime.LamaValueStringifier;

public final class LamaStringifyNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode receiverNode;

    public LamaStringifyNode(LamaExpressionNode receiverNode) {
        this.receiverNode = receiverNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return stringify(receiverNode.executeGeneric(frame));
    }

    @CompilerDirectives.TruffleBoundary
    private static LamaString stringify(Object value) {
        return new LamaString(LamaValueStringifier.stringify(value));
    }
}
