package ua.com.fielden.platform.serialisation.jackson.serialisers;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification.MASTER_ENTITY_PROPERTY_NAME;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.stripIfNeeded;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.isGenerated;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.web.centre.AbstractCentreConfigAction.APPLIED_CRITERIA_ENTITY_NAME;
import static ua.com.fielden.platform.web.centre.AbstractCentreConfigAction.CUSTOM_OBJECT_PROPERTY_NAME;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.EntityType;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.centre.AbstractCentreConfigAction;
import ua.com.fielden.platform.web.centre.CentreConfigLoadAction;

/**
 * Serialiser for {@link Result} type.
 * <p>
 * Serialises information about concrete {@link Result}'s subtype, message, exception and instance with its type.
 *
 * @author TG Team
 *
 */
public class ResultJsonSerialiser extends StdSerializer<Result> {
    private final TgJackson tgJackson;

    public ResultJsonSerialiser(final TgJackson tgJackson) {
        super(Result.class);
        this.tgJackson = tgJackson;
    }

    @Override
    public void serialize(final Result result, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
        generator.writeStartObject();

        generator.writeFieldName("@resultType");
        generator.writeObject(result.getClass().getName());
        generator.writeFieldName("message");
        generator.writeObject(result.getMessage());

        if (result.getInstance() != null) {
            generator.writeFieldName("@instanceType");
            final Class<?> type = PropertyTypeDeterminator.stripIfNeeded(result.getInstance().getClass());

            if (List.class.isAssignableFrom(type)) {
                generator.writeObject(type.getName());

                generator.writeFieldName("@instanceTypes");
                final List<Object> list = (List<Object>) result.getInstance();
                // TODO there could be the hierarchy of generatedTypes!
                // TODO there could be the hierarchy of generatedTypes!
                // TODO there could be the hierarchy of generatedTypes!
                // TODO there could be the hierarchy of generatedTypes!
                // TODO there could be the hierarchy of generatedTypes!
                // TODO there could be the hierarchy of generatedTypes!
                // TODO there could be the hierarchy of generatedTypes!
                final Set<Class<?>> generatedTypes = new LinkedHashSet<>();
                list.forEach(item -> {
                    if (item != null) {
                        final Class<?> itemClass = stripIfNeeded(item.getClass());
                        if (isGenerated(itemClass)) {
                            generatedTypes.add(itemClass);
                        } else {
                            if (AbstractFunctionalEntityForCollectionModification.class.isAssignableFrom(itemClass)) {
                                // As it was mentioned above there could be a deep hierarchy of generated types, not only 'root' value.
                                // This is especially relevant to the situation of domain trees with calculated properties on different levels.
                                // At this stage we have only one situation where generated type (potentially not registered in serialiser) exists on first level of root type's properties.
                                // This is the case for both CentreConfigUpdater and CentreConfigLoadAction, but potentially may occur in other collectional functional entities.
                                // At this stage only this small case will be handled -- generated type of 'masterEntity' value will be registered in TgJackson.
                                // In future, traversal of entity instance tree can be done similarly as in DefinersExecutor using DFS algorithm.
                                // The checks on proxiness and idOnlyProxiness was heavily inspired by DefinersExecutor logic.
                                final AbstractFunctionalEntityForCollectionModification collectionUpdater = (AbstractFunctionalEntityForCollectionModification) item;
                                if (!collectionUpdater.proxiedPropertyNames().contains(MASTER_ENTITY_PROPERTY_NAME)) {
                                    final AbstractEntity<?> value = collectionUpdater.getMasterEntity();
                                    if (value != null && !value.isIdOnlyProxy() && isGenerated(value.getClass())) {
                                        generatedTypes.add(stripIfNeeded(value.getClass()));
                                    }
                                }
                            }
                            if (isCentreConfigAction(itemClass)) {
                                possiblyUnregisteredCriteriaTypeFrom(item).ifPresent(generatedTypes::add);
                            }
                        }
                    }
                });
                generator.writeObject(generatedTypes.stream().map(this::toEntityType).collect(Collectors.toList()));
            } else if (EntityUtils.isEntityType(type) && EntityQueryCriteria.class.isAssignableFrom(type)) {
                generator.writeObject(toEntityType(type)); 
            } else {
                generator.writeObject(type.getName());
                if (isCentreConfigAction(type)) {
                    final Optional<Class<?>> possiblyUnregisteredCriteriaType = possiblyUnregisteredCriteriaTypeFrom(result.getInstance());
                    if (possiblyUnregisteredCriteriaType.isPresent()) { // isPresent was used here instead of ifPresent due to the need to pass exceptions from 'write' methods upward
                        generator.writeFieldName("@instanceTypes");
                        generator.writeObject(listOf(toEntityType(possiblyUnregisteredCriteriaType.get()))); // deliberately used ArrayList for graceful serialisation
                    }
                }
            }

            generator.writeFieldName("instance");
            generator.writeObject(result.getInstance());
        }

        if (result.getEx() != null) {
            generator.writeFieldName("ex");
            generator.writeObject(result.getEx());
        }

        generator.writeEndObject();
    }
    
    private EntityType toEntityType(final Class<?> type) {
        return tgJackson.registerNewEntityType((Class<AbstractEntity<?>>) type); 
    }
    
    /**
     * Returns <code>true</code> if entity is centre config action containing custom object with possibly unregistered criteria entity type, <code>false</code> otherwise.
     * 
     * @param type
     * @return
     */
    private static boolean isCentreConfigAction(final Class<?> type) {
        return AbstractCentreConfigAction.class.isAssignableFrom(type) || CentreConfigLoadAction.class.isAssignableFrom(type);
    }
    
    /**
     * Returns the type of criteria entity from custom object of <code>centreConfigActionObj</code>.
     * This type may not be registered in serialiser due to origination on other server node. In this case it is needed to
     * provide adhoc registration.
     * 
     * @param centreConfigActionObj -- entity of type {@link AbstractCentreConfigAction} or {@link CentreConfigLoadAction}
     * @return
     */
    private static Optional<Class<?>> possiblyUnregisteredCriteriaTypeFrom(final Object centreConfigActionObj) {
        final AbstractEntity<?> centreConfigAction = (AbstractEntity<?>) centreConfigActionObj;
        if (!centreConfigAction.proxiedPropertyNames().contains(CUSTOM_OBJECT_PROPERTY_NAME)) {
            final Map<String, Object> customObject = centreConfigAction.get(CUSTOM_OBJECT_PROPERTY_NAME);
            if (customObject != null) {
                final Object appliedCriteriaEntity = customObject.get(APPLIED_CRITERIA_ENTITY_NAME);
                if (appliedCriteriaEntity != null && EntityQueryCriteria.class.isAssignableFrom(appliedCriteriaEntity.getClass())) {
                    return of(stripIfNeeded(appliedCriteriaEntity.getClass()));
                }
            }
        }
        return empty();
    }
    
}