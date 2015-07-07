package ua.com.fielden.platform.eql.meta;


public interface IResolvable {
    ResolutionPath resolve(final String dotNotatedPropName);

    Class javaType();
}
