package ua.com.fielden.platform.entity.query.model.structure;

public interface IQuerySourceItem {
    void addReference(IQueryItem referencingItem);
    void removeReference(IQueryItem referencingItem);
}
