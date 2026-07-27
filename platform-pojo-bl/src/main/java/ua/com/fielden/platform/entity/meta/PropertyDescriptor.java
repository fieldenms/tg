package ua.com.fielden.platform.entity.meta;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.*;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
import static ua.com.fielden.platform.utils.EntityUtils.isIntrospectionDenied;

import java.lang.reflect.Field;
import java.util.Optional;

import org.apache.logging.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipDefaultStringKeyMemberValidation;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * A class that describes entity property. It is like {@link Field}, but for types descendant from {@link AbstractEntity} and simpler holding only property name and description.
 * <p>
 * It does not overlap with {@link MetaProperty} in any way since the latter represents a property of an actual entity instance whereas this one represents a property description
 * as determined from an entity class.
 * <p>
 * Due to the fact that this class is itself derived from {@link AbstractEntity} it can be used, for example, for implementation of a value matcher and autocompletion (this was the
 * original purpose).
 * <p>
 * The following is the meaning behind class fields
 * <ul>
 * <li><code>key</key> -- holds property title
 * <li><code>desc</key> -- holds property description
 * <li><code>entityType</key> -- class of the enclosing entity type
 * <li><code>propertyName</key> -- the name of the property as present in the enclosing entity type
 * </ul>
 * Instantiation can be done with ordinary <code>new</code> -- there is no need to use {@link EntityFactory}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Property", desc = "Property title")
@DescTitle(value = "Description", desc = "Property description")
public class PropertyDescriptor<T extends AbstractEntity<?>> extends AbstractEntity<String> {

    private static final Logger LOGGER = getLogger(PropertyDescriptor.class);

    private static final String ERR_INTROSPECTION_DENIED = "Introspection is denied for [%s.%s].";
    public static final String ERR_COULD_NOT_BE_CREATED = "PropertyDescriptor could not be created from value [%s].";

    private Class<T> entityType;
    private String propertyName;

    @IsProperty
    @Title(value = "Property", desc = "Property title")
    @SkipDefaultStringKeyMemberValidation
    private String key;

    /**
     * Default constructor is required for serialisation.
     */
    protected PropertyDescriptor() {
        super(null, null, null);
    }

    /**
     * Instantiates property descriptor based on the provided entity type and property name.
     *
     * @param entityType
     *            -- any descendant of {@link AbstractEntity}.
     * @param propertyName
     *            -- name of the property that directly belongs to the specified entity (i.e. support for dot notation does not make any sense in this case)
     */
    public PropertyDescriptor(final Class<T> entityType, final CharSequence propertyName) {
        validateArguments(entityType, propertyName);

        setKey(nonBlankPropertyTitle(propertyName, entityType));
        setDesc(getTitleAndDesc(propertyName, entityType).getValue());
        this.entityType = entityType;
        this.propertyName = propertyName.toString();
    }

    public PropertyDescriptor(final Class<T> entityType, final String propertyName) {
        this(entityType, (CharSequence) propertyName);
    }

    /**
     * A convenient factory method.
     */
    public static <T extends AbstractEntity<?>> PropertyDescriptor<T> pd(final Class<T> entityType, final String propName) {
        return new PropertyDescriptor<>(entityType, propName);
    }

    /**
     * A convenient factory method.
     */
    public static <T extends AbstractEntity<?>> PropertyDescriptor<T> pd(final Class<T> entityType, final CharSequence propName) {
        return new PropertyDescriptor<>(entityType, propName);
    }

    /**
     * A convenience factory method to produce a parameterised class {@link PropertyDescriptor} for a specific {@code entityType}.
     *
     * @param <T>
     * @param entityType
     * @return
     */
    public static <T extends AbstractEntity<?>> Class<PropertyDescriptor<T>> pdTypeFor(final Class<T> entityType) {
        return (Class) PropertyDescriptor.class;
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Observable
    @Override
    public PropertyDescriptor setKey(final String key) {
        this.key = key;
        return this;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return entityType.getName() + ":" + propertyName;
    }

    @Override
    public int hashCode() {
        return entityType.hashCode() * 31 + propertyName.hashCode() + 23;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof PropertyDescriptor)) {
            return false;
        }
        
        final PropertyDescriptor<?> that = (PropertyDescriptor<?>) obj;
        return equalsEx(this.entityType, that.entityType) && equalsEx(this.propertyName, that.propertyName);
    }

    /** A convenient factory method, which instantiates property descriptor from its toString representation. */
    public static <T extends AbstractEntity<?>> PropertyDescriptor<T> fromString(final String toStringRepresentation) {
        return fromString(toStringRepresentation, Optional.empty());
    }

    /** A convenient factory method, which instantiates property descriptor from its toString representation. */
    public static <T extends AbstractEntity<?>> PropertyDescriptor<T> fromString(final String toStringRepresentation, final Optional<EntityFactory> maybeFactory) {
        try {
            final String[] parts = toStringRepresentation.split(":");
            final Class<T> entityType = (Class<T>) Class.forName(parts[0]);
            final String propertyName = parts[1];

            validateArguments(entityType, propertyName);

            // If a property title is explicitly set to a blank string, the value of PropertyDescriptor.key will be null,
            // which will cause an error during retrieval with EQL. To prevent this, let's always use a non-blank title.
            final String propTitle = nonBlankPropertyTitle(propertyName, entityType);

            final PropertyDescriptor<T> pd = maybeFactory.map(f -> f.newByKey(PropertyDescriptor.class, propTitle)).orElseGet(PropertyDescriptor<T>::new);
            pd.setKey(propTitle);
            pd.setDesc(getTitleAndDesc(propertyName, entityType).getValue());
            pd.entityType = entityType;
            pd.propertyName = propertyName;
            return pd;
        } catch (final Exception ex) {
            final String msg = format(ERR_COULD_NOT_BE_CREATED, toStringRepresentation);
            LOGGER.error(msg, ex);
            throw EntityException.wrapIfNecessary(msg, ex);
        }
    }

    /**
     * Validates the property being modelled by a property desctiptor.
     * This method must always be called during initialisation.
     */
    private static void validateArguments(final Class<? extends AbstractEntity<?>> entityType, final CharSequence property) {
        if (isIntrospectionDenied(entityType, property)) {
            throw new InvalidArgumentException(ERR_INTROSPECTION_DENIED.formatted(entityType.getSimpleName(), property));
        }
    }

}
