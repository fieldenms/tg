package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.IElement2;

public interface IElement1<S2 extends IElement2> {
    S2 transform(TransformatorToS2 resolver);
}