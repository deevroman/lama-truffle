package lama.truffle.ast;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import lama.truffle.LamaLanguage;

public final class LamaRootNode extends RootNode {
    @Child
    private LamaExpressionNode bodyNode;

    private final SourceSection sourceSection;
    private final String name;

    public LamaRootNode(
        LamaLanguage language,
        FrameDescriptor frameDescriptor,
        LamaExpressionNode bodyNode,
        Source source,
        String name
    ) {
        super(language, frameDescriptor);
        this.bodyNode = bodyNode;
        this.sourceSection = source.createSection(0, source.getLength());
        this.name = name;
    }

    @Override
    public SourceSection getSourceSection() {
        return sourceSection;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return bodyNode.executeGeneric(frame);
    }
}
