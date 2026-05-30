package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.LamaException;
import lama.truffle.runtime.LamaArray;
import lama.truffle.runtime.LamaSexp;
import lama.truffle.runtime.LamaString;

public final class LamaStoreIndexNode extends LamaExpressionNode {
    @Child
    private LamaExpressionNode baseNode;

    @Child
    private LamaExpressionNode indexNode;

    @Child
    private LamaExpressionNode valueNode;

    public LamaStoreIndexNode(LamaExpressionNode baseNode, LamaExpressionNode indexNode, LamaExpressionNode valueNode) {
        this.baseNode = baseNode;
        this.indexNode = indexNode;
        this.valueNode = valueNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        Object base = baseNode.executeGeneric(frame);
        long index = indexNode.executeLong(frame);
        Object value = valueNode.executeGeneric(frame);

        if (base instanceof LamaArray array) {
            checkIndex(index, array.elements().length);
            array.elements()[(int) index] = value;
            return value;
        }
        if (base instanceof LamaString string) {
            if (!(value instanceof Long chr)) {
                throw LamaException.typeError(this, "integer", value);
            }
            if (chr < 0 || chr > 255) {
                throw LamaException.charError(this, chr);
            }
            checkIndex(index, string.length());
            string.set(index, chr);
            return chr;
        }
        if (base instanceof LamaSexp sexp) {
            checkIndex(index, sexp.elements().length);
            sexp.elements()[(int) index] = value;
            return value;
        }
        throw LamaException.typeError(this, "aggregate", base);
    }

    private void checkIndex(long index, long length) {
        if (index < 0 || index >= length) {
            throw LamaException.indexError(this, index, length);
        }
    }
}
