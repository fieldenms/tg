package ua.com.fielden.platform.eql.s1.elements;

import java.util.List;

public interface IElement {
    List<EntProp> getLocalProps();
    List<EntQuery> getLocalSubQueries();
    List<EntValue> getAllValues();
    boolean ignore();
}