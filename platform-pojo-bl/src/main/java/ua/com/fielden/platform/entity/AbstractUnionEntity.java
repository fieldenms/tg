package ua.com.fielden.platform.entity;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.Finder.findRealProperties;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

/**
 * A base class for implementing synthetic entities to be used for modelling situations where a property of some entity can be of multiple types, but any individual instance of a
 * holding entity should reference a specific instance of one of those types or none (null value).
 * <p>
 * The descendants of this class will have several entity-type properties of unique types. The <i>union</i> part of the class name alludes to the fact that at most one property can
 * have a value, which basically defines the type and the value of a property in the holding entity.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public abstract class AbstractUnionEntity extends AbstractEntity<String> {
    /** Points out the name of a non-null property. */
    private String activePropertyName;

    /**
     * Enforces union rule -- only one property can be set and only once from the time of union entity instantiation. Such property drives values for properties <code>id</code>,
     * <code>key</code> and <code>desc</code>.
     */
    public final void ensureUnion(final String propertyName) {
        if (!StringUtils.isEmpty(activePropertyName)) {
            throw new EntityException(format("Invalid attempt to set property [%s] as active for union entity [%s] that already has property [%s] identified as active.", propertyName, getType().getName(),  activePropertyName));
        }
        activePropertyName = propertyName;
    }

    @Override
    public Long getId() {
        ensureActiveProperty();
        final AbstractEntity<?> activeEntity = activeEntity();
        return activeEntity != null ? activeEntity.getId() : null;
    }

    @Override
    public String getKey() {
        ensureActiveProperty();
        final AbstractEntity<?> activeEntity = activeEntity();
        return activeEntity != null && activeEntity.getKey() != null
               ? activeEntity.getKey().toString()
               : null;
    }

    @Override
    public String getDesc() {
        ensureActiveProperty();
        final AbstractEntity<?> activeEntity = activeEntity();
        return activeEntity != null ? activeEntity.getDesc() : null;
    }

    @Override
    protected void setId(final Long id) {
        throw new UnsupportedOperationException("Setting id is not permitted for union entity.");
    }

    @Override
    @Observable
    public AbstractEntity<String> setKey(final String key) {
        throw new UnsupportedOperationException("Setting key is not permitted for union entity.");
    }

    @Override
    @Observable
    public AbstractUnionEntity setDesc(final String desc) {
        throw new UnsupportedOperationException("Setting desc is not permitted for union entity.");
    }

    private void ensureActiveProperty() {
        if (StringUtils.isEmpty(activePropertyName)) {
            activePropertyName = getNameOfAssignedUnionProperty();
            if (StringUtils.isEmpty(activePropertyName)) {
                throw new EntityException(format("Active property for union entity [%s] has not been determined.", getType().getName()));
            }
        }
    }

    /**
     * Performs validation to ensure correctness of the union type definition producing an early runtime exception (instantiation time). The two basic rules are:
     * <ul>
     * <li>union entities should not have properties that are not of entity type
     * <li>union properties should not contain more than one property of a certain entity type
     * </ul>
     * <p>
     * Only if this validation passes, the super method is invoked to proceed with building of meta-properties.
     */
    @Override
    protected final void setMetaPropertyFactory(final IMetaPropertyFactory metaPropertyFactory) {
        // check for inappropriate union entity properties
        final List<Field> fields = Finder.findRealProperties(getType());
        final List<Class<? extends AbstractEntity<?>>> propertyTypes = new ArrayList<>();
        for (final Field field : fields) {
            if (!COMMON_PROPS.contains(field.getName())) {
                // union entities should not have properties that are not of entity type
                if (!AbstractEntity.class.isAssignableFrom(field.getType())) {
                    throw new IllegalStateException("Union entity should not contain properties of ordinary type."); // kind one error
                }
                // union properties should not contain more than one property of a certain entity type
                if (propertyTypes.contains(field.getType())) {
                    throw new IllegalStateException("Union entity should contain only properties of unique types."); // kind two error
                }
                propertyTypes.add((Class<AbstractEntity<?>>) field.getType());
            }
        }
        // run the super logic
        super.setMetaPropertyFactory(metaPropertyFactory);
    }

    private String getNameOfAssignedUnionProperty() {
        final List<Field> fields = findRealProperties(getType());
        for (final Field field : fields) {
            field.setAccessible(true);
            try {
                if (field.get(this) != null) {
                    return field.getName();
                }
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return null;
    }

    /**
     * A convenient method to obtain the value of an active property. Returns null if all properties are null.
     *
     * @return
     */
    public final AbstractEntity<?> activeEntity() {
        final Stream<String> propertyNames = Finder.streamRealProperties(getType()).map(Field::getName);

        return propertyNames
                .filter(propName -> !Reflector.isPropertyProxied(this, propName) && !COMMON_PROPS.contains(propName) && get(propName) != null)
                .findFirst() // returns Optional
                .map(propName -> (AbstractEntity<?>) get(propName)) // map optional propName value to an actual property value
                .orElse(null); // return the property value or null if there was no matching propName
    }

    /**
     * Provides the list of property names, which are common for entity types used in "polymorphic" association.
     *
     * @param type
     * @param propertyFilter
     * @return
     */
    public static final List<String> commonProperties(final Class<? extends AbstractUnionEntity> type) {
        // collect all properties of entity type
        final List<Class<? extends AbstractEntity<?>>> propertyTypes = new ArrayList<>();
        final List<Field> fields = unionProperties(type);
        for (final Field field : fields) {
            if (AbstractEntity.class.isAssignableFrom(field.getType())) {
                propertyTypes.add((Class<AbstractEntity<?>>) field.getType());
            }
        }
        // return the list of common properties
        return Finder.findCommonProperties(propertyTypes);
    }

    public String activePropertyName() {
        return activePropertyName;
    }

    /**
     * Finds all properties of {@link AbstractEntity} type that will form properties "union".
     *
     * Important : no other (non-union) properties should exist inside {@link AbstractUnionEntity} class.
     *
     * @return
     */
    public static final List<Field> unionProperties(final Class<? extends AbstractUnionEntity> type) {
        final List<Field> unionProperties = new ArrayList<>();
        // find all properties of AE type that will form properties "union". Note 1 : no other properties should exist inside AUE class. Note 2: desc and key are ignored.
        for (final Field field : Finder.findRealProperties(type)) {
            if (AbstractEntity.class.isAssignableFrom(field.getType())) {
                unionProperties.add(field);
            }
        }
        return unionProperties;
    }

    /**
     * Returns getters and setters method names for AbstractUnionEntity common properties.
     *
     * @param type
     * @return
     * @throws NoSuchMethodException
     *             - throws when couldn't found property getter or setter for some property.
     */
    public static final List<String> commonMethodNames(final Class<? extends AbstractUnionEntity> type) {
        final List<String> commonMethods = new ArrayList<>();
        for (final Method method : commonMethods(type)) {
            commonMethods.add(method.getName());
        }
        return commonMethods;
    }

    /**
     * Returns getters and setters for AbstractUnionEntity common properties.
     *
     * @param type
     * @param propertyFilter
     * @return
     * @throws NoSuchMethodException
     *             - throws when couldn't found property getter or setter for some property.
     */
    public static final List<Method> commonMethods(final Class<? extends AbstractUnionEntity> type) {
        final List<String> commonProperties = commonProperties(type);
        final List<Field> unionProperties = unionProperties(type);
        final List<Method> commonMethods = new ArrayList<>();
        final Class<?> propertyType = unionProperties.get(0).getType();
        for (final String property : commonProperties) {
            try {
                commonMethods.add(Reflector.obtainPropertyAccessor(propertyType, property));
                commonMethods.add(Reflector.obtainPropertySetter(propertyType, property));
            } catch (final ReflectionException ex) {
                throw new ReflectionException(format("Common property [%s] inside [%s] does not have accessor or setter.", property, propertyType), ex);
            }
        }
        return commonMethods;
    }
}
