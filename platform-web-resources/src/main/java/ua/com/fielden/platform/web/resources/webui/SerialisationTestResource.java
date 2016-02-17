package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getChangedFromOriginal;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getEditable;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getRequired;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getValidationResult;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getVisible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
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
    private final List<AbstractEntity<?>> entities = new ArrayList<>();

    public SerialisationTestResource(final RestServerUtil restUtil, final Context context, final Request request, final Response response, final FactoryForTestingEntities testingEntitiesFactory, final List<AbstractEntity<?>> entities) {
        init(context, request, response);
        this.restUtil = restUtil;
        this.entities.addAll(entities);
    }
    
    public SerialisationTestResource(final RestServerUtil restUtil, final Context context, final Request request, final Response response, final FactoryForTestingEntities testingEntitiesFactory) {
        this(restUtil, context, request, response, testingEntitiesFactory, createEntities(restUtil, testingEntitiesFactory));
    }

    /**
     * Handles receiving back serialised testing entities from the Web UI client and checking whether they are 'deep equal' to the send ones.
     */
    @Post
    public Representation checkEntitiesOnEqualityAndSendResult(final Representation envelope) {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            final List<AbstractEntity<?>> entities = (List<AbstractEntity<?>>) EntityResourceUtils.restoreJSONResult(envelope, restUtil).getInstance();

            final Result result = deepEqualsForTesting(this.entities, entities);
            if (!result.isSuccessful()) {
                throw result;
            }
            return restUtil.resultJSONRepresentation(result);
        }, restUtil);
    }

    /**
     * Handles sending of the serialised testing entities to the Web UI client (GET method).
     */
    @Get
    public Representation sendSerialisedEntities() {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            return restUtil.listJSONRepresentation(this.entities);
        }, restUtil);
    }

    private static Result deepEqualsForTesting(final List<AbstractEntity<?>> entities1, final List<AbstractEntity<?>> entities2) {
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
                        final boolean e1Instrumented = e1 == null ? false : PropertyTypeDeterminator.isInstrumented(e1.getClass());
                        final boolean e2Instrumented = e2 == null ? false : PropertyTypeDeterminator.isInstrumented(e2.getClass());
                        if (e1Instrumented != e2Instrumented) {
                            return Result.failure(format("e1's [%s] instrumentation [%s] does not equal to e2's [%s] instrumentation [%s].", e1, e1Instrumented, e2, e2Instrumented));
                        }
                        final Result deepEquals = deepEqualsForTesting(e1, e2, setOfCheckedEntities);
                        if (!deepEquals.isSuccessful()) {
                            return deepEquals;
                        }
                    }
                    return new Result(null, "okay");
                }
            }
        }
    }

    private static Result deepEqualsForTesting(final AbstractEntity<?> e1, final AbstractEntity<?> e2, final IdentityHashMap<AbstractEntity<?>, String> setOfCheckedEntities) {
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
                        final Result metaPropEq = deepEqualsForTesting(e1.getProperty(propName), e2.getProperty(propName));
                        if (!metaPropEq.isSuccessful()) {
                            return metaPropEq;
                        }
                        // check property value equality
                        if (EntityUtils.isEntityType(prop.getPropertyType())) {
                            final Result eq = deepEqualsForTesting((AbstractEntity<?>) e1.get(propName), (AbstractEntity<?>) e2.get(propName), setOfCheckedEntities);
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

    private static <M> Result deepEqualsForTesting(final MetaProperty<M> metaProp1, final MetaProperty<M> metaProp2) {
        if (metaProp1 == null && metaProp2 != null) {
            return Result.failure(format("MetaProperty of originally created entity is null, but meta property of deserialised entity is not."));
        }
        // dirty equality
        //        if (!metaProp1.isCollectional()) {
        if (getChangedFromOriginal(metaProp1)) {
            if (!EntityUtils.equalsEx(getChangedFromOriginal(metaProp1), metaProp2.isDirty())) {
                return Result.failure(format("e1 [%s] prop's [%s] changedFromOriginal [%s] does not equal to e2 [%s] prop's [%s] changedFromOriginal [%s].", metaProp1.getEntity().getType().getSimpleName(), metaProp1.getName(), metaProp1.isChangedFromOriginal(), metaProp2.getEntity().getType().getSimpleName(), metaProp1.getName(), metaProp2.isChangedFromOriginal()));
            }
        }
        //        } else {
        //            // not supported -- dirtiness of the collectional properties for new entities differs from the regular properties
        //        }
        // editable equality
        if (!EntityUtils.equalsEx(getEditable(metaProp1), getEditable(metaProp2))) {
            return Result.failure(format("e1 [%s] editability [%s] does not equal to e2 [%s] editability [%s].", metaProp1.getEntity(), metaProp1.isEditable(), metaProp2.getEntity(), metaProp2.isEditable()));
        }
        // required equality
        if (!EntityUtils.equalsEx(getRequired(metaProp1), getRequired(metaProp2))) {
            return Result.failure(format("e1 [%s] requiredness [%s] does not equal to e2 [%s] requiredness [%s].", metaProp1.getEntity(), metaProp1.isRequired(), metaProp2.getEntity(), metaProp2.isRequired()));
        }
        // visible equality
        if (!EntityUtils.equalsEx(getVisible(metaProp1), getVisible(metaProp2))) {
            return Result.failure(format("e1 [%s] Visible [%s] does not equal to e2 [%s] Visible [%s].", metaProp1.getEntity(), metaProp1.isVisible(), metaProp2.getEntity(), metaProp2.isVisible()));
        }
        // validationResult equality
        if (!resultsAreExpected(getValidationResult(metaProp1), getValidationResult(metaProp2))) {
            return Result.failure(format("e1 [%s] ValidationResult [%s] does not equal to e2 [%s] ValidationResult [%s].", metaProp1.getEntity(), getValidationResult(metaProp1), metaProp2.getEntity(), getValidationResult(metaProp2)));
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

    private static List<AbstractEntity<?>> createEntities(final RestServerUtil restUtil, final FactoryForTestingEntities factory) {
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
                factory.createGeneratedEntity(restUtil.getSerialiser(), false), // uninstrumented
                factory.createGeneratedEntity(restUtil.getSerialiser(), true), // instrumented
                factory.createInstrumentedEntityWithUninstrumentedProperty(),
                factory.createUninstrumentedEntityWithInstrumentedProperty()
                );
    }
    
    public List<AbstractEntity<?>> getEntities() {
        return Collections.unmodifiableList(entities);
    }
}
