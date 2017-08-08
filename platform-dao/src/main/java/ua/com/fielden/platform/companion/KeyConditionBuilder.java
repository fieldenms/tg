package ua.com.fielden.platform.companion;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.Finder;


/**
 * A helper class with functions to assist with construction of EQL condition based on entity key members.
 * 
 * @author TG Team
 *
 */
public class KeyConditionBuilder {
    private KeyConditionBuilder() {}
    
    /**
     * Convenient method for composing a query to select an entity by key value.
     * 
     * @param entityType
     * @param keyType
     * @param filetrable
     * @param keyValues
     * @return
     */
    public static <T extends AbstractEntity<?>> EntityResultQueryModel<T> createQueryByKey(
            final Class<T> entityType,
            final Class<? extends Comparable<?>> keyType,
            final boolean filetrable,
            final Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            throw new IllegalArgumentException("No key values provided.");
        }
        final EntityResultQueryModel<T> query = attachKeyConditions(entityType, keyType, select(entityType).where(), keyValues).model();
        query.setFilterable(filetrable);
        return query;
    }
    
    /**
     * Creates a query for entities by their keys (simple or composite). If <code>entitiesWithKeys</code> are empty -- returns empty optional. 
     * 
     * @param entityType -- the entity type
     * @param keyType
     * @param entitiesWithKeys -- the entities with <b>all</b> key values correctly fetched / assigned
     * @return
     */
    public static <T extends AbstractEntity<?>> Optional<EntityResultQueryModel<T>> createQueryByKeyFor(
            final Class<T> entityType,
            final Class<? extends Comparable<?>> keyType,
            final Collection<T> entitiesWithKeys) {
        IWhere0<T> partQ = select(entityType).where();
        final List<Field> keyMembers = Finder.getKeyMembers(entityType);
        
        for (final Iterator<T> iter = entitiesWithKeys.iterator(); iter.hasNext();) {
            final T entityWithKey = iter.next();
            final ICompoundCondition0<T> or = attachKeyConditions(keyType, partQ, keyMembers, keyMembers.stream().map(keyMember -> entityWithKey.get(keyMember.getName())).toArray());
            if (iter.hasNext()) {
                partQ = or.or();
            } else {
                return Optional.of(or.model());
            }
        }

        return Optional.empty();
    }
    
    /**
     * Attaches key member conditions to the partially constructed query <code>entryPoint</code> based on its values. 
     * 
     * @param entityType
     * @param keyTypet
     * @param entryPoint
     * @param keyValues
     * @return
     */
    public static <T extends AbstractEntity<?>> ICompoundCondition0<T> attachKeyConditions(
            final Class<T> entityType,
            final Class<? extends Comparable<?>> keyType,
            final IWhere0<T> entryPoint,
            final Object... keyValues) {
        return attachKeyConditions(keyType, entryPoint, Finder.getKeyMembers(entityType), keyValues);
    }

    /**
     * Attaches key member conditions to the partially constructed query <code>entryPoint</code> based on its values. 
     * 
     * @param entryPoint
     * @param keyMembers
     * @param keyValues
     * @return
     */
    public static <T extends AbstractEntity<?>> ICompoundCondition0<T> attachKeyConditions(
            final Class<? extends Comparable<?>> keyType, 
            final IWhere0<T> entryPoint, final List<Field> keyMembers, 
            final Object... keyValues) {
        if (keyType == DynamicEntityKey.class) {
            // let's be smart about the key values and support the case where an instance of DynamicEntityKey is passed.
            final Object[] realKeyValues;
            if (keyValues.length == 1 && keyValues[0] instanceof DynamicEntityKey) {
                realKeyValues = ((DynamicEntityKey) keyValues[0]).getKeyValues(); 
            } else {
                realKeyValues = keyValues;
            }

            if (keyMembers.size() != realKeyValues.length) {
                throw new EntityCompanionException(format("The number of provided values (%s) does not match the number of properties in the entity composite key (%s).", realKeyValues.length, keyMembers.size()));
            }

            ICompoundCondition0<T> cc = entryPoint.condition(buildConditionForKeyMember(keyMembers.get(0).getName(), keyMembers.get(0).getType(), realKeyValues[0]));

            for (int index = 1; index < keyMembers.size(); index++) {
                cc = cc.and().condition(buildConditionForKeyMember(keyMembers.get(index).getName(), keyMembers.get(index).getType(), realKeyValues[index]));
            }
            return cc;
        } else if (keyValues.length != 1) {
            throw new EntityCompanionException(format("Only one key value is expected instead of %s when looking for an entity by a non-composite key.", keyValues.length));
        } else {
            return entryPoint.condition(buildConditionForKeyMember(AbstractEntity.KEY, keyType, keyValues[0]));
        }
    }

    private static ConditionModel buildConditionForKeyMember(final String propName, final Class<?> propType, final Object propValue) {
        if (propValue == null) {
            return cond().prop(propName).isNull().model();
        } else if (String.class.equals(propType)) {
            return cond().lowerCase().prop(propName).eq().lowerCase().val(propValue).model();
        } else if (Class.class.equals(propType)) {
            return cond().prop(propName).eq().val(((Class<?>) propValue).getName()).model();
        } else {
            return cond().prop(propName).eq().val(propValue).model();
        }
    }


}
