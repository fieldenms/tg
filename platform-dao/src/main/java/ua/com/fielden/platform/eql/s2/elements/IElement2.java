package ua.com.fielden.platform.eql.s2.elements;

import java.util.List;

public interface IElement2 {
    List<EntProp2> getLocalProps();
    List<EntQuery2> getLocalSubQueries();
    List<EntValue2> getAllValues();
    boolean ignore();
}