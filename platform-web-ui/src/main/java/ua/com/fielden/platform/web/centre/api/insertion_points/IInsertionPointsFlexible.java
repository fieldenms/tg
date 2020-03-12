package ua.com.fielden.platform.web.centre.api.insertion_points;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IInsertionPointsFlexible <T extends AbstractEntity<?>> extends IInsertionPoints<T>{

    IInsertionPoints<T> flex();
}
