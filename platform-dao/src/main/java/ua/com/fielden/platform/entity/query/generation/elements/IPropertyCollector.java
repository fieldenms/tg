package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.List;

public interface IPropertyCollector {
    List<EntProp> getLocalProps();

    List<EntQuery> getLocalSubQueries();

    List<EntValue> getAllValues();
}
