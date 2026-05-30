package lama.truffle;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.nodes.Node;

public final class LamaException extends AbstractTruffleException {
    public LamaException(String message, Node location) {
        super(message, location);
    }

    @CompilerDirectives.TruffleBoundary
    public static LamaException notImplemented(Node node, String feature) {
        return new LamaException(location(node) + ": " + feature + " is not implemented yet", node);
    }

    @CompilerDirectives.TruffleBoundary
    public static LamaException typeError(Node node, String expected, Object actual) {
        return new LamaException(
            location(node) + ": expected " + expected + ", got " + describe(actual),
            node
        );
    }

    @CompilerDirectives.TruffleBoundary
    public static LamaException arityError(Node node, int expected, int actual) {
        return new LamaException(
            location(node) + ": expected " + expected + " arguments, got " + actual,
            node
        );
    }

    @CompilerDirectives.TruffleBoundary
    public static LamaException indexError(Node node, long index, long length) {
        return new LamaException(
            location(node) + ": index " + index + " is out of bounds for length " + length,
            node
        );
    }

    @CompilerDirectives.TruffleBoundary
    public static LamaException charError(Node node, long value) {
        return new LamaException(
            location(node) + ": expected character code in range 0..255, got " + value,
            node
        );
    }

    @CompilerDirectives.TruffleBoundary
    public static LamaException matchFailure(Node node) {
        return new LamaException(location(node) + ": match failure", node);
    }

    @CompilerDirectives.TruffleBoundary
    private static String describe(Object value) {
        if (value == null) {
            return "null";
        }
        return value.getClass().getSimpleName();
    }

    @CompilerDirectives.TruffleBoundary
    private static String location(Node node) {
        if (node != null && node.getEncapsulatingSourceSection() != null) {
            var section = node.getEncapsulatingSourceSection();
            return "%s:L%d:%d".formatted(
                section.getSource().getName(),
                section.getStartLine(),
                section.getStartColumn()
            );
        }

        return "<unknown>";
    }
}
