package lama.truffle.pattern;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LamaLiteralLongPattern implements LamaPattern {
    private final long expected;

    public LamaLiteralLongPattern(long expected) {
        this.expected = expected;
    }

    @Override
    public boolean matches(VirtualFrame frame, Object value) {
        return value instanceof Long actual && actual == expected;
    }
}
