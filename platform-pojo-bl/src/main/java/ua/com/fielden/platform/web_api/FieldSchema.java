package ua.com.fielden.platform.web_api;

import static graphql.Scalars.GraphQLBigDecimal;
import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotationOptionally;
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
import static ua.com.fielden.platform.web_api.TgScalars.GraphQLColour;
import static ua.com.fielden.platform.web_api.TgScalars.GraphQLDate;
import static ua.com.fielden.platform.web_api.TgScalars.GraphQLHyperlink;
import static ua.com.fielden.platform.web_api.TgScalars.GraphQLMoney;

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
 * Contains utilities to convert TG entity properties to GraphQL query fields that reside under <code>Query.exampleEntityType</code> fields.
 * 
 * @author TG Team
 *
 */
public class FieldSchema {
    static final String LIKE = "like";
    static final String VALUE = "value";
    static final String FROM = "from";
    static final String TO = "to";

    /**
     * Creates GraphQL field definition for <code>entityType</code> and <code>property</code>.
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
                .description(titleAndDescRepresentation(entityType, property) + metaInformationFor(entityType, property))
                .type(typeAndArguments._1)
                .arguments(typeAndArguments._2)
                .build();
        });
    }
    
    private static String titleAndDescRepresentation(final Class<? extends AbstractEntity<?>> entityType, final String property) {
        final Pair<String, String> titleAndDesc = getTitleAndDesc(property, entityType);
        return equalsEx(titleAndDesc.getKey(), titleAndDesc.getValue()) ? titleAndDesc.getKey() : titleAndDesc.getKey() + " &ndash; " + titleAndDesc.getValue();
    }
    
    private static String metaInformationFor(final Class<? extends AbstractEntity<?>> entityType, final String property) {
        // TODO support @DescReadonly / @DescRequired as annotations added to entity type, not property
        // TODO support @KeyReadonly                  as annotations added to entity type, not property
        return Arrays.asList(
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
                Secrete.class, // TODO should it be exposed?
                SkipActivatableTracking.class,
                SkipEntityExistsValidation.class, // this annotation is believed to be of interest, mostly for skipActiveOnly situation, so TODO don't include if not skipActiveOnly
                TimeOnly.class,
                Unique.class,
                UpperCase.class,
                // TODO @WhenNullMessage is not used?
                AfterChange.class,
                BeforeChange.class,
                Subtitles.class
            ).stream()
            .map(annotationType -> getPropertyAnnotationOptionally(annotationType, entityType, property))
            .flatMap(annotation -> annotation.isPresent() ? Stream.of(annotation.get()) : Stream.empty())
            .map(FieldSchema::toString)
            .flatMap(str -> str.isPresent() ? Stream.of(str.get()) : Stream.empty())
            .collect(joining("  \n", "  \n  \n", "")); // GraphiQL uses Markdown -- way to achieve new lines
    }
    
    private static Optional<String> toString(final Annotation annotation) {
        final Class<? extends Annotation> type = annotation.annotationType();
        final String typeName = type.getSimpleName();
        final String str;
        if (annotation instanceof Calculated) {
            str = isValueDefault(annotation, "value") ? typeName : format("%s(%s)", typeName, ((Calculated) annotation).value());
        } else if (annotation instanceof CompositeKeyMember) {
            str = format("%s(%s)", typeName, ((CompositeKeyMember) annotation).value());
        } else if (annotation instanceof CritOnly) {
            str = format("%s(%s)", typeName, ((CritOnly) annotation).value()); // TODO mnemonics + excludeMissing? TODO precision + scale?
        } else if (annotation instanceof Dependent) {
            str = format("%s(%s)", typeName, stream(((Dependent) annotation).value()).collect(joining(", ")));
        } else if (annotation instanceof PersistentType) {
            str = IUtcDateTimeType.class.equals(((PersistentType) annotation).userType()) ? "UTC" : null;
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
            final String newLineLeft = params.size() > 1 ? "  \n&nbsp;&nbsp;" : "";
            final String newLineRight = params.size() > 1 ? "  \n" : "";
            str = format("%s(%s%s%s)", typeName, newLineLeft, params.stream().collect(joining(",  \n&nbsp;&nbsp;")), newLineRight);
        } else if (annotation instanceof BeforeChange) {
            final List<String> handlerStrs = new ArrayList<>();
            final BeforeChange beforeChange = (BeforeChange) annotation;
            final Handler[] handlers = beforeChange.value();
            Arrays.stream(handlers).forEach(handler -> {
                final List<String> params = new ArrayList<>();
                params.add(handler.value().getSimpleName());
                addIfNonDefault(params, handler);
                final String newLineLeft = params.size() > 1 ? "  \n&nbsp;&nbsp;&nbsp;&nbsp;" : "";
                final String newLineRight = params.size() > 1 ? "  \n&nbsp;&nbsp;" : "";
                handlerStrs.add("@Handler(" + newLineLeft + params.stream().collect(joining(",  \n&nbsp;&nbsp;&nbsp;&nbsp;")) + newLineRight +")");
            });
            final String newLineLeft = handlerStrs.size() > 1 ? "  \n&nbsp;&nbsp;" : "";
            final String newLineRight = handlerStrs.size() > 1 ? "  \n" : "";
            str = format("%s(%s%s%s)", typeName, newLineLeft, handlerStrs.stream().collect(joining(",  \n&nbsp;&nbsp;")), newLineRight);
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
                subtitleStrs.add("@PathTitle(" + params.stream().collect(joining(", ")) + ")");
            });
            final String newLineLeft = subtitleStrs.size() > 1 ? "  \n&nbsp;&nbsp;" : "";
            final String newLineRight = subtitleStrs.size() > 1 ? "  \n" : "";
            str = format("%s(%s%s%s)", typeName, newLineLeft, subtitleStrs.stream().collect(joining(",  \n&nbsp;&nbsp;")), newLineRight);
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
        return str == null ? empty() : of("@" + str);
    }
    
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
    
    private static void addIfNonDefault(final List<String> params, final Annotation annotation, final String methodName) {
        try {
            final Method declaredMethod = annotation.annotationType().getDeclaredMethod(methodName);
            final Annotation[] value = (Annotation[]) declaredMethod.invoke(annotation);
            if (!Arrays.equals((Object[])declaredMethod.getDefaultValue(), value) && value != null) {
                params.addAll(Arrays.stream(value).map(FieldSchema::toString).flatMap(str -> str.isPresent() ? Stream.of(str.get()) : Stream.empty()).collect(toList()));
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    private static boolean isValueDefault(final Annotation annotation, final String methodName) {
        try {
            final Method declaredMethod = annotation.annotationType().getDeclaredMethod(methodName);
            return equalsEx(declaredMethod.getDefaultValue(), declaredMethod.invoke(annotation));
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Determines GraphQL field type for <code>entityType</code> and <code>property</code>.
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
     * Determines GraphQL field type for <code>propertyType</code>.
     * <p>
     * See {@link #createGraphQLFieldDefinition(Class, String)} for the list of supported property types.
     * Note that abstract types derived from {@link AbstractEntity} are not supported; {@link PropertyDescriptor} and {@link AbstractUnionEntity} descendants too.
     * 
     * @param propertyType
     * @return
     */
    private static Optional<T2<GraphQLOutputType, List<GraphQLArgument>>> determineFieldTypeNonCollectional(final Class<?> propertyType) {
        if (isString(propertyType)) {
            return of(t2(GraphQLString, asList(newArgument()
                .name(LIKE)
                .description("Include entities with specified string value pattern with % as a wildcard.")
                .type(GraphQLString)
                .build()
            )));
        } else if (isBoolean(propertyType)) {
            return of(t2(GraphQLBoolean, asList(newArgument() // null-valued or non-existing argument in GraphQL query means entities with both true and false values in the property
                .name(VALUE)
                .description("Include entities with specified boolean value.")
                .type(GraphQLBoolean)
                .build()
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
            return of(t2(GraphQLHyperlink, asList()));
        } else if (Colour.class.isAssignableFrom(propertyType)) {
            return of(t2(GraphQLColour, asList()));
        } else if (AbstractView.class == propertyType
            || PropertyDescriptor.class == propertyType
            || AbstractUnionEntity.class.isAssignableFrom(propertyType) // not supported yet
            || isAbstract(propertyType.getModifiers())) { // be careful with boolean.class because it has abstract modifier
            return empty();
        } else if (isEntityType(propertyType)) {
            return of(t2(new GraphQLTypeReference(propertyType.getSimpleName()), asList()));
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
            .build()
        );
    }
    
}