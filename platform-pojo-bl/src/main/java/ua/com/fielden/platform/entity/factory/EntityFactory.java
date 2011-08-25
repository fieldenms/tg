package ua.com.fielden.platform.entity.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;

/**
 *
 * Factory for instantiating entities derived from {@link AbstractEntity} with APO/IoC support.
 *
 * This factory is thread-safe -- the only state it has is final {@link Injector}, which is thread-safe because it uses new anonymous instances of {@link Provider} interface in its
 * {@link Injector#getInstance(Class)} method.
 *
 * @author TG Team
 *
 */
public class EntityFactory {
    private Injector injector;

    public void setModule(final Module module, final Module... modules) {
	injector = Guice.createInjector(aggregate(module, modules));
    }

    /**
     * Setting of a different injector instead of the one created upon class instantiation might be required usually in cases where an injector with more modules is needed to
     * entity instantiation.
     *
     * @param injector
     */
    public void setInjector(final Injector injector) {
	this.injector = injector;
    }

    protected EntityFactory() {
    }

    /**
     * Constructor that allow passing one or more modules.
     *
     * @param module
     * @param modules
     */
    public EntityFactory(final Module module, final Module... modules) {
	this(aggregate(module, modules));
    }

    /**
     * Constructs factory instance using provided modules and internal modules for instantiation of the Guice injector.
     *
     * There can be zero or more provided modules.
     *
     * @param modules
     */
    private EntityFactory(final List<Module> modules) {
	if (modules == null || modules.size() == 0) {
	    throw new IllegalArgumentException("One or more modules are expected.");
	}
	injector = Guice.createInjector(modules);
    }

    /**
     * Sometimes it is more convenient to provide factory with already created injector rather than modules.
     *
     * @param injector
     */
    public EntityFactory(final Injector injector) {
	this.injector = injector;
    }

    /**
     * This factory method should be used for lightweight entity instantiation (not via injector).
     *
     * @param <T>
     * @param entityClass
     * @param id
     * @return
     * @throws RuntimeException
     */
    public synchronized <T extends AbstractEntity<?>> T newPlainEntity(final Class<T> entityClass, final Long id) {
	try {
	    final Constructor<T> constructor = entityClass.getDeclaredConstructor();
	    constructor.setAccessible(true);
	    final T entity = constructor.newInstance();
	    constructor.setAccessible(false);
	    setId(entityClass, id, entity);
	    return entity;
	} catch (final Exception ex) {
	    throw new IllegalStateException(ex);
	}
    }

    /**
     * This factory method should be used strictly for Hibernate intercepter during entity instantiation.
     *
     * @param <T>
     * @param entityClass
     * @param id
     * @return
     * @throws RuntimeException
     */
    public synchronized <T extends AbstractEntity<?>> T newEntity(final Class<T> entityClass, final Long id) {
	try {
	    final T entity = injector.getInstance(entityClass);
	    setReferenceToThis(entity);
	    setId(entityClass, id, entity);
	    return entity;
	} catch (final Exception ex) {
	    throw new IllegalStateException(ex);
	}
    }

    /**
     * A convenient factory method, which creates entity with <code>null</code> id.
     *
     * @param <T>
     * @param entityClass
     * @return
     */
    public synchronized <T extends AbstractEntity<?>> T newEntity(final Class<T> entityClass) {
	return newEntity(entityClass, null);
    }

    /**
     * Invokes protected 'setEntityFactory' method on passed {@link AbstractEntity} to set reference to this {@link EntityFactory} instance.
     *
     * @param entity
     * @throws RuntimeException
     */
    @SuppressWarnings("unchecked")
    private void setReferenceToThis(final AbstractEntity entity) {
	try {
	    final Method method = Reflector.getMethod(entity.getType(), "setEntityFactory", EntityFactory.class);
	    method.setAccessible(true);
	    method.invoke(entity, this);
	    method.setAccessible(false);
	} catch (final Exception e) {
	    throw new IllegalStateException(e);
	}
    }

    /**
     * Instantiates an entity of the specified type with the passed parameters.
     *
     * @param <T>
     *            -- entity type
     * @param <K>
     *            -- type of the entity key property
     * @param entityClass
     *            -- entity class
     * @param id
     *            -- id value, which can be null if this is a bran new entity
     * @param key
     *            -- key value
     * @param desc
     *            -- entity description, which can be null
     * @return
     * @throws Exception
     */
    public synchronized <T extends AbstractEntity<K>, K extends Comparable> T newEntity(final Class<T> entityClass, final Long id, final K key, final String desc)
    throws RuntimeException {
	try {
	    final T entity = injector.getInstance(entityClass);
	    setReferenceToThis(entity);
	    setId(entityClass, id, entity);
	    setKey(entityClass, key, entity);
	    entity.setDesc(desc);
	    return entity;
	} catch (final Exception ex) {
	    throw new IllegalStateException(ex);
	}
    }

    /**
     * Convenient constructor for instantiation of the brand new entity with no id yet assigned.
     */
    public <T extends AbstractEntity<K>, K extends Comparable> T newEntity(final Class<T> entityClass, final K key, final String desc) {
	return newEntity(entityClass, null, key, desc);
    }

    /**
     * Convenient constructor for instantiation of the brand new entity with no id yet assigned and with no description.
     */
    public <T extends AbstractEntity<K>, K extends Comparable> T newByKey(final Class<T> entityClass, final K key) throws RuntimeException {
	return newEntity(entityClass, null, key, null);
    }

    /**
     * Creates entity with {@link DynamicEntityKey} with specified keys set
     *
     * @param keys
     *            - key values that are composite key members of {@link DynamicEntityKey} of entity
     * @return created entity with keys set
     * @throws Exception
     *             - if number of keys instances is not equal to number of composite key member in entity.<br>
     *             And in other cases.
     */
    public synchronized <T extends AbstractEntity<DynamicEntityKey>> T newByKey(final Class<T> entityClass, final Object... keys) throws RuntimeException {
	try {
	    final T entity = injector.getInstance(entityClass); // DynamicEntityKey should be set in default constructor of entity
	    setReferenceToThis(entity);
	    // setting composite key fields
	    final List<Field> fieldList = Finder.getKeyMembers(entityClass);
	    if (fieldList.size() != keys.length) {
		throw new IllegalArgumentException("Number of key values is " + keys.length + " but should be " + fieldList.size());
	    }
	    for (int i = 0; i < fieldList.size(); i++) {
		final Field keyField = fieldList.get(i);
		final Object keyValue = keys[i];

		entity.set(keyField.getName(), keyValue);
	    }

	    return entity;
	} catch (final Exception ex) {
	    throw new IllegalStateException(ex);
	}
    }

    /**
     * Convenient constructor for instantiation of an entity without description.
     */
    public <T extends AbstractEntity<K>, K extends Comparable> T newEntity(final Class<T> entityClass, final Long id, final K key) throws RuntimeException {
	return newEntity(entityClass, id, key, null);
    }

    /**
     * Convenience method for setting entity id value.
     */
    private <T> void setId(final Class<T> entityClass, final Long id, final T entity) throws Exception {
	final Field idField = Finder.getFieldByName(entityClass, AbstractEntity.ID);
	final boolean accessible = idField.isAccessible();
	idField.setAccessible(true);
	idField.set(entity, id);
	idField.setAccessible(accessible);
    }

    /**
     * Convenience method for setting entity key value.
     */
    private <T, K> void setKey(final Class<T> entityClass, final K key, final T entity) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
	final Method setKey = Reflector.getMethod(entityClass, "setKey", Comparable.class);
	setKey.setAccessible(true);
	setKey.invoke(entity, key);
    }

    /**
     * Puts the passed values into one list.
     *
     * @param module
     * @param modules
     * @return
     */
    private static List<Module> aggregate(final Module module, final Module... modules) {
	final List<Module> list = new ArrayList<Module>();
	list.add(module);
	for (final Module module2 : modules) {
	    list.add(module2);
	}
	return list;
    }
}
