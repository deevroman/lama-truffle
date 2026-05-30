package lama.truffle.pattern;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LamaWildcardPattern implements LamaPattern {
    @Override
    public boolean matches(VirtualFrame frame, Object value) {
        return true;
    }
}
