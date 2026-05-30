package lama.truffle.runtime;

import com.oracle.truffle.api.CompilerDirectives;

public final class LamaValueStringifier {
    private LamaValueStringifier() {
    }

    @CompilerDirectives.TruffleBoundary
    public static String stringify(Object value) {
        StringBuilder builder = new StringBuilder();
        append(builder, value);
        return builder.toString();
    }

    @CompilerDirectives.TruffleBoundary
    private static void append(StringBuilder builder, Object value) {
        switch (value) {
            case Long longValue -> builder.append(longValue);
            case LamaString stringValue -> {
                builder.append('"');
                builder.append(stringValue.value());
                builder.append('"');
            }
            case LamaArray arrayValue -> {
                builder.append('[');
                for (int i = 0; i < arrayValue.elements().length; i++) {
                    if (i > 0) {
                        builder.append(", ");
                    }
                    append(builder, arrayValue.elements()[i]);
                }
                builder.append(']');
            }
            case LamaSexp sexpValue -> {
                builder.append(sexpValue.tag());
                if (sexpValue.elements().length > 0) {
                    builder.append(" (");
                    for (int i = 0; i < sexpValue.elements().length; i++) {
                        if (i > 0) {
                            builder.append(", ");
                        }
                        append(builder, sexpValue.elements()[i]);
                    }
                    builder.append(')');
                }
            }
            case LamaClosure ignored -> builder.append("<function>");
            case null -> builder.append("null");
            default -> {
                builder.append('<');
                builder.append(value.getClass().getSimpleName());
                builder.append('>');
            }
        }
    }
}
