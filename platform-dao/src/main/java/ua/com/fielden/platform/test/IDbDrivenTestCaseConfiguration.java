package ua.com.fielden.platform.test;

import java.util.List;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.persistence.HibernateUtil;

import com.google.inject.Injector;

/**
 * Contract for configuration used by db driven test cases such as {@link DbDrivenTestCase}.
 *
 * @author TG Team
 *
 */
public interface IDbDrivenTestCaseConfiguration {
    EntityFactory getEntityFactory();
    Injector getInjector();
    HibernateUtil getHibernateUtil();
    DomainValidationConfig getDomainValidationConfig();
    DomainMetaPropertyConfig getDomainMetaPropertyConfig();

    List<String> getDdl();
}
