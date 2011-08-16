package ua.com.fielden.platform.treemodel.rules.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;
import ua.com.fielden.platform.treemodel.rules.Function;
import ua.com.fielden.platform.treemodel.rules.ICalculatedProperty;
import ua.com.fielden.platform.treemodel.rules.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeEnhancer;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeManager.ITickManager;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeRepresentation.ITickRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.treemodel.rules.impl.DomainTreeEnhancer.ByteArray;
import ua.com.fielden.platform.treemodel.rules.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.utils.Pair;

/**
 * A base class for representations and managers with useful utility methods.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainTree {
    /** A base types to be checked for its non-emptiness and non-emptiness of their children. */
    public static final List<Class<?>> DOMAIN_TREE_TYPES = new ArrayList<Class<?>>() {{
	add(AbstractEntity.class); //
	add(ByteArray.class); //
	add(Ordering.class); //
	add(Function.class); //
	add(CalculatedPropertyCategory.class); //
	add(ICalculatedProperty.class); //
	add(IMasterDomainTreeManager.class); //
	add(IDomainTreeEnhancer.class); //
	add(IDomainTreeRepresentation.class); //
	add(IDomainTreeManager.class); //
	add(ITickRepresentation.class); //
	add(ITickManager.class); //
	/* TODO ? */
    }};
    private final transient ISerialiser serialiser;

    /**
     * Constructs base domain tree with a <code>serialiser</copy> instance.
     *
     * @param serialiser
     */
    protected AbstractDomainTree(final ISerialiser serialiser) {
	this.serialiser = serialiser;
    }

    /**
     * Returns an instance of serialiser for persistence and copying.
     *
     * @return
     */
    protected ISerialiser getSerialiser() {
	return serialiser;
    }

    /**
     * Throws an {@link IllegalArgumentException} if the property is unchecked.
     *
     * @param tm
     * @param root
     * @param property
     * @param message
     */
    protected static void illegalUncheckedProperties(final ITickManager tm, final Class<?> root, final String property, final String message) {
        if (!tm.isChecked(root, property)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws an {@link IllegalArgumentException} if the property type is not legal.
     *
     * @param root
     * @param property
     * @param message
     */
    protected static void illegalType(final Class<?> root, final String property, final String message, final Class<?> ... legalTypes) {
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
        for (final Class<?> legalType : legalTypes) {
            if (legalType.isAssignableFrom(propertyType)) {
        	return;
            }
        }
        throw new IllegalArgumentException(message);
    }

    protected static String generateKey(final Class<?> forType) {
	return PropertyTypeDeterminator.stripIfNeeded(forType).getName();
    }

    /**
     * Creates a set of properties (pairs root+propertyName). This set will correctly handle "enhanced" root types.
     * It can be used with enhanced types, but inner mechanism will "persist" not enhanced ones.
     *
     * @return
     */
    public static Set<Pair<Class<?>, String>> createSet() {
	return new HashSet<Pair<Class<?>,String>>() {
	    private static final long serialVersionUID = 7773953570321667258L;

	    @Override
	    public boolean contains(final Object key) {
		final Pair<Class<?>, String> key1 = (Pair<Class<?>, String>) key;
		return super.contains(new Pair<Class<?>, String>(DynamicEntityClassLoader.getOriginalType(key1.getKey()), key1.getValue()));
	    }

	    @Override
	    public boolean add(final Pair<Class<?>, String> key) {
		return super.add(new Pair<Class<?>, String>(DynamicEntityClassLoader.getOriginalType(key.getKey()), key.getValue()));
	    }

	    @Override
	    public boolean remove(final Object key) {
		final Pair<Class<?>, String> key1 = (Pair<Class<?>, String>) key;
		return super.remove(new Pair<Class<?>, String>(DynamicEntityClassLoader.getOriginalType(key1.getKey()), key1.getValue()));
	    }
	};
    }

    /**
     * Creates a map of properties => values (pairs root+propertyName). This map will correctly handle "enhanced" root types.
     * It can be used with enhanced types, but inner mechanism will "persist" not enhanced ones.
     *
     * @param <T> -- a type of values in map
     * @return
     */
    public static <T> Map<Pair<Class<?>, String>, T> createPropertiesMap() {
	return new HashMap<Pair<Class<?>,String>, T>() {
	    private static final long serialVersionUID = -1754488088205944423L;

	    @Override
	    public boolean containsKey(final Object key) {
		final Pair<Class<?>, String> key1 = (Pair<Class<?>, String>) key;
		return super.containsKey(new Pair<Class<?>, String>(DynamicEntityClassLoader.getOriginalType(key1.getKey()), key1.getValue()));
	    };

	    @Override
	    public T put(final Pair<Class<?>,String> key, final T value) {
		return super.put(new Pair<Class<?>, String>(DynamicEntityClassLoader.getOriginalType(key.getKey()), key.getValue()), value);
	    };

	    @Override
	    public T get(final Object key) {
		final Pair<Class<?>, String> key1 = (Pair<Class<?>, String>) key;
		return super.get(new Pair<Class<?>, String>(DynamicEntityClassLoader.getOriginalType(key1.getKey()), key1.getValue()));
	    };
	};
    }

    /**
     * Creates a map of properties => values (pairs root+propertyName). This map will correctly handle "enhanced" root types.
     * It can be used with enhanced types, but inner mechanism will "persist" not enhanced ones.
     *
     * @param <T> -- a type of values in map
     * @return
     */
    protected static <T> Map<Class<?>, T> createRootsMap() {
	return new HashMap<Class<?>, T>() {
	    private static final long serialVersionUID = -2157556231184035447L;

	    @Override
	    public boolean containsKey(final Object key) {
		final Class<?> key1 = (Class<?>) key;
		return super.containsKey(DynamicEntityClassLoader.getOriginalType(key1));
	    };

	    @Override
	    public T put(final Class<?> key, final T value) {
		return super.put(DynamicEntityClassLoader.getOriginalType(key), value);
	    };

	    @Override
	    public T get(final Object key) {
		final Class<?> key1 = (Class<?>) key;
		return super.get(DynamicEntityClassLoader.getOriginalType(key1));
	    };
	};
    }

    /**
     * Returns a key pair for [root + property].
     *
     * @param root
     * @param property
     * @return
     */
    public static Pair<Class<?>, String> key(final Class<?> root, final String property) {
	return new Pair<Class<?>, String>(root, property);
    }

    /**
     * A specific Kryo serialiser for {@link AbstractDomainTree}.
     *
     * @author TG Team
     *
     */
    protected abstract static class AbstractDomainTreeSerialiser<T> extends TgSimpleSerializer<T> {
	private final TgKryo kryo;

	public AbstractDomainTreeSerialiser(final TgKryo kryo) {
	    super(kryo);
	    this.kryo = kryo;
	}

	protected TgKryo kryo() {
	    return kryo;
	}
    }

}
