package lama.truffle.runtime;

import com.oracle.truffle.api.interop.TruffleObject;

public record LamaArray(Object[] elements) implements TruffleObject {
    public long length() {
        return elements.length;
    }
}
