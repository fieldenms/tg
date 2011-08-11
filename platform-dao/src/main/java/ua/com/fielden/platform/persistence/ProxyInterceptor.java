package ua.com.fielden.platform.persistence;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.type.Type;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * This is a thread-safe implementation of the global Hibernate intercepter for correct handling of entities enhanced with Guice(CGLIB) method intercepter.
 *
 * @author 01es
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
     *
     * TODO: Potentially there could be a check whether the passed entityName is in AbstractEntity hierarchy. However, at the moment it is envisaged any objects that is stored in
     * the DB should be derived from AbstractEntity.
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

    //    /**
    //     * Executes meta property definer for each entity property.
    //     *
    //     * Method {@link MetaProperty#define(Object, Object[], String[])} accepts
    //     * the <code>state</code> and <code>propertyNames</code>, which allows to
    //     * overcome the issue with entity not being initialised at the stage of
    //     * loading in cases where there are inter-property dependency.
    //     *
    //     * Also this method ensures property <code>key</code> assignment via setter,
    //     * which handles determination of the actual key type and its association
    //     * with meta-property.
    //     *
    //     */
    //    @Override
    //    public boolean onLoad(final Object entity, final Serializable id, final Object[] state, final String[] propertyNames, final Type[] types) {
    //	//	System.out.println("????? onLoad : entity(" + entity.getClass().getSimpleName() + ") = [" + ((AbstractEntity<?>) entity).getId() + "]");
    //	final boolean result = super.onLoad(entity, id, state, propertyNames, types);
    //	final AbstractEntity<?> instance = (AbstractEntity<?>) entity;
    //	// handle property "key" assignment
    //	final int keyIndex = Arrays.asList(propertyNames).indexOf("key");
    //	if (keyIndex >= 0 && state[keyIndex] != null) {
    //	    instance.set("key", state[keyIndex]);
    //	}
    //
    //	for (int index = 0; index < propertyNames.length; index++) {
    //	    final MetaProperty meta = instance.getProperty(propertyNames[index]);
    //	    if (meta != null) {
    //		meta.setOriginalValue(state[index]);
    //		meta.define(state[index], state, propertyNames);
    //	    }
    //	}
    //	instance.setDirty(false);
    //	return result;
    //    }

    @Override
    public boolean onSave(final Object entity, final Serializable id, final Object[] state, final String[] propertyNames, final Type[] types) {
	//	final AbstractEntity<?> instance = (AbstractEntity<?>) entity;
	//	for (int index = 0; index < propertyNames.length; index++) {
	//	    final MetaProperty meta = instance.getProperty(propertyNames[index]);
	//	    if (meta != null) {
	//		//		if (!meta.isCollectional()) { // single property
	//		//		    meta.setOriginalValue(state[index]);
	//		//		} else if (state[index] instanceof PersistentCollection && !((PersistentCollection) state[index]).wasInitialized()) { // not-initialized collection
	//		//		    meta.setOriginalValue(MetaProperty.ORIGINAL_VALUE_NOT_INIT_COLL);
	//		//		} else {
	//		//		    meta.setOriginalValue((state[index] != null) ? ((Collection<?>) state[index]).size() : null);
	//		//		}
	//		meta.setOriginalValue(state[index]);
	//		meta.define(state[index]);
	//	    }
	//	}
	//	instance.setDirty(false);
	return super.onSave(entity, id, state, propertyNames, types);
    }
}