package ua.com.fielden.platform.entity.query.model.elements;

import java.util.List;

import ua.com.fielden.platform.entity.query.model.elements.AbstractEntQuerySource.PropResolutionInfo;
import ua.com.fielden.platform.utils.Pair;


public interface IEntQuerySource extends IEntQuerySourceDataProvider {
    String getAlias();
    Class getType();
    Pair<Boolean, PropResolutionInfo> containsProperty(EntProp prop);
    void addReferencingProp(EntProp prop);
    List<EntProp> getReferencingProps();
    boolean generated();
}