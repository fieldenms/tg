package ua.com.fielden.platform.entity.query.model.elements;

import java.util.List;

public interface IPropertyCollector {
    List<EntProp> getProps();
    List<EntQuery> getSubqueries();
    List<EntValue> getValues();
}
