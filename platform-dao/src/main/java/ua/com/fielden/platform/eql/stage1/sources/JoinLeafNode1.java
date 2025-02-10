package ua.com.fielden.platform.eql.stage1.sources;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.TransformationResultFromStage1To2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.JoinLeafNode2;
import ua.com.fielden.platform.utils.ToString;

import java.util.Set;

public record JoinLeafNode1 (ISource1<?> source) implements IJoinNode1<JoinLeafNode2>, ToString.IFormattable {

    @Override
    public TransformationResultFromStage1To2<JoinLeafNode2> transform(TransformationContextFromStage1To2 context) {
        final ISource2<?> mainTransformed = source.transform(context);
        return new TransformationResultFromStage1To2<>(new JoinLeafNode2(mainTransformed),
                                                       context.cloneWithAdded(mainTransformed));
    }

    @Override
    public ISource1<? extends ISource2<?>> mainSource() {
        return source;
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return source.collectEntityTypes();
    }

    @Override
    public String toString() {
        return toString(ToString.standard);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("source", source)
                .$();
    }

}
