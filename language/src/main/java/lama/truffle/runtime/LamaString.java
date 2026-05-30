package lama.truffle.runtime;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.TruffleObject;

public final class LamaString implements TruffleObject {
    private final StringBuilder value;

    @CompilerDirectives.TruffleBoundary
    public LamaString(String value) {
        this.value = new StringBuilder(value);
    }

    @CompilerDirectives.TruffleBoundary
    public long length() {
        return value.length();
    }

    @CompilerDirectives.TruffleBoundary
    public long get(long index) {
        return value.charAt((int) index);
    }

    @CompilerDirectives.TruffleBoundary
    public void set(long index, long chr) {
        value.setCharAt((int) index, (char) chr);
    }

    @CompilerDirectives.TruffleBoundary
    public String value() {
        return value.toString();
    }
}
