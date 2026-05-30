package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.LamaException;
import lama.truffle.runtime.LamaSexp;

public final class LamaHeadNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode receiverNode;

    public LamaHeadNode(LamaExpressionNode receiverNode) {
        this.receiverNode = receiverNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object receiver = receiverNode.executeGeneric(frame);
        if (receiver instanceof LamaSexp sexp && "Cons".equals(sexp.tag()) && sexp.elements().length == 2) {
            return sexp.elements()[0];
        }
        throw LamaException.typeError(this, "list", receiver);
    }
}
