package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.LamaException;
import lama.truffle.runtime.LamaArray;
import lama.truffle.runtime.LamaSexp;
import lama.truffle.runtime.LamaString;

public final class LamaLengthNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode receiverNode;

    public LamaLengthNode(LamaExpressionNode receiverNode) {
        this.receiverNode = receiverNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return executeLong(frame);
    }

    @Override
    public long executeLong(VirtualFrame frame) {
        Object receiver = receiverNode.executeGeneric(frame);
        if (receiver instanceof LamaString string) {
            return string.length();
        }
        if (receiver instanceof LamaArray array) {
            return array.length();
        }
        if (receiver instanceof LamaSexp sexp) {
            return sexp.length();
        }
        throw LamaException.typeError(this, "aggregate", receiver);
    }
}
