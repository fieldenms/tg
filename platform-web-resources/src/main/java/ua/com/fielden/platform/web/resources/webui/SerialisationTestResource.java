package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.CachedProperty;
import ua.com.fielden.platform.serialisation.jackson.entities.FactoryForTestingEntities;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Resource for integration test of Java and JavaScript serialisation.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 */
public class SerialisationTestResource extends ServerResource {
    private final RestServerUtil restUtil;
    private final List<AbstractEntity<?>> entities;
    private final DefaultValueContract dvc;

    public SerialisationTestResource(final EntityFactory entityFactory, final RestServerUtil restUtil, final Context context, final Request request, final Response response, final Date testingDate) {
        init(context, request, response);
        this.restUtil = restUtil;
        this.entities = createEntities(entityFactory, testingDate);
        this.dvc = new DefaultValueContract();
    }

    /**
     * Handles receiving back serialised testing entities from the Web UI client and checking whether they are 'deep equal' to the send ones.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            final List<AbstractEntity<?>> entities = (List<AbstractEntity<?>>) EntityResourceUtils.restoreJSONResult(envelope, restUtil).getInstance();

            final Result result = deepEqualsForTesting(this.entities, entities, this.dvc);
            if (!result.isSuccessful()) {
                throw result;
            }
            return restUtil.resultJSONRepresentation(result);
        }, restUtil);
    }

    /**
     * Handles sending of the serialised testing entities to the Web UI client (GET method).
     */
    @Override
    protected Representation get() {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            return restUtil.listJSONRepresentation(this.entities);
        }, restUtil);
    }

    private static Result deepEqualsForTesting(final List<AbstractEntity<?>> entities1, final List<AbstractEntity<?>> entities2, final DefaultValueContract dvc) {
        final IdentityHashMap<AbstractEntity<?>, String> setOfCheckedEntities = new IdentityHashMap<>();
        if (entities1 == null) {
            if (entities2 == null) {
                return new Result(null, "okay");
            } else {
                return Result.failure(format("entities1 [%s] does not equal to entities2 [%s].", entities1, entities2));
            }
        } else {
            if (entities2 == null) {
                return Result.failure(format("entities1 [%s] does not equal to entities2 [%s].", entities1, entities2));
            } else {
                if (entities1.size() != entities2.size()) {
                    return Result.failure(format("entities1.size() [%s] does not equal to entities2.size() [%s].", entities1.size(), entities2.size()));
                } else {
                    final Iterator<AbstractEntity<?>> iter1 = entities1.iterator();
                    final Iterator<AbstractEntity<?>> iter2 = entities2.iterator();
                    while (iter1.hasNext()) {
                        final AbstractEntity<?> e1 = iter1.next();
                        final AbstractEntity<?> e2 = iter2.next();
                        final Result deepEquals = deepEqualsForTesting(e1, e2, setOfCheckedEntities, dvc);
                        if (!deepEquals.isSuccessful()) {
                            return deepEquals;
                        }
                    }
                    return new Result(null, "okay");
                }
            }
        }
    }

    private static Result deepEqualsForTesting(final AbstractEntity<?> e1, final AbstractEntity<?> e2, final IdentityHashMap<AbstractEntity<?>, String> setOfCheckedEntities, final DefaultValueContract dvc) {
        if (e1 == null) {
            if (e2 == null) {
                return Result.successful(e1);
            } else {
                return Result.failure(format("e1 [%s] does not equal to e2 [%s].", e1, e2));
            }
        } else {
            if (e2 == null) {
                return Result.failure(format("e1 [%s] does not equal to e2 [%s].", e1, e2));
            } else {
                if (markedAsChecked(e1, setOfCheckedEntities)) {
                    return Result.successful(e1);
                }
                markAsChecked(e1, setOfCheckedEntities);
                // reference equality (should NOT be referentially equal)
                if (e1 == e2) {
                    return Result.failure(format("e1 [%s] equals to e2 [%s] by reference.", e1, e2));
                }

                // type equality
                if (!EntityUtils.equalsEx(e1.getType(), e2.getType())) {
                    return Result.failure(format("e1 [%s] type [%s] does not equal to e2 [%s] type [%s].", e1, e1.getType(), e2, e2.getType()));
                }
                // id equality
                if (!EntityUtils.equalsEx(e1.getId(), e2.getId())) {
                    return Result.failure(format("e1 [%s] id [%s] does not equal to e2 [%s] id [%s].", e1, e1.getId(), e2, e2.getId()));
                }
                // version equality
                if (!EntityUtils.equalsEx(e1.getVersion(), e2.getVersion())) {
                    return Result.failure(format("e1 [%s] version [%s] does not equal to e2 [%s] version [%s].", e1, e1.getVersion(), e2, e2.getVersion()));
                }
                final List<CachedProperty> props = EntitySerialiser.createCachedProperties(e1.getType());
                for (final CachedProperty prop : props) {
                    final String propName = prop.field().getName();
                    if (prop.getPropertyType() != null) {
                        // check property meta-info equality
                        final Result metaPropEq = deepEqualsForTesting(e1.getProperty(propName), e2.getProperty(propName), dvc);
                        if (!metaPropEq.isSuccessful()) {
                            return metaPropEq;
                        }
                        // check property value equality
                        if (EntityUtils.isEntityType(prop.getPropertyType())) {
                            final Result eq = deepEqualsForTesting((AbstractEntity<?>) e1.get(propName), (AbstractEntity<?>) e2.get(propName), setOfCheckedEntities, dvc);
                            if (!eq.isSuccessful()) {
                                return eq;
                            }
                        } else {
                            if (e1.getType().getSimpleName().equals("EntityWithDefiner") && propName.equals("prop2")) { // special check for the entity which has definer artifacts (the props do not equal)
                                if (e1.get(propName) != null || !e2.get(propName).equals("okay_defined")) {
                                    return Result.failure(format("e1 [%s] (type = %s) prop [%s] value [%s] does not equal to null OR e2 [%s] (type = %s) prop [%s] value [%s] not equal to 'okay_defined'.", e1, e1.getType().getSimpleName(), propName, e1.get(propName), e2, e2.getType().getSimpleName(), propName, e2.get(propName)));
                                }
                            } else if (!EntityUtils.equalsEx(e1.get(propName), e2.get(propName))) { // prop equality
                                final String value1 = getValue(e1, propName);
                                final String value2 = getValue(e2, propName);
                                return Result.failure(format("e1 [%s] (type = %s) prop [%s] value [%s] does not equal to e2 [%s] (type = %s) prop [%s] value [%s].", e1, e1.getType().getSimpleName(), propName, value1, e2, e2.getType().getSimpleName(), propName, value2));
                            }

                        }
                    }
                }
                return Result.successful(e1);
            }
        }
    }

    private static String getValue(final AbstractEntity<?> e1, final String propName) {
        if (e1.get(propName) != null) {
            final Object tempValue1 = e1.get(propName);
            if (tempValue1.getClass().isAssignableFrom(Colour.class)) {
                return ((Colour) e1.get(propName)).getColourValue();
            } else if (tempValue1.getClass().isAssignableFrom(Date.class)) {
                return Long.toString(((Date) e1.get(propName)).getTime());
            }
        }
        return e1.get(propName);
    }

    private static <M> Result deepEqualsForTesting(final MetaProperty<M> metaProp1, final MetaProperty<M> metaProp2, final DefaultValueContract dvc) {
        if (metaProp1 == null && metaProp2 != null) {
            return Result.failure(format("MetaProperty of originally created entity is null, but meta property of deserialised entity is not."));
        }
        // dirty equality
        //        if (!metaProp1.isCollectional()) {
        if (DefaultValueContract.getChangedFromOriginal(metaProp1)) {
            if (!EntityUtils.equalsEx(DefaultValueContract.getChangedFromOriginal(metaProp1), metaProp2.isDirty())) {
                return Result.failure(format("e1 [%s] prop's [%s] changedFromOriginal [%s] does not equal to e2 [%s] prop's [%s] changedFromOriginal [%s].", metaProp1.getEntity().getType().getSimpleName(), metaProp1.getName(), metaProp1.isChangedFromOriginal(), metaProp2.getEntity().getType().getSimpleName(), metaProp1.getName(), metaProp2.isChangedFromOriginal()));
            }
        }
        //        } else {
        //            // not supported -- dirtiness of the collectional properties for new entities differs from the regular properties
        //        }
        // editable equality
        if (!EntityUtils.equalsEx(DefaultValueContract.getEditable(metaProp1), DefaultValueContract.getEditable(metaProp2))) {
            return Result.failure(format("e1 [%s] editability [%s] does not equal to e2 [%s] editability [%s].", metaProp1.getEntity(), metaProp1.isEditable(), metaProp2.getEntity(), metaProp2.isEditable()));
        }
        // required equality
        if (!EntityUtils.equalsEx(DefaultValueContract.getRequired(metaProp1), DefaultValueContract.getRequired(metaProp2))) {
            return Result.failure(format("e1 [%s] requiredness [%s] does not equal to e2 [%s] requiredness [%s].", metaProp1.getEntity(), metaProp1.isRequired(), metaProp2.getEntity(), metaProp2.isRequired()));
        }
        // visible equality
        if (!EntityUtils.equalsEx(DefaultValueContract.getVisible(metaProp1), DefaultValueContract.getVisible(metaProp2))) {
            return Result.failure(format("e1 [%s] Visible [%s] does not equal to e2 [%s] Visible [%s].", metaProp1.getEntity(), metaProp1.isVisible(), metaProp2.getEntity(), metaProp2.isVisible()));
        }
        // validationResult equality
        if (!resultsAreExpected(DefaultValueContract.getValidationResult(metaProp1), DefaultValueContract.getValidationResult(metaProp2))) {
            return Result.failure(format("e1 [%s] ValidationResult [%s] does not equal to e2 [%s] ValidationResult [%s].", metaProp1.getEntity(), DefaultValueContract.getValidationResult(metaProp1), metaProp2.getEntity(), DefaultValueContract.getValidationResult(metaProp2)));
        }
        return Result.successful("OK");
    }

    private static boolean resultsAreExpected(final Result validationResult, final Result validationResult2) {
        if (validationResult2 != null) { // validation result should disappear after deserialisation
            return false;
        }
        return true;
        //        if (validationResult == null) {
        //            if (validationResult2 == null) {
        //                return true;
        //            } else {
        //                return false;
        //            }
        //        } else {
        //            if (validationResult2 == null) {
        //                return false;
        //            } else {
        //                if (!EntityUtils.equalsEx(validationResult.getMessage(), validationResult2.getMessage())) {
        //                    return false;
        //                }
        //                if (!EntityUtils.equalsEx(validationResult.getEx().getMessage(), validationResult2.getEx().getMessage())) {
        //                    return false;
        //                }
        //                if (!EntityUtils.equalsEx(validationResult.getInstance(), validationResult2.getInstance())) {
        //                    return false;
        //                }
        //                return true;
        //            }
        //        }
    }

    private static void markAsChecked(final AbstractEntity<?> e1, final IdentityHashMap<AbstractEntity<?>, String> setOfCheckedEntities) {
        setOfCheckedEntities.put(e1, null);
    }

    private static boolean markedAsChecked(final AbstractEntity<?> e1, final IdentityHashMap<AbstractEntity<?>, String> setOfCheckedEntities) {
        return setOfCheckedEntities.containsKey(e1);
    }

    private static List<AbstractEntity<?>> createEntities(final EntityFactory entityFactory, final Date testingDate) {
        final FactoryForTestingEntities factory = new FactoryForTestingEntities(entityFactory, testingDate);
        return Arrays.asList(factory.createNullEmptyEntity(),
                factory.createSimpleEmptyEntity(),
                factory.createEmptyEntityWithNoId(),
                factory.createEmptyEntityWithNoKey(),
                factory.createEmptyEntityWithNoDescription(),
                factory.createEntityWithBigDecimal(),
                factory.createEntityWithInteger(),
                factory.createEntityWithString(),
                factory.createEntityWithStringNonEditable(),
                factory.createEntityWithStringRequired(),
                factory.createEntityWithStringNonVisible(),
                factory.createEntityWithStringAndResult(),
                factory.createEntityWithPropertyWithDefiner(),
                factory.createEntityWithBoolean(),
                factory.createEntityWithDate(),
                factory.createEntityWithMoney(),
                factory.createEntityWithColour(),
                factory.createEntityWithOtherEntity(),
                factory.createEntityWithSameEntity(),
                factory.createEntityWithSameEntityCircularlyReferencingItself(),
                factory.createEntityWithOtherEntityCircularlyReferencingItself(),
                factory.createEntityWithSetOfSameEntities(),
                factory.createEntityWithListOfSameEntities(),
                factory.createEntityWithArraysAsListOfSameEntities(),
                factory.createEntityWithMapOfSameEntities(),
                factory.createEntityWithCompositeKey(),
                factory.createUninstrumentedEntity(),
                factory.createUninstrumentedEntity(),
                factory.createUninstrumentedEntity(),
                factory.createUninstrumentedEntity(),
                factory.createUninstrumentedEntity(),
                factory.createUninstrumentedEntity()
                );
    }
}
