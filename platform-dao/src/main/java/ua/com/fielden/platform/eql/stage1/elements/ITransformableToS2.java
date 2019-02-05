package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;

public interface ITransformableToS2<S2> {
    S2 transform(PropsResolutionContext resolutionContext);
}