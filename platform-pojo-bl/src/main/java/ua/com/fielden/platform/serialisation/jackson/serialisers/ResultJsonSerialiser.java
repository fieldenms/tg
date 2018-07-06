package ua.com.fielden.platform.serialisation.jackson.serialisers;

import static ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification.MASTER_ENTITY_PROPERTY_NAME;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.stripIfNeeded;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.isGenerated;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.EntityType;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.centre.CentreConfigDeleteAction;
import ua.com.fielden.platform.web.centre.CentreConfigDuplicateAction;
import ua.com.fielden.platform.web.centre.CentreConfigEditAction;
import ua.com.fielden.platform.web.centre.CentreConfigLoadAction;
import ua.com.fielden.platform.web.centre.CentreConfigNewAction;
import ua.com.fielden.platform.web.centre.CentreConfigSaveAction;

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
    public void serialize(final Result result, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
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
                        } else if (AbstractFunctionalEntityForCollectionModification.class.isAssignableFrom(itemClass)) {
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
                        } else if (CentreConfigNewAction.class.isAssignableFrom(itemClass) || CentreConfigDuplicateAction.class.isAssignableFrom(itemClass) || CentreConfigLoadAction.class.isAssignableFrom(itemClass) || CentreConfigEditAction.class.isAssignableFrom(itemClass) || CentreConfigDeleteAction.class.isAssignableFrom(itemClass) || CentreConfigSaveAction.class.isAssignableFrom(itemClass)) {
                            final Map<String, Object> customObject = ((AbstractEntity<?>) item).get("customObject");
                            if (customObject.get("appliedCriteriaEntity") != null && EntityQueryCriteria.class.isAssignableFrom(customObject.get("appliedCriteriaEntity").getClass())) {
                                generatedTypes.add(stripIfNeeded(customObject.get("appliedCriteriaEntity").getClass()));
                            }
                        } else if (isEntityType(itemClass) && EntityQueryCriteria.class.isAssignableFrom(itemClass)) {
                            generatedTypes.add(itemClass);
                        }
                    }
                });
                final ArrayList<EntityType> genList = new ArrayList<>();
                generatedTypes.forEach(t -> {
                    genList.add(tgJackson.registerNewEntityType((Class<AbstractEntity<?>>) t));
                });
                generator.writeObject(genList);
            } else if (EntityUtils.isEntityType(type) && EntityQueryCriteria.class.isAssignableFrom(type)) {
                final Class<AbstractEntity<?>> newType = (Class<AbstractEntity<?>>) type;
                generator.writeObject(tgJackson.registerNewEntityType(newType));
            } else {
                generator.writeObject(type.getName());
                if (CentreConfigNewAction.class.isAssignableFrom(type) || CentreConfigDuplicateAction.class.isAssignableFrom(type) || CentreConfigLoadAction.class.isAssignableFrom(type) || CentreConfigEditAction.class.isAssignableFrom(type) || CentreConfigDeleteAction.class.isAssignableFrom(type) || CentreConfigSaveAction.class.isAssignableFrom(type)) {
                    final Map<String, Object> customObject = ((AbstractEntity<?>) result.getInstance()).get("customObject");
                    if (customObject.get("appliedCriteriaEntity") != null && EntityQueryCriteria.class.isAssignableFrom(customObject.get("appliedCriteriaEntity").getClass())) {
                        generator.writeFieldName("@instanceTypes");
                        final ArrayList<EntityType> genList = new ArrayList<>();
                        final Class<AbstractEntity<?>> newType = (Class<AbstractEntity<?>>) stripIfNeeded(customObject.get("appliedCriteriaEntity").getClass());
                        genList.add(tgJackson.registerNewEntityType(newType));
                        generator.writeObject(genList);
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
}
