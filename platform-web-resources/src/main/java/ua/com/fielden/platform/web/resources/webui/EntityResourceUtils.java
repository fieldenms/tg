package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.representation.Representation;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.MiscUtilities;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * This utility class contains the methods that are shared across {@link EntityResource} and {@link EntityValidationResource}.
 *
 * @author TG Team
 *
 */
public class EntityResourceUtils<T extends AbstractEntity<?>> {
    private final EntityFactory entityFactory;
    private final Logger logger = Logger.getLogger(getClass());
    private final Class<T> entityType;
    private final IFetchProvider<T> fetchProvider;
    private final IEntityDao<T> dao;
    private final IEntityProducer<T> entityProducer;
    private final ICompanionObjectFinder companionFinder;

    public EntityResourceUtils(final Class<T> entityType, final IEntityProducer<T> entityProducer, final EntityFactory entityFactory, final RestServerUtil restUtil, final ICompanionObjectFinder companionFinder) {
        this.entityType = entityType;
        this.companionFinder = companionFinder;
        this.dao = companionFinder.<IEntityDao<T>, T> find(this.entityType);

        this.fetchProvider = this.dao.getFetchProvider();
        this.entityFactory = entityFactory;
        this.entityProducer = entityProducer;
    }

    /**
     * Initialises the entity for retrieval.
     *
     * @param id
     *            -- entity identifier
     * @return
     */
    public T createEntityForRetrieval(final Long id) {
        final T entity;
        if (id != null) {
            entity = dao.findById(id, fetchProvider.fetchModel());
        } else {
            entity = entityProducer.newEntity();
        }
        return entity;
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public ICompanionObjectFinder getCompanionFinder() {
        return companionFinder;
    }

    public IFetchProvider<T> getFetchProvider() {
        return fetchProvider;
    }

    /**
     * Applies the values from <code>dirtyPropertiesHolder</code> into the <code>entity</code>. The values needs to be converted from the client-side component-specific form into
     * the values, which can be set into Java entity's property.
     *
     * @param modifiedPropertiesHolder
     * @param entity
     * @return
     */
    public T apply(final Map<String, Object> modifiedPropertiesHolder, final T entity) {
        final Object arrivedVersionVal = modifiedPropertiesHolder.get(AbstractEntity.VERSION);
        final Long version = ((Integer) arrivedVersionVal).longValue();
        final boolean isEntityStale = entity.getVersion() > version;

        // iterate through modified properties:
        for (final Map.Entry<String, Object> nameAndVal : modifiedPropertiesHolder.entrySet()) {
            final String name = nameAndVal.getKey();
            if (!name.equals(AbstractEntity.ID) && !name.equals(AbstractEntity.VERSION)) {
                final Map<String, Object> valAndOrigVal = (Map<String, Object>) nameAndVal.getValue();
                if (valAndOrigVal.containsKey("val")) { // this is a modified property
                    final Object newValue = convert(getEntityType(), name, valAndOrigVal.get("val"));
                    if (!isEntityStale) {
                        entity.set(name, newValue);
                    } else {
                        final Object staleOriginalValue = convert(getEntityType(), name, valAndOrigVal.get("origVal"));
                        if (EntityUtils.isConflicting(newValue, staleOriginalValue, entity.get(name))) {
                            entity.getProperty(name).setDomainValidationResult(Result.failure(entity, "The property has been recently changed by other user. Please revert property value to resolve conflict."));
                        } else {
                            entity.set(name, newValue);
                        }
                    }
                } else { // this is unmodified property
                    if (!isEntityStale) {
                        // do nothing
                    } else {
                        final Object originalValue = convert(getEntityType(), name, valAndOrigVal.get("origVal"));
                        if (EntityUtils.isStale(originalValue, entity.get(name))) {
                            entity.getProperty(name).setDomainValidationResult(Result.warning(entity, "The property has been recently changed by other user."));
                        }
                    }
                }
            }
        }
        return entity;
    }

    /**
     * Converts <code>reflectedValue</code> (which is a string, number, boolean or null) into a value of appropriate type (the type of actual property).
     *
     * @param type
     * @param propertyName
     * @param reflectedValue
     * @return
     */
    private Object convert(final Class<T> type, final String propertyName, final Object reflectedValue) {
        final Class propertyType = PropertyTypeDeterminator.determinePropertyType(type, propertyName);

        // NOTE: "missing value" for Java entities is also 'null' as for JS entities
        if (EntityUtils.isEntityType(propertyType)) {
            if (reflectedValue == null) {
                return null;
            }
            final Class<AbstractEntity<?>> entityPropertyType = propertyType;

            final IEntityDao<AbstractEntity<?>> propertyCompanion = getCompanionFinder().<IEntityDao<AbstractEntity<?>>, AbstractEntity<?>> find(entityPropertyType);

            final EntityResultQueryModel<AbstractEntity<?>> model = select(entityPropertyType).where().//
            /*      */prop(AbstractEntity.KEY).iLike().anyOfValues((Object[]) MiscUtilities.prepare(Arrays.asList((String) reflectedValue))).//
            /*      */model();
            final QueryExecutionModel<AbstractEntity<?>, EntityResultQueryModel<AbstractEntity<?>>> qem = from(model).with(getFetchProvider().fetchFor(propertyName).fetchModel()).model();
            return propertyCompanion.getEntity(qem);
            // prev implementation => return propertyCompanion.findByKeyAndFetch(getFetchProvider().fetchFor(propertyName).fetchModel(), reflectedValue);
        } else if (EntityUtils.isString(propertyType)) {
            return reflectedValue == null ? null : reflectedValue;
        } else if (Integer.class.isAssignableFrom(propertyType)) {
            return reflectedValue == null ? null : reflectedValue;
        } else if (Boolean.class.isAssignableFrom(propertyType)) {
            return reflectedValue == null ? null : reflectedValue;
        } else if (Date.class.isAssignableFrom(propertyType)) {
            return reflectedValue == null ? null : (reflectedValue instanceof Integer ? new Date(((Integer) reflectedValue).longValue()) : new Date((Long) reflectedValue));
        } else if (BigDecimal.class.isAssignableFrom(propertyType)) {
            return reflectedValue == null ? null : (reflectedValue instanceof Integer ? new BigDecimal((Integer) reflectedValue) : new BigDecimal((Double) reflectedValue));
        } else if (Money.class.isAssignableFrom(propertyType)) {
            if (reflectedValue == null) {
                return null;
            }
            final Map<String, Object> map = (Map<String, Object>) reflectedValue;

            final BigDecimal amount = map.get("amount") instanceof Integer ? new BigDecimal((Integer) map.get("amount")) : new BigDecimal((Double) map.get("amount"));
            final String currencyStr = (String) map.get("currency");
            final Integer taxPercentage = (Integer) map.get("taxPercent");

            return taxPercentage == null ? new Money(amount, Currency.getInstance(currencyStr)) : new Money(amount, taxPercentage, Currency.getInstance(currencyStr));
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported conversion to [%s + %s] from reflected value [%s].", type.getSimpleName(), propertyName, reflectedValue));
        }
    }

    /**
     * Restores the holder of modified properties into the map [propertyName; webEditorSpecificValue].
     *
     * @param envelope
     * @return
     */
    public Map<String, Object> restoreModifiedPropertiesHolderFrom(final Representation envelope, final RestServerUtil restUtil) {
        try {
            return (Map<String, Object>) restUtil.restoreJSONMap(envelope);
        } catch (final Exception ex) {
            logger.error("An undesirable error has occured during deserialisation of modified properties holder, which should be validated.", ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Just saves the entity.
     *
     * @param entity
     * @return
     */
    public T save(final T entity) {
        return dao.save(entity);
    }

    /**
     * Deletes the entity.
     *
     * @param entityId
     */
    public void delete(final Long entityId) {
        dao.delete(entityFactory.newEntity(entityType, entityId));
    }

}
