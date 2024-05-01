package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.reflection.Finder.findRealProperties;
import static ua.com.fielden.platform.reflection.Finder.streamRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

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

    public static final String ERR_UNION_PROPERTY_ALREADY_HAS_VALUE = "Invalid attempt to set property [%s] as active for union entity [%s] with active property [%s].";
    public static final String ERR_ACTIVE_PROPERTY_NOT_DETERMINED = "Active property for union entity [%s] has not been determined.";

    /** Points out the name of a non-null property. */
    private String activePropertyName;

    /**
     * Enforces union rule: only one property can be set and only once from the moment of union entity instantiation.
     * Such property drives values for properties {@code id}, {@code key} and {@code desc}.
     */
    public final void ensureUnion(final String propertyName) {
        if (!isEmpty(activePropertyName)) {
            throw new EntityException(ERR_UNION_PROPERTY_ALREADY_HAS_VALUE.formatted(propertyName, getType().getSimpleName(),  activePropertyName));
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
        if (isEmpty(activePropertyName)) {
            activePropertyName = getNameOfAssignedUnionProperty();
            if (isEmpty(activePropertyName)) {
                throw new EntityException(ERR_ACTIVE_PROPERTY_NOT_DETERMINED.formatted(getType().getSimpleName()));
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
        final List<Field> fields = findRealProperties(getType());
        final List<Class<? extends AbstractEntity<?>>> propertyTypes = new ArrayList<>();
        for (final Field field : fields) {
            // union entities should not have properties that are not of entity type
            if (!AbstractEntity.class.isAssignableFrom(field.getType())) {
                throw new EntityDefinitionException("Union entity should not contain properties of ordinary type. Check property [%s].".formatted(field.getName())); // kind one error
            }
            // union properties should not contain more than one property of a certain entity type
            if (propertyTypes.contains(field.getType())) {
                throw new EntityDefinitionException("Union entity should contain only properties of unique types. Check property [%s].".formatted(field.getName())); // kind two error
            }
            propertyTypes.add((Class<AbstractEntity<?>>) field.getType());
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
        final Stream<String> propertyNames = streamRealProperties(getType()).map(Field::getName);

        return propertyNames
                .filter(propName -> !Reflector.isPropertyProxied(this, propName) && get(propName) != null)
                .findFirst() // returns Optional
                .map(propName -> (AbstractEntity<?>) get(propName)) // map optional propName value to an actual property value
                .orElse(null); // return the property value or null if there was no matching propName
    }

    /**
     * A convenient method for setting a union property to a non-null value.
     * It should only be used on union entity instances that do not yet have an active union-property.
     * <br><br>
     * This method looks for an appropriate union-property based on the type of {@code value}, which gets assigned to that property.
     * If no appropriate union-property is found, exception {@link EntityException} is thrown.
     *
     * @param value a non-null value
     * @return an instance of this union entity
     * @param <T> a type of the union entity as a convenience for method chaining
     */
    public final <T extends AbstractUnionEntity> T setUnionProperty(@Nonnull final AbstractEntity<?> value) {
        // If null is being assigned then we only need to clear the active union property, if it exists.
        if (value == null) {
            throw new EntityException("Null is not a valid value for union-properties (union entity [%s]).".formatted(getType().getSimpleName()));
        }
        // A non-null value can only be assigned if it matches one of the union properties by type.
        final Optional<Field> maybeMatchingProp = streamRealProperties(getType()).filter(field -> field.getType().equals(value.getType())).findFirst();
        if (maybeMatchingProp.isPresent()) {
            final String propertyName = maybeMatchingProp.get().getName();
            if (!isEmpty(activePropertyName)) {
                throw new EntityException(ERR_UNION_PROPERTY_ALREADY_HAS_VALUE.formatted(propertyName, getType().getSimpleName(),  activePropertyName));
            }
            // property setter should be used to trigger the assignment logic
            this.set(propertyName, value);
            return (T) this;
        }
        // If no matching union property is found, we throw an exception.
        throw new EntityException("None of the union properties match type [%s].".formatted(value.getType().getSimpleName()));
    }

    /**
     * Provides the set of property names, which are common for entity types used in "polymorphic" association.
     *
     * @param type
     * @return
     */
    public static final Set<String> commonProperties(final Class<? extends AbstractUnionEntity> type) {
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
     * Returns getter and setter method names for all common properties.
     *
     * @param type
     * @return
     * @throws NoSuchMethodException
     *             - throws when couldn't be found property getter or setter for some property.
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
     * @return
     * @throws NoSuchMethodException
     *             - throws when could not find property getter or setter for some property.
     */
    public static final List<Method> commonMethods(final Class<? extends AbstractUnionEntity> type) {
        final Set<String> commonProperties = commonProperties(type);
        final List<Field> unionProperties = unionProperties(type);
        final List<Method> commonMethods = new ArrayList<>();
        final Class<?> propertyType = unionProperties.get(0).getType();
        for (final String property : commonProperties) {
            try {
                commonMethods.add(Reflector.obtainPropertyAccessor(propertyType, property));
                commonMethods.add(Reflector.obtainPropertySetter(propertyType, property));
            } catch (final ReflectionException ex) {
                throw new ReflectionException("Common property [%s] inside [%s] does not have accessor or setter.".formatted(property, propertyType), ex);
            }
        }
        return commonMethods;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        // use standard equality from AbstractEntity and, if equals, compare active entities
        if (!super.equals(obj)) { // there are special handling for id-only-proxies and may be more specialisation in future
            return false;
        }
        // this.getType() and obj.getType() are equal as per super.equals call; so we can safely convert 'obj' to AbstractUnionEntity
        return equalsEx(this.activeEntity(), ((AbstractUnionEntity) obj).activeEntity());
    }

    @Override
    public int hashCode() {
        return ofNullable(activeEntity()).map(AbstractEntity::hashCode).orElse(0) * 23;
    }

}
