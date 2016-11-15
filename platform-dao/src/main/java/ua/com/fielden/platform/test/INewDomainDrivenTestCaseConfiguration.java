package ua.com.fielden.platform.test;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;

/**
 * Contract for configuration used by db driven test cases such as {@link AbstractDomainDrivenTestCase}.
 * 
 * @author TG Team
 * 
 */
public interface INewDomainDrivenTestCaseConfiguration {

    EntityFactory getEntityFactory();

    <T> T getInstance(Class<T> type);

    DomainMetadata getDomainMetadata();
    
    IdOnlyProxiedEntityTypeCache getIdOnlyProxiedEntityTypeCache();
}
