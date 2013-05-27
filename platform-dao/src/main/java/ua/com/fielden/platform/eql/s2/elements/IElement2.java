package ua.com.fielden.platform.eql.s2.elements;

import java.util.List;

public interface IElement2 {
    List<EntProp> getLocalProps();
    List<EntQuery> getLocalSubQueries();
    List<EntValue> getAllValues();
    boolean ignore();
}