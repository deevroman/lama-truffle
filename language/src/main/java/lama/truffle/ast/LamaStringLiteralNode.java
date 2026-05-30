package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.runtime.LamaString;

public final class LamaStringLiteralNode extends LamaExpressionNode {
    private final String value;

    public LamaStringLiteralNode(String value) {
        this.value = value;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return new LamaString(value);
    }
}
