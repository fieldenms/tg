package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;

import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;

public interface IRetrievalModel<T extends AbstractEntity<?>> {
    Class<T> getEntityType();

    boolean isInstrumented();
    
    Map<String, IRetrievalModel<? extends AbstractEntity<?>>> getRetrievalModels();
    
    Set<String> getPrimProps();
    
    Set<String> getProxiedProps();
    
    boolean containsProp(final String propName);
    
    boolean containsProxy(final String propName);
    
    default boolean isFetchIdOnly() {
        return getPrimProps().size() == 1 && getRetrievalModels().size() == 0 && containsProp(ID);
    }
    
    fetch<T> getOriginalFetch();
}