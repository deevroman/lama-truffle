package lama.truffle.runtime;

import com.oracle.truffle.api.RootCallTarget;

public record LamaFunction(RootCallTarget callTarget, int arity) {
}
