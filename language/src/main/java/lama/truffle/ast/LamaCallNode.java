package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import lama.truffle.LamaException;
import lama.truffle.runtime.LamaClosure;

public final class LamaCallNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode callableNode;

    @Children
    private final LamaExpressionNode[] argumentNodes;

    @Child
    private IndirectCallNode callNode = IndirectCallNode.create();

    public LamaCallNode(LamaExpressionNode callableNode, LamaExpressionNode[] argumentNodes) {
        this.callableNode = callableNode;
        this.argumentNodes = argumentNodes;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object callable = callableNode.executeGeneric(frame);
        if (!(callable instanceof LamaClosure closure)) {
            throw LamaException.typeError(this, "function", callable);
        }
        if (argumentNodes.length != closure.function().arity()) {
            throw LamaException.arityError(this, closure.function().arity(), argumentNodes.length);
        }

        Object[] arguments = new Object[argumentNodes.length + 1];
        arguments[0] = closure;
        for (int i = 0; i < argumentNodes.length; i++) {
            arguments[i + 1] = argumentNodes[i].executeGeneric(frame);
        }

        return callNode.call(closure.function().callTarget(), arguments);
    }
}
