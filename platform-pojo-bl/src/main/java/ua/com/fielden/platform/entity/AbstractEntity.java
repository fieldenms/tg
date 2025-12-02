package ua.com.fielden.platform.entity;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.factory.BeforeChangeAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.HandlerAnnotation;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.exceptions.DynamicPropertyAccessCriticalError;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.MetaPropertyFull;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxyEntity;
import ua.com.fielden.platform.entity.proxy.StrictProxyException;
import ua.com.fielden.platform.entity.validation.*;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.ioc.EntityIocModule;
import ua.com.fielden.platform.ioc.ObservableMutatorInterceptor;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.reflection.*;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.types.IWithValidation;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.StreamUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.*;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.annotation.IsProperty.*;
import static ua.com.fielden.platform.entity.annotation.SkipDefaultStringKeyMemberValidation.ALL_DEFAULT_STRING_KEY_VALIDATORS;
import static ua.com.fielden.platform.entity.exceptions.EntityDefinitionException.*;
import static ua.com.fielden.platform.entity.validation.custom.DefaultEntityValidator.validateWithCritOnly;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresentForClass;
import static ua.com.fielden.platform.reflection.EntityMetadata.entityExistsAnnotation;
import static ua.com.fielden.platform.reflection.EntityMetadata.isEntityExistsValidationApplicable;
import static ua.com.fielden.platform.reflection.Finder.isKeyOrKeyMember;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isNumeric;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.stripIfNeeded;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedSetOf;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.utils.StreamUtils.typeFilter;

/**
 * <h3>General Info</h3>
 *
 * This class serves as a common parent for all entity types. It is envisaged that all entity classes should have a surrogate key <code>id</code>, business key <code>key</code> and
 * the description <code>desc</code>.
 *
 * Also, this class provides a generic implementation for methods {@link #hashCode()}, {@link #equals(Object)()}, {@link #toString()} and {@link #compareTo(AbstractEntity)}, which
 * is based on the used of the business key and thus should be suitable in most cases.
 *
 * Composite key with no reference to other entities can be easily mapped by Hibernate as a component. However, Hibernate does not permit many-to-one associations as part of the
 * component mapping. Thus, the need for special treatment of composite keys with such associations.
 *
 * Let's introduce a use case. Consider that there is a purchase order (PO) entity and its items. The business key for PO is just a string reflecting its number. The business key
 * for PO item is composite consisting of the sequential number (i.e. 1, 2, 3) representing the order of items associated with a PO, and a PO itself. A possible implementation of
 * such key could be as follows:
 * <p>
 *
 * <pre>
 * public class PoItemKey {
 * 	private Integer number;
 * 	private PurchaserOrder po;
 * 	...
 * }
 * </pre>
 *
 * <p>
 * And the PO item class would have this class as a <code>key</code> property:
 * <p>
 *
 * <pre>
 * public class PoItem {
 * 	private PoItemKey key;
 * 	...
 * }
 * </pre>
 *
 * <p>
 * Unfortunately, there is no way to map property PoItem.key with Hibernate. So what needs to be done is this.<br/>
 * <p>
 * First, introduce public inner class PoItemKey within PoItem, which implements {@link Comparable} and methods {@link #equals(Object)}, {@link #hashCode()} based on properties
 * from the enclosing class PoItem:<br/>
 *
 * <pre>
 * class PoItemKey implements Comparable[PoItemKey] {
 * 	 &#064;Override public int hashCode() {
 * 		return getPurchaseOrder().hashCode() * 23 + getNumber().hashCode() * 13;
 *    }
 * 	 &#064;Override public boolean equals(final Object obj) {
 * 		...
 * 		final PoItemKey cmpTo = (PoItemKey) obj;
 * 		return getPurchaseOrder().equals(cmpTo.getPurchaseOrder()) &amp;&amp;
 *                     getNumber().equals(cmpTo.getNumber());
 *    }
 * 	 &#064;Override public int compareTo(final PoItemKey cmpTo) {
 *              if (getPurchaseOrder().equals(cmpTo.getPurchaseOrder())) {
 *                 return getNumber().compareTo(cmpTo.getNumber());
 *              }  else {
 *                 return getPurchaseOrder().compareTo(cmpTo.getPurchaseOrder());
 *              }
 *    }
 *      ...
 * }
 * </pre>
 *
 * <p>
 * The PoItem class itself should need to have properties <code>purchaseOrder</code> and <code>number</>, which can be easily mapped by Hibernate.
 * <p>
 * Please note that class {@link DynamicEntityKey} automates implementation of entities with composite keys.
 * <p>
 *
 * <h3>Additional Functionality</h3>
 *
 * AbstractEntity supports meta properties, property change listeners and validation.
 * <p>
 * For any field to be recognised as property that requires meta-information annotation {@link IsProperty} should be used.
 * Meta-properties are represented by {@link MetaProperty}, which are instantiated by {@link IMetaPropertyFactory}.
 * A concrete {@link IMetaPropertyFactory} implementation can be set or injected using setter {@link #setMetaPropertyFactory(IMetaPropertyFactory)}.
 * <p>
 * At this stage meta-property instantiation also ensures association of the created instance with property validators, which are defined by annotations on the corresponding setter that are listed by enumeration {@link ValidationAnnotation}.
 * An instance of {@link IMetaPropertyFactory} is responsible for instantiation of validators, which implement {@link IBeforeChangeEventHandler} interface.
 * <p>
 * In order for validators to perform validation upon an attempt to set a property value, setters should be intercepted.
 * Intercepter {@link ObservableMutatorInterceptor} was implemented specifically to handle validation of values being passed into setters.
 * Its implementation uses validators associated with property during meta-property instantiation.
 *
 * However, entity instance should be created with Guice intercepter provided with a module configured to bind this intercepter.
 * <p>
 * A similar situation is with support of property change event handling. Any setter annotated with {@link Observable} should be intercepted by {@link ObservableMutatorInterceptor},
 * which can be achieved by using appropriately configured Guice module.
 *
 * Please refer {@link EntityIocModule} for more details.
 *
 * <h3>Property mutators</h3>
 * The <i>property</i> specification as defined in JavaBeans does not cover fully the needs identified by our team for working with business entities where properties have loosely coupled validation logic and change observation.
 * Also, the approach taken in JavaBeans does not provide the possibility to follow <a href="http://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a> programming approach.
 * Thus, Trident Genesis introduces its own notion of property, property mutators and accessors.
 * <p>
 * <p>
 * In TG there are two main kinds of properties:
 * <ul>
 *  <li>Simple Property -- such as Double, String, Entity etc.
 *  <li>Collectional Property -- Collection[String], List[Entity] etc.
 * </ul>
 * The treatment for accessors of these two kinds are identical. However, mutators are quite different.
 *  <p>
 * Let's denote [property] as a placeholder for the property name. The property accessor is a method with no parameters and name '''get[property]'''.
 * At least at this stage it does not support any annotations and its use goes unnoticeably. It is important, however, to note that accessor implementation for collectional properties has to return immutable collection, which is purely developer's responsibility.
 * Otherwise, any modification to the property would also happen unnoticeably and would most likely violate TG principles of validation and observation. Also, this is a good programming practice in general, enforced in the TG programming model.
 * <p>
 * The mutator concept is a different beast, and should be introduced separately for simple and collectional properties.
 * <ul>
 *   <li>Mutator for a simple property -- a method with name '''set[property]''' and one parameter matching the type of the field representing the property.
 *
 *       It may and usually should be annotated with {@link Observable} to ensure observation of the property change. And may have a number of validation annotations.
 *   </li>
 *   <li>Mutators for a collectional property -- there are three possible mutators recognised by TG:
 *       <ul>
 *          <li>Method name is '''set[property]''', which accepts one parameter of the same type such as Collection[Type] or List[type].
 *              This method as any other mutator should be provided with annotations discussed in the previous item.
 *              However, there is a general recommendation to implement such mutator in the following manner:
 *              <pre>
 *                  EntityOwner setCollecionalProperty(final Collection[Type] newValues) {
 *                  	collecionalProperty.clear();
 *                  	collecionalProperty.addAll(newValues);
 *                  	return this;
 *                  }
 * </pre>
 *          </li>
 *          <li>Method name is '''addTo[property]''' with one parameter of collectional parameter type. So, if property has type ''Collection[Type]'' then input parameter of type ''Type'' is expected.
 *              This mutator is called ''incrementor'' -- it leads to an incremental expansion of the collectional property. The same rules for method annotations apply as above.
 *          </li>
 *          <li>Method name is '''removeFrom[property]''' with one parameter of collectional parameter type. So, if property has type ''Collection[Type]'' then input parameter of type ''Type'' is expected.
 *              This mutator is called ''decrementor'' -- it leads to an incremental reduction of the collectional property. The same rules for method annotations apply as above.
 *          </li>
 *       </ul>
 *   </li>
 * </ul>
 * <p>
 * Please note that mutators for the same collectional property may have different validation annotations.
 * <p>
 * ==================================================================<br/>
 * <p>
 * <h3>Notable changes</h3>
 * Date: 2008-10-28
 * Introduced validation synchronisation mechanism.
 *
 * Date: 2009-02-09
 * Introduced property <code>initialising</code> to indicate an entity state where its properties are being initialised and thus no properties require any validation.
 *
 * Date: 2009-02-10 Implemented runtime validation of {@link KeyType} presence. If annotation is not present a runtime exception is thrown preventing entity instantiation.
 *
 * Date: 2009-06-10 Introduced property <code>version</code> to enable optimistic locking in Hibernate.
 *
 * Date: 2010-03-18 Added restore to original feature.
 *
 * Date: 2010-04-06 Implemented method copy, which can copy anything to anything.
 *
 * @param <K>
 *            Type of the business key (e.g. String, Integer, component), which must implement {@link Comparable}.
 *
 * @author TG Team
 */
public abstract class AbstractEntity<K extends Comparable> implements Comparable<AbstractEntity<K>> {

    public static final String ERR_IS_EDITABLE_UNINSTRUMENTED = "Uninstrumented instance is not suitable for editing.";
    public static final String ERR_ENSURE_INSTRUMENTED = "Meta-properties for this instance of entity [%s] do not exist as it was not instrumented.";
    public static final String ERR_COULD_NOT_GET_PROP_VALUE = "Could not get the value for property [%s] in instance %s.";
    public static final String PROP_TEMPLATE_FOR_MESSAGES = "[%s]@[%s]";
    public static final String ERR_COULD_NOT_SET_PROP_VALUE = "Error setting value [%s] into property [%s] for entity " + PROP_TEMPLATE_FOR_MESSAGES + ".";
    public static final String ERR_CANNOT_GET_VALUE_FOR_PROXIED_PROPERTY = "Cannot get value for proxied property [%s] of entity [%s].";

    protected final Logger logger;

    @MapTo("_ID")
    @Title(value = "Id", desc = "Surrogate unique identifier.")
    private Long id;

    @MapTo(value = "_VERSION", defaultValue = "0")
    private Long version = 0L;

    @IsProperty
    @UpperCase
    @MapTo("KEY_")
    @Required
    private K key;

    private final boolean compositeKey;

    @IsProperty
    @MapTo("DESC_")
    private String desc;

    /** Convenient constants to reference properties by name */
    public static final String ID = "id";
    public static final String VERSION = "version";
    public static final String KEY = "key";
    public static final String GETKEY = "getKey()";
    public static final String DESC = "desc";
    public static final String KEY_NOT_ASSIGNED = "[key is not assigned]";

    /**
     * A flag that provides a way to enforce more strict model verification, which is the default approach.
     */
    private static boolean STRICT_MODEL_VERIFICATION = true;
    public static boolean isStrictModelVerification() {
    	return STRICT_MODEL_VERIFICATION;
    }

    /**
     * Enforces the non-strict verification of the domain model.
     * This mode improves performance, but does not verify the domain model for self-consistency.
     * It is strongly recommended not to use this mode during application development.
     */
    public static void useNonStrictModelVerification() {
    	STRICT_MODEL_VERIFICATION = false;
    }

    /**
     * Enforces the strict verification of the domain model, which is the default approach.
     */
    public static void useStrictModelVerification() {
    	STRICT_MODEL_VERIFICATION = true;
    }

    @Inject
    private static DynamicPropertyAccess dynamicPropertyAccess;

    /**
     * Holds meta-properties for entity properties.
     */
    private final Map<String, MetaProperty<?>> properties = new LinkedHashMap<>();
    /**
     * Indicates if entity instance is being initialised.
     */
    private boolean initialising = false;

    /**
     * True indicates that the editable state of entity should be ignored during entity mutation
     * This property should be used with care. */
    private boolean ignoreEditableState = false;

    private final Class<K> keyType;
    /** Type of this entity with all non-structural enhancements removed. */
    private final Class<? extends AbstractEntity<?>> actualEntityType;
    /**
     * A reference to the application specific {@link EntityFactory} instance responsible for instantiation of this and other entities. It is also used for entity cloning.
     */
    private EntityFactory entityFactory;

    /**
     * Property factory is responsible for meta-property instantiation. The actual instantiation happens in the setter.
     */
    private java.util.Optional<IMetaPropertyFactory> metaPropertyFactory = empty();

    /**
     * Preferred property should be used by custom logic to set what property is from certain perspective is preferred.
     * The original requirement for this was due to custom logic driven determination as to what property should be focused by default on an entity master.
     * So, the place where in the application logic an entity was instantiated can determine which of its properties should be focused by default.
     */
    private String preferredProperty;

    /**
     * This is a default constructor, which is required for reflective construction.
     */
    @SuppressWarnings("unchecked")
    protected AbstractEntity() {
        actualEntityType = (Class<? extends AbstractEntity<?>>) stripIfNeeded(getClass());

        keyType = (Class<K>) EntityMetadata.keyTypeInfo(actualEntityType);

        if(!(this instanceof ActivatableAbstractEntity) && getType().isAnnotationPresent(DeactivatableDependencies.class)) {
            throw new EntityDefinitionException(format("Non-activatable entity [%s] cannot have deactivatable dependencies.", actualEntityType.getName()));
        }


        logger = getLogger(this.getType());

        compositeKey = DynamicEntityKey.class.equals(keyType);
        if (compositeKey) {
            setKey((K) new DynamicEntityKey((AbstractEntity<DynamicEntityKey>) this));
        }
    }

    /**
     * A convenient method for obtaining the actual type that was used to potentially derive the type of the current instance.
     * For example, generated types are always derived from some prototype entity type.
     * @return
     */
    @SuppressWarnings("unchecked")
	public Class<? extends AbstractEntity<?>> getDerivedFromType() {
        Class<? extends AbstractEntity<?>> tmpDerivedFromType = getType();
        final String name = getType().getName();
        final int index = name.indexOf("$$");
        if (index > 0) {
        	final String cleanName = name.substring(0, index);
        	try {
				tmpDerivedFromType = (Class<? extends AbstractEntity<?>>) Class.forName(cleanName);
			} catch (final ClassNotFoundException e) {
				e.printStackTrace();
			}
        }
        return tmpDerivedFromType;
	}

	/**
     * The main entity constructor.
     *
     * @param id
     * @param key
     * @param desc
     */
    protected AbstractEntity(final Long id, final K key, final String desc) {
        this();
        this.id = id;
        this.key = key;
        this.desc = desc;
    }

    public Long getId() {
        return id;
    }

    /**
     * This setter is protected so that descendants could provide additional functionality if required (e.g. validation).
     *
     * @param id
     */
    protected void setId(final Long id) {
        this.id = id;
    }

    public String getDesc() {
        if (AbstractEntity.class.isAssignableFrom(getKeyType())) {
            return getKey() != null ? AbstractEntity.class.cast(getKey()).getDesc() : "";
        }
        return desc;
    }

    @Observable
    protected <ET extends AbstractEntity<K>> ET setDesc(final String desc) {
        this.desc = desc;
        return (ET) this;
    }

    public K getKey() {
        return key;
    }

    @Observable
    public AbstractEntity<K> setKey(final K key) {
        this.key = key;
        return this;
    }

    /**
     * Hashing is based on the business key implementation.
     */
    @Override
    public int hashCode() {
        return (getKey() != null ? getKey().hashCode() : 0) * 23;
    }

    /**
     * Equality is based on the business key implementation.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractEntity<?> that)) {
            return false;
        }

        // Let's ensure that types match.
        if (this.getType() != that.getType()) {
            return false;
        }

        // If both or one of instances is an id-only proxy while the other is not mutated,
        // then compare them by `id` values.
        // N.B.: Only instances of persistent entities can be id-only proxies.
        if ((that.isIdOnlyProxy() || this.isIdOnlyProxy())
            && (!that.isInstrumented() || !that.isDirty())
            && (!this.isInstrumented() || !this.isDirty()))
        {
            return that.getId().equals(this.getId());
        }

        // Otherwise, compare instances by their key values.
        return Objects.equals(this.getKey(), that.getKey());
    }

    @Override
    public String toString() {
        if (isIdOnlyProxy()) {
            return format("ID = %s", getId());
        }
        return getKey() != null ? getKey().toString() : KEY_NOT_ASSIGNED;
    }

    /**
     * Implements comparison based on the business key implementation.
     */
    @Override
    public final int compareTo(final AbstractEntity<K> cmpTo) {
        return getKey().compareTo(cmpTo.getKey());
    }

    /**
     * A convenient method for obtaining entity key type.
     *
     * @return
     */
    public final Class<K> getKeyType() {
        return keyType;
    }

    /**
     * Returns <code>true</code> if entity has a composite key.
     *
     * @return
     */
    public boolean isComposite() {
        return DynamicEntityKey.class.isAssignableFrom(getKeyType());
    }

    /**
     * Determines whether class was enhanced in order to get the correct entity class.
     *
     * @return
     */
    public final Class<? extends AbstractEntity<?>> getType() {
        return actualEntityType;
    }

    /**
     * Dynamic getter for accessing property value.
     *
     * @param propertyName
     * @return
     */
    public <T> T get(final String propertyName) {
        if (Reflector.isPropertyProxied(this, propertyName)) {
            throw new StrictProxyException(ERR_CANNOT_GET_VALUE_FOR_PROXIED_PROPERTY.formatted(propertyName, getType().getName()));
        }
        try {
            return (T) dynamicPropertyAccess.getProperty(this, propertyName);
        }
        catch (final StrictProxyException ex) {
            throw new StrictProxyException(ERR_CANNOT_GET_VALUE_FOR_PROXIED_PROPERTY.formatted(propertyName, getType().getName()), ex);
        }
        catch (final Throwable ex) {
            // There are cases where this.toString() may fail such as for non-initialized union entities.
            // Need to degrade gracefully to hide the original exception.
            // Also, do not try toString() if dynamic property access fails critically.
            // This is because toString() itself may require access to that property (e.g., in case of DynamicEntityKey).
            @Nullable String thisToString;
            if (ex instanceof DynamicPropertyAccessCriticalError) {
                thisToString = null;
            }
            else {
                try {
                    thisToString = this.toString();
                } catch (final Throwable _$) {
                    thisToString = null;
                }
            }
            throw new EntityException(ERR_COULD_NOT_GET_PROP_VALUE.formatted(propertyName, thisToString == null ? '[' + getType().getTypeName() + ']' : PROP_TEMPLATE_FOR_MESSAGES.formatted(thisToString, getType().getTypeName())),
                                      ex);
        }
    }

    /**
     * Dynamic getter for accessing property value.
     *
     * @param propertyName
     * @return
     */
    public <T> T get(final IConvertableToPath propertyName) {
        return get(propertyName.toPath());
    }

    /**
     * Dynamic setter for setting property value.
     *
     * @param propertyName
     * @param value
     */
    public AbstractEntity<K> set(final String propertyName, final Object value) {
        try {
            dynamicPropertyAccess.setProperty(this, propertyName, value);
            return this;
        } catch (final Throwable ex) {
            if (ex instanceof Result result) {
                throw result;
            }
            else {
                throw new EntityException(ERR_COULD_NOT_SET_PROP_VALUE.formatted(value, propertyName, this, getType().getName()), ex);
            }
        }
    }

    /**
     * Dynamic setter for setting property value.
     *
     * @param propertyName
     * @param value
     */
    public AbstractEntity<K> set(final IConvertableToPath propertyName, final Object value) {
        return set(propertyName.toPath(), value);
    }

    /**
     * This setter is responsible for meta-property creation. It is envisaged that {@link IMetaPropertyFactory} is be provided as an injection. An thus, meta-property instantiation
     * should happen immediately after entity creation when being created via IoC mechanism.
     *
     * @param metaPropertyFactory
     */
    @Inject
    protected void setMetaPropertyFactory(final IMetaPropertyFactory metaPropertyFactory) {
        // mark the start of the initialisation phase as part of entity creation
        beginInitialising();
        // if meta-property factory has already been assigned it should not change
        if (this.metaPropertyFactory.isPresent()) {
            logger.error("Property factory can be assigned only once.");
            throw new EntityException("Property factory can be assigned only once.");
        }
        this.metaPropertyFactory = of(metaPropertyFactory);

        final List<Field> keyMembers = Finder.getKeyMembers(getType());
        final Set<Field> fieldsForProperties = fieldsForProperties();
        final boolean isEntityPersistent = isPersistent();
        final boolean shouldNotSkipKeyChangeValidation = !isAnnotationPresentForClass(SkipKeyChangeValidation.class, this.getClass());
        for (final Field field : fieldsForProperties) { // for each field that represents a property
            final String propName = field.getName();

            if (STRICT_MODEL_VERIFICATION) {
                // TODO this kind of validation should really be implemented as part of the compilation process
                // ensure that there is an accessor -- with out it field is not a property
                // throws exception if method does not exists
                Reflector.obtainPropertyAccessor(getType(), propName); // computationally heavy
            }
            // determine property type and adjacent virtues
            final Class<?> type = EntityMetadata.determinePropType(getType(), field);
            final boolean isKey = keyMembers.contains(field);

            if (Reflector.isPropertyProxied(this, propName)) {
                properties.put(propName, new MetaProperty(this, field, type, isKey, true, extractDependentProperties(field, fieldsForProperties)));
            } else {
                try {
                    final boolean isCollectional = Collection.class.isAssignableFrom(type);

                    final IsProperty isPropertyAnnotation = AnnotationReflector.getAnnotation(field, IsProperty.class);
                    final Class<?> propertyAnnotationType = isPropertyAnnotation.value();

                    // perform some early runtime validation whether property was defined correctly
                    if (STRICT_MODEL_VERIFICATION) {
                        // TODO this kind of validation should really be implemented as part of the compilation process
                        earlyRuntimePropertyDefinitionValidation(propName, type, isCollectional, isPropertyAnnotation, propertyAnnotationType); // computationally heavy
                    }

                    // if a setter is annotated, then try to instantiate the specified validator.
                    final var annotationsAndValidators = collectValidators(metaPropertyFactory, field, type, isCollectional, isEntityPersistent, shouldNotSkipKeyChangeValidation);
                    final var declaredValidationAnnotations = annotationsAndValidators._1;
                    final var validators = annotationsAndValidators._2;
                    // create ACE handler
                    final IAfterChangeEventHandler<?> definer = metaPropertyFactory.create(this, field);
                    // create meta-property
                    final boolean isUpperCase = AnnotationReflector.isAnnotationPresent(field, UpperCase.class);
                    final MetaProperty<?> metaProperty = new MetaPropertyFull(
                            this,
                            field,
                            type,
                            false,
                            isKey,
                            isCollectional,
                            isPropertyAnnotation.assignBeforeSave(),
                            propertyAnnotationType,
                            AnnotationReflector.isAnnotationPresent(field, Calculated.class),
                            isUpperCase,
                            declaredValidationAnnotations,
                            validators,
                            definer,
                            extractDependentProperties(field, fieldsForProperties));
                    // define meta-property properties used most commonly for UI construction: required, editable, title and desc //
                    initProperty(keyMembers, field, metaProperty);
                    // put meta-property in the map associating it with a corresponding property name
                    properties.put(propName, metaProperty);
                } catch (final Exception e) {
                    logger.error("Entity instantiation failed.", e);
                    throw new EntityException(format("Instantiation of entity [%s] has failed (see cause for more details).", getType().getName()), e);
                }
            }
        }
        endInitialising();
    }

    /**
     * Returns a set of fields that represent "real" properties, but also "key" and "desc", which should have meta-properties constructed for them irrespectively of whether they belong to "real" properties.
     * Refer to issue <a href='https://github.com/fieldenms/tg/issues/1729'>1729</a> for more details.
     *
     * @return
     */
    private Set<Field> fieldsForProperties() {
        final Class<? extends AbstractEntity<?>> thisType = (Class<? extends AbstractEntity<?>>) getClass();
        final Set<Field> fields = Finder.streamRealProperties(thisType).collect(toCollection(LinkedHashSet::new));
        try {
            ALWAYS_PRESENT_META_PROPERTIES.forEach(prop -> fields.add(Finder.getFieldByName(thisType, prop)));
        } catch (final Exception ex) {
            final String error = "Could not get field for one of [%s].".formatted(CollectionUtil.toString(ALWAYS_PRESENT_META_PROPERTIES, ", "));
            logger.error(error, ex);
            throw new ReflectionException(error, ex);
        }
        return fields;
    }

    private static final Set<String> ALWAYS_PRESENT_META_PROPERTIES = ImmutableSet.of(KEY, DESC);

    /**
     * Indicates whether a property is such that a {@link MetaProperty} always exists for it, regardless of the type that
     * declares the property.
     */
    public static boolean isAlwaysMetaProperty(final String property) {
        return ALWAYS_PRESENT_META_PROPERTIES.contains(property);
    }

    /**
     * Early runtime validation of property definitions. This kind of validations should be moved to complile time in due course.
     *
     * @param propName
     * @param type
     * @param isCollectional
     * @param isPropertyAnnotation
     * @param propertyAnnotationType
     */
    private void earlyRuntimePropertyDefinitionValidation(final String propName, final Class<?> type, final boolean isCollectional, final IsProperty isPropertyAnnotation, final Class<?> propertyAnnotationType) {
        final boolean isNumeric = isNumeric(type);

        if (!isNumeric &&
            (isPropertyAnnotation.precision() != DEFAULT_PRECISION ||
             isPropertyAnnotation.scale() != DEFAULT_SCALE ||
             isPropertyAnnotation.trailingZeros() != DEFAULT_TRAILING_ZEROS)) {
            final String error = format(INVALID_USE_OF_NUMERIC_PARAMS_MSG,  propName, getType().getName());
            logger.error(error);
            throw new EntityDefinitionException(error);

        }

        if (isNumeric &&
            (isPropertyAnnotation.precision() != DEFAULT_PRECISION || isPropertyAnnotation.scale() != DEFAULT_SCALE) &&
            (isPropertyAnnotation.precision() <= 0 || isPropertyAnnotation.scale() < 0)) {
            final String error = format(INVALID_USE_FOR_PRECISION_AND_SCALE_MSG, propName, getType().getName());
            logger.error(error);
            throw new EntityDefinitionException(error);
        }

        if (isNumeric && isPropertyAnnotation.precision() != DEFAULT_PRECISION && isPropertyAnnotation.precision() <= isPropertyAnnotation.scale()) {
                final String error = format(INVALID_VALUES_FOR_PRECISION_AND_SCALE_MSG, propName, getType().getName());
                logger.error(error);
                throw new EntityDefinitionException(error);

        }

        if (!isString(type) && !isHyperlink(type) && !RichText.class.isAssignableFrom(type) && !type.isArray() && isPropertyAnnotation.length() != DEFAULT_LENGTH) {
            final String error = format(INVALID_USE_OF_PARAM_LENGTH_MSG, propName, getType().getName());
            logger.error(error);
            throw new EntityDefinitionException(error);
        }

        if ((isCollectional || PropertyDescriptor.class.isAssignableFrom(type)) && (propertyAnnotationType == Void.class || propertyAnnotationType == null)) {
            final String error = format(COLLECTIONAL_PROP_MISSING_TYPE_MSG, propName, getType().getName());
            logger.error(error);
            throw new EntityDefinitionException(error);
        }

        final Class<? extends AbstractEntity<?>> entityType = getType();
        if (isCollectional && isLinkPropertyRequiredButMissing(propName)) {
            final String error = format(COLLECTIONAL_PROP_MISSING_LINK_MSG, propName, getType().getName());
            logger.error(error);
            throw new EntityDefinitionException(error);
        }

        // FIXME there are cases where entities inherit from an entity with implicitly-calculated one-2-one associations, which fail the association check
        // Finder.isOne2One_association uses "equals" to validate the key of the one-2-one- entity matching the holding entity type.
        // This needs to be considered and resolved.
        // if (EntityUtils.isEntityType(type) && EntityUtils.isEntityType(PropertyTypeDeterminator.determinePropertyType(type, KEY))
        //        && !Finder.isOne2One_association(entityType, propName)) {
        //    final String error = format(INVALID_ONE2ONE_ASSOCIATION_MSG, propName, getType().getName());
        //    logger.error(error);
        //    throw new EntityDefinitionException(error);
        //}
    }

    /**
     * A predicate method to identify whether a collectional property requires, but is missing a corresponding <code>link property</code> information.
     *
     * @param propertyName
     * @return
     */
    protected boolean isLinkPropertyRequiredButMissing(final String propertyName) {
        return isPersistentEntityType(getType()) && !Finder.hasLinkProperty(getType(), propertyName);
    }

    /**
     * Analyses property definition to collect and instantiate all property validators.
     * Among the returned annotations and validators are both explicit (i.e. directly present on a property) and implicit ones.
     * An example of implicit validation is the standard validation of {@code String}-typed property {@code key} (see {@link SkipDefaultStringKeyMemberValidation}).
     *
     * @return  a pair of validation annotations that apply to the property and a map {@code {ValidationAnnotation : {validator : result}}}
     */
    private T2<Set<Annotation>, Map<ValidationAnnotation, Map<IBeforeChangeEventHandler<?>, Result>>> collectValidators(
            final IMetaPropertyFactory metaPropertyFactory,
            final Field propField,
            final Class<?> propType,
            final boolean isCollectional,
            final boolean isEntityPersistent,
            final boolean shouldNotSkipKeyChangeValidation)
            throws Exception
    {
        try {
            final var annotations = mergeAnnotations(collectValidationAnnotations(propField, propType, isCollectional, isEntityPersistent, shouldNotSkipKeyChangeValidation));

            final var validators = new EnumMap<ValidationAnnotation, Map<IBeforeChangeEventHandler<?>, Result>>(ValidationAnnotation.class);
            for (final var entry : annotations.entrySet()) {
                final var type = entry.getKey();
                final var annotation = entry.getValue();
                final IBeforeChangeEventHandler<?>[] handlers = metaPropertyFactory.create(annotation, this, propField.getName(), propType);
                if (handlers.length > 0) {
                    final var handlersAndResults = new LinkedHashMap<IBeforeChangeEventHandler<?>, Result>();
                    for (final var handler : handlers) {
                        handlersAndResults.put(handler, null);
                    }
                    validators.put(ValidationAnnotation.getValueByType(type), handlersAndResults);
                }
            }

            return t2(ImmutableSet.copyOf(annotations.values()), validators);
        } catch (final Exception ex) {
            logger.error(() -> "Exception during collection of validators for property [%s] in entity type [%s].".formatted(propField.getName(), getType().getSimpleName()), ex);
            throw ex;
        }
    }

    /**
     * For each unique annotation type in a set, merges all annotations of that type into one.
     * If an annotation type that is not mergeable is encountered, this method fails.
     */
    private Map<Class<? extends Annotation>, Annotation> mergeAnnotations(final Set<Annotation> annotations) {
        return annotations.stream().collect(groupingBy(Annotation::annotationType, collectingAndThen(toList(), AbstractEntity::mergeAnnotations_)));
    }

    /**
     * @param annotations  a non-empty sequence
     */
    private static <A extends Annotation> A mergeAnnotations_(final SequencedCollection<? extends A> annotations) {
        return switch (annotations.size()) {
            case 0 -> throw new IllegalArgumentException("Expected a non-empty sequence.");
            case 1 -> annotations.getFirst();
            default -> switch (annotations.getFirst()) {
                case BeforeChange $ -> (A) BeforeChangeAnnotation.merge((SequencedCollection<BeforeChange>) annotations);
                // provide more branches if necessary
                default -> throw new EntityDefinitionException(
                        "Non-repeatable annotation [%s] cannot be applied more than once."
                                .formatted(annotations.getFirst().annotationType().getTypeName()));
            };
        };
    }

    private SequencedSet<Annotation> collectValidationAnnotations(
            final Field propField, final Class<?> propType,
            final boolean isCollectional,
            final boolean isEntityPersistent,
            final boolean shouldNotSkipKeyChangeValidation)
    {
        // order matters
        final var annotations = new LinkedHashSet<Annotation>();

        // implicit key validators are applied before any declared (via @BeforeChange) ones
        annotations.addAll(collectValidationAnnotationsForKey(propField, isEntityPersistent, shouldNotSkipKeyChangeValidation));
        annotations.addAll(findValidationAnnotationsForProperty(propField, propType));

        makeEntityExistsAnnotationIfApplicable(propField, propType).ifPresent(annotations::add);

        // Exclude handlers that should not be defined explicitly.
        // This is necessary to ensure the correct order of default validators.
        // Default validators are placed before explicit ones, and the order of default validators is important.
        // SanitiseHtmlValidator goes after MaxLengthValidator.
        // DefaultValidatorForValueTypeWithValidation should be before MaxLengthValidator, which requires instantiation of RichText.searchText.
        // Note that the code below prepends validators via addFirst, so the execution order is the reverse of the logical order.

        final List<Handler> bceHandlers = annotations.stream()
                                          .mapMulti(typeFilter(BeforeChange.class))
                                          // filter out the default validators that could have been assigned explicitly by mistake
                                          .flatMap(bce -> Stream.of(bce.value())
                                                  .filter(handler -> handler.value() != DefaultValidatorForValueTypeWithValidation.class)
                                                  .filter(handler -> handler.value() != SanitiseHtmlValidator.class))
                                          .collect(toCollection(ArrayList::new));

        // Should SanitiseHtmlValidator be added?
        if (propType == String.class && !propField.isAnnotationPresent(Calculated.class)) {
            bceHandlers.addFirst(new HandlerAnnotation(SanitiseHtmlValidator.class).newInstance());
        }
        // Should MaxLengthValidator be added?
        if (MaxLengthValidator.SUPPORTED_TYPES.contains(propType) &&
            !propField.isAnnotationPresent(Calculated.class) &&
            propField.getAnnotation(IsProperty.class).length() > 0)
        {
            final var maybeMaxLengthValidator = bceHandlers.stream().filter(handler -> handler.value() == MaxLengthValidator.class).findFirst();
            // If MaxLengthValidator is defined explicitly, we need to ensure that it the first validator.
            if (maybeMaxLengthValidator.isPresent()) {
                final var handler = maybeMaxLengthValidator.get();
                bceHandlers.remove(handler);
                bceHandlers.addFirst(handler);
            }
            // Otherwise, register a new instance.
            else {
                bceHandlers.addFirst(new HandlerAnnotation(MaxLengthValidator.class).newInstance());
            }
        }
        // Should DefaultValidatorForValueTypeWithValidation be added?
        if (IWithValidation.class.isAssignableFrom(propType)) {
            bceHandlers.addFirst(new HandlerAnnotation(DefaultValidatorForValueTypeWithValidation.class).newInstance());
        }

        // If there are any BCE handlers, need to add/replace the BeforeChangeAnnotation instance.
        if (!bceHandlers.isEmpty()) {
            final var newBce = BeforeChangeAnnotation.newInstance(bceHandlers.toArray(new Handler[]{}));
            annotations.removeIf(at -> at instanceof BeforeChange);
            annotations.add(newBce);
        }

        return unmodifiableSequencedSet(annotations);
    }

    private Set<Annotation> collectValidationAnnotationsForKey(
            final Field propField,
            final boolean isEntityPersistent,
            final boolean shouldNotSkipKeyChangeValidation)
    {
        if (isKeyOrKeyMember(propField)) {
            final var annotations = ImmutableSet.<Annotation>builder();

            // special validation of String-typed key or String-typed key-members
            if (String.class.equals(propField.getType()) || String.class.equals(this.getKeyType())) {
                final var skipAnnot = propField.getAnnotation(SkipDefaultStringKeyMemberValidation.class);
                final var handlers = StreamUtils.removeAll(Arrays.stream(ALL_DEFAULT_STRING_KEY_VALIDATORS),
                                                           skipAnnot == null ? List.of() : Arrays.asList(skipAnnot.value()))
                        .map(bce -> new HandlerAnnotation(bce).newInstance())
                        .toArray(Handler[]::new);
                if (handlers.length > 0) {
                    annotations.add(BeforeChangeAnnotation.newInstance(handlers));
                }
            }

            if (isEntityPersistent && shouldNotSkipKeyChangeValidation) {
                annotations.add(BeforeChangeAnnotation.newInstance(new HandlerAnnotation(KeyMemberChangeValidator.class).newInstance()));
            }

            return annotations.build();
        }
        else {
            return ImmutableSet.of();
        }
    }

    private Optional<Annotation> makeEntityExistsAnnotationIfApplicable(final Field propField, final Class<?> propType) {
        return isEntityExistsValidationApplicable(getType(), propField)
            ? of(entityExistsAnnotation(getType(), propField.getName(), (Class<? extends AbstractEntity<?>>) propType))
            : empty();
    }

    /**
     * Initialises meta-property properties used most commonly for UI construction such as required, editable, title and desc.
     *
     * @param keyMembers
     * @param field
     * @param metaProperty
     */
    private void initProperty(final List<Field> keyMembers, final Field field, final MetaProperty<?> metaProperty) {
        if (KEY.equals(field.getName())) {
            metaProperty.setVisible(!(KEY.equals(field.getName()) && keyMembers.size() > 1)); // if entity is composite then "key" should be inactive
            metaProperty.setEditable(!isAnnotationPresentForClass(KeyReadonly.class, getType()));
            metaProperty.setRequired(true);
            if (isAnnotationPresentForClass(KeyTitle.class, getType())) {
                final KeyTitle title = AnnotationReflector.getAnnotation(getType(), KeyTitle.class);
                metaProperty.setTitle(title.value());
                metaProperty.setDesc(StringUtils.isEmpty(title.desc()) ? title.value() : title.desc());
            }
        } else if (DESC.equals(field.getName())) {
            metaProperty.setEditable(!isAnnotationPresentForClass(DescReadonly.class, getType()));
            metaProperty.setRequired(isAnnotationPresentForClass(DescRequired.class, getType()));
            if (isAnnotationPresentForClass(DescTitle.class, getType())) {
                final DescTitle title = AnnotationReflector.getAnnotation(getType(), DescTitle.class);
                metaProperty.setTitle(title.value());
                metaProperty.setDesc(StringUtils.isEmpty(title.desc()) ? title.value() : title.desc());
            }
        } else {
            metaProperty.setVisible(!AnnotationReflector.isAnnotationPresent(field, Invisible.class)
                    || (AnnotationReflector.isAnnotationPresent(field, Invisible.class) && AnnotationReflector.getAnnotation(field, Invisible.class).centreOnly()));
            metaProperty.setEditable(!AnnotationReflector.isAnnotationPresent(field, Readonly.class));

            // TODO may need to relax this condition for composite key member in order to support empty composite members
            // As part of issue #28 need to relax requiredness for composite key members in case they have transactional nature
            if (metaProperty.shouldAssignBeforeSave()) { // this should really be strictly for not yet persisted entities!
                metaProperty.setRequired(false);
            } else {
                metaProperty.setRequired(
                        AnnotationReflector.isAnnotationPresent(field, Required.class) ||
                        (AnnotationReflector.isAnnotationPresent(field, CompositeKeyMember.class) &&
                        !AnnotationReflector.isAnnotationPresent(field, ua.com.fielden.platform.entity.annotation.Optional.class)));
            }

            if (AnnotationReflector.isAnnotationPresent(field, Title.class)) {
                final Title title = AnnotationReflector.getAnnotation(field, Title.class);
                metaProperty.setTitle(title.value());
                metaProperty.setDesc(StringUtils.isEmpty(title.desc()) ? title.value() : title.desc());
            }
        }
    }

    /**
     * Extracts and validates the array of dependent propertyNames related to the 'field' in the case if it is annotated with @Dependent. Throws RuntimeException if there is no
     * property with the specified propertyNames, or if the self-dependence was specified.
     *
     * @param field
     * @param allFields
     * @return
     */
    private String[] extractDependentProperties(final Field field, final Set<Field> allFields) {
        if (AnnotationReflector.isAnnotationPresent(field, Dependent.class)) {
            final List<String> allFieldsNames = new ArrayList<>();
            for (final Field f : allFields) {
                allFieldsNames.add(f.getName());
            }
            final String[] dependentPropertyNames = AnnotationReflector.getAnnotation(field, Dependent.class).value();
            for (final String propName : dependentPropertyNames) {
                if (!allFieldsNames.contains(propName)) {
                    throw new EntityDefinitionException(format("There is no dependent property [%s] in entity [%s].", propName, getType().getName()));
                } else if (propName.equals(field.getName())) {
                    throw new EntityDefinitionException(format("Self-dependency is discovered for property [%s] in entity [%s].", propName, getType().getName()));
                }
            }
            return dependentPropertyNames;
        }
        return null;
    }

    private static final SequencedSet<Class<? extends Annotation>> fieldValidationAnnotationTypes = linkedSetOf(
            ValidationAnnotation.UNIQUE.getType(),
            ValidationAnnotation.FINAL.getType(),
            ValidationAnnotation.LE_PROPETY.getType(),
            ValidationAnnotation.GE_PROPETY.getType(),
            ValidationAnnotation.BEFORE_CHANGE.getType());
    /**
     * Finds all explicitly declared validation annotations for a property and its mutators.
     */
    public Set<Annotation> findValidationAnnotationsForProperty(final Field field, final Class<?> type) {
        final var annotations = ImmutableSet.<Annotation>builder();
        annotations.addAll(extractSetterAnnotations(field, type));
        AnnotationReflector.getFieldAnnotations(field).values().stream().filter(annotation -> fieldValidationAnnotationTypes.contains(annotation.annotationType())).forEach(annotations::add);
        return annotations.build();
    }

    /**
     * Attempts to obtain field setter and extract its annotations for further processing. If there is no setter defined then an empty set of annotations is returned.
     *
     * @param field
     * @param type
     * @return
     */
    private Set<Annotation> extractSetterAnnotations(final Field field, final Class<?> type) {
        //logger.debug("Extracting validation annotations for property " + field.getName() + ".");
        try {
            final Method setter = Reflector.getMethod(this, Mutator.SETTER.getName(field.getName()), type);
            if (AnnotationReflector.getAnnotation(setter, Observable.class) == null) {
                final String errorMsg = format("Setter [%s] for property [%s] in entity [%s] is not observable (missing @Observable).", setter.getName(), field.getName(), getType().getName());
                logger.error(errorMsg);
                throw new EntityDefinitionException(errorMsg);
            }
            final Set<Annotation> annotations = AnnotationReflector.getValidationAnnotations(setter);
            //logger.debug("Number of validation annotations for property " + field.getName() + ": " + annotations.size());
            return annotations;
        } catch (final NoSuchMethodException e) {
            // do nothing if setter does not exist
            logger.debug(format("There is no setter for property [%s] in entity [%s].", field.getName(), getType().getName()));
        }
        return ImmutableSet.of();
    }

    /**
     * Returns unmodifiable map of properties.
     *
     * @return
     */
    public final Map<String, MetaProperty<?>> getProperties() {
        assertInstrumented();
        return unmodifiableMap(properties);
    }

    /**
     * Guarantees to return an instance of {@link MetaProperty} for the specified property name if it exists.
     * Otherwise, throws an exception.
     *
     * @param name
     * @return
     */
    public final <T> MetaProperty<T> getProperty(final String name) {
        final MetaProperty<T> mp = (MetaProperty<T>) getProperties().get(name);
        if (mp != null) {
            return mp;
        }

        throw new EntityException(format("Meta-data for property [%s] in entity [%s] could not be located.", name, getType().getName()));
    }

    /**
     * Guarantees to return an instance of {@link MetaProperty} for the specified property name if it exists.
     * Otherwise, throws an exception.
     *
     * @param name
     * @return
     */
    public final <T> MetaProperty<T> getProperty(final IConvertableToPath name) {
        return getProperty(name.toPath());
    }

    /**
     * Returns an empty optional if the specified name represents a proxied property.
     * Throws {@link EntityException} in case of uninstrumeted entity.
     *
     * @param name
     * @return
     */
    public final <T> java.util.Optional<MetaProperty<T>> getPropertyIfNotProxy(final String name) {
        final MetaProperty<T> prop = getProperty(name);
        return prop.isProxy() ? empty() : of(prop);
    }

    /**
     * Returns an empty optional if the specified name represents a proxied property.
     * Throws {@link EntityException} in case of uninstrumeted entity.
     *
     * @param name
     * @return
     */
    public final <T> java.util.Optional<MetaProperty<T>> getPropertyIfNotProxy(final IConvertableToPath name) {
        return getPropertyIfNotProxy(name.toPath());
    }

    /**
     * A convenient alternative to {@link #getProperty(String)} that returns an optional value with either an instance of {@link MetaProperty} or without.
     * An empty optional value indicates that either this entity instance was not instrumented or the specified property does not belong to this entity.
     *
     * @param name
     * @return
     */
    public final java.util.Optional<MetaProperty<?>> getPropertyOptionally(final String name) {
        if (metaPropertyFactory.isPresent()) {
            final MetaProperty<?> mp = getProperties().get(name);
            return mp != null ? of(mp) : empty();
        }
        return empty();
    }

    /**
     * A convenient alternative to {@link #getProperty(String)} that returns an optional value with either an instance of {@link MetaProperty} or without.
     * An empty optional value indicates that either this entity instance was not instrumented or the specified property does not belong to this entity.
     *
     * @param name
     * @return
     */
    public final java.util.Optional<MetaProperty<?>> getPropertyOptionally(final IConvertableToPath name) {
        return getPropertyOptionally(name.toPath());
    }

    /**
     * Throws {@link EntityException} if this instance is not instrumented.
     */
    public final void assertInstrumented() {
        if (!isInstrumented()) {
            throw new EntityException(format(ERR_ENSURE_INSTRUMENTED, getType().getName()));
        }
    }

    /**
     * A convenient method to check if this instance is instrumented.
     *
     * @return
     */
    public final boolean isInstrumented() {
        return PropertyTypeDeterminator.isInstrumented(this.getClass());
    }

    /**
     * This method should be used to check whether entity is valid.
     * <p>
     * Supports locking mechanism to ensure that property validation finishes before checking its results.
     *
     * @return
     */
    public final Result isValid() {
        // invoke validation logic
        return validate();
    }

    /**
     * {@link AbstractEntity} has a default way of validating, which has a support for descendants to override it. However, sometimes it is required to validate an entity ad-hoc
     * from some specific perspective.
     * <p>
     * This method performs entity validation based on the provided custom validator.
     *
     * @param validator
     * @return
     */
    public final Result isValid(final ICustomValidator validator) {
            // invoke custom validation logic
            return validator.validate(this);
    }

    /**
     * This method is provided to implement entity specific validation.
     * <p>
     * The default implementation checks fields declared as property (see {@link IsProperty}) for validity. It is envisaged that descendants will invoke super implementation and
     * provide some specific logic.
     * <p>
     *
     * @return validation result
     */
    protected Result validate() {
        return validateWithCritOnly.validate(this);
    }

    /**
     * Returns either empty or a list of warnings associated with entity's non-proxied properties.
     *
     * @return
     */
    public final List<Warning> warnings() {
        if (!isInstrumented()) {
            throw new EntityException(format("Uninstrumented entity [%s] should not be checked for warnings.", getType().getName()));
        }

        // collect all warnings as the result
        return nonProxiedProperties()
               .filter(MetaProperty::hasWarnings)
               .map(MetaProperty::getFirstWarning)
               .collect(toList());
    }

    public boolean hasWarnings() {
        if (!isInstrumented()) {
            throw new EntityException(format("Uninstrumented entity [%s] should not be checked for warnings.", getType().getName()));
        }
        // identify if there are any warnings
        return nonProxiedProperties().anyMatch(MetaProperty::hasWarnings);
    }

    /**
     * A convenient getter to obtain an entity factory.
     *
     * @return {@link EntityFactory} which created this {@link AbstractEntity}
     */
    public final EntityFactory getEntityFactory() {
        return entityFactory;
    }

    /**
     * Sets reference to new {@link EntityFactory} instance. However this method should be called only once (probably in {@link EntityFactory}), because during runtime there is
     * only one {@link EntityFactory} instance
     *
     * @param entityFactory
     */
    public final void setEntityFactory(final EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    /**
     * A convenience method for determining whether this entity has been persisted.
     *
     * @return true if this entity is persisted in the database (i.e. id is not null), false otherwise
     */
    public final boolean isPersisted() {
        return getId() != null;
    }

    public final boolean isInitialising() {
        return initialising;
    }

    public final AbstractEntity<K> beginInitialising() {
        this.initialising = true;
        return this;
    }

    public final AbstractEntity<K> endInitialising() {
        this.initialising = false;
        return this;
    }

    public final boolean hasCompositeKey() {
        return compositeKey;
    }

    /**
     * Any change to a property on an instrumented entity instance leads to its dirtiness.
     * Once a dirty entity is persisted, the dirty state gets reset to <code>false</code>.
     *
     * The dirty state is applicable strictly to instrumented entities.
     * Therefore, an exception is thrown if this method is invoked on an uninstrumented instance.
     *
     * @return
     */
    public final boolean isDirty() {
        assertInstrumented();
        return !isPersisted() ||
                nonProxiedProperties().anyMatch(MetaProperty::isDirty);
    }

    public final AbstractEntity<K> setDirty(final boolean dirty) {
        // reset dirty state for properties in case where entity becomes not dirty
        if (!dirty) {
            getDirtyProperties().forEach(prop -> prop.setDirty(false));
        }
        return this;
    }

    /**
     * A convenient method to obtain only meta-properties representing non-proxied properties.
     *
     * @return
     */
    public Stream<MetaProperty<?>> nonProxiedProperties() {
        return getProperties().values().stream().filter(mp -> !mp.isProxy());
    }

    /**
     * A utility method for accessing dirty properties.
     *
     * @return
     */
    public final List<MetaProperty<?>> getDirtyProperties() {
        return nonProxiedProperties().filter(mp -> !mp.isCalculated() && mp.isDirty()).collect(toList());
    }

    public AbstractEntity<?> resetMetaState() {
        nonProxiedProperties().forEach(MetaProperty::resetState);
        return this;
    }

    public AbstractEntity<?> resetMetaValue() {
        nonProxiedProperties().forEach(MetaProperty::resetValues);
        return this;
    }

    /**
     * Indicates whether this entity instance can be changed.
     * By default, if an entity instance is instrumented, returns a successful {@link Result} indicating editability of the entity.
     * Otherwise, returns failure due to the fact that uninstrumented entities should not be modified.
     * <p>
     * This method should be overridden if some custom logic needs to be provided.
     * The default result (i.e. super call) should be honored by overridden methods.
     * <p>
     * For example, some entity instance should not be changed when its certain property has some specific value.
     * But if the default result is a failure then it should be return as the result of the overridden method.
     *
     * @return
     */
    public Result isEditable() {
        return isInstrumented() ? successful(this) : failure(ERR_IS_EDITABLE_UNINSTRUMENTED);
    }

    protected void setVersion(final Long ver) {
        version = ver;
    }

    public Long getVersion() {
        return version;
    }

    public Class<?> getPropertyType(final String propertyName) {
        return getProperty(propertyName).getType();
    }

    public Class<?> getPropertyType(final IConvertableToPath propertyName) {
        return getPropertyType(propertyName.toPath());
    }

    /**
     * Restores state of all properties to original, which includes setting original values and removal of all validation errors.
     */
    public AbstractEntity<K> restoreToOriginal() {
        beginInitialising();
        try {
            // restore property value state to original
            nonProxiedProperties().forEach(MetaProperty::restoreToOriginal);
            // run definers to restore meta-state that could have been set as part of some property after change logic
            nonProxiedProperties().filter(mp -> !mp.isCollectional()).forEach(MetaProperty::defineForOriginalValue);
        } finally {
            endInitialising();
        }
        return this;
    }

    /**
     * Creates a new instance of the given type and copies all properties including ID from this instance into the created one.
     *
     * @param <COPY>
     * @param type
     * @return
     */
    public final <COPY extends AbstractEntity> COPY copy(final Class<COPY> type) {
        return copyTo(createCopyInstance(type));
    }

    /**
     * Throws an exception if entity factory was not provided for this entity instance.
     */
    private void assertEntityFactoryPresence() {
        if (getEntityFactory() == null) {
            throw new EntityException(format("Entity factory is required, but is missing from an instance of entity [%s].", getType().getName()));
        }
    }
    /**
     * Creates a new instance of the given type and copies all properties including, but not system properties such as ID and version.
     *
     * @param type
     * @return
     */
    public final <COPY extends AbstractEntity> COPY copyWithoutIdentity(final Class<COPY> type) {
        assertEntityFactoryPresence();
        return copyTo(getEntityFactory().newEntity(type));
    }

    /**
     * Copies the content of this instance into the provided instance. Does not touch system properties such as ID and version. This means that only properties returned from method
     * call <code>getProperties()</code> are copied.
     * <p>
     * <i>IMPORTANT: the type of provided instance and the type of this instance do not have to be the same or even be polymorphic; properties are copied on name by name basis.
     * properties.</i>
     *
     * @param copy
     * @return
     */
    public final <COPY extends AbstractEntity> COPY copyTo(final COPY copy) {
        copy.beginInitialising();
        EntityUtils.copy(this, copy, ID, VERSION);
        copy.endInitialising();
        return copy;
    }

    /**
     * Creates a new instance of the given type and copies ID from this instance into the created one.
     *
     * @param <COPY>
     * @param type
     * @return
     */
    protected final <COPY extends AbstractEntity> COPY createCopyInstance(final Class<COPY> type) {
        final COPY copy;
        if (this.isInstrumented()) {
            assertEntityFactoryPresence();
            copy = getEntityFactory().newEntity(type, getId());
        } else {
            copy = EntityFactory.newPlainEntity(type,  getId());
        }
        copy.setVersion(getVersion());
        return copy;
    }

    /**
     * Returns a property, which is some sense is preferred.
     * <p>
     * For example, it is used as part of the UI logic to determine what property should be focused when entity is being switched into the edit mode.
     *
     * @return
     */
    public String getPreferredProperty() {
        return preferredProperty;
    }

    /**
     * Sets the preferred property.
     *
     * @param preferredProperty
     */
    public void setPreferredProperty(final String preferredProperty) {
        if (preferredProperty != null && !EntityUtils.isProperty(getType(), preferredProperty)) { // null 'preferredProperty' can be used to reset existing preferred property that was set previously
            throw new EntityException(format("The specified property name [%s] does not represent a valid property for type [%s].", preferredProperty, getType().getName()));
        }
        this.preferredProperty = preferredProperty;
    }

    /**
     * Sets the preferred property.
     *
     * @param preferredProperty
     */
    public void setPreferredProperty(final IConvertableToPath preferredProperty) {
        setPreferredProperty(preferredProperty.toPath());
    }

    /**
     * If this entity is persisted, then ID is used to identify whether this and that entities represent the same thing. If both entities are not persisted then equality is used,
     * which is based on their keys for comparison. Otherwise, returns false.
     *
     * @param that
     * @return
     */
    public boolean sameAs(final AbstractEntity<K> that) {
        if (that == null) {
            return false;
        }

        if (this.isPersisted()) {
            return this.getId().equals(that.getId());
        } else if (!this.isPersisted() && !that.isPersisted()) {
            return this.equals(that);
        } else {
            return false;
        }

    }

    /**
     * Indicates if entity represents an instance of a persistent type.
     *
     * @return
     */
    public boolean isPersistent() {
        return getType().isAnnotationPresent(MapEntityTo.class);
    }

    /** Indicates whether the editable state of the entity should be ignored durting mutation. */
    public boolean isIgnoreEditableState() {
        return ignoreEditableState;
    }

    /**
     * The main intent of this method is to support entity modification in rare situations where it is being marked as read-only.
     * Should be used with great care as it may alter the intended domain behaviour if used carelessly.
     * At this stage, there is no reason for this setter to be used as part of the domain logic.
     */
    public void setIgnoreEditableState(final boolean ignoreEditableStateDuringSave) {
        this.ignoreEditableState = ignoreEditableStateDuringSave;
    }

    /**
     * Returns a list of proxied properties. Could return an empty set.
     * This method should not be final due to the need for interception.
     *
     * @return
     */
    public Set<String> proxiedPropertyNames() {
        return emptySet();
    }

    public static final String PROXIED_PROPERTY_NAMES_METHOD_NAME = "proxiedPropertyNames";

    /**
     * Indicates whether this instance represents a proxied id-only value.
     *
     * @return
     */
    public boolean isIdOnlyProxy() {
        return this instanceof IIdOnlyProxyEntity;
    }

}
