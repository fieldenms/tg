package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;

public interface IResolvable<T> {
    PropResolutionProgress resolve(final PropResolutionProgress context);
    Class<T> javaType();
}
