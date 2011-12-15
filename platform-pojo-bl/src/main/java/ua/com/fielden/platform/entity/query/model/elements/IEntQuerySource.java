package ua.com.fielden.platform.entity.query.model.elements;


public interface IEntQuerySource {
    String getAlias();
    Class getType();
    boolean hasProperty(String dotNotatedPropName);
}
