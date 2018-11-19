package ua.com.fielden.platform.entity.query;

import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IRetrievalModel<T extends AbstractEntity<?>> {
    Class<T> getEntityType();

    boolean isInstrumented();
    
    Map<String, EntityRetrievalModel<? extends AbstractEntity<?>>> getRetrievalModels();
    
    Set<String> getPrimProps();
    
    Set<String> getProxiedProps();
    
    boolean containsProp(final String propName);
    
    boolean containsProxy(final String propName);
}