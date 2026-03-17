package ua.com.fielden.platform.companion.helper;

import jakarta.inject.Inject;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.DbVersion.CaseSensitivity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.Money;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.exceptions.InvalidArgumentException.requireNotNullArgument;
import static ua.com.fielden.platform.entity.exceptions.NoSuchPropertyException.noSuchPropertyException;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.meta.PropertyMetadataUtils.SubPropertyNaming.SIMPLE;
import static ua.com.fielden.platform.types.Money.*;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;


/**
 * A helper class with functions to assist with construction of EQL condition based on entity key members.
 * 
 * @author TG Team
 *
 */
public class KeyConditionBuilder {

    private static final String ERR_DYNAMIC_PROPERTY_ACCESS_UNSUPPORTED = "Dynamic property access is not supported for [%s].";

    // TODO Replace by constructor injection.
    //      Refactoring static method into instance methods will be a breaking change.
    @Inject
    private static IDomainMetadata domainMetadata;

    private KeyConditionBuilder() {}
    
    /**
     * Convenient method for composing a query to select an entity by key value.
     *
     * @param dbVersion
     * @param entityType
     * @param keyType
     * @param filetrable
     * @param keyValues
     * @return
     */
    public static <T extends AbstractEntity<?>> EntityResultQueryModel<T> createQueryByKey(
            final DbVersion dbVersion,
            final Class<T> entityType,
            final Class<? extends Comparable<?>> keyType,
            final boolean filetrable,
            final Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            throw new IllegalArgumentException("No key values provided.");
        }
        final EntityResultQueryModel<T> query = attachKeyConditions(dbVersion, entityType, keyType, select(entityType).where(), keyValues).model();
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
            final DbVersion dbVersion,
            final Class<T> entityType,
            final Class<? extends Comparable<?>> keyType,
            final Collection<T> entitiesWithKeys) {
        IWhere0<T> partQ = select(entityType).where();
        final List<Field> keyMembers = Finder.getKeyMembers(entityType);
        
        for (final Iterator<T> iter = entitiesWithKeys.iterator(); iter.hasNext();) {
            final T entityWithKey = iter.next();
            final ICompoundCondition0<T> or = attachKeyConditions(dbVersion, entityType, keyType, partQ, keyMembers, keyMembers.stream().map(keyMember -> entityWithKey.get(keyMember.getName())).toArray());
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
            final DbVersion dbVersion,
            final Class<T> entityType,
            final Class<? extends Comparable<?>> keyType,
            final IWhere0<T> entryPoint,
            final Object... keyValues) {
        return attachKeyConditions(dbVersion, entityType, keyType, entryPoint, Finder.getKeyMembers(entityType), keyValues);
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
            final DbVersion dbVersion,
            final Class<T> entityType,
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

            // there could be a case where only one string value is provided, which in fact represents a composite key
            // that is why comparing the number of key members with the number of values provided to identify an error is not sufficient
            // we should admit a single string representation
            if (realKeyValues.length == 1 && realKeyValues[0] instanceof String) {
                return entryPoint.condition(buildConditionForKeyMember(dbVersion, entityType, KEY, String.class, realKeyValues[0]));
            } else if (keyMembers.size() != realKeyValues.length) {
                throw new EntityCompanionException(format("The number of provided values (%s) does not match the number of properties in the entity composite key (%s).", realKeyValues.length, keyMembers.size()));
            }

            ICompoundCondition0<T> cc = entryPoint.condition(buildConditionForKeyMember(dbVersion, entityType, keyMembers.get(0).getName(), keyMembers.get(0).getType(), realKeyValues[0]));
            for (int index = 1; index < keyMembers.size(); index++) {
                cc = cc.and().condition(buildConditionForKeyMember(dbVersion, entityType, keyMembers.get(index).getName(), keyMembers.get(index).getType(), realKeyValues[index]));
            }
            return cc;
        } else if (keyValues.length != 1) {
            throw new EntityCompanionException(format("Only one key value is expected instead of %s when looking for an entity by a non-composite key.", keyValues.length));
        } else if (isEntityType(keyType) && keyValues[0] instanceof String) {
            return entryPoint.condition(buildConditionForKeyMember(dbVersion, entityType, KEY + "." + KEY, keyType, keyValues[0]));
        } else {
            return entryPoint.condition(buildConditionForKeyMember(dbVersion, entityType, KEY , keyType, keyValues[0]));
        }
    }

    private static ConditionModel buildConditionForKeyMember(
            final DbVersion dbVersion,
            final Class<?> enclosingType,
            final String propPath,
            final Class<?> propType,
            final Object propValue)
    {
        if (propValue == null) {
            return cond().prop(propPath).isNull().model();
        } else if (String.class.equals(propType)) {
            if (dbVersion.caseSensitivity == CaseSensitivity.SENSITIVE) {
                return cond().lowerCase().prop(propPath).eq().lowerCase().val(propValue).model();
            } else {
                return cond().prop(propPath).eq().val(propValue).model();
            }
        } else if (Class.class.equals(propType)) {
            return cond().prop(propPath).eq().val(((Class<?>) propValue).getName()).model();
        }
        else if (domainMetadata.forProperty(enclosingType, propPath).type().isComponent()) {
            return domainMetadata.propertyMetadataUtils().subProperties(domainMetadata.forProperty(enclosingType, propPath), SIMPLE)
                    .stream()
                    .filter(PropertyMetadata::isPersistent)
                    .map(subProp -> buildConditionForKeyMember(dbVersion,
                                                               enclosingType,
                                                               propPath + "." + subProp.name(),
                                                               subProp.type().javaType(),
                                                               getComponent(propValue, subProp.name())))
                    .reduce((cond1, cond2) -> cond().condition(cond1).and().condition(cond2).model())
                    .orElseThrow();
        } else {
            return cond().prop(propPath).eq().val(propValue).model();
        }
    }

    // TODO This could be generalised to support all component types by enhancing DynamicPropertyAccess to support component types.
    private static Object getComponent(final Object object, final String prop) {
        requireNotNullArgument(object, "object");
        return switch (object) {
            case Money money -> switch (prop) {
                case AMOUNT ->  money.getAmount();
                case CURRENCY -> money.getCurrency();
                case TAX_AMOUNT -> money.getTaxAmount();
                case TAX_PERCENT -> money.getTaxPercent();
                case EX_TAX_AMOUNT -> money.getExTaxAmount();
                default -> throw noSuchPropertyException(Money.class, prop);
            };
            default -> throw new InvalidArgumentException(ERR_DYNAMIC_PROPERTY_ACCESS_UNSUPPORTED.formatted(object.getClass().getName()));
        };
    }


}
