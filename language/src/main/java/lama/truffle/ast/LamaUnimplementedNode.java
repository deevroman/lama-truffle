package lama.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.LamaException;

public final class LamaUnimplementedNode extends LamaExpressionNode {
    private final String feature;

    public LamaUnimplementedNode(String feature) {
        this.feature = feature;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        throw LamaException.notImplemented(this, feature);
    }
}
