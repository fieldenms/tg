package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.IIgnorableAtS2;
import ua.com.fielden.platform.utils.Pair;

public interface ITransformableWithSourceToS2<S2> {
    Pair<S2, PropsResolutionContext> transform(PropsResolutionContext resolutionContext);
}