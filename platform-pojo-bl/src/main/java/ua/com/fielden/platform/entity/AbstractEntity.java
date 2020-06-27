package ua.com.fielden.platform.entity;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_LENGTH;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_PRECISION;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_SCALE;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_TRAILING_ZEROS;
import static ua.com.fielden.platform.entity.exceptions.EntityDefinitionException.COLLECTIONAL_PROP_MISSING_LINK_MSG;
import static ua.com.fielden.platform.entity.exceptions.EntityDefinitionException.COLLECTIONAL_PROP_MISSING_TYPE_MSG;
import static ua.com.fielden.platform.entity.exceptions.EntityDefinitionException.INVALID_ONE2ONE_ASSOCIATION_MSG;
import static ua.com.fielden.platform.entity.exceptions.EntityDefinitionException.INVALID_USE_FOR_PRECITION_AND_SCALE_MSG;
import static ua.com.fielden.platform.entity.exceptions.EntityDefinitionException.INVALID_USE_OF_NUMERIC_PARAMS_MSG;
import static ua.com.fielden.platform.entity.exceptions.EntityDefinitionException.INVALID_USE_OF_PARAM_LENGTH_MSG;
import static ua.com.fielden.platform.entity.exceptions.EntityDefinitionException.INVALID_VALUES_FOR_PRECITION_AND_SCALE_MSG;
import static ua.com.fielden.platform.entity.validation.custom.DefaultEntityValidator.validateWithCritOnly;
import static ua.com.fielden.platform.error.Result.asRuntime;
import static ua.com.fielden.platform.reflection.EntityMetadata.entityExistsAnnotation;
import static ua.com.fielden.platform.reflection.EntityMetadata.isEntityExistsValidationApplicable;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isNumeric;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.stripIfNeeded;
import static ua.com.fielden.platform.utils.CollectionUtil.unmodifiableSetOf;
import static ua.com.fielden.platform.utils.EntityUtils.isString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.DescReadonly;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyReadonly;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Optional;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.Unique;
import ua.com.fielden.platform.entity.annotation.UpperCase;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.entity.ioc.ObservableMutatorInterceptor;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.MetaPropertyFull;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.proxy.StrictProxyException;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.ICustomValidator;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.EntityMetadata;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.utils.EntityUtils;

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
 * 	}
 * 	 &#064;Override public boolean equals(final Object obj) {
 * 		...
 * 		final PoItemKey cmpTo = (PoItemKey) obj;
 * 		return getPurchaseOrder().equals(cmpTo.getPurchaseOrder()) &amp;&amp;
 *                     getNumber().equals(cmpTo.getNumber());
 * 	}
 * 	 &#064;Override public int compareTo(final PoItemKey cmpTo) {
 *              if (getPurchaseOrder().equals(cmpTo.getPurchaseOrder())) {
 *                 return getNumber().compareTo(cmpTo.getNumber());
 *              }  else {
 *                 return getPurchaseOrder().compareTo(cmpTo.getPurchaseOrder());
 *              }
 * 	}
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
 * Intercepter {@link ValidationMutatorInterceptor} was implemented specifically to handle validation of values being passed into setters.
 * Its implementation uses validators associated with property during meta-property instantiation.
 *
 * However, entity instance should be created with Guice intercepter provided with a module configured to bind this intercepter.
 * <p>
 * A similar situation is with support of property change event handling. Any setter annotated with {@link Observable} should be intercepted by {@link ObservableMutatorInterceptor},
 * which can be achieved by using appropriately configured Guice module.
 *
 * Please refer {@link EntityModule} for more details.
 *
 * <h3>Property mutators</h3>
 * The <i>property</i> specification as defined in JavaBeans does not cover fully the needs identified by our team for working with business entities where properties have loosely coupled validation logic and change observation.
 * Also, the approach taken in JavaBeans does not provide the possibility to follow [http://en.wikipedia.org/wiki/Fluent_interface fluent interface] programming approach.
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
 *       Please note that mutator with at least one validation annotation should also be annotated with {@link ValidationRequired} -- this is enforced by the platform and failure to comply results in early runtime exception.
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

    protected final Logger logger;

    @MapTo("_ID")
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
    public static final Set<String> COMMON_PROPS = unmodifiableSetOf(KEY, DESC, "referencesCount", "referenced");
    
    /**
     * A flag that provides a way to enforce more strict model verification, which is the default approach.
     */
    public static final boolean STRICT_MODEL_VERIFICATION;
    static { // static initialisation block is required instead of direct value assignment to enable reassignment of the value at runtime
        STRICT_MODEL_VERIFICATION = true;
    }

    /**
     * Enforces the non-strict verification of the domain model.
     * This mode improves performance, but does not verify the domain model for self-consistency.
     * It is strongly recommended not to use this mode during application development.
     */
    public static void useNonStrictModelVerification() {
        try {
            Reflector.assignStatic(AbstractEntity.class.getDeclaredField("STRICT_MODEL_VERIFICATION"), false);
        } catch (final Exception ex) {
            throw asRuntime(ex);
        }
    }

    /**
     * Enforces the strict verification of the domain model, which is the default approach.
     */
    public static void useStrictModelVerification() {
        try {
            Reflector.assignStatic(AbstractEntity.class.getDeclaredField("STRICT_MODEL_VERIFICATION"), true);
        } catch (final Exception ex) {
            throw asRuntime(ex);
        }
    }

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


        logger = Logger.getLogger(this.getType());

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
    public <ET extends AbstractEntity<K>> ET setDesc(final String desc) {
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
        if (!(obj instanceof AbstractEntity)) {
            return false;
        }
        // let's ensure that types match
        final AbstractEntity<?> that = (AbstractEntity<?>) obj;
        if (this.getType() != that.getType()) {
            return false;
        }

        // TODO need to carefully consider this bit of logic for comparing ID-only values
        if (that.isIdOnlyProxy() && this.isIdOnlyProxy()) {
            return that.getId().equals(this.getId());
        }
        // TODO need to carefully consider this bit of logic for comparing ID-only values
        if (this.isPersistent() && (that.isIdOnlyProxy() || this.isIdOnlyProxy()) &&
                (!that.isInstrumented() || !that.isDirty()) &&
                (!this.isInstrumented() || !this.isDirty()) &&
                that.getId().equals(this.getId())) {
            return true;
        }
        // now can compare key values
        final Object thatKey = that.getKey();
        return getKey() != null && getKey().equals(thatKey) || getKey() == null && thatKey == null;
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
            throw new StrictProxyException(format("Cannot get value for proxied property [%s] of entity [%s].", propertyName, getType().getName()));
        }
        try {
            return Finder.findFieldValueByName(this, propertyName);
        } catch (final Exception e) {
            // there are cases where this.toString() may fail such as for non-initialized union entities
            // need to degrade gracefully in order to to hide the original exception...
            String thisToString;
            try {
                thisToString = this.toString();
            } catch (final Exception ex) {
                thisToString = "this.toString()";
            }
            throw new EntityException(format("Could not get the value for property [%s] in instance [%s]@[%s].", propertyName , thisToString, getType().getName()), e);
        }
    }

    /**
     * Dynamic setter for setting property value.
     *
     * @param propertyName
     * @param value
     */
    public AbstractEntity<K> set(final String propertyName, final Object value) {
        try {
            final Class<?> propertyType = Finder.findFieldByName(getType(), propertyName).getType();
            final String setterName = Mutator.SETTER.getName(propertyName);
            final Method setter = Reflector.getMethod(this, setterName, propertyType);
            Object valueToInvokeOn = this;
            if (!setter.getDeclaringClass().isAssignableFrom(getType()) && AbstractUnionEntity.class.isAssignableFrom(getType())) {
                valueToInvokeOn = ((AbstractUnionEntity) this).activeEntity();
            }
            // making method accessible if it isn't
            final boolean isAccessible = setter.isAccessible();
            setter.setAccessible(true);
            setter.invoke(valueToInvokeOn, value);
            // reverting changes to 'accessible' property of Method class
            setter.setAccessible(isAccessible);
            return this;
        } catch (final Exception e) {
            // let's be a little more intelligent about handling instances of InvocationTargetException to report errors without the unnecessary nesting
            if (e instanceof InvocationTargetException && e.getCause() != null) {
                // the cause of type Result should be reported as is
                if (e.getCause() instanceof Result) {
                    throw (Result) e.getCause();
                } else { // otherwise wrap the cause in EntityException
                    throw new EntityException(format("Error setting value [%s] into property [%s] for entity [%s]@[%s].", value, propertyName, this, getType().getName()), e.getCause());
                }
            } else {
                throw new EntityException(format("Error setting value [%s] into property [%s] for entity [%s]@[%s].", value, propertyName, this, getType().getName()), e);
            }
        }
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

        // obtain field annotated as properties
        final List<Field> fields = Finder.findRealProperties(getClass());
        for (final Field field : fields) { // for each property field
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
                properties.put(propName, new MetaProperty(this, field, type, isKey, true, extractDependentProperties(field, fields)));
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

                    // if setter is annotated then try to instantiate specified validator
                    final Set<Annotation> declatedValidationAnnotations = new HashSet<>();
                    final Map<ValidationAnnotation, Map<IBeforeChangeEventHandler<?>, Result>> validators = collectValidators(metaPropertyFactory, field, type, isCollectional, declatedValidationAnnotations);
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
                            declatedValidationAnnotations,
                            validators,
                            definer,
                            extractDependentProperties(field, fields));
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
            final String error = format(INVALID_USE_FOR_PRECITION_AND_SCALE_MSG, propName, getType().getName());
            logger.error(error);
            throw new EntityDefinitionException(error);
        }

        if (isNumeric && isPropertyAnnotation.precision() != DEFAULT_PRECISION && isPropertyAnnotation.precision() <= isPropertyAnnotation.scale()) {
                final String error = format(INVALID_VALUES_FOR_PRECITION_AND_SCALE_MSG, propName, getType().getName());
                logger.error(error);
                throw new EntityDefinitionException(error);
                
        }

        if (!isString(type) && !type.isArray() && isPropertyAnnotation.length() != DEFAULT_LENGTH) {
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

        if (EntityUtils.isEntityType(type) && EntityUtils.isEntityType(PropertyTypeDeterminator.determinePropertyType(type, KEY))
                && !Finder.isOne2One_association(entityType, propName)) {
            final String error = format(INVALID_ONE2ONE_ASSOCIATION_MSG, propName, getType().getName());
            logger.error(error);
            throw new EntityDefinitionException(error);
        }
    }

    /**
     * A predicate method to identify whether a collectional property requires, but is missing a corresponding <code>link property</code> information.
     *
     * @param propertyName
     * @return
     */
    protected boolean isLinkPropertyRequiredButMissing(final String propertyName) {
        return EntityUtils.isPersistedEntityType(getType()) && !Finder.hasLinkProperty(getType(), propertyName);
    }

    /**
     * Analyses mutators and their annotations to collect and instantiate all property validators.
     *
     * @param metaPropertyFactory
     * @param field
     * @param properyType
     * @param isCollectional
     * @return map of validators
     * @throws Exception
     */
    private Map<ValidationAnnotation, Map<IBeforeChangeEventHandler<?>, Result>> collectValidators(
            final IMetaPropertyFactory metaPropertyFactory,
            final Field field,
            final Class<?> properyType,
            final boolean isCollectional,
            final Set<Annotation> validationAnnotations)
            throws Exception {
        try {
            final Map<ValidationAnnotation, Map<IBeforeChangeEventHandler<?>, Result>> validators = new EnumMap<>(ValidationAnnotation.class);
            // Get corresponding mutators to pick all specified validators in case of a collectional property there can be up to three mutators --
            // removeFrom[property name], addTo[property name] and set[property name]
            final Set<Annotation> propertyValidationAnotations = extractValidationAnnotationForProperty(field, properyType, isCollectional);
            for (final Annotation annotation : propertyValidationAnotations) {
                final ValidationAnnotation validationAnnotation = ValidationAnnotation.getValueByType(annotation);
                // if property factory cannot instantiate a validator for the specified annotation then null is returned
                final IBeforeChangeEventHandler<?>[] annotationValidators = metaPropertyFactory.create(annotation, this, field.getName(), properyType);
                if (annotationValidators.length > 0) {
                    final Map<IBeforeChangeEventHandler<?>, Result> handlersAndResults = new LinkedHashMap<>();
                    for (final IBeforeChangeEventHandler<?> handler : annotationValidators) {
                        handlersAndResults.put(handler, null);
                    }
                    validators.put(validationAnnotation, handlersAndResults);
                }
            }

            // now let's see if we need to add EntityExists validation
            if (!validators.containsKey(ValidationAnnotation.ENTITY_EXISTS) && (isEntityExistsValidationApplicable(getType(), field))) {
                final EntityExists eeAnnotation = entityExistsAnnotation(getType(), field.getName(),  (Class<? extends AbstractEntity<?>>) properyType);
                final IBeforeChangeEventHandler<?>[] annotationValidators = metaPropertyFactory.create(eeAnnotation, this, field.getName(), properyType);

                if (annotationValidators.length != 1) {
                    throw new EntityDefinitionException(format("Unexpexted number of @EntityExists annotations (expected 1, but actual %s) for property [%s] in entity [%s].", annotationValidators.length, field.getType(), getType().getName()));
                }

                propertyValidationAnotations.add(eeAnnotation);
                final Map<IBeforeChangeEventHandler<?>, Result> handlersAndResults = new LinkedHashMap<>();
                final IBeforeChangeEventHandler<?> handler = annotationValidators[0];
                handlersAndResults.put(handler, null);

                validators.put(ValidationAnnotation.ENTITY_EXISTS, handlersAndResults);
            }

            validationAnnotations.addAll(propertyValidationAnotations);

            return validators;
        } catch (final Exception ex) {
            logger.error(format("Exception during collection of validators for property [%s] in entity type [%s].", field.getName(), getType().getSimpleName()), ex);
            throw ex;
        }
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
            metaProperty.setEditable(!AnnotationReflector.isAnnotationPresentForClass(KeyReadonly.class, getType()));
            metaProperty.setRequired(true);
            if (AnnotationReflector.isAnnotationPresentForClass(KeyTitle.class, getType())) {
                final KeyTitle title = AnnotationReflector.getAnnotation(getType(), KeyTitle.class);
                metaProperty.setTitle(title.value());
                metaProperty.setDesc(StringUtils.isEmpty(title.desc()) ? title.value() : title.desc());
            }
        } else if (DESC.equals(field.getName())) {
            metaProperty.setEditable(!AnnotationReflector.isAnnotationPresentForClass(DescReadonly.class, getType()));
            metaProperty.setRequired(AnnotationReflector.isAnnotationPresentForClass(DescRequired.class, getType()));
            if (AnnotationReflector.isAnnotationPresentForClass(DescTitle.class, getType())) {
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
                        !AnnotationReflector.isAnnotationPresent(field, Optional.class)));
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
    private String[] extractDependentProperties(final Field field, final List<Field> allFields) {
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

    /**
     * Creates a set of all validation annotations specified for property mutators.
     *
     * @param field
     * @param type
     * @param isCollectional
     * @return
     * @throws NoSuchMethodException
     */
    public Set<Annotation> extractValidationAnnotationForProperty(final Field field, final Class<?> type, final boolean isCollectional) {
        final Set<Annotation> propertyValidationAnotations = new HashSet<>();
        // try to obtain setter
        propertyValidationAnotations.addAll(extractSetterAnnotations(field, type));
        propertyValidationAnotations.addAll(extractFieldBeforeChangeAnnotations(field));
        propertyValidationAnotations.addAll(extractFieldUniqueAnnotation(field));
        propertyValidationAnotations.addAll(extractFieldFinalAnnotation(field));
        return propertyValidationAnotations;
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
        return new HashSet<>();
    }

    /**
     * Processed BCE and ACE declarations in order to instantiate event handlers.
     *
     * @param field
     * @param entityType
     * @return
     */
    private static List<Annotation> extractFieldBeforeChangeAnnotations(final Field field) {
        final List<Annotation> propertyValidationAnotations = new ArrayList<>();
        final BeforeChange bce = AnnotationReflector.getAnnotation(field, BeforeChange.class);
        if (bce != null) {
            propertyValidationAnotations.add(bce);
        }
        return propertyValidationAnotations;
    }

    /**
     * Looks for {@link Unique} annotation.
     *
     * @param field
     * @param entityType
     * @return
     */
    private static List<Annotation> extractFieldUniqueAnnotation(final Field field) {
        final List<Annotation> propertyValidationAnotations = new ArrayList<>();
        final Unique uniqueAnnotation = AnnotationReflector.getAnnotation(field, Unique.class);
        if (uniqueAnnotation != null) {
            propertyValidationAnotations.add(uniqueAnnotation);
        }
        return propertyValidationAnotations;
    }

    /**
     * Looks for {@link Final} annotation.
     *
     * @param field
     * @return
     */
    private static List<Annotation> extractFieldFinalAnnotation(final Field field) {
        final List<Annotation> propertyValidationAnotations = new ArrayList<>();
        final Final finalAnnotation = AnnotationReflector.getAnnotation(field, Final.class);
        if (finalAnnotation != null) {
            propertyValidationAnotations.add(finalAnnotation);
        }
        return propertyValidationAnotations;
    }

    /**
     * Returns unmodifiable map of properties.
     *
     * @return
     */
    public final Map<String, MetaProperty<?>> getProperties() {
        assertInstrumented();
        return Collections.unmodifiableMap(properties);
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
     * Throws {@link EntityException} if this instance is not instrumented.
     */
    public final void assertInstrumented() {
        if (!isInstrumented()) {
            throw new EntityException(format("Meta-properties for this instance of entity [%s] do not exist as it was not instrumented.", getType().getName()));
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
     * Indicates whether this entity instance can be changed. By default returns a successful {@link Result} indicating editability of the entity.
     * <p>
     * This method should be overridden if some custom logic needs to be provided.
     * <p>
     * For example, some entity instance should not be changed when its certain property has some specific value.
     *
     * @return
     */
    public Result isEditable() {
        return Result.successful(null);
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
        if (!EntityUtils.isProperty(getType(), preferredProperty)) {
            throw new EntityException(format("The specified property name [%s] does not represent a valid property for type [%s].", preferredProperty, getType().getName()));
        }
        this.preferredProperty = preferredProperty;
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
     * The main intent of this method is to support entity modification in rare situation while it it being marked as read-only.
     * Should be used with great care as it may alter the intended domain behaviour if used carelessly.
     * At this stage there is no reason for this setter to be used as part of the domain logic. */
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
        return Collections.emptySet();
    }

    /**
     * Indicates whether this instance represents a proxied id-only value.
     *
     * @return
     */
    public boolean isIdOnlyProxy() {
        return proxiedPropertyNames().contains(VERSION);
    }
}
