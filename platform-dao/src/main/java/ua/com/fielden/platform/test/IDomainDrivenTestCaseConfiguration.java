package ua.com.fielden.platform.test;

import java.util.Properties;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;

/**
 * Contract for configuration used by db driven test cases such as {@link AbstractDomainDrivenTestCase}.
 *
 * @author TG Team
 *
 */
public interface IDomainDrivenTestCaseConfiguration {
    final static Properties hbc = new Properties();

    EntityFactory getEntityFactory();
    <T> T getInstance(Class<T> type);
    DomainValidationConfig getDomainValidationConfig();
    DomainMetaPropertyConfig getDomainMetaPropertyConfig();
    DomainMetadata getDomainMetadata();
}
