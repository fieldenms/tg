package ua.com.fielden.platform.persistence;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * This is a thread-safe implementation of the global Hibernate intercepter for correct handling of entities enhanced with Guice(CGLIB) method intercepter.
 *
 * @author TG Team
 */
public class ProxyInterceptor extends EmptyInterceptor {
    private static final long serialVersionUID = 1L;

    private Logger logger = Logger.getLogger(this.getClass());

    private EntityFactory factory;

    public ProxyInterceptor() {

    }

    public void setFactory(final EntityFactory factory) {
	this.factory = factory;
    }

    /**
     * Determines the correct class name for enhanced instances.
     */
    @Override
    public String getEntityName(final Object object) {
	if (object instanceof AbstractEntity) {
	    return ((AbstractEntity<?>) object).getType().getName();
	}
	return super.getEntityName(object);
    }

    /**
     * Instantiates entities using {@link EntityFactory} instead of the default constructor.
     */
    @Override
    public Object instantiate(final String entityName, final EntityMode entityMode, final Serializable id) {
	logger.info("instantiating: " + entityName + " for id = " +  id);
	try {
	    logger.info("instantiating using factory.newEntity(...)");
	    return factory.newEntity((Class<AbstractEntity<?>>) Class.forName(entityName), (Long) id);
	} catch (final RuntimeException e) {
	    e.printStackTrace();
	} catch (final ClassNotFoundException e) {
	    e.printStackTrace();
	}
	logger.info("instantiating using super.instantiate(...)");
	return super.instantiate(entityName, entityMode, id);
    }

}