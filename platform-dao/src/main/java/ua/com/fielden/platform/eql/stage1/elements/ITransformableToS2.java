package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.stage2.elements.IIgnorableAtS2;

public interface ITransformableToS2<S2 extends IIgnorableAtS2> {
    S2 transform(TransformatorToS2 resolver);
}