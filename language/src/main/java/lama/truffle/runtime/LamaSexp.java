package lama.truffle.runtime;

import com.oracle.truffle.api.interop.TruffleObject;

public record LamaSexp(String tag, Object[] elements) implements TruffleObject {
    public long length() {
        return elements.length;
    }
}
