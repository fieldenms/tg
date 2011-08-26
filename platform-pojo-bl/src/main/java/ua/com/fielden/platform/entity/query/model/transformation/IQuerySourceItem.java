package ua.com.fielden.platform.entity.query.model.transformation;


public interface IQuerySourceItem {
    String name(); // e.g. key, vehicle, vehicle.model, vehicle.finDetails.purchPrice
    String sql(); // e.g. T1.C1, T1.KEY, T1.PURCH_PRICE

//  void addReference(IQueryItem referencingItem);
//  void removeReference(IQueryItem referencingItem);
}
