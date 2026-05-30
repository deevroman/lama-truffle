package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.LamaException;
import lama.truffle.runtime.LamaArray;
import lama.truffle.runtime.LamaSexp;
import lama.truffle.runtime.LamaString;

public final class LamaLoadIndexNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode baseNode;

    @Child
    private LamaExpressionNode indexNode;

    public LamaLoadIndexNode(LamaExpressionNode baseNode, LamaExpressionNode indexNode) {
        this.baseNode = baseNode;
        this.indexNode = indexNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object base = baseNode.executeGeneric(frame);
        long index = indexNode.executeLong(frame);
        if (base instanceof LamaArray array) {
            checkIndex(index, array.elements().length);
            return array.elements()[(int) index];
        }
        if (base instanceof LamaString string) {
            checkIndex(index, string.length());
            return string.get(index);
        }
        if (base instanceof LamaSexp sexp) {
            checkIndex(index, sexp.elements().length);
            return sexp.elements()[(int) index];
        }
        throw LamaException.typeError(this, "aggregate", base);
    }

    @Override
    public long executeLong(VirtualFrame frame) {
        return (long) executeGeneric(frame);
    }

    private void checkIndex(long index, long length) {
        if (index < 0 || index >= length) {
            throw LamaException.indexError(this, index, length);
        }
    }
}
