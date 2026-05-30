package lama.truffle.runtime;

import com.oracle.truffle.api.interop.TruffleObject;

public record LamaClosure(LamaFunction function, Object[] captures) implements TruffleObject {
}
