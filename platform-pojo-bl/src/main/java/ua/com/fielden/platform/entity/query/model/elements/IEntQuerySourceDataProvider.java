package ua.com.fielden.platform.entity.query.model.elements;



public interface IEntQuerySourceDataProvider {
    Class parentType();
    Class propType(String propSimpleName);
}