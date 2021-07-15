package ua.com.fielden.platform.eql.stage1.sources;

import ua.com.fielden.platform.eql.stage1.ITransformableToS2;
import ua.com.fielden.platform.eql.stage1.TransformationResult;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.ISources2;

public interface ISources1<S2 extends ISources2<?>> extends ITransformableToS2<TransformationResult<S2>> {
    ISource1<? extends ISource2<?>> mainSource();
}
