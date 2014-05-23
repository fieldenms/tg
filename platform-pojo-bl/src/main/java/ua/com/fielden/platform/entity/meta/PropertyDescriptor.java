package ua.com.fielden.platform.entity.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

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
    private static final long serialVersionUID = 1L;

    private Class<T> entityType;
    private String propertyName;

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
    public PropertyDescriptor(final Class<T> entityType, final String propertyName) {
        final Pair<String, String> pair = TitlesDescsGetter.getTitleAndDesc(propertyName, entityType);
        setKey(pair.getKey());
        setDesc(pair.getValue());
        this.entityType = entityType;
        this.propertyName = propertyName;
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public String toString() {
        return entityType.getName() + ":" + propertyName;
    }

    /** A convenient factory method, which instantiates property descriptor from its toString representation. */
    public static <T extends AbstractEntity<?>> PropertyDescriptor<T> fromString(final String toStringRepresentation) throws Exception {
        final String[] parts = toStringRepresentation.split(":");
        final Class<T> entityType = (Class<T>) Class.forName(parts[0]);
        final String propertyName = parts[1];
        return new PropertyDescriptor<T>(entityType, propertyName);
    }

    /** A convenient factory method, which instantiates property descriptor from its toString representation. */
    public static <T extends AbstractEntity<?>> PropertyDescriptor<T> fromString(final String toStringRepresentation, final EntityFactory factory) throws Exception {
        final String[] parts = toStringRepresentation.split(":");
        final Class<T> entityType = (Class<T>) Class.forName(parts[0]);
        final String propertyName = parts[1];

        final Pair<String, String> pair = TitlesDescsGetter.getTitleAndDesc(propertyName, entityType);
        final PropertyDescriptor inst = (PropertyDescriptor) factory.newByKey(PropertyDescriptor.class, pair.getKey());
        inst.setDesc(pair.getValue());
        inst.setEntityType(entityType);
        inst.setPropertyName(propertyName);

        return inst;//new PropertyDescriptor<T>(entityType, propertyName);
    }

    private void setEntityType(final Class<T> entityType) {
        this.entityType = entityType;
    }

    private void setPropertyName(final String propertyName) {
        this.propertyName = propertyName;
    }

}
