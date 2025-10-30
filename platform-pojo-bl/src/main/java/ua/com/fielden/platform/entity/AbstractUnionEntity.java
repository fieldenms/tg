package ua.com.fielden.platform.entity;

import jakarta.annotation.Nonnull;
import ua.com.fielden.platform.annotations.metamodel.WithMetaModel;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.reflection.Finder.findRealProperties;
import static ua.com.fielden.platform.reflection.Finder.streamRealProperties;
import static ua.com.fielden.platform.reflection.exceptions.ReflectionException.requireNotNullArgument;

/// A base class for implementing synthetic entities to be used for modelling situations where a property of some entity can be of multiple types,
/// but any individual instance of a holding entity should reference a specific instance of one of those types or none (null value).
///
/// The descendants of this class will have several entity-type properties of unique types.
/// The _union_ part of the class name alludes to the fact that at most one property can have a value,
/// which basically defines the type and the value of a property in the holding entity.
///
@KeyType(String.class)
@WithMetaModel
public abstract class AbstractUnionEntity extends AbstractEntity<String> {

    public static final String ERR_UNION_PROPERTY_ALREADY_HAS_VALUE = "Invalid attempt to set property [%s] as active for union entity [%s] with active property [%s].",
                               ERR_ACTIVE_PROPERTY_NOT_DETERMINED = "Active property for union entity [%s] has not been determined.",
                               ERR_CONTAINS_PROPERTIES_OF_ORDINARY_TYPE = "Union entity should not contain properties of ordinary type. Check property [%s].",
                               ERR_CONTAINS_PROPERTIES_OF_THE_SAME_TYPES = "Union entity should contain only properties of unique types. Check property [%s].",
                               ERR_MISSING_ACCESSOR_OR_SETTER = "Common property [%s] inside [%s] does not have accessor or setter.",
                               ERR_NO_MATCHING_PROP_TYPE = "None of the union properties match type [%s].",
                               ERR_NULL_IS_NOT_ACCEPTABLE = "Null is not a valid value for union-properties (union entity [%s]).",
                               ERR_MISSING_ACTIVE_PROP_TO_CHECK_MEMBERSHIP = "Active property cannot be null when checking for membership in [%s].";

    /// Points out the name of a non-null property.
    private String activePropertyName;

    /// Enforces union rule: only one property can be set and only once from the moment of union entity instantiation.
    /// Such property drives values for properties `id`, `key` and `desc`.
    ///
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

    /// Performs validation to ensure correctness of the union type definition producing an early runtime exception (instantiation time).
    /// The two basic rules are:
    ///
    ///   - Union entities should not have properties that are not of entity type.
    ///   - Union properties should not contain more than one property of a certain entity type.
    ///
    /// Only if this validation passes, the super method is invoked to proceed with building of meta-properties.
    ///
    @Override
    protected final void setMetaPropertyFactory(final IMetaPropertyFactory metaPropertyFactory) {
        // check for inappropriate union entity properties
        final List<Field> fields = findRealProperties(getType());
        final List<Class<? extends AbstractEntity<?>>> propertyTypes = new ArrayList<>();
        for (final Field field : fields) {
            // union entities should not have properties that are not of entity type
            if (!AbstractEntity.class.isAssignableFrom(field.getType())) {
                throw new EntityDefinitionException(ERR_CONTAINS_PROPERTIES_OF_ORDINARY_TYPE.formatted(field.getName())); // kind one error
            }
            // union properties should not contain more than one property of a certain entity type
            if (propertyTypes.contains(field.getType())) {
                throw new EntityDefinitionException(ERR_CONTAINS_PROPERTIES_OF_THE_SAME_TYPES.formatted(field.getName())); // kind two error
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

    /// A convenient method to obtain the value of an active property. Returns null if all properties are null.
    ///
    public final AbstractEntity<?> activeEntity() {
        final Stream<String> propertyNames = streamRealProperties(getType()).map(Field::getName);

        return propertyNames
                .filter(propName -> !Reflector.isPropertyProxied(this, propName) && get(propName) != null)
                .findFirst() // returns Optional
                .map(propName -> (AbstractEntity<?>) get(propName)) // map optional propName value to an actual property value
                .orElse(null); // return the property value or null if there was no matching propName
    }

    /// A convenient method for setting a union property to a non-null value.
    /// It should only be used on union entity instances that do not yet have an active union-property.
    ///
    ///
    /// This method looks for an appropriate union-property based on the type of `value`, which gets assigned to that property.
    /// If no appropriate union-property is found, exception [EntityException] is thrown.
    ///
    /// @param value a non-null value
    /// @return an instance of this union entity
    /// @param <T> a type of the union entity as a convenience for method chaining
    ///
    public final <T extends AbstractUnionEntity> T setUnionProperty(@Nonnull final AbstractEntity<?> value) {
        // If null is being assigned then we only need to clear the active union property, if it exists.
        if (value == null) {
            throw new EntityException(ERR_NULL_IS_NOT_ACCEPTABLE.formatted(getType().getSimpleName()));
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
        throw new EntityException(ERR_NO_MATCHING_PROP_TYPE.formatted(value.getType().getSimpleName()));
    }

    /// Returns a set of property names that are common to all members of the specified union type.
    ///
    public static SequencedSet<String> commonProperties(final Class<? extends AbstractUnionEntity> type) {
        return Finder.commonPropertiesForUnion(type);
    }

    public String activePropertyName() {
        return activePropertyName;
    }

    /// Returns a list of properties that represent the members of the specified union type.
    ///
    public static List<Field> unionProperties(final Class<? extends AbstractUnionEntity> type) {
        return Finder.unionProperties(type);
    }

    /// Returns the name of a union property of type `propType`, declared in union entity of type `unionEntityType`.
    /// An empty result is returned if no matching property could be found.
    ///
    public static Optional<String> unionPropertyNameByType(
            @Nonnull final Class<? extends AbstractUnionEntity> unionType,
            @Nonnull final Class<? extends AbstractEntity<?>> propType)
    {
        requireNotNullArgument(unionType, "unionType");
        requireNotNullArgument(propType, "propType");

        return unionProperties(unionType).stream().filter(prop -> prop.getType().equals(propType)).findFirst().map(Field::getName);
    }

    /// Returns getter and setter method names for all common properties.
    ///
    public static List<String> commonMethodNames(final Class<? extends AbstractUnionEntity> type) {
        final List<String> commonMethods = new ArrayList<>();
        for (final Method method : commonMethods(type)) {
            commonMethods.add(method.getName());
        }
        return commonMethods;
    }

    /// Returns getters and setters for [AbstractUnionEntity] common properties.
    ///
    public static List<Method> commonMethods(final Class<? extends AbstractUnionEntity> type) {
        final Set<String> commonProperties = commonProperties(type);
        final List<Field> unionProperties = unionProperties(type);
        final List<Method> commonMethods = new ArrayList<>();
        final Class<?> propertyType = unionProperties.getFirst().getType();
        for (final String property : commonProperties) {
            try {
                commonMethods.add(Reflector.obtainPropertyAccessor(propertyType, property));
                commonMethods.add(Reflector.obtainPropertySetter(propertyType, property));
            } catch (final ReflectionException ex) {
                throw new ReflectionException(ERR_MISSING_ACCESSOR_OR_SETTER.formatted(property, propertyType), ex);
            }
        }
        return commonMethods;
    }

    // ===============================
    // Union membership predicates
    // ===============================

    /// Tests whether entity type `typeToCheckForMembership` matches one of the types of union properties in `unionType`.`
    ///
    public static boolean isUnionMember(
            @Nonnull final Class<? extends AbstractUnionEntity> unionType,
            @Nonnull final Class<? extends AbstractEntity<?>> typeToCheckForMembership)
    {
        requireNotNullArgument(unionType, "unionType");
        requireNotNullArgument(typeToCheckForMembership, "typeToCheckForMembership");

        return unionPropertyNameByType(unionType, typeToCheckForMembership).isPresent();
    }

    /// Tests whether entity type `typeToCheckForMembership` matches one of the types of union properties.
    ///
    public boolean isUnionMember(@Nonnull final Class<? extends AbstractEntity<?>> typeToCheckForMembership) {
        return isUnionMember((Class<? extends AbstractUnionEntity>) getType(), typeToCheckForMembership);
    }

    /// Tests whether the type of entity `valueWithTypeToCheckForMembership` matches one of the types of union properties in `unionType`.
    ///
    public static boolean isUnionMember(
            @Nonnull final Class<? extends AbstractUnionEntity> unionType,
            @Nonnull final AbstractEntity<?> valueWithTypeToCheckForMembership)
    {
        requireNotNullArgument(unionType, "unionType");
        requireNotNullArgument(valueWithTypeToCheckForMembership, "valueWithTypeToCheckForMembership");

        return unionPropertyNameByType(unionType, valueWithTypeToCheckForMembership.getType()).isPresent();
    }

    /// Tests whether the type of entity `valueWithTypeToCheckForMembership` matches one of the types of union properties.
    ///
    public boolean isUnionMember(@Nonnull final AbstractEntity<?> valueWithTypeToCheckForMembership) {
        return isUnionMember((Class<? extends AbstractUnionEntity>) getType(), valueWithTypeToCheckForMembership);
    }

    /// Tests whether the type of the active property in `unionWithActivePropertyToCheckForMembership` matches one of the types of union properties in `unionType`.
    /// This method should be convenient where there is a need to check if an active property of one capability is a member of another capability.
    ///
    public static boolean isUnionMember(
            @Nonnull final Class<? extends AbstractUnionEntity> unionType,
            @Nonnull final AbstractUnionEntity unionWithActivePropertyToCheckForMembership)
    {
        requireNotNullArgument(unionType, "unionType");
        requireNotNullArgument(unionWithActivePropertyToCheckForMembership, "unionWithActivePropertyToCheckForMembership");

        if (unionWithActivePropertyToCheckForMembership.activeEntity() == null) {
            throw new EntityException(ERR_MISSING_ACTIVE_PROP_TO_CHECK_MEMBERSHIP.formatted(unionType.getSimpleName()));
        }

        return unionPropertyNameByType(unionType, unionWithActivePropertyToCheckForMembership.activeEntity().getType()).isPresent();
    }

    // Test whether the type of the active property in `unionWithActivePropertyToCheckForMembership` matches one of the types of union properties.
    /// This method should be convenient where there is a need to check if an active property of one capability is a member of another capability.
    ///
    public boolean isUnionMember(@Nonnull final AbstractUnionEntity unionWithActivePropertyToCheckForMembership) {
        return isUnionMember((Class<? extends AbstractUnionEntity>) getType(), unionWithActivePropertyToCheckForMembership);
    }

    /// Tests whether the type of the active property of this instance matches one of the types of union properties in `unionType`.
    /// This method should be convenient where there is a need to check if an active property of one capability is a member of another capability.
    ///
    public boolean isActivePropertyUnionMemberOf(@Nonnull final Class<? extends AbstractUnionEntity> unionType) {
        return isUnionMember(unionType, this);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof AbstractUnionEntity that
                  && getType().equals(that.getType())
                  && Objects.equals(activeEntity(), that.activeEntity());
    }

    @Override
    public int hashCode() {
        return ofNullable(activeEntity()).map(AbstractEntity::hashCode).orElse(0) * 23;
    }

}
