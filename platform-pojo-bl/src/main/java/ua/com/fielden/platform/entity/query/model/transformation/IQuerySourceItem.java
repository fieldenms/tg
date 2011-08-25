package ua.com.fielden.platform.entity.query.model.transformation;

import ua.com.fielden.platform.entity.query.model.structure.IQueryItem;

public interface IQuerySourceItem {
    void addReference(IQueryItem referencingItem);
    void removeReference(IQueryItem referencingItem);
    String name(); // e.g. key, vehicle, vehicle.model
    String sql(); // e.g. T1.C1, T1.KEY
}
