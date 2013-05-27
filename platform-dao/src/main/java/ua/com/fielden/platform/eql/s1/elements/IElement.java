package ua.com.fielden.platform.eql.s1.elements;

import java.util.List;

import ua.com.fielden.platform.eql.s2.elements.IElement2;

public interface IElement<S2 extends IElement2> {
    List<EntProp> getLocalProps();
    List<EntQuery> getLocalSubQueries();
    List<EntValue> getAllValues();
    boolean ignore();
    S2 transform();
}