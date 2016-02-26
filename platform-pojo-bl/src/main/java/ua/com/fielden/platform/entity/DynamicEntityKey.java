package ua.com.fielden.platform.entity;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;

/**
 * Represents a composite entity key that should be used whenever entity key has a reference to another entity. This class provides dynamic implementation of required methods (i.e.
 * equals, compareTo and hashCode) based on JEXL expressions and completely takes away the need for manual implementation of complex composite keys.
 * <p>
 * The current implementation takes a full advantage of annotations in determining what entity properties should be included into the composite key. Annotation
 * {@link CompositeKeyMember} should be used to declare property to be included. There should be no properties with the same order, and there should be at least one annotated
 * property. Failure to comply with these requirements cause a runtime exception.
 * <p>
 * The order defined for annotated properties is used for implementing methods <code>equals</code>, <code>compareTo</code> and <code>hasCode</code>. Please also note that all
 * annotated properties should implement {@link Comparable}.
 * <p>
 * In cases where {@link Comparable} cannot be implemented by the property class, a {@link Comparator} instance should be provided using method
 * {@link #addKeyMemberComparator(Integer, Comparator)}.
 *
 * @author TG Team
 *
 * @param <T>
 */
public final class DynamicEntityKey implements Comparable<DynamicEntityKey> {

    private List<String> memberNames = new ArrayList<>();

    private transient final AbstractEntity<DynamicEntityKey> entity;
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    public final String KEY_MEMBERS_SEPARATOR;
    /** There case where key members do not implement Comparable. In such cases a comparator class should be provided. */
    private transient final Map<Integer, Comparator<?>> keyMemberComparables = new HashMap<>();

    /**
     * This default constructor is required purely for Kryo serialisation.
     */
    protected DynamicEntityKey() {
        KEY_MEMBERS_SEPARATOR = null;
        entity = null;
    }

    /**
     * Constructs composite key for the specified entity based on the list of expressions, which are in most cases properties of the entity.
     *
     * @param entity
     * @param expressions
     */
    public DynamicEntityKey(final AbstractEntity<DynamicEntityKey> entity) {
        final List<Field> compositeKeyMambers = Finder.getKeyMembers(entity.getType());
        // If there is only one key member and it has name KEY then this is the wrong place to be using composite key
        if (compositeKeyMambers.size() == 1 && AbstractEntity.KEY.equals(compositeKeyMambers.get(0).getName())) {
            throw new EntityDefinitionException("Composite key should have at least one member.");
        }

        this.entity = entity;

        KEY_MEMBERS_SEPARATOR = Reflector.getKeyMemberSeparator((Class<? extends AbstractEntity<DynamicEntityKey>>) entity.getType());

        for (final Field member : compositeKeyMambers) {
            memberNames.add(member.getName());
        }
    }

    /**
     * Associates comparator instance with a key member by its order number.
     * <p>
     * The associated comparator is used if provided even if the key member implements Comparable.
     *
     * @param keyMemberOrderNumber
     * @param comparator
     */
    public void addKeyMemberComparator(final Integer keyMemberOrderNumber, final Comparator<?> comparator) {
        keyMemberComparables.put(keyMemberOrderNumber, comparator);
    }

    /**
     * Obtains the value of the key member with the specified index.
     *
     * @param memberIndex
     * @return
     */
    private Object value(final int memberIndex) {
        return entity.get(memberNames.get(memberIndex));
    }

    /**
     * A convenient method for obtaining values of the entity properties constituting its composite key.
     *
     * @return an array of values
     */
    public Object[] getKeyValues() {
        final List<Object> values = new ArrayList<Object>();
        for (int index = 0; index < memberNames.size(); index++) {
            values.add(value(index));
        }
        return values.toArray();
    }

    /**
     * Perform dynamic comparison of this and passed key instances sequentially comparing associated with them expressions. The first mismatch is used for the final comparison,
     * which becomes the result of this method.
     *
     */
    @SuppressWarnings("unchecked")
    @Override
    public final int compareTo(final DynamicEntityKey key) {
        for (int index = 0; index < memberNames.size(); index++) {
            if (keyMemberComparables.get(index + 1) != null) {
                final Comparator comparator = keyMemberComparables.get(index + 1);
                final int result = comparator.compare(value(index), key.value(index));
                if (result != 0) {
                    return result;
                }
            } else {
                final Comparable thisValue = (Comparable) value(index);
                final Comparable thatValue = (Comparable) key.value(index);
                // first check the cases where one of or both values are null
                if (thisValue == null && thatValue != null) {
                    return -1;
                }
                if (thisValue != null && thatValue == null) {
                    return 1;
                }
                if (thisValue != null && thatValue != null) {
                    // there are no nulls, so need to perform comparison
                    final int partialCmpResult = thisValue.compareTo(thatValue);
                    if (partialCmpResult != 0) {
                        return partialCmpResult;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Performs comparison base on method {@link #compareTo(DynamicEntityKey)}.
     */
    @Override
    public final boolean equals(final Object key) {
        if (this == key) {
            return true;
        }
        if (!(key instanceof DynamicEntityKey)) {
            return false;
        }
        return compareTo((DynamicEntityKey) key) == 0;
    }

    /**
     * Calculates hash code as a sum of hash codes for all expressions multiplied by some prime number.
     */
    @Override
    public final int hashCode() {
        int result = 29;
        for (int index = 0; index < memberNames.size(); index++) {
            final Object keyMemberValue = value(index);
            if (keyMemberValue != null) {
                result += value(index).hashCode() * 13;
            }
        }
        return result;
    }

    @Override
    public final String toString() {
        // collect not null values of composite key members to simplify stringification
        final List<Object> notNullValues = new ArrayList<>();
        for (int index = 0; index < memberNames.size(); index++) {
            final Object value = value(index);
            if (value != null) {
                notNullValues.add(value);
            }
        }

        // stringify the key
        final StringBuilder buffer = new StringBuilder();
        for (int index = 0; index < notNullValues.size(); index++) {
            buffer.append(convertToString(notNullValues.get(index)) + (index + 1 < notNullValues.size() ? KEY_MEMBERS_SEPARATOR : ""));
        }
        return buffer.toString();
    }

    private final String convertToString(final Object value) {
        if (value instanceof Date) {
            return dateFormatter.format(value);
        } else {
            return value.toString();
        }
    }

    public final List<String> getMemberNames() {
        return memberNames;
    }
}
