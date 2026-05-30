package lama.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import lama.truffle.parser.LamaNodeParser;

@TruffleLanguage.Registration(
    id = LamaLanguage.ID,
    name = "Lama",
    defaultMimeType = LamaLanguage.MIME_TYPE,
    characterMimeTypes = LamaLanguage.MIME_TYPE,
    fileTypeDetectors = LamaFileDetector.class
)
public final class LamaLanguage extends TruffleLanguage<LamaContext> {
    public static final String ID = "lama";
    public static final String MIME_TYPE = "application/x-lama";

    @Override
    protected LamaContext createContext(Env env) {
        return new LamaContext(env);
    }

    @Override
    protected CallTarget parse(ParsingRequest request) {
        return LamaNodeParser.parse(this, request.getSource());
    }
}
