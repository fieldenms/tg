package ua.com.fielden.platform.test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.meta.IDomainMetadata;

/**
 * Contract for configuration used by db driven test cases such as {@link AbstractDomainDrivenTestCase}.
 * 
 * @author TG Team
 * 
 */
public interface IDomainDrivenTestCaseConfiguration {

    EntityFactory getEntityFactory();

    <T> T getInstance(Class<T> type);

    IDomainMetadata getDomainMetadata();
    
    IdOnlyProxiedEntityTypeCache getIdOnlyProxiedEntityTypeCache();
}
