package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;

public interface ITransformableWithSourceToS2<S2> {
    TransformationResult<S2> transform(PropsResolutionContext resolutionContext);
}