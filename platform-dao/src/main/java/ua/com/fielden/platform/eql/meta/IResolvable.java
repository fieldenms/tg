package ua.com.fielden.platform.eql.meta;

public interface IResolvable<T> {
    ResolutionResult resolve(final ResolutionContext context);
    Class<T> javaType();
}
