package ua.com.fielden.platform.entity.validation.test_entities;

import java.lang.reflect.Field;
import java.util.stream.Collectors;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.EntityUtils;

public interface EntityWithDynamicRequirednessCo extends IEntityDao<EntityWithDynamicRequiredness> {
    
    public static final String ERR_REQUIRED = "Required test message.";
    
    public static final IFetchProvider<EntityWithDynamicRequiredness> FETCH_PROVIDER = EntityUtils.fetch(EntityWithDynamicRequiredness.class)
            .with(Finder.streamRealProperties(EntityWithDynamicRequiredness.class).map(Field::getName).collect(Collectors.toSet()));
}