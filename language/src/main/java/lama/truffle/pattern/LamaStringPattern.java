package lama.truffle.pattern;

import com.oracle.truffle.api.frame.VirtualFrame;
import lama.truffle.runtime.LamaString;

public final class LamaStringPattern implements LamaPattern {
    private final String expected;

    public LamaStringPattern(String expected) {
        this.expected = expected;
    }

    @Override
    public boolean matches(VirtualFrame frame, Object value) {
        return value instanceof LamaString string && expected.equals(string.value());
    }
}
