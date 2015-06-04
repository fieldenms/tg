package ua.com.fielden.platform.entity.query;

import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;

public interface IRetrievalModel<T extends AbstractEntity<?>> {
    Class<T> getEntityType();

    Map<String, fetch<? extends AbstractEntity<?>>> getFetchModels();
    
    Set<String> getPrimProps();
    
    boolean containsProp(final String propName);
}