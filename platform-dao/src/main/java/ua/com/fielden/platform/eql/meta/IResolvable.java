package ua.com.fielden.platform.eql.meta;

public interface IResolvable<T> {
    AbstractPropInfo resolve(final String dotNotatedPropName);

    Class<T> javaType();
}
