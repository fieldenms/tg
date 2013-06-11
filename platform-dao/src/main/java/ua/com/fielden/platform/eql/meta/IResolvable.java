package ua.com.fielden.platform.eql.meta;

public interface IResolvable {
    AbstractPropInfo resolve(final String dotNotatedPropName);
    Class javaType();
}
