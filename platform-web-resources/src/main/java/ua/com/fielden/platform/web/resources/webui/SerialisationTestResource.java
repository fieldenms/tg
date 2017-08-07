package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static ua.com.fielden.platform.serialisation.jackson.DefaultValueContract.getValidationResult;
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
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser.CachedProperty;
import ua.com.fielden.platform.serialisation.jackson.entities.EmptyEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithOtherEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.FactoryForTestingEntities;
import ua.com.fielden.platform.serialisation.jackson.entities.OtherEntity;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.ui.menu.MiTypeAnnotation;
import ua.com.fielden.platform.ui.menu.sample.MiEmptyEntity;
import ua.com.fielden.platform.ui.menu.sample.MiEntityWithOtherEntity;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.utils.WebUiResourceUtils;

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
        return WebUiResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            final List<AbstractEntity<?>> entities = (List<AbstractEntity<?>>) WebUiResourceUtils.restoreJSONResult(envelope, restUtil).getInstance();

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
        return WebUiResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
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
                if (EntityUtils.isPropertyDescriptor(e1.getType())) {
                    final PropertyDescriptor pd1 = (PropertyDescriptor) e1;
                    final PropertyDescriptor pd2 = (PropertyDescriptor) e2;
                    if (!EntityUtils.equalsEx(pd1.getEntityType(), pd2.getEntityType())) {
                        return Result.failure(format("PropertyDescriptors equality: pd1 [%s] entityType [%s] does not equal to pd2 [%s] entityType [%s].", pd1, pd1.getEntityType(), pd2, pd2.getEntityType()));
                    }
                    if (!EntityUtils.equalsEx(pd1.getPropertyName(), pd2.getPropertyName())) {
                        return Result.failure(format("PropertyDescriptors equality: pd1 [%s] propertyName [%s] does not equal to pd2 [%s] propertyName [%s].", pd1, pd1.getPropertyName(), pd2, pd2.getPropertyName()));
                    }
                }
                // id equality
                if (!EntityUtils.equalsEx(e1.getId(), e2.getId())) {
                    return Result.failure(format("e1 [%s] id [%s] does not equal to e2 [%s] id [%s].", e1, e1.getId(), e2, e2.getId()));
                }
                
                final boolean e1Instrumented = e1 == null ? false : PropertyTypeDeterminator.isInstrumented(e1.getClass());
                final boolean e2Instrumented = e2 == null ? false : PropertyTypeDeterminator.isInstrumented(e2.getClass());
                if (e1Instrumented != e2Instrumented) {
                    return Result.failure(format("e1's [%s] instrumentation [%s] does not equal to e2's [%s] instrumentation [%s].", e1, e1Instrumented, e2, e2Instrumented));
                }
                
                // id-only proxy instances equality
                if (e1.isIdOnlyProxy()) {
                    if (!e2.isIdOnlyProxy()) {
                        return Result.failure(format("e1 [%s] id-only proxiness [%s] does not equal to e2 [%s] id-only proxiness [%s].", e1, e1.isIdOnlyProxy(), e2, e2.isIdOnlyProxy()));
                    }
                    
                    // id-only proxy class equality
                    if (e1Instrumented) {
                        // getSuperclass() is needed due to wrapping of the actual class by Guice during instrumentation
                        if (!EntityUtils.equalsEx(e1.getClass().getSuperclass(), e2.getClass().getSuperclass())) {
                            return Result.failure(format("e1 [%s] id-only proxy type [%s] does not equal to e2 [%s] id-only proxy type [%s].", e1, e1.getClass().getSuperclass(), e2, e2.getClass().getSuperclass()));
                        }
                    } else {
                        if (!EntityUtils.equalsEx(e1.getClass(), e2.getClass())) {
                            return Result.failure(format("e1 [%s] id-only proxy type [%s] does not equal to e2 [%s] id-only proxy type [%s].", e1, e1.getClass(), e2, e2.getClass()));
                        }
                    }
                } else {
                    // version equality
                    if (!EntityUtils.equalsEx(e1.getVersion(), e2.getVersion())) {
                        return Result.failure(format("e1 [%s] version [%s] does not equal to e2 [%s] version [%s].", e1, e1.getVersion(), e2, e2.getVersion()));
                    }
                    
                    final List<CachedProperty> props = EntitySerialiser.createCachedProperties(e1.getType());
                    for (final CachedProperty prop : props) {
                        final String propName = prop.field().getName();
                        if (prop.getPropertyType() != null) {
                            // check property meta-info equality
                            if (e1Instrumented) {
                                final Result metaPropEq = deepEqualsForTesting(e1.getProperty(propName), e2.getProperty(propName), setOfCheckedEntities, prop.getPropertyType());
                                if (!metaPropEq.isSuccessful()) {
                                    return metaPropEq;
                                }
                            }
                            if (e1.proxiedPropertyNames().contains(propName)) {
                                if (!e2.proxiedPropertyNames().contains(propName)) {
                                    return Result.failure(format("e1 [%s] (type = %s) prop [%s] isProxied (true) does not equal to e2 [%s] (type = %s) prop [%s] isProxied (false).", e1, e1.getType().getSimpleName(), propName, e2, e2.getType().getSimpleName(), propName));
                                }
                            } else {
                                // check property value equality
                                final Object value1 = e1.get(propName);
                                final Object value2 = e2.get(propName);
                                final Result propValsEqual = deepEqualsForTestingForPropValues(e1, e2, setOfCheckedEntities, value1, value2, prop.getPropertyType(), propName, false);
                                if (!propValsEqual.isSuccessful()) {
                                    return propValsEqual;
                                }
                            }
                        }
                    }
                }
                return Result.successful(e1);
            }
        }
    }
    
    private static Result deepEqualsForTestingForPropValues(
        final AbstractEntity<?> e1, 
        final AbstractEntity<?> e2, 
        final IdentityHashMap<AbstractEntity<?>, String> setOfCheckedEntities, 
        final Object value1, 
        final Object value2, 
        final Class<?> propType,
        final String propName,
        final boolean original
    ) {
        if (EntityUtils.isEntityType(propType)) {
            final Result eq = deepEqualsForTesting((AbstractEntity<?>) value1, (AbstractEntity<?>) value2, setOfCheckedEntities);
            if (!eq.isSuccessful()) {
                return eq;
            }
        } else {
            final String origPrefix = original ? "original " : "";
            if (!EntityUtils.equalsEx(value1, value2)) { // prop equality
                return Result.failure(format("e1 [%s] (type = %s) " + origPrefix + "prop [%s] value [%s] does not equal to e2 [%s] (type = %s) " + origPrefix + "prop [%s] value [%s].", e1, e1.getType().getSimpleName(), propName, toString(value1), e2, e2.getType().getSimpleName(), propName, toString(value2)));
            }
        }
        return Result.successful("Ok");
    }

    private static String toString(final Object value) {
        if (value != null) {
            final Object tempValue1 = value;
            if (tempValue1.getClass().isAssignableFrom(Colour.class)) {
                return ((Colour) value).getColourValue();
            } else if (tempValue1.getClass().isAssignableFrom(Date.class)) {
                return Long.toString(((Date) value).getTime());
            }
        }
        return value + "";
    }

    private static <M> Result deepEqualsForTesting(
        final MetaProperty<M> metaProp1, 
        final MetaProperty<M> metaProp2,
        final IdentityHashMap<AbstractEntity<?>, String> setOfCheckedEntities, 
        final Class<?> propType
    ) {
        if (metaProp1 == null && metaProp2 != null) {
            return Result.failure(format("MetaProperty of originally created entity is null, but meta property of deserialised entity is not."));
        }
        
        if (metaProp1.isProxy() && !metaProp2.isProxy()) {
            return Result.failure(format("MetaProperty of originally created entity is proxied, but meta property of deserialised entity is not."));
        }
        if (!metaProp1.isProxy()) {
            // dirty equality
            //        if (!metaProp1.isCollectional()) {
            
            if (!EntityUtils.equalsEx(metaProp1.isChangedFromOriginal(), metaProp2.isChangedFromOriginal())) {
                return Result.failure(format("e1 [%s] prop's [%s] changedFromOriginal [%s] does not equal to e2 [%s] prop's [%s] changedFromOriginal [%s].", metaProp1.getEntity().getType().getSimpleName(), metaProp1.getName(), metaProp1.isChangedFromOriginal(), metaProp2.getEntity().getType().getSimpleName(), metaProp1.getName(), metaProp2.isChangedFromOriginal()));
            }
            // please refer to provideOriginalValue method in EntityJsonDeserialiser for more details on 'dirtiness'
//                if (!EntityUtils.equalsEx(metaProp1.isDirty(), metaProp2.isDirty())) {
//                    return Result.failure(format("e1 [%s] prop's [%s] dirtiness [%s] does not equal to e2 [%s] prop's [%s] dirtiness [%s].", metaProp1.getEntity().getType().getSimpleName(), metaProp1.getName(), metaProp1.isDirty(), metaProp2.getEntity().getType().getSimpleName(), metaProp1.getName(), metaProp2.isDirty()));
//                }
            
            // check property original value equality
            final Object originalValue1 = metaProp1.getOriginalValue();
            final Object originalValue2 = metaProp2.getOriginalValue();
            final Result propValsEqual = deepEqualsForTestingForPropValues(metaProp1.getEntity(), metaProp2.getEntity(), setOfCheckedEntities, originalValue1, originalValue2, propType, metaProp1.getName(), true);
            if (!propValsEqual.isSuccessful()) {
                return propValsEqual;
            }
            //        } else {
            //            // not supported -- dirtiness of the collectional properties for new entities differs from the regular properties
            //        }
            // editable equality
            if (!EntityUtils.equalsEx(metaProp1.isEditable(), metaProp2.isEditable())) {
                return Result.failure(format("e1 [%s] editability [%s] does not equal to e2 [%s] editability [%s].", metaProp1.getEntity(), metaProp1.isEditable(), metaProp2.getEntity(), metaProp2.isEditable()));
            }
            // required equality
            if (!EntityUtils.equalsEx(metaProp1.isRequired(), metaProp2.isRequired())) {
                return Result.failure(format("e1 [%s] requiredness [%s] does not equal to e2 [%s] requiredness [%s].", metaProp1.getEntity(), metaProp1.isRequired(), metaProp2.getEntity(), metaProp2.isRequired()));
            }
            // visible equality
            if (!EntityUtils.equalsEx(metaProp1.isVisible(), metaProp2.isVisible())) {
                return Result.failure(format("e1 [%s] Visible [%s] does not equal to e2 [%s] Visible [%s].", metaProp1.getEntity(), metaProp1.isVisible(), metaProp2.getEntity(), metaProp2.isVisible()));
            }
            // validationResult equality
            if (!resultsAreExpected(getValidationResult(metaProp1), getValidationResult(metaProp2))) {
                return Result.failure(format("e1 [%s] ValidationResult [%s] does not equal to e2 [%s] ValidationResult [%s].", metaProp1.getEntity(), getValidationResult(metaProp1), metaProp2.getEntity(), getValidationResult(metaProp2)));
            }
        }
        return Result.successful("OK");
    }

    private static boolean resultsAreExpected(final Result validationResult, final Result validationResult2) {
        if (validationResult == null) {
            return validationResult2 == null;
        } else {
            if (validationResult2 == null) {
                return false;
            } else {
                if (!EntityUtils.equalsEx(validationResult.getClass(), validationResult2.getClass())) {
                    return false;
                }
                if (!EntityUtils.equalsEx(validationResult.getMessage(), validationResult2.getMessage())) {
                    return false;
                }
                if (validationResult.getEx() == null) {
                    return validationResult2.getEx() == null;
                }
                if (!EntityUtils.equalsEx(validationResult.getEx().getMessage(), validationResult2.getEx().getMessage())) {
                    return false;
                }
                if (!EntityUtils.equalsEx(validationResult.getInstance(), validationResult2.getInstance())) {
                    return false;
                }
                return true;
            }
        }
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
                factory.createEntityWithStringAndFailure(),
                factory.createEntityWithStringAndPropertyConflict(),
                factory.createEntityWithStringAndWarning(),
                factory.createEntityWithStringAndSuccessfulResult(),
                factory.createEntityWithPropertyWithDefiner(),
                factory.createEntityWithBoolean(),
                factory.createEntityWithDate(),
                factory.createEntityWithMoney(),
                factory.createEntityWithColour(),
                factory.createEntityWithOtherEntity(),
                factory.createEntityWithSameEntity(),
                factory.createEntityWithSameEntityThatIsChangedFromOriginal(),
                factory.createEntityWithSameEntityCircularlyReferencingItself(),
                factory.createEntityWithOtherEntityCircularlyReferencingItself(),
                factory.createEntityWithSetOfSameEntities(),
                factory.createEntityWithListOfSameEntities(),
                factory.createEntityWithArraysAsListOfSameEntities(),
                factory.createEntityWithMapOfSameEntities(),
                factory.createEntityWithCompositeKey(),
                factory.createUninstrumentedEntity(),
                createGeneratedEntity(restUtil.getSerialiser(), false), // uninstrumented
                createGeneratedEntity(restUtil.getSerialiser(), true), // instrumented
                factory.createInstrumentedEntityWithUninstrumentedProperty(),
                factory.createUninstrumentedEntityWithInstrumentedProperty(),
                factory.createPropertyDescriptor(),
                factory.createPropertyDescriptorInstrumented(),
                factory.createEntityWithHyperlink(),
                factory.createUninstrumentedEntity(true, EntityWithOtherEntity.class),
                factory.createInstrumentedEntity(true, EntityWithOtherEntity.class),
                factory.createUninstrumentedGeneratedEntity(true, EntityWithOtherEntity.class, MiEntityWithOtherEntity.class)._1,
                factory.createInstrumentedGeneratedEntity(true, EntityWithOtherEntity.class, MiEntityWithOtherEntity.class)._1,
                factory.createUninstrumentedEntity(false, EntityWithOtherEntity.class).set("prop", createIdOnlyProxy(restUtil.getSerialiser())),
                factory.createInstrumentedEntity(false, EntityWithOtherEntity.class).set("prop", createIdOnlyProxy(restUtil.getSerialiser())),
                factory.createUninstrumentedGeneratedEntity(false, EntityWithOtherEntity.class, MiEntityWithOtherEntity.class)._1.set("prop", createIdOnlyProxy(restUtil.getSerialiser())),
                factory.createInstrumentedGeneratedEntity(false, EntityWithOtherEntity.class, MiEntityWithOtherEntity.class)._1.set("prop", createIdOnlyProxy(restUtil.getSerialiser()))
                );
    }
    
    private static AbstractEntity createIdOnlyProxy(final ISerialiser serialiser) {
        final TgJackson tgJackson = (TgJackson) serialiser.getEngine(SerialiserEngines.JACKSON);
        final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache = tgJackson.idOnlyProxiedEntityTypeCache;
        return EntityFactory.newPlainEntity(idOnlyProxiedEntityTypeCache.getIdOnlyProxiedTypeFor(OtherEntity.class), 189L);
    }
    
    private static AbstractEntity<String> createGeneratedEntity(final ISerialiser serialiser, final boolean instrumented) {
        final DynamicEntityClassLoader cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
        
        final Class<AbstractEntity<?>> emptyEntityTypeEnhanced;
        try {
            emptyEntityTypeEnhanced = (Class<AbstractEntity<?>>) 
                    cl.startModification(EmptyEntity.class.getName())
                        .modifyTypeName(new DynamicTypeNamingService().nextTypeName(EmptyEntity.class.getName()))
                        .addClassAnnotations(new MiTypeAnnotation().newInstance(MiEmptyEntity.class))
                    .endModification();
        } catch (final ClassNotFoundException e) {
            throw Result.failure(e);
        }
        final TgJackson tgJackson = (TgJackson) serialiser.getEngine(SerialiserEngines.JACKSON);
        tgJackson.registerNewEntityType(emptyEntityTypeEnhanced);
        
        final AbstractEntity<String> entity;
        if (instrumented) {
            entity = (AbstractEntity<String>) serialiser.factory().newEntity(emptyEntityTypeEnhanced, 159L);
        } else {
            entity = (AbstractEntity<String>) EntityFactory.newPlainEntity(emptyEntityTypeEnhanced, 159L);
        }

        entity.beginInitialising();
        entity.setKey("GENERATED+UNINSTRUMENTED");
        entity.setDesc("GENERATED+UNINSTRUMENTED desc");
        entity.endInitialising();
        
        return entity;
    }
    
    public List<AbstractEntity<?>> getEntities() {
        return Collections.unmodifiableList(entities);
    }
}
