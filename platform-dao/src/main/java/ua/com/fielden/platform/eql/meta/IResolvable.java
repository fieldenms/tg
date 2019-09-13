package ua.com.fielden.platform.eql.meta;

public interface IResolvable<T> {
    ResolutionContext resolve(final ResolutionContext context);
    Class<T> javaType();
}
