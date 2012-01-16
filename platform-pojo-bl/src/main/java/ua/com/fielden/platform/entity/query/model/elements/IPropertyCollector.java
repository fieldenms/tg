package ua.com.fielden.platform.entity.query.model.elements;

import java.util.List;
import java.util.Set;

public interface IPropertyCollector {
    Set<String> getPropNames();
    List<EntProp> getProps();
    List<EntQuery> getSubqueries();
}
