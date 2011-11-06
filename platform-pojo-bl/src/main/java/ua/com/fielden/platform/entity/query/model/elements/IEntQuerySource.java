package ua.com.fielden.platform.entity.query.model.elements;


public interface IEntQuerySource {
    String getAlias();
    boolean hasProperty(String dotNotatedPropName);
}
