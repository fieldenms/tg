package ua.com.fielden.platform.test;

import java.util.List;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.persistence.HibernateUtil;

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

    List<String> getDdl();
}