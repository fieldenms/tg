package ua.com.fielden.platform.domaintree.impl;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IProperty;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.factory.CalculatedAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.CustomPropAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.EntityUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Comparator.*;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.isGenerated;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.modifiedClass;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService.generateTypeName;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.mapValues;

/// A domain manager implementation with all sufficient logic for domain modification / loading.
///
/// ### Implementation notes
///
/// 1. After modifications are applied, the manager stores a map of the form `entityType -> real enhanced entityType`,
///    which should be used to obtain an enhanced type via [#getManagedType] whenever an entity type is operated on.
///    E.g., use `(getManagedType(entityType), propertyPath)` rather than `(entityType, propertyPath)`.
///
/// 2. After modifications are applied, such as adding/removing/changing a calculated property, the manager stores
///    a full list of calculated properties for all types.
///    This list should be applied or discarded using [#apply] or [#discard].
///
public final class DomainTreeEnhancer extends AbstractDomainTree implements IDomainTreeEnhancer {

    private static final Logger LOGGER = getLogger(DomainTreeEnhancer.class);
    private static final String ERR_TYPE_COULD_NOT_BE_GENERATED = "Type for [%s] could not be generated.";
    private static final String ERR_COULD_NOT_PROPAGATE_ENHANCED_TYPE_TO_ROOT = "Enhanced type [%s] could not be propagated to [%s] root along path [%s].";
    private static final String ERR_COULD_NOT_MODIFY_ROOT_TYPE_NAME = "Root type [%s] name could not be modified to [%s].";

    /// Type diff object's key for calculated properties, used in generated type naming.
    private static final String CALCULATED_PROPERTIES = "calculatedProperties";

    /// Type diff object's key for custom properties, used in generated type naming.
    private static final String CUSTOM_PROPERTIES = "customProperties";

    /// Comparator for [CalculatedPropertyInfo] instances to impose order in type diff object during SHA-256 calculation.
    private static final Comparator<CalculatedPropertyInfo> CALCULATED_PROPERTY_INFO_COMPARATOR =
        comparing(CalculatedPropertyInfo::contextPath) // in most cases this property defines the path where calculated property will be placed
        .thenComparing(CalculatedPropertyInfo::customPropertyName, nullsFirst(naturalOrder())) // in most cases this property defines the actual name of calculated property
        .thenComparing(CalculatedPropertyInfo::contextualExpression) // expression may influence the path, where calculated property will be placed (in future; currently it does not)
        .thenComparing(CalculatedPropertyInfo::title); // title may define actual name of calculated property without customPropertyName (in future; currently it does not)

    /// Cache of [DomainTreeEnhancer]s with keys `(rootTypes, calcProps, customProps)`.
    ///
    /// The main purpose of this cache is to reduce heavy calculated properties re-computation and the amount of generated
    /// entity types.
    private static final ConcurrentMap<T3<Set<Class<?>>, Map<Class<?>, Set<CalculatedPropertyInfo>>, Map<Class<?>, List<CustomProperty>>>, DomainTreeEnhancer> domainTreeEnhancers = new ConcurrentHashMap<>();

    /// A mapping between original and enhanced types.
    /// Contains entries of `original -> real` or `original -> original` (in case of not enhanced type).
    private final Map<Class<?>, Class<?>> originalAndEnhancedRootTypes;

    /// Holds differences from the original domain (all calculated properties for all root types).
    private final Map<Class<?>, List<CalculatedProperty>> calculatedProperties;

    /// Holds differences from the original domain (all custom properties for all root types).
    private final Map<Class<?>, List<CustomProperty>> customProperties;

    /// Constructs a new instance of domain enhancer with a clean, not enhanced domain.
    ///
    public DomainTreeEnhancer(final EntityFactory entityFactory, final Set<Class<?>> rootTypes) {
        this(entityFactory, rootTypes,
             rootTypes.stream().collect(toMap(Function.identity(), _ -> new HashSet<>())),
             rootTypes.stream().collect(toMap(Function.identity(), _ -> new ArrayList<>())));
    }

    /// Constructs a new instance of the domain enhancer with full information about containing root types (**enhanced** or not).
    /// This primary constructor should be used for serialisation and copying.
    /// Please also note that calculated property changes that were not applied will disappear!
    /// So every enhancer should be carefully applied (or discarded) before serialisation.
    ///
    private DomainTreeEnhancer(
            final EntityFactory entityFactory,
            final Set<Class<?>> rootTypes,
            final Map<Class<?>, Set<CalculatedPropertyInfo>> calculatedPropertiesInfo,
            final Map<Class<?>, List<CustomProperty>> customProperties)
    {
        super(entityFactory);

        this.originalAndEnhancedRootTypes = new LinkedHashMap<>();
        // init a map with NOT enhanced types and empty byte arrays.
        this.originalAndEnhancedRootTypes.putAll(createOriginalAndEnhancedRootTypes(rootTypes));

        this.customProperties = new LinkedHashMap<>();
        this.customProperties.putAll(customProperties);

        this.calculatedProperties = new LinkedHashMap<>();
        this.calculatedProperties.putAll(createCalculatedPropertiesFrom(this, calculatedPropertiesInfo));

        apply();

        this.customProperties.clear();
        this.customProperties.putAll(customProperties);

        this.calculatedProperties.clear();
        this.calculatedProperties.putAll(extractAll(this, true));
        
        // Perform some post-creation validation.
        validateEnhancer();
    }
    
    /// Copy constructor for [DomainTreeEnhancer] relying on shared inner resources (e.g., ASTs of corresponding [CalculatedProperty] instances).
    ///
    /// This is a performance-friendly version of the copying function without unnecessary parsing of [CalculatedProperty#getContextualExpression()],
    /// which is a costly operation.
    ///
    private DomainTreeEnhancer(final DomainTreeEnhancer enhancer) {
        super(enhancer.getFactory());
        
        this.originalAndEnhancedRootTypes = new LinkedHashMap<>();
        for (final Entry<Class<?>, Class<?>> entry: enhancer.originalAndEnhancedRootTypes.entrySet()) {
            this.originalAndEnhancedRootTypes.put(entry.getKey(), entry.getValue());
        }
        
        // CustomProperty is fully immutable and it is also safe to use the same shared instances of that type across all DomainTreeEnhancer copies.
        this.customProperties = new LinkedHashMap<>();
        this.customProperties.putAll(enhancer.customProperties);
        
        // CalculatedProperty instances will be copied through CalculatedProperty.copy method which shares inner 'ast's and other derived information across all CalculatedProperty copies.
        this.calculatedProperties = new LinkedHashMap<>();
        for (final Entry<Class<?>, List<CalculatedProperty>> entry: enhancer.calculatedProperties.entrySet()) {
            this.calculatedProperties.put(entry.getKey(), entry.getValue().stream().map(cp -> cp.copy(this)).collect(toCollection(ArrayList::new)));
        }
        
        // Perform some post-creation validation.
        validateEnhancer();
    }
    
    /// Validates this instance on subject of conformity of resultant managed type and presence of calculated / custom properties.
    ///
    private void validateEnhancer() {
        for (final Class<?> rootType : rootTypes()) {
            // check whether the type WITH calculated properties IS enhanced 
            if (!hasNoAdditionalProperties(rootType) && !isGenerated(getManagedType(rootType))) {
                throw new IllegalStateException(format("The type [%s] should be enhanced -- it has %s properties.", rootType.getSimpleName(), additionalPropDefinitionsAsString(rootType)));
            }
            // check whether the type WITHOUT calculated properties IS NOT enhanced 
            if (hasNoAdditionalProperties(rootType) && isGenerated(getManagedType(rootType))) {
                throw new IllegalStateException(format("The type [%s] should be NOT enhanced -- it has no additional properties.", rootType.getSimpleName()));
            }
        }
    }
    
    /// A constructor **strictly** for version maintenance.
    ///
    public DomainTreeEnhancer(
            final EntityFactory entityFactory,
            final Map<Class<?>, Class<?>> originalAndEnhancedRootTypes,
            final Map<Class<?>, List<CalculatedProperty>> calculatedProperties,
            final Map<Class<?>, List<CustomProperty>> customProperties)
    {
        super(entityFactory);
        this.originalAndEnhancedRootTypes = originalAndEnhancedRootTypes;
        this.calculatedProperties = calculatedProperties;
        this.customProperties = customProperties;
    }
    
    /// Creates a [DomainTreeEnhancer] instance from `rootTypes` that are used for calculated/custom properties mapped to each root type.
    ///
    /// This method uses the [DomainTreeEnhancer] cache.
    /// At this stage, instances of [DomainTreeEnhancer] are mutable, but in practice we do not use their mutability,
    /// so we can safely cache every instance.
    ///
    /// Mutability is not an issue due to the following:
    /// 1. [ua.com.fielden.platform.web.centre.EntityCentre#createDefaultCentre] applies calculated/custom properties
    ///    and after that [DomainTreeEnhancer] remains in the same state.
    /// 2. Instances that were deserialised after retrieval from database or during copying are not mutated afterwards.
    ///
    /// We need to consider [DomainTreeEnhancer] cache management when implementing the ability to add calculated properties from UI.
    ///
    public static DomainTreeEnhancer createFrom(
            final EntityFactory entityFactory,
            final Set<Class<?>> rootTypes,
            final Map<Class<?>, Set<CalculatedPropertyInfo>> calculatedPropertiesInfo,
            final Map<Class<?>, List<CustomProperty>> customProperties) {
        final var cachedInstance = domainTreeEnhancers.computeIfAbsent(
            t3(rootTypes, calculatedPropertiesInfo, customProperties),
            _ -> new DomainTreeEnhancer(entityFactory, rootTypes, calculatedPropertiesInfo, customProperties));
        // Create a copy to avoid further mutation of the cached instance (e.g. in postCentreCreated hooks).
        return new DomainTreeEnhancer(cachedInstance);
    }
    
    private boolean hasNoAdditionalProperties(final Class<?> rootType) {
        return this.calculatedProperties.get(rootType) == null && (this.customProperties.get(rootType) == null || this.customProperties.get(rootType).isEmpty());
    }
    
    private String additionalPropDefinitionsAsString(final Class<?> rootType) {
        final StringBuilder sb = new StringBuilder();
        if (this.calculatedProperties.get(rootType) != null) {
            sb.append(this.calculatedProperties.get(rootType).size() + " calculated ");
        }
        if (this.customProperties.get(rootType) != null && !this.customProperties.get(rootType).isEmpty()) {
            sb.append(this.customProperties.get(rootType).size() + " custom ");
        }
        return StringUtils.isEmpty(sb.toString()) ? " no " : sb.toString();
    }

    /// Creates a map of `original -> original` for provided `rootTypes`.
    ///
    /// @return a modifiable map
    ///
    private static Map<Class<?>, Class<?>> createOriginalAndEnhancedRootTypes(final Set<Class<?>> rootTypes) {
        final Map<Class<?>, Class<?>> originalAndEnhancedRootTypes = new LinkedHashMap<>();
        for (final Class<?> rootType : rootTypes) {
            originalAndEnhancedRootTypes.put(rootType, rootType);
        }
        return originalAndEnhancedRootTypes;
    }

    /// Groups properties by their domain paths.
    /// In the returned map, a type may be mapped to `null` if it's not associated with any of the given properties.
    ///
    /// @param calculatedProperties  a map of the form `{ type : calculatedProperties }`
    /// @param customProperties  a map of the form `{ type : custom properties }`
    /// @param rootTypes  types owning the given properties (may also include types that aren't present in any properties map)
    ///
    /// @return a map of the form `{ type : { propertyPath : { propertyName : property } } }`
    ///
    private static Map<Class<?>, Map<String, Map<String, IProperty>>> groupByPaths(
            final Map<Class<?>, List<CalculatedProperty>> calculatedProperties,
            final Map<Class<?>, List<CustomProperty>> customProperties,
            final Set<Class<?>> rootTypes)
    {
        final Map<Class<?>, Map<String, Map<String, IProperty>>> grouped = new LinkedHashMap<>();
        calculatedProperties.forEach((root, props) -> {
            if (props != null && !props.isEmpty()) {
                final var paths = grouped.computeIfAbsent(root, $ -> new LinkedHashMap<>());
                for (final CalculatedProperty prop : props) {
                    final var names = paths.computeIfAbsent(prop.path(), $ -> new LinkedHashMap<>());
                    names.put(prop.name(), prop);
                }
            } else {
                grouped.put(root, null);
            }
        });
        customProperties.forEach((root, props) -> {
            if (props != null && !props.isEmpty()) {
                final var paths = grouped.computeIfAbsent(root, $ -> new LinkedHashMap<>());
                for (final CustomProperty prop : props) {
                    final var names = paths.computeIfAbsent(prop.path(), $ -> new LinkedHashMap<>());
                    names.put(prop.name(), prop);
                }
            } else if (!grouped.containsKey(root)) {
                grouped.put(root, null);
            }
        });
        // add the types, not enhanced with any calc prop
        for (final Class<?> originalRoot : rootTypes) {
            if (!grouped.containsKey(originalRoot)) {
                grouped.put(originalRoot, null);
            }
        }
        return grouped;
    }

    @Override
    public Class<?> getManagedType(final Class<?> type) {
        CalculatedProperty.validateRootWithoutRootTypeEnforcement(this, type);
        return originalAndEnhancedRootTypes.get(type) == null ? type : originalAndEnhancedRootTypes.get(type);
    }

    @Override
    public void apply() {
        //////////// Performs migration [calculatedProperties => originalAndEnhancedRootTypes] ////////////
        final Map<Class<?>, Class<?>> freshOriginalAndEnhancedRootTypes = generateHierarchy(originalAndEnhancedRootTypes.keySet(), calculatedProperties, customProperties);
        originalAndEnhancedRootTypes.clear();
        originalAndEnhancedRootTypes.putAll(freshOriginalAndEnhancedRootTypes);
    }

    /// Fully generates a new hierarchy of "originalAndEnhancedRootTypes" according to `calculatedProperties`.
    ///
    /// Root types for which no calculated properties were specified will have a marker calculated property added.
    ///
    private static Map<Class<?>, Class<?>> generateHierarchy(
            final Set<Class<?>> rootTypes,
            final Map<Class<?>, List<CalculatedProperty>> calculatedProperties,
            final Map<Class<?>, List<CustomProperty>> customProperties)
    {
        // single classLoader instance is needed for single "apply" transaction
        final Map<Class<?>, Class<?>> originalAndEnhancedRootTypes = createOriginalAndEnhancedRootTypes(rootTypes);
        final Map<Class<?>, Map<String, Map<String, IProperty>>> groupedCalculatedProperties = groupByPaths(calculatedProperties, customProperties, rootTypes);

        // iterate through calculated property places (e.g. Vehicle.class+"" or WorkOrder.class+"veh.status") with no care about order
        for (final Entry<Class<?>, Map<String, Map<String, IProperty>>> entry : groupedCalculatedProperties.entrySet()) {
            final Class<?> originalRoot = entry.getKey();
            // generate predefined root type name for all calculated properties
            final String predefinedRootTypeName = generateTypeName(originalRoot, linkedMapOf(t2(CALCULATED_PROPERTIES, calculatedPropertiesInfo(calculatedProperties, rootTypes)), t2(CUSTOM_PROPERTIES, customPropertiesInfo(customProperties))));
            final boolean calcOrCustomPropsOnlyInRoot = entry.getValue() != null && entry.getValue().size() == 1 && StringUtils.isEmpty(entry.getValue().entrySet().iterator().next().getKey());
            if (entry.getValue() == null) {
                originalAndEnhancedRootTypes.put(originalRoot, originalRoot);
            } else {
                for (final Entry<String, Map<String, IProperty>> placeAndProps : entry.getValue().entrySet()) {
                    final Map<String, IProperty> props = placeAndProps.getValue();
                    if (props != null && !props.isEmpty()) {
                        final Supplier<Set<NewProperty>> createNewProperties = () -> {
                            return props.entrySet().stream().map(nameWithProp -> {
                                final IProperty iProp = nameWithProp.getValue();
                                if (iProp instanceof final CalculatedProperty prop) {
                                    final String originationProperty = prop.getOriginationProperty() == null ? "" : prop.getOriginationProperty();
                                    final Annotation calcAnnotation = new CalculatedAnnotation().contextualExpression(prop.getContextualExpression()).rootTypeName(predefinedRootTypeName).contextPath(prop.getContextPath()).origination(originationProperty).attribute(prop.getAttribute()).category(prop.category()).newInstance();
                                    final IsProperty isPropAnnot = prop.getPrecision() != null && prop.getScale() != null ? new IsPropertyAnnotation(prop.getPrecision(), prop.getScale()).newInstance() : NewProperty.DEFAULT_IS_PROPERTY_ANNOTATION;
                                    return new NewProperty(nameWithProp.getKey(), prop.resultType(), prop.getTitle(), prop.getDesc(), isPropAnnot, calcAnnotation);
                                } else { // this should be CustomProperty!
                                    final CustomProperty prop = (CustomProperty) iProp;
                                    return new NewProperty(nameWithProp.getKey(), prop.resultType(), prop.getTitle(), prop.getDesc(), new CustomPropAnnotation().newInstance());
                                }
                            }).collect(toCollection(LinkedHashSet::new));
                        };
                        final Class<?> realRoot = originalAndEnhancedRootTypes.get(originalRoot);
                        // a path to calculated properties
                        final String path = placeAndProps.getKey();

                        // determine a "real" parent type:
                        final Class<?> realParentToBeEnhanced = StringUtils.isEmpty(path) ? realRoot : PropertyTypeDeterminator.determinePropertyType(realRoot, path);
                        final Class<?> realParentEnhanced;
                        try {
                            // generate & load new type enhanced by calculated properties
                            if (calcOrCustomPropsOnlyInRoot) {
                                realParentEnhanced = modifiedClass(predefinedRootTypeName, realParentToBeEnhanced, typeMaker -> typeMaker.addProperties(createNewProperties.get()));
                            } else {
                                realParentEnhanced = modifiedClass(realParentToBeEnhanced, typeMaker -> typeMaker.addProperties(createNewProperties.get()));
                            }
                        } catch (final Exception ex) {
                            final var typeGenEx = new DomainTreeException(ERR_TYPE_COULD_NOT_BE_GENERATED.formatted(realParentToBeEnhanced.getSimpleName()), ex);
                            LOGGER.error(typeGenEx);
                            throw typeGenEx;
                        }
                        // propagate enhanced type to root
                        final Class<?> rootAfterPropagation = propagateEnhancedTypeToRoot(realParentEnhanced, realRoot, path);
                        // replace relevant root type in cache
                        originalAndEnhancedRootTypes.put(originalRoot, rootAfterPropagation);
                    }
                }
            }
            if (!calcOrCustomPropsOnlyInRoot) {
                final Class<?> enhancedRoot = originalAndEnhancedRootTypes.get(originalRoot);
                // modify root type name with predefinedRootTypeName
                if (originalRoot != enhancedRoot) { // calculated properties exist -- root type should be enhanced
                    final Class<?> rootWithPredefinedName;
                    try {
                        rootWithPredefinedName = modifiedClass(predefinedRootTypeName, enhancedRoot, identity());
                    } catch (final Exception ex) {
                        final var typeGenEx = new DomainTreeException(ERR_COULD_NOT_MODIFY_ROOT_TYPE_NAME.formatted(enhancedRoot.getSimpleName(), predefinedRootTypeName), ex);
                        LOGGER.error(typeGenEx);
                        throw typeGenEx;
                    }
                    originalAndEnhancedRootTypes.put(originalRoot, rootWithPredefinedName);
                }
            }
        }
        return originalAndEnhancedRootTypes;
    }
    
    /// Propagates recursively the `enhancedType` from `(root, path)` to `(root, "")`.
    ///
    /// @param enhancedType  the type to replace the current type of property `path` in type `root`
    ///
    private static Class<?> propagateEnhancedTypeToRoot(final Class<?> enhancedType, final Class<?> root, final String path) {
        if (StringUtils.isEmpty(path)) { // replace current root type with new one
            return enhancedType;
        }

        try {
            final var owningTypeAndField = Finder.findFieldByNameWithOwningType(root, path);
            final Class<?> typeToAdapt = owningTypeAndField._1;
            final Field field = owningTypeAndField._2;
            // change type if simple field and change signature in case of collectional field
            final boolean isCollectional = Collection.class.isAssignableFrom(PropertyTypeDeterminator.determineClass(typeToAdapt, field.getName(), true, false));
            final NewProperty<?> propertyToBeModified = !isCollectional
                                                        ? NewProperty.fromField(field).changeType(enhancedType)
                                                        : NewProperty.fromField(field).setTypeArguments(enhancedType);
            final Class<?> nextEnhancedType = modifiedClass(typeToAdapt, typeMaker -> typeMaker.modifyProperties(propertyToBeModified));
            final String nextProp = PropertyTypeDeterminator.isDotExpression(path) ? PropertyTypeDeterminator.penultAndLast(path).getKey() : "";
            return propagateEnhancedTypeToRoot(nextEnhancedType, root, nextProp);
        } catch (final Exception ex) {
            final var typeGenEx = new DomainTreeException(ERR_COULD_NOT_PROPAGATE_ENHANCED_TYPE_TO_ROOT.formatted(enhancedType.getSimpleName(), root.getSimpleName(), path), ex);
            LOGGER.error(typeGenEx);
            throw typeGenEx;
        }
    }

    @Override
    public void discard() {
        //////////// Performs migration [originalAndEnhancedRootTypes => calculatedProperties] ////////////
        calculatedProperties.clear();
        calculatedProperties.putAll(extractAll(this, true));
        
        customProperties.clear(); // FIXME this piece of logic is not properly workable -- this must be considered when discard action will be used anywhere. Low priority for now.
    }

    @Override
    public void restoreAll() {
        calculatedProperties.clear();
        customProperties.clear(); // NOTE Pay attention to the FIXME in `discard`.
    }

    /// Extracts all calculated properties from enhanced root types.
    ///
    static Map<Class<?>, List<CalculatedProperty>> extractAll(final IDomainTreeEnhancer dte, final boolean validateTitleContextOfExtractedProperties) {
        final Map<Class<?>, List<CalculatedProperty>> newCalculatedProperties = new LinkedHashMap<>();
        for (final Entry<Class<?>, Class<?>> originalAndEnhanced : dte.originalAndEnhancedRootTypes().entrySet()) {
            final List<CalculatedProperty> calc = reload(originalAndEnhanced.getValue(), originalAndEnhanced.getKey(), "", dte, validateTitleContextOfExtractedProperties);
            for (final CalculatedProperty calculatedProperty : calc) {
                addCalculatedProperty(calculatedProperty, newCalculatedProperties);
            }
        }
        return newCalculatedProperties;
    }

    /// Extracts recursively contextual `calculatedProperties` from enhanced domain `type`.
    ///
    /// @param type  enhanced type to load properties
    /// @param root  not enhanced root type
    /// @param path  the path to loaded calculated props
    ///
    private static List<CalculatedProperty> reload(final Class<?> type, final Class<?> root, final String path, final IDomainTreeEnhancer dte, final boolean validateTitleContextOfExtractedProperties) {
        final List<CalculatedProperty> newCalcProperties = new ArrayList<>();
        if (!DynamicEntityClassLoader.isGenerated(type)) {
            return newCalcProperties;
        } else {
            // add all first level calculated properties if any exist
            for (final Field calculatedField : Finder.findRealProperties((Class<? extends AbstractEntity<?>>) type, Calculated.class)) {
                final Calculated calcAnnotation = AnnotationReflector.getAnnotation(calculatedField, Calculated.class);
                if (calcAnnotation != null && !StringUtils.isEmpty(calcAnnotation.value()) && AnnotationReflector.isContextual(calcAnnotation)) {
                    final Title titleAnnotation = AnnotationReflector.getAnnotation(calculatedField, Title.class);
                    final String title = titleAnnotation == null ? "" : titleAnnotation.value();
                    final String desc = titleAnnotation == null ? "" : titleAnnotation.desc();
                    final IsProperty isPropertyAnnotation = AnnotationReflector.getAnnotation(calculatedField, IsProperty.class);
                    final CalculatedProperty calculatedProperty = CalculatedProperty.createCorrect(dte.getFactory(), root, calcAnnotation.contextPath(), calcAnnotation.value(), title, desc, calcAnnotation.attribute(), "".equals(calcAnnotation.origination()) ? null
                            : calcAnnotation.origination(), isPropertyAnnotation.precision(), isPropertyAnnotation.scale(), dte, validateTitleContextOfExtractedProperties);

                    // TODO tricky setting!
                    if (!EntityUtils.equalsEx(calculatedField.getName(), calculatedProperty.name())) {
                        calculatedProperty.provideCustomPropertyName(calculatedField.getName());
                    }
                    calculatedProperty.setNameVeryTricky(calculatedField.getName());

                    newCalcProperties.add(calculatedProperty);
                }
            }
            // reload all "entity-typed" and "collectional entity-typed" sub-properties if they are enhanced
            for (final Field prop : Finder.findProperties(type)) {
                if (EntityUtils.isEntityType(prop.getType()) || EntityUtils.isCollectional(prop.getType())) {
                    final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(type, prop.getName());
                    final String newPath = StringUtils.isEmpty(path) ? prop.getName() : (path + "." + prop.getName());
                    newCalcProperties.addAll(reload(propType, root, newPath, dte, validateTitleContextOfExtractedProperties));
                }
            }
            return newCalcProperties;
        }
    }

    /// Checks whether a calculated property with the specified path and name exists and returns it.
    ///
    /// @throws IncorrectCalcPropertyException  if the property does not exist
    ///
    private CalculatedProperty calculatedPropertyWhichShouldExist(final Class<?> root, final String pathAndName) {
        final var calculatedProperty = calculatedProperty(root, pathAndName);
        if (calculatedProperty == null) {
            throw new IncorrectCalcPropertyException("The calculated property with path & name [" + pathAndName + "] does not exist in type [" + root + "].");
        }
        return calculatedProperty;
    }

    /// Finds the property for `pathAndName` among the calculated properties for `root`.
    ///
    CalculatedProperty calculatedProperty(final Class<?> root, final String pathAndName) {
        return calculatedProperty(calculatedProperties.get(DynamicEntityClassLoader.getOriginalType(root)), pathAndName);
    }

    /// Finds the property for `pathAndName` among `calcProperties`.
    ///
    static @Nullable CalculatedProperty calculatedProperty(final List<CalculatedProperty> calcProperties, final String pathAndName) {
        if (calcProperties != null) {
            for (final CalculatedProperty prop : calcProperties) {
                if (prop.pathAndName().equals(pathAndName)) {
                    return prop;
                }
            }
        }
        return null;
    }

    @Override
    public IDomainTreeEnhancer addCustomProperty(
            final Class<?> root,
            final String contextPath,
            final String name,
            final String title,
            final String desc,
            final Class<?> type,
            final Integer precision,
            final Integer scale)
    {
        addCustomProperty(new CustomProperty(root, getManagedType(root), contextPath, name, title, desc, type, precision, scale), customProperties);
        return this;
    }

    /// Validates and adds `customProperty` to `customProperties`.
    ///
    private static void addCustomProperty(final CustomProperty customProperty, final Map<Class<?>, List<CustomProperty>> customProperties) {
        customProperties.computeIfAbsent(customProperty.getRoot(), _ -> new ArrayList<>()).add(customProperty);
    }

    /// Validates and adds the property to `calculatedProperties`.
    ///
    private static ICalculatedProperty addCalculatedProperty(final CalculatedProperty calculatedProperty, final Map<Class<?>, List<CalculatedProperty>> calculatedProperties) {
        final Class<?> root = calculatedProperty.getRoot();
        if (!calculatedProperty.isValid().isSuccessful()) {
            throw calculatedProperty.isValid();
        }
        final var list = calculatedProperties.computeIfAbsent(root, _ -> new ArrayList<>());
        list.add(calculatedProperty);
        return calculatedProperty;
    }

    @Override
    public ICalculatedProperty addCalculatedProperty(final ICalculatedProperty calculatedProperty) {
        return addCalculatedProperty((CalculatedProperty) calculatedProperty, calculatedProperties);
    }

    @Override
    public ICalculatedProperty addCalculatedProperty(
            final Class<?> root,
            final String contextPath,
            final String contextualExpression,
            final String title,
            final String desc,
            final CalculatedPropertyAttribute attribute,
            final String originationProperty,
            final Integer precision,
            final Integer scale)
    {
        return addCalculatedProperty(CalculatedProperty.createCorrect(getFactory(), root, contextPath, contextualExpression, title, desc, attribute, originationProperty, precision, scale, this));
    }

    @Override
    public ICalculatedProperty addCalculatedProperty(
            final Class<?> root,
            final String contextPath,
            final String customPropertyName,
            final String contextualExpression,
            final String title,
            final String desc,
            final CalculatedPropertyAttribute attribute,
            final String originationProperty,
            final Integer precision,
            final Integer scale)
    {
        return addCalculatedProperty(CalculatedProperty.createCorrect(getFactory(), root, contextPath, customPropertyName, contextualExpression, title, desc, attribute, originationProperty, precision, scale, this));
    }

    @Override
    public ICalculatedProperty getCalculatedProperty(final Class<?> rootType, final String propertyPath) {
        return calculatedPropertyWhichShouldExist(rootType, propertyPath);
    }

    @Override
    public Optional<ICalculatedProperty> findCalculatedProperty(final Class<?> rootType, final String propertyPath) {
        return Optional.ofNullable(calculatedProperty(rootType, propertyPath));
    }

    @Override
    public ICalculatedProperty copyCalculatedProperty(final Class<?> rootType, final String propertyPath) {
        return calculatedPropertyWhichShouldExist(rootType, propertyPath).copy();
    }

    @Override
    public void removeCalculatedProperty(final Class<?> rootType, final String propertyPath) {
        final CalculatedProperty calculatedProperty = calculatedPropertyWhichShouldExist(rootType, propertyPath);

        /////////////////
        final List<CalculatedProperty> calcs = calculatedProperties.get(rootType);
        boolean existsInOtherExpressionsAsOriginationProperty = false;
        String containingExpression = null;
        for (final CalculatedProperty calc : calcs) {
            if (StringUtils.equals(calc.getOriginationProperty(), Reflector.fromAbsolute2RelativePath(calc.getContextPath(),
                                                                                                      propertyPath))) {
                existsInOtherExpressionsAsOriginationProperty = true;
                containingExpression = calc.pathAndName();
                break;
            }
        }
        if (existsInOtherExpressionsAsOriginationProperty) {
            throw new DomainTreeException("Cannot remove a property that exists in other expressions as 'origination' property. See property [" + containingExpression + "].");
        }
        /////////////////

        final boolean removed = calculatedProperties.get(rootType).remove(calculatedProperty);

        if (!removed) {
            throw new IllegalStateException("The property [" + propertyPath + "] has been validated but can not be removed.");
        }
        if (calculatedProperties.get(rootType).isEmpty()) {
            calculatedProperties.remove(rootType);
        }
    }

    @Override
    public Set<Class<?>> rootTypes() {
        return new LinkedHashSet<>(originalAndEnhancedRootTypes.keySet());
    }

    //    /**
    //     * Extracts only <b>enhanced</b> type's arrays mapped to original types.
    //     *
    //     * @return
    //     */
    //    private static Map<Class<?>, Map<String, ByteArray>> originalTypesAndEnhancedArrays(final Map<Class<?>, Pair<Class<?>, Map<String, ByteArray>>> originalAndEnhancedRootTypesAndArrays) {
    //	final Map<Class<?>, Map<String, ByteArray>> originalTypesAndEnhancedArrays = new LinkedHashMap<Class<?>, Map<String, ByteArray>>();
    //	for (final Entry<Class<?>, Pair<Class<?>, Map<String, ByteArray>>> entry : originalAndEnhancedRootTypesAndArrays.entrySet()) {
    //	    if (!entry.getValue().getValue().isEmpty()) {
    //		originalTypesAndEnhancedArrays.put(entry.getKey(), new LinkedHashMap<String, ByteArray>(entry.getValue().getValue()));
    //	    }
    //	}
    //	return originalTypesAndEnhancedArrays;
    //    }
    //
    //    /**
    //     * Extracts only <b>enhanced</b> type's arrays mapped to original types.
    //     *
    //     * @return
    //     */
    //    private Map<Class<?>, Map<String, ByteArray>> originalTypesAndEnhancedArrays() {
    //	return originalTypesAndEnhancedArrays(originalAndEnhancedRootTypesAndArrays);
    //    }

    @Override
    public Map<Class<?>, List<CalculatedProperty>> calculatedProperties() {
        // The lists have to be copied, as operations that mutate them may be called while they are being traversed,
        // which otherwise causes a ConcurrentModificationException.
        // E.g., calculatedProperties().forEach((type, props) -> props.forEach(prop -> removeCalculatedProperty(type, prop.pathAndName())))
        return mapValues(calculatedProperties, (_, props) -> List.copyOf(props));
    }

    /// Converts `calculatedProperties` into an ordered format.
    ///
    /// Warning: it is necessary to have applied all changes.
    ///
    public static SortedMap<String, SortedSet<CalculatedPropertyInfo>> calculatedPropertiesInfo(
            final Map<Class<?>, List<CalculatedProperty>> calculatedProperties,
            final Set<Class<?>> rootTypes)
    {
        final SortedMap<String, SortedSet<CalculatedPropertyInfo>> map = calculatedProperties.entrySet()
                .stream()
                .collect(toMap(entry -> entry.getKey().getName(),
                               entry -> {
                                   return entry.getValue().stream()
                                           .map(cp -> new CalculatedPropertyInfo(cp.getRoot(), cp.getContextPath(), cp.getCustomPropertyName(), cp.getContextualExpression(), cp.getTitle(), cp.getAttribute(), cp.getOriginationProperty(), cp.getDesc(), cp.getPrecision(), cp.getScale()))
                                           // Impose order to ensure equal SHA-256 checksum for the same set of properties.
                                           .collect(toCollection(() -> new TreeSet<>(CALCULATED_PROPERTY_INFO_COMPARATOR)));
                               },
                               (a, _) -> a,
                               // Impose order to ensure equal SHA-256 checksums for the same set of properties.
                               TreeMap::new));
        rootTypes.forEach(root -> map.computeIfAbsent(root.getName(), _ -> new TreeSet<>(CALCULATED_PROPERTY_INFO_COMPARATOR)));
        return map;
    }

    /// Converts `customProperties` into an ordered format.
    ///
    public static SortedMap<String, SortedSet<CustomProperty>> customPropertiesInfo(final Map<Class<?>, List<CustomProperty>> customProperties) {
        return customProperties.entrySet()
                .stream()
                .collect(toMap(entry -> entry.getKey().getName(),
                               entry -> {
                                   // Impose order to prevent different SHA-256 checksums for the same set of properties.
                                   final var set = new TreeSet<>(comparing(CustomProperty::path).thenComparing(CustomProperty::name));
                                   set.addAll(entry.getValue());
                                   return set;
                               },
                               (a, _) -> a,
                               // Impose order to prevent different SHA-256 checksums for the same set of properties.
                               TreeMap::new));
    }

    /// Creates calculated properties from raw [CalculatedPropertyInfo] instances.
    /// Also performs validation to ensure that `dte` is correct, which may be a deserialised instance or one created from scratch.
    ///
    /// Created calculated properties are fully dependent on `dte`.
    ///
    private static Map<Class<?>, List<CalculatedProperty>> createCalculatedPropertiesFrom(
            final DomainTreeEnhancer dte,
            final Map<Class<?>, Set<CalculatedPropertyInfo>> calculatedPropertiesInfo)
    {
        return calculatedPropertiesInfo.entrySet()
                .stream()
                .collect(toMap(Entry::getKey,
                               entry -> entry.getValue().stream()
                                       .map(cpInfo -> CalculatedProperty.createCorrect(
                                               dte.getFactory(),
                                               cpInfo.root(),
                                               cpInfo.contextPath(),
                                               cpInfo.customPropertyName(),
                                               cpInfo.contextualExpression(),
                                               cpInfo.title(),
                                               cpInfo.desc(),
                                               cpInfo.attribute(),
                                               cpInfo.originationProperty(),
                                               cpInfo.precision(),
                                               cpInfo.scale(),
                                               dte,
                                               true))
                                       .collect(toCollection(ArrayList::new)),
                               (a, _) -> a,
                               LinkedHashMap::new));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // IMPORTANT : rootTypes() and calculatedPropertiesInfo() are the mirror for "calculatedProperties".
        // So they should be used for serialisation, comparison and hashCode() implementation.
        result = prime * result + rootTypes().hashCode();
        result = prime * result + calculatedPropertiesInfo(calculatedProperties, rootTypes()).hashCode();
        result = prime * result + customProperties().hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DomainTreeEnhancer other = (DomainTreeEnhancer) obj;
        // IMPORTANT : rootTypes() and calculatedPropertiesInfo() are the mirror for "calculatedProperties".
        // So they should be used for serialisation, comparison and hashCode() implementation.
        return rootTypes().equals(other.rootTypes()) && calculatedPropertiesInfo(calculatedProperties, rootTypes()).equals(calculatedPropertiesInfo(other.calculatedProperties, other.rootTypes())) && customProperties().equals(other.customProperties());
    }

    @Override
    public Map<Class<?>, Class<?>> originalAndEnhancedRootTypes() {
        return originalAndEnhancedRootTypes;
    }

    /// Returns an entity factory that is essential for inner [AbstractEntity] instances (e.g. calculated properties) creation.
    ///
    @Override
    public EntityFactory getFactory() {
        return super.getFactory();
    }

    @Override
    public Map<Class<?>, List<CustomProperty>> customProperties() {
        // The lists have to be copied, as operations that mutate them may be called while they are being traversed,
        // which otherwise causes a ConcurrentModificationException.
        return mapValues(customProperties, (_, props) -> List.copyOf(props));
    }

}
