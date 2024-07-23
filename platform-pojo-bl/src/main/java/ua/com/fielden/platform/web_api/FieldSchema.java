package ua.com.fielden.platform.web_api;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.scalars.ExtendedScalars.GraphQLBigDecimal;
import static graphql.scalars.ExtendedScalars.GraphQLLong;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotationOptionally;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresentForClass;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isPropertyAnnotationPresent;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determineClass;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
import static ua.com.fielden.platform.utils.EntityUtils.isBoolean;
import static ua.com.fielden.platform.utils.EntityUtils.isCollectional;
import static ua.com.fielden.platform.utils.EntityUtils.isDate;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isString;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;
import static ua.com.fielden.platform.web_api.GraphQLScalars.GraphQLColour;
import static ua.com.fielden.platform.web_api.GraphQLScalars.GraphQLDate;
import static ua.com.fielden.platform.web_api.GraphQLScalars.GraphQLHyperlink;
import static ua.com.fielden.platform.web_api.GraphQLScalars.GraphQLMoney;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.DateOnly;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.DescReadonly;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.KeyReadonly;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.ResultOnly;
import ua.com.fielden.platform.entity.annotation.Secrete;
import ua.com.fielden.platform.entity.annotation.SkipActivatableTracking;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.annotation.TimeOnly;
import ua.com.fielden.platform.entity.annotation.Unique;
import ua.com.fielden.platform.entity.annotation.UpperCase;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.ClassParam;
import ua.com.fielden.platform.entity.annotation.mutator.DateParam;
import ua.com.fielden.platform.entity.annotation.mutator.DateTimeParam;
import ua.com.fielden.platform.entity.annotation.mutator.DblParam;
import ua.com.fielden.platform.entity.annotation.mutator.EnumParam;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.annotation.mutator.IntParam;
import ua.com.fielden.platform.entity.annotation.mutator.MoneyParam;
import ua.com.fielden.platform.entity.annotation.mutator.StrParam;
import ua.com.fielden.platform.entity.annotation.titles.PathTitle;
import ua.com.fielden.platform.entity.annotation.titles.Subtitles;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.menu.AbstractView;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.Pair;

/**
 * Contains utilities to convert TG entity properties to GraphQL query fields that reside under {@code Query.exampleEntityType} fields.
 * 
 * @author TG Team
 *
 */
public class FieldSchema {
    private FieldSchema() {}
    
    private static final String SPACE = " ";
    public static final String NEWLINE = "  \n"; // GraphiQL uses Markdown -- way to achieve new lines
    private static final String SPACE_SEPARATOR = "," + SPACE;
    private static final String NEWLINE_SEPARATOR = "," + NEWLINE;
    private static final String INDENT_STEP = "&nbsp;&nbsp;&nbsp;&nbsp;";
    static final String LIKE = "like";
    static final String VALUE = "value";
    static final String FROM = "from";
    static final String TO = "to";
    static final String ORDER = "order";
    static final String PAGE_NUMBER = "pageNumber";
    static final String PAGE_CAPACITY = "pageCapacity";
    static final GraphQLArgument LIKE_ARGUMENT = newArgument()
        .name(LIKE)
        .description("Include entities with specified string value pattern with * as a wildcard.")
        .type(GraphQLString)
        .build();
    static final GraphQLArgument ORDER_ARGUMENT = newArgument()
        .name(ORDER)
        .description("Order entities by this property with specified **ASC_n** / **DESC_m** value. Use **n** / **m** numbers (0..9) to define priority among other properties.")
        .type(newEnum()
            .name("Order")
            .description("Type for property order with priority.")
            .value("ASC_1", "ASC_1", "Ascending order, priority 1.")
            .value("DESC_1", "DESC_1", "Descending order, priority 1.")
            .value("ASC_2", "ASC_2", "Ascending order, priority 2.")
            .value("DESC_2", "DESC_2", "Descending order, priority 2.")
            .value("ASC_3", "ASC_3", "Ascending order, priority 3.")
            .value("DESC_3", "DESC_3", "Descending order, priority 3.")
            .value("ASC_4", "ASC_4", "Ascending order, priority 4.")
            .value("DESC_4", "DESC_4", "Descending order, priority 4.")
            .value("ASC_5", "ASC_5", "Ascending order, priority 5.")
            .value("DESC_5", "DESC_5", "Descending order, priority 5.")
            .value("ASC_6", "ASC_6", "Ascending order, priority 6.")
            .value("DESC_6", "DESC_6", "Descending order, priority 6.")
            .value("ASC_7", "ASC_7", "Ascending order, priority 7.")
            .value("DESC_7", "DESC_7", "Descending order, priority 7.")
            .value("ASC_8", "ASC_8", "Ascending order, priority 8.")
            .value("DESC_8", "DESC_8", "Descending order, priority 8.")
            .value("ASC_9", "ASC_9", "Ascending order, priority 9.")
            .value("DESC_9", "DESC_9", "Descending order, priority 9.")
            .build()
        )
        .build();
    /**
     * Default page number of entities returned in a single root field of a {@code Query}.
     */
    static final int DEFAULT_PAGE_NUMBER = 0;
    static final GraphQLArgument PAGE_NUMBER_ARGUMENT = newArgument()
            .name(PAGE_NUMBER)
            .description(format("Non-negative number indicating the 'page' of entities returned. Numbers < 0 is ignored. %s by default.", DEFAULT_PAGE_NUMBER))
            .type(GraphQLInt)
            .build();
    /**
     * Default maximum number of entities returned in a single root field of a {@code Query}.
     */
    static final int DEFAULT_PAGE_CAPACITY = 25;
    static final GraphQLArgument PAGE_CAPACITY_ARGUMENT = newArgument()
        .name(PAGE_CAPACITY)
        .description(format("Positive number to limit the maximum number of entities returned. Numbers <= 0 is ignored. %s by default.", DEFAULT_PAGE_CAPACITY))
        .type(GraphQLInt)
        .build();
    
    /**
     * Returns whether argument definition is related to query criteria.
     * 
     * @param argumentDefinition
     * @return
     */
    public static boolean isQueryArgument(final GraphQLArgument argumentDefinition) {
        return asList(LIKE, VALUE, FROM, TO).contains(argumentDefinition.getName());
    }
    
    /**
     * Creates GraphQL field definition for {@code entityType} and {@code property}.
     * Set of supported property types:
     * <ul>
     * <li>{@link String}</li>
     * <li>boolean</li>
     * <li>{@link Integer}</li>
     * <li>{@link Long}</li>
     * <li>{@link BigDecimal}</li>
     * <li>{@link Money}</li>
     * <li>entity</li>
     * <li>{@link Date}</li>
     * <li>{@link Hyperlink}</li>
     * <li>{@link Colour}</li>
     * </ul>
     * Returns {@link Optional#empty()} if the property type is not supported.
     * 
     * @param entityType
     * @param property
     * @return
     */
    public static Optional<GraphQLFieldDefinition> createGraphQLFieldDefinition(final Class<? extends AbstractEntity<?>> entityType, final String property) {
        return determineFieldType(entityType, property).map(typeAndArguments -> {
            return newFieldDefinition()
                .name(property)
                .description(
                    asList(
                        titleAndDescRepresentation(getTitleAndDesc(property, entityType)),
                        metaInformationFor(entityType, property)
                    ).stream()
                    .filter(s -> !isEmpty(s))
                    .collect(joining(NEWLINE + NEWLINE))) // split title+desc from metaInfo
                .type(typeAndArguments._1)
                .arguments(typeAndArguments._2)
                .build();
        });
    }
    
    /**
     * Concatenated [through en-dash] version of title and description for entity type with various representational improvements.
     * 
     * @param titleAndDesc
     * @return
     */
    public static String titleAndDescRepresentation(final Pair<String, String> titleAndDesc) {
        final String title = isEmpty(titleAndDesc.getKey()) ? titleAndDesc.getKey() : bold(titleAndDesc.getKey()); // 'title' in bold if not empty
        return isEmpty(titleAndDesc.getValue()) // no 'desc' -- only 'title' to be shown
            || equalsEx(titleAndDesc.getKey(),             titleAndDesc.getValue()) // 'title' equals to 'desc' -- only 'title' to be shown
            || equalsEx(titleAndDesc.getKey() + " entity", titleAndDesc.getValue()) // [title + ' entity'] equals to 'desc' -- only 'title' to be shown
            ? title : title + " \u2013 " + titleAndDesc.getValue(); // ['title' en-dash 'desc'] to be shown
    }

    /**
     * A string relevant entity property meta-information to be included into field description.
     * 
     * @param entityType
     * @param property
     * @return
     */
    private static String metaInformationFor(final Class<? extends AbstractEntity<?>> entityType, final String property) {
        return concat(
            asList(
                Calculated.class,
                CompositeKeyMember.class,
                CritOnly.class,
                DateOnly.class,
                Dependent.class,
                // @Ignore and @Invisible properties are excluded; no need to check these annotations
                // @IsProperty is not really interesting for integrator; all of properties have this annotation except both 'id' and 'version'
                //   expose its TODO @AssignBeforeSave, TODO @Length, TODO @PrecisionAndScale
                //   value(type)+linkProperty are internal implementation detail for collections; trailingZeros+displayAs are related to UI display representation
                // @MapTo is believed to be internal and should not be exposed
                ua.com.fielden.platform.entity.annotation.Optional.class,
                PersistentType.class, // this is only relevant for UTC dates that are represented as @PersistentType(userType = IUtcDateTimeType.class)
                Readonly.class,
                Required.class,
                ResultOnly.class,
                Secrete.class,
                SkipActivatableTracking.class,
                SkipEntityExistsValidation.class, // this annotation is believed to be of interest, mostly for skipActiveOnly situation, so TODO don't include if not skipActiveOnly
                TimeOnly.class,
                Unique.class,
                UpperCase.class,
                AfterChange.class,
                BeforeChange.class,
                Subtitles.class
            ).stream()
            .map(annotationType -> getPropertyAnnotationOptionally(annotationType, entityType, property))
            .flatMap(annotation -> annotation.map(Stream::of).orElseGet(Stream::empty))
            .map(FieldSchema::toString)
            .flatMap(str -> str.map(Stream::of).orElseGet(Stream::empty)),
            
            Stream.of(Required.class, Readonly.class).filter(type -> {
                if (KEY.equals(property)) {
                    return Required.class.equals(type)
                        || Readonly.class.equals(type) && isAnnotationPresentForClass(KeyReadonly.class, entityType);
                } else if (DESC.equals(property)) {
                    return Required.class.equals(type) && isAnnotationPresentForClass(DescRequired.class, entityType)
                        || Readonly.class.equals(type) && isAnnotationPresentForClass(DescReadonly.class, entityType);
                } else if (isPropertyAnnotationPresent(CompositeKeyMember.class, entityType, property)) {
                    return Required.class.equals(type) && !isPropertyAnnotationPresent(ua.com.fielden.platform.entity.annotation.Optional.class, entityType, property);
                } else {
                    return false;
                }
            }).map(type -> annotationTypeName(type))
            
            ).distinct().collect(joining(NEWLINE));
    }
    
    /**
     * Returns string representation for annotation type.
     * 
     * @param annotationType
     * @return
     */
    private static String annotationTypeName(final Class<? extends Annotation> annotationType) {
        return bold("@" + annotationType.getSimpleName());
    }
    
    /**
     * Returns string representation for annotation type taken from {@code annotation} instance.
     * 
     * @param annotation
     * @return
     */
    private static String annotationTypeName(final Annotation annotation) {
        return annotationTypeName(annotation.annotationType());
    }
    
    /**
     * Returns string representation for annotation.
     * 
     * @param annotation
     * @return
     */
    private static Optional<String> toString(final Annotation annotation) {
        final String typeName = annotationTypeName(annotation);
        final String str;
        if (annotation instanceof Calculated) {
            str = isValueDefault(annotation, "value") ? typeName : format("%s(%s)", typeName, ((Calculated) annotation).value());
        } else if (annotation instanceof CompositeKeyMember) {
            str = format("%s(%s)", typeName, ((CompositeKeyMember) annotation).value());
        } else if (annotation instanceof CritOnly) {
            str = format("%s(%s)", typeName, ((CritOnly) annotation).value()); // TODO mnemonics + excludeMissing? TODO precision + scale?
        } else if (annotation instanceof Dependent) {
            str = format("%s(%s)", typeName, stream(((Dependent) annotation).value()).collect(joining(SPACE_SEPARATOR)));
        } else if (annotation instanceof PersistentType) {
            str = IUtcDateTimeType.class.equals(((PersistentType) annotation).userType()) ? bold("@UTC") : null;
        } else if (annotation instanceof Required) {
            str = isValueDefault(annotation, "value") ? typeName : format("%s(%s)", typeName, ((Required) annotation).value());
        } else if (annotation instanceof SkipEntityExistsValidation) {
            final boolean skipActiveOnlyDefault = isValueDefault(annotation, "skipActiveOnly");
            final boolean skipNewDefault = isValueDefault(annotation, "skipNew");
            str = skipActiveOnlyDefault && skipNewDefault ? null : format("%s(%s)", typeName, !skipActiveOnlyDefault ? "active" : "new"); // TODO can both be true?
        } else if (annotation instanceof AfterChange) {
            final List<String> params = new ArrayList<>();
            final AfterChange afterChange = (AfterChange) annotation;
            params.add(afterChange.value().getSimpleName());
            addIfNonDefault(params, annotation);
            str = annotationWithParams(typeName, params);
        } else if (annotation instanceof BeforeChange) {
            final List<String> handlerStrs = new ArrayList<>();
            final BeforeChange beforeChange = (BeforeChange) annotation;
            final Handler[] handlers = beforeChange.value();
            Arrays.stream(handlers).forEach(handler -> {
                final List<String> params = new ArrayList<>();
                params.add(handler.value().getSimpleName());
                addIfNonDefault(params, handler);
                handlerStrs.add(annotationWithParams(annotationTypeName(handler), params, INDENT_STEP));
            });
            str = annotationWithParams(typeName, handlerStrs);
        } else if (annotation instanceof Subtitles) {
            final List<String> subtitleStrs = new ArrayList<>();
            final Subtitles subtitles = (Subtitles) annotation;
            final PathTitle[] pathTitles = subtitles.value();
            Arrays.stream(pathTitles).forEach(pathTitle -> {
                final List<String> params = new ArrayList<>();
                params.add("path=" + pathTitle.path());
                params.add("title=" + pathTitle.title());
                if (!isValueDefault(pathTitle, "desc")) {
                    params.add("desc=" + pathTitle.desc());
                }
                subtitleStrs.add(annotationTypeName(pathTitle) + "(" + params.stream().collect(joining(SPACE_SEPARATOR)) + ")");
            });
            str = annotationWithParams(typeName, subtitleStrs);
        } else if (annotation instanceof StrParam) {
            str = format("%s(%s)", typeName, "name=" + ((StrParam) annotation).name() + ", value=" + ((StrParam) annotation).value());
        } else if (annotation instanceof ClassParam) {
            str = format("%s(%s)", typeName, "name=" + ((ClassParam) annotation).name() + ", value=" + ((ClassParam) annotation).value().getSimpleName());
        } else if (annotation instanceof DateParam) {
            str = format("%s(%s)", typeName, "name=" + ((DateParam) annotation).name() + ", value=" + ((DateParam) annotation).value());
        } else if (annotation instanceof DateTimeParam) {
            str = format("%s(%s)", typeName, "name=" + ((DateTimeParam) annotation).name() + ", value=" + ((DateTimeParam) annotation).value());
        } else if (annotation instanceof DblParam) {
            str = format("%s(%s)", typeName, "name=" + ((DblParam) annotation).name() + ", value=" + ((DblParam) annotation).value());
        } else if (annotation instanceof EnumParam) {
            str = format("%s(%s)", typeName, "name=" + ((EnumParam) annotation).name() + ", value=" + ((EnumParam) annotation).value() + ", class=" + ((EnumParam) annotation).clazz().getSimpleName());
        } else if (annotation instanceof IntParam) {
            str = format("%s(%s)", typeName, "name=" + ((IntParam) annotation).name() + ", value=" + ((IntParam) annotation).value());
        } else if (annotation instanceof MoneyParam) {
            str = format("%s(%s)", typeName, "name=" + ((MoneyParam) annotation).name() + ", value=" + ((MoneyParam) annotation).value());
        } else {
            str = typeName;
        }
        return str == null ? empty() : of(str);
    }
    
    /**
     * Returns string representation for annotation with parameters.
     * 
     * @param typeName
     * @param params
     * @return
     */
    private static String annotationWithParams(final String typeName, final List<String> params) {
        return annotationWithParams(typeName, params, "");
    }
    
    /**
     * Returns string representation for annotation with parameters.
     * 
     * @param typeName
     * @param params
     * @param base -- base indentation for all params
     * @return
     */
    private static String annotationWithParams(final String typeName, final List<String> params, final String base) {
        final String newLineLeft = params.size() > 1 ? NEWLINE + base + INDENT_STEP : "";
        final String newLineRight = params.size() > 1 ? NEWLINE + base : "";
        return typeName + "(" + newLineLeft + params.stream().collect(joining(NEWLINE_SEPARATOR + base + INDENT_STEP)) + newLineRight +")";
    }
    
    /**
     * Wraps the string to be bold in Markdown (used by GraphiQL).
     * 
     * @param str
     * @return
     */
    public static String bold(final String str) {
        return "**" + str + "**";
    }
    
    /**
     * Adds params from {@code annotation} if they are not default-valued.
     * 
     * @param params
     * @param annotation
     */
    private static void addIfNonDefault(final List<String> params, final Annotation annotation) {
        addIfNonDefault(params, annotation, "non_ordinary");
        addIfNonDefault(params, annotation, "clazz");
        addIfNonDefault(params, annotation, "integer");
        addIfNonDefault(params, annotation, "str");
        addIfNonDefault(params, annotation, "dbl");
        addIfNonDefault(params, annotation, "date");
        addIfNonDefault(params, annotation, "date_time");
        addIfNonDefault(params, annotation, "money");
        addIfNonDefault(params, annotation, "enumeration");
    }
    
    /**
     * Adds param with concrete {@code methodName} from {@code annotation} if it is not default-valued.
     * 
     * @param params
     * @param annotation
     * @param methodName
     */
    private static void addIfNonDefault(final List<String> params, final Annotation annotation, final String methodName) {
        try {
            final Method declaredMethod = annotation.annotationType().getDeclaredMethod(methodName);
            final Annotation[] value = (Annotation[]) declaredMethod.invoke(annotation);
            if (!Arrays.equals((Object[]) declaredMethod.getDefaultValue(), value) && value != null) {
                params.addAll(Arrays.stream(value)
                    .map(FieldSchema::toString)
                    .flatMap(str -> str.map(Stream::of).orElseGet(Stream::empty))
                    .collect(toList())
                );
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Checks whether value for param with concrete {@code methodName} in {@code annotation} is equal to default one.
     * 
     * @param annotation
     * @param methodName
     * @return
     */
    private static boolean isValueDefault(final Annotation annotation, final String methodName) {
        try {
            final Method declaredMethod = annotation.annotationType().getDeclaredMethod(methodName);
            return equalsEx(declaredMethod.getDefaultValue(), declaredMethod.invoke(annotation));
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Determines GraphQL field type [+argument definitions] for {@code entityType} and {@code property}. Returns empty {@link Optional} if property is not supported / applicable.
     * <p>
     * This method takes into account collectional associations and creates {@link GraphQLList} wrapper around determined field type to be able to fetch list of
     * entities into such GraphQL fields.
     * 
     * @param entityType
     * @param property
     * @return
     */
    private static Optional<T2<GraphQLOutputType, List<GraphQLArgument>>> determineFieldType(final Class<? extends AbstractEntity<?>> entityType, final String property) {
        final Optional<T2<GraphQLOutputType, List<GraphQLArgument>>> nonCollectionalFieldType = determineFieldTypeNonCollectional(determinePropertyType(entityType, property));
        return isCollectional(determineClass(entityType, property, true, false))
            ? nonCollectionalFieldType.map(typeAndArguments -> t2(new GraphQLList(typeAndArguments._1), new ArrayList<>()))
            : nonCollectionalFieldType;
    }
    
    /**
     * Determines GraphQL field type for {@code propertyType}. Returns empty {@link Optional} if property is not supported / applicable.
     * <p>
     * See {@link #createGraphQLFieldDefinition(Class, String)} for the list of supported property types.
     * Note that abstract types derived from {@link AbstractEntity} are not supported; {@link PropertyDescriptor} and {@link AbstractUnionEntity} descendants too.
     * 
     * @param propertyType
     * @return
     */
    private static Optional<T2<GraphQLOutputType, List<GraphQLArgument>>> determineFieldTypeNonCollectional(final Class<?> propertyType) {
        if (isString(propertyType)) {
            return of(t2(GraphQLString, asList(LIKE_ARGUMENT, ORDER_ARGUMENT)));
        } else if (isBoolean(propertyType)) {
            return of(t2(GraphQLBoolean, asList(newArgument() // null-valued or non-existing argument in GraphQL query means entities with both true and false values in the property
                .name(VALUE)
                .description("Include entities with specified boolean value.")
                .type(GraphQLBoolean)
                .build(),
                ORDER_ARGUMENT
            )));
        } else if (Integer.class.isAssignableFrom(propertyType)) {
            return of(t2(GraphQLInt, createRangeArgumentsFor(GraphQLInt)));
        } else if (Long.class.isAssignableFrom(propertyType)) {
            // Even though we add here the support for Long values [-9,223,372,036,854,775,808; 9,223,372,036,854,775,807] = [-2^63; 2^63 - 1],
            // the actual support would be limited to              [    -9,007,199,254,740,992;     9,007,199,254,740,991] = [-2^53; 2^53 - 1];
            // This is because Javascript numbers, that are used in GraphiQL client, truncates higher numbers with zeros and performs weird rounding.
            return of(t2(GraphQLLong, createRangeArgumentsFor(GraphQLLong)));
        } else if (BigDecimal.class.isAssignableFrom(propertyType)) {
            return of(t2(GraphQLBigDecimal, createRangeArgumentsFor(GraphQLBigDecimal)));
        } else if (Money.class.isAssignableFrom(propertyType)) {
            return of(t2(GraphQLMoney, createRangeArgumentsFor(GraphQLMoney)));
        } else if (isDate(propertyType)) {
            return of(t2(GraphQLDate, createRangeArgumentsFor(GraphQLDate)));
        } else if (Hyperlink.class.isAssignableFrom(propertyType)) {
            return of(t2(GraphQLHyperlink, asList(ORDER_ARGUMENT)));
        } else if (Colour.class.isAssignableFrom(propertyType)) {
            return of(t2(GraphQLColour, asList(ORDER_ARGUMENT)));
        } else if (AbstractView.class == propertyType
            || PropertyDescriptor.class == propertyType
            || isAbstract(propertyType.getModifiers())) { // be careful with boolean.class because it has abstract modifier
            return empty();
        } else if (isUnionEntityType(propertyType)) {
            return of(t2(new GraphQLTypeReference(propertyType.getSimpleName()), asList()));
        } else if (isEntityType(propertyType)) {
            return of(t2(new GraphQLTypeReference(propertyType.getSimpleName()), asList(LIKE_ARGUMENT, ORDER_ARGUMENT)));
        } else {
            return empty();
        }
    }
    
    /**
     * Creates GraphQL [from; to] argument definitions for range input type.
     * 
     * @param inputType
     * @return
     */
    private static List<GraphQLArgument> createRangeArgumentsFor(final GraphQLInputType inputType) {
        return asList(
            newArgument()
            .name(FROM)
            .description("Include entities with property greater than (or equal to) specified value.")
            .type(inputType)
            .build(),
            
            newArgument()
            .name(TO)
            .description("Include entities with property less than (or equal to) specified value.")
            .type(inputType)
            .build(),
            
            ORDER_ARGUMENT
        );
    }
    
}