package ua.com.fielden.platform.web_api;

import graphql.schema.*;
import graphql.schema.visibility.GraphqlFieldVisibility;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Streams.stream;
import static graphql.Directives.OneOfDirective;
import static graphql.Scalars.*;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static java.lang.reflect.Modifier.isAbstract;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.concat;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determineClass;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitle;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;
import static ua.com.fielden.platform.web_api.FieldVisibility.visibilityPredicate;
import static ua.com.fielden.platform.web_api.GraphQLCommon.*;
import static ua.com.fielden.platform.web_api.GraphQLScalars.*;

/// GraphQL definitions that add the ability to express fluent filtering conditions on entity types.
///
/// Use [#enhanceSchema] to enhance an existing schema.
///
/// For each entity type `E`, the following are defined:
/// * Input object type `E_Cond`, a starting point to express conditions over `E`.
/// * Root field for `E` is redefined by adding argument `where: E_Cond`.
/// * Root field for `E_Agg` is redefined by adding argument `where: E_Cond`.
///
/// Field visibility is extended to apply to entity properties used in filtering conditions.
/// If a user does not have access to a property, then that property cannot be filtered on.
/// If a user does not have access to an entity type, then none of its properties can be filtered on.
/// These rules apply at any level where a property is accessed (either in a root entity graph or in a sub-graph).
///
@Singleton
public class FluentConditions {

    public static final String
            STRING_COND = "String_Cond",
            BOOLEAN_COND = "Boolean_Cond",
            INT_COND = "Int_Cond",
            LONG_COND = "Long_Cond",
            BIG_DECIMAL_COND = "BigDecimal_Cond",
            MONEY_COND = "Money_Cond",
            DATE_COND = "Date_Cond",
            HYPERLINK_COND = "Hyperlink_Cond",
            COLOUR_COND = "Colour_Cond";

    public static final String WHERE = "where", COND = "cond", AND = "and", OR = "or", NOT = "not", IS_NULL = "isNull";

    /// Operators of value condition types.
    /// [#IS_NULL] is also an operator.
    ///
    public static final String
            EQ = "eq", NE = "ne",
            LT = "lt", LE = "le", GT = "gt", GE = "ge",
            IN = "in", NOT_IN = "notIn",
            LIKE = "like", NOT_LIKE = "notLike", I_LIKE = "iLike", NOT_I_LIKE = "notILike";

    private final IApplicationDomainProvider appDomainProvider;
    private final IAuthorisationModel authorisationModel;
    private final ISecurityTokenProvider securityTokenProvider;

    @Inject
    protected FluentConditions(
            final IApplicationDomainProvider appDomainProvider,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider)
    {
        this.appDomainProvider = appDomainProvider;
        this.authorisationModel = authorisationModel;
        this.securityTokenProvider = securityTokenProvider;
    }

    public GraphQLSchema enhanceSchema(final GraphQLSchema schema) {
        final var entityTypes = streamVisibleTypes(appDomainProvider)
                .filter(ty -> hasEntityCondType(ty, schema))
                .toList();

        final var queryType = schema.getQueryType();
        return schema.transform(builder -> builder
                .additionalTypes(graphQlValueCondTypes())
                .additionalTypes(entityTypes.stream()
                                         .flatMap(ty -> Stream.of(mkGraphQlEntityCondType(ty), mkGraphQlEntityCondCondType(ty, schema)))
                                         .collect(toSet()))
                .codeRegistry(enhanceCodeRegistry(schema.getCodeRegistry()))
                .query(enhanceQueryType(queryType, entityTypes)));
    }

    public static boolean hasEntityCondType(final Class<? extends AbstractEntity<?>> entityType, final GraphQLSchema schema) {
        return schema.getType(graphQlTypeNameForEntity(entityType)) != null;
    }

    public static boolean isEntityCondType(final GraphQLType type) {
        return type instanceof GraphQLNamedInputType it && it.getName().endsWith("_Cond");
    }

    /// Value condition types, one for each value type that can be the type of a filterable property.
    ///
    private static Set<GraphQLInputObjectType> graphQlValueCondTypes() {
        return Set.of(graphQlStringCondType(),
                      graphQlBooleanCondType(),
                      graphQlNumericCondType(INT_COND, GraphQLInt),
                      graphQlNumericCondType(LONG_COND, GraphQLLong),
                      graphQlNumericCondType(BIG_DECIMAL_COND, GraphQLBigDecimal),
                      graphQlNumericCondType(MONEY_COND, GraphQLMoney),
                      graphQlDateCondType(),
                      graphQlNullOnlyCondType(HYPERLINK_COND, GraphQLHyperlink),
                      graphQlNullOnlyCondType(COLOUR_COND, GraphQLColour));
    }

    private static GraphQLInputObjectType graphQlStringCondType() {
        return newInputObject()
                .name(STRING_COND)
                .description("Conditions on a value of type %s.".formatted(GraphQLString.getName()))
                .fields(equalityOps(GraphQLString))
                .fields(membershipOps(GraphQLString))
                .fields(List.of(patternOp(LIKE, "The value matches the pattern; `*` matches any sequence of characters."),
                                patternOp(NOT_LIKE, "The value does not match the pattern; `*` matches any sequence of characters."),
                                patternOp(I_LIKE, "The value matches the pattern, ignoring case; `*` matches any sequence of characters."),
                                patternOp(NOT_I_LIKE, "The value does not match the pattern, ignoring case; `*` matches any sequence of characters.")))
                .field(isNullOp())
                .build();
    }

    /// `Boolean` properties are primitive and always assigned, hence there is no `isNull`.
    ///
    private static GraphQLInputObjectType graphQlBooleanCondType() {
        return newInputObject()
                .name(BOOLEAN_COND)
                .description("Conditions on a value of type %s.".formatted(GraphQLBoolean.getName()))
                .field(op(EQ, GraphQLBoolean, "The value equals the given one."))
                .build();
    }

    /// A condition type for an ordered value type that also supports membership tests: `Int`, `Long`, `BigDecimal`, `Money`.
    ///
    /// `Money` values are compared by amount.
    ///
    private static GraphQLInputObjectType graphQlNumericCondType(final String name, final GraphQLScalarType scalar) {
        return newInputObject()
                .name(name)
                .description("Conditions on a value of type %s.".formatted(scalar.getName()))
                .fields(equalityOps(scalar))
                .fields(comparisonOps(scalar))
                .fields(membershipOps(scalar))
                .field(isNullOp())
                .build();
    }

    /// Date-time values are constrained by equality and ranges.
    ///
    private static GraphQLInputObjectType graphQlDateCondType() {
        return newInputObject()
                .name(DATE_COND)
                .description("Conditions on a value of type %s.".formatted(GraphQLDate.getName()))
                .fields(equalityOps(GraphQLDate))
                .fields(comparisonOps(GraphQLDate))
                .field(isNullOp())
                .build();
    }

    /// A condition type for a value type whose scalar has no input coercion ([Hyperlink], [Colour]): only `isNull` is supported.
    ///
    private static GraphQLInputObjectType graphQlNullOnlyCondType(final String name, final GraphQLScalarType scalar) {
        return newInputObject()
                .name(name)
                .description("Conditions on a value of type %s.".formatted(scalar.getName()))
                .field(isNullOp())
                .build();
    }

    private static List<GraphQLInputObjectField> equalityOps(final GraphQLScalarType scalar) {
        return List.of(op(EQ, scalar, "The value equals the given one."),
                       op(NE, scalar, "The value does not equal the given one."));
    }

    private static List<GraphQLInputObjectField> comparisonOps(final GraphQLScalarType scalar) {
        return List.of(op(LT, scalar, "The value is less than the given one."),
                       op(LE, scalar, "The value is less than or equal to the given one."),
                       op(GT, scalar, "The value is greater than the given one."),
                       op(GE, scalar, "The value is greater than or equal to the given one."));
    }

    /// An empty list is an error for both `in` and `notIn`.
    ///
    private static List<GraphQLInputObjectField> membershipOps(final GraphQLScalarType scalar) {
        final var listType = new GraphQLList(new GraphQLNonNull(scalar));
        return List.of(op(IN, listType, "The value is one of the given ones. The list must not be empty."),
                       op(NOT_IN, listType, "The value is none of the given ones. The list must not be empty."));
    }

    private static GraphQLInputObjectField patternOp(final String name, final String description) {
        return op(name, GraphQLString, description);
    }

    private static GraphQLInputObjectField isNullOp() {
        return op(IS_NULL, GraphQLBoolean, "`true` means the value is unassigned, `false` means it is assigned.");
    }

    private static GraphQLInputObjectField op(final String name, final GraphQLInputType type, final String desc) {
        return newInputObjectField()
                .name(name)
                .description(desc)
                .type(type)
                .build();
    }

    private GraphQLObjectType enhanceQueryType(
            final GraphQLObjectType queryType,
            final List<Class<? extends AbstractEntity<?>>> entityTypes)
    {
        return queryType.transform(builder -> {
            // Enhance root entity fields with argument `where`.
            entityTypes.forEach(ty -> {
                final var fieldDef = queryType.getFieldDefinition(rootFieldName(ty));
                if (fieldDef != null) {
                    builder.field(fieldDef.transform(fldBld -> {
                        fldBld.argument(newArgument()
                                        .name(WHERE)
                                        .description("Filtering conditions.")
                                        .type(new GraphQLTypeReference(graphQlTypeNameForCondEntity(ty)))
                                        .build());
                    }));
                }
            });
            // Enhance root aggregation entity fields with argument `where`.
            entityTypes.forEach(ty -> {
                final var fieldDef = queryType.getFieldDefinition(rootFieldNameForAggEntity(ty));
                if (fieldDef != null) {
                    builder.field(fieldDef.transform(fldBld -> {
                        fldBld.argument(newArgument()
                                        .name(WHERE)
                                        .description("Filtering conditions.")
                                        .type(new GraphQLTypeReference(graphQlTypeNameForCondEntity(ty)))
                                        .build());
                    }));
                }
            });
        });
    }

    /// Defines a GraphQL input object type representing an entity condition type for `entityType`.
    /// E.g., `User_Cond` for entity type `User`.
    ///
    private GraphQLInputObjectType mkGraphQlEntityCondType(final Class<? extends AbstractEntity<?>> entityType) {
        final var graphQlEntityTypeRef = new GraphQLTypeReference(graphQlTypeNameForCondEntityCond(entityType));
        final var condEntityTypeName = graphQlTypeNameForCondEntity(entityType);
        final var graphQlEntityCondTypeRef = new GraphQLTypeReference(condEntityTypeName);
        final var fields = List.of(
                newInputObjectField()
                        .name(COND)
                        .description("Property conditions.")
                        .type(graphQlEntityTypeRef)
                        .build(),
                newInputObjectField()
                        .name(AND)
                        .description("List of conditions combined with AND.")
                        .type(new GraphQLList(graphQlEntityCondTypeRef))
                        .build(),
                newInputObjectField()
                        .name(OR)
                        .description("List of conditions combined with OR.")
                        .type(new GraphQLList(graphQlEntityCondTypeRef))
                        .build(),
                newInputObjectField()
                        .name(NOT)
                        .description("Negated condition.")
                        .type(graphQlEntityCondTypeRef)
                        .build(),
                newInputObjectField()
                        .name(IS_NULL)
                        .description("Meaningful only where the condition applies to an entity-typed property: `true` means the property is unassigned, `false` means it is assigned")
                        .type(GraphQLBoolean)
                        .build());
        return newInputObject()
                .name(condEntityTypeName)
                .description("Filtering conditions for %s.".formatted(getEntityTitle(entityType)))
                .withDirective(OneOfDirective)
                .fields(fields)
                .build();
    }

    /// Defines a GraphQL input object type for field `cond` of the entity condition type associated with `entityType`
    /// (e.g., `User_Cond_cond`).
    /// This type provides access to filterable properties of an entity type.
    ///
    private GraphQLInputObjectType mkGraphQlEntityCondCondType(final Class<? extends AbstractEntity<?>> entityType, final GraphQLSchema schema) {
        final var graphQlEntityTypeName = graphQlTypeNameForEntity(entityType);
        final var graphQlEntityType = schema.getObjectType(graphQlEntityTypeName);
        if (graphQlEntityType == null) {
            throw new WebApiException("GraphQL schema does not contain type [%s].".formatted(graphQlEntityTypeName));
        }
        final var fields = propertiesForGraphQlFields(entityType)
                .stream()
                .filter(prop -> graphQlEntityType.getField(prop.getName()) != null)
                // TODO Crit-only properties are to be supported.
                .filter(prop -> !isCritOnly(entityType, prop.getName()))
                .map(prop -> {
                    final var type = mkGraphQlEntityPropCondType(entityType, prop.getName(), schema);
                    if (type == null) {
                        return null;
                    }
                    return newInputObjectField()
                            .name(prop.getName())
                            .type(type)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
        return newInputObject()
                .name(graphQlTypeNameForCondEntityCond(entityType))
                .fields(fields)
                .build();
    }

    /// Determines the condition type for `property` of `entityType`.
    /// The dispatch on the property type follows [FieldSchema#determineFieldTypeNonCollectional(Class)], which determines the corresponding GraphQL field type.
    ///
    /// * A property of value type `S` is constrained by value condition type `S_Cond`.
    /// * A property of entity or union type `R` is constrained by `R_Cond`, provided that this condition type is defined in the schema.
    ///
    /// Returns null if the condition type cannot be determined.
    ///
    private @Nullable GraphQLInputType mkGraphQlEntityPropCondType(
            final Class<? extends AbstractEntity<?>> entityType,
            final CharSequence property,
            final GraphQLSchema schema)
    {
        if (isCollectional(determineClass(entityType, property.toString(), true, false))) {
            return null;
        }
        final var propType = determinePropertyType(entityType, property);
        if (isString(propType) || isDynamicEntityKey(propType)) {
            return new GraphQLTypeReference(STRING_COND);
        }
        else if (isBoolean(propType)) {
            return new GraphQLTypeReference(BOOLEAN_COND);
        }
        else if (Integer.class.isAssignableFrom(propType)) {
            return new GraphQLTypeReference(INT_COND);
        }
        else if (Long.class.isAssignableFrom(propType)) {
            return new GraphQLTypeReference(LONG_COND);
        }
        else if (BigDecimal.class.isAssignableFrom(propType)) {
            return new GraphQLTypeReference(BIG_DECIMAL_COND);
        }
        else if (Money.class.isAssignableFrom(propType)) {
            return new GraphQLTypeReference(MONEY_COND);
        }
        else if (isDate(propType)) {
            return new GraphQLTypeReference(DATE_COND);
        }
        else if (Hyperlink.class.isAssignableFrom(propType)) {
            return new GraphQLTypeReference(HYPERLINK_COND);
        }
        else if (Colour.class.isAssignableFrom(propType)) {
            return new GraphQLTypeReference(COLOUR_COND);
        }
        else if (!isAbstract(propType.getModifiers()) && isEntityType(propType)) {
            final var refType = (Class<? extends AbstractEntity<?>>) propType;
            return hasEntityCondType(refType, schema)
                    ? new GraphQLTypeReference(graphQlTypeNameForCondEntity(refType))
                    : null;
        }
        else {
            return null;
        }
    }

    private GraphQLCodeRegistry enhanceCodeRegistry(final GraphQLCodeRegistry codeRegistry) {
        return codeRegistry.transform(builder -> builder
                .fieldVisibility(new FieldVisibility(codeRegistry.getFieldVisibility(), streamVisibleTypes(appDomainProvider).toList())));
    }

    /// Restricts access to fields of `E_Cond_cond` types whose corresponding properties cannot be read by the user.
    /// Effectively, properties that cannot be read also cannot be used in filtering conditions.
    ///
    private class FieldVisibility implements GraphqlFieldVisibility {

        private final GraphqlFieldVisibility base;
        private final Map<String, Class<? extends AbstractEntity<?>>> domainTypes;

        public FieldVisibility(
                final GraphqlFieldVisibility base,
                final Iterable<Class<? extends AbstractEntity<?>>> domainTypes)
        {
            this.base = base;
            this.domainTypes = stream(domainTypes).collect(toMap(Class::getSimpleName, Function.identity()));
        }

        @Override
        public List<GraphQLFieldDefinition> getFieldDefinitions(final GraphQLFieldsContainer fieldsContainer) {
            return base.getFieldDefinitions(fieldsContainer);
        }

        @Override
        public GraphQLFieldDefinition getFieldDefinition(final GraphQLFieldsContainer fieldsContainer, final String fieldName) {
            return base.getFieldDefinition(fieldsContainer, fieldName);
        }

        @Override
        public List<GraphQLInputObjectField> getFieldDefinitions(final GraphQLInputFieldsContainer fieldsContainer) {
            return entityTypeNameFromGraphQlCondEntityCondType(fieldsContainer.getName())
                    .map(entitySimpleName -> {
                        final var entityType = domainTypes.get(entitySimpleName);
                        if (entityType == null) {
                            throw new WebApiException("Unrecognised entity type: %s.".formatted(entitySimpleName));
                        }
                        final var isVisible = visibilityPredicate(entityType, authorisationModel, securityTokenProvider);
                        return fieldsContainer.getFieldDefinitions()
                                .stream()
                                .filter(field -> isVisible.test(field.getName()))
                                .toList();
                    })
                    .orElseGet(() -> base.getFieldDefinitions(fieldsContainer));
        }

        @Override
        public GraphQLInputObjectField getFieldDefinition(final GraphQLInputFieldsContainer fieldsContainer, final String fieldName) {
            return getFieldDefinitions(fieldsContainer).stream()
                    .filter(def -> Objects.equals(fieldName, def.getName()))
                    .findAny()
                    .orElse(null);
        }

    }

    /// A visitor of entity condition objects.
    ///
    interface EntityCondVisitor<R> {

        /// Visits a root entity condition object provided as a value to field `where`.
        ///
        /// E.g., `workOrder (where: object)`.
        ///
        default R rootEntityCond(Map<String, Object> object) {
            return entityCond(object, "");
        }

        /// Combines multiple results.
        /// Defines the combination strategy for conditions expressed on `E_Cond_cond` types (i.e., conditions on properties).
        /// This method also defines the identity value, which should be returned when `rs` is empty.
        ///
        R combine(Stream<R> rs);

        /// Visits an entity condition object provided as a value to an entity-typed field of an `E_Cond_cond` type or to `where` (root case).
        ///
        /// Example (root case):
        /// ```
        /// workOrder (where: object)
        /// ==>
        /// entityCond(object, "")
        /// ```
        ///
        /// Example (entiy-typed field):
        /// ```
        /// workOrder (where: {
        ///    cond: { technician: object }
        /// })
        /// ==>
        /// entityCond(object, "cond.technician")
        /// ```
        ///
        R entityCond(Map<String, Object> object, CharSequence path);

        /// Called on a list of entity condition objects provided as a value to `E_Cond.or`.
        ///
        /// Example:
        /// ```
        /// workOrder (where: {
        ///    or: [{cond: ...}, {cond: ...}]
        /// })
        /// ==>
        /// or([{cond: ...}, {cond: ...}], "or")
        /// ```
        ///
        R or(List<Map<String, Object>> object, CharSequence path);

        /// Called on a list of entity condition objects provided as a value to `E_Cond.and`.
        ///
        /// Example:
        /// ```
        /// workOrder (where: {
        ///    and: [{cond: ...}, {cond: ...}]
        /// })
        /// ==>
        /// and([{cond: ...}, {cond: ...}], "and")
        /// ```
        ///
        R and(List<Map<String, Object>> object, CharSequence path);

        /// Called on an entity condition object provided as a value to `E_Cond.not`.
        ///
        /// Example:
        /// ```
        /// workOrder (where: {
        ///    not: {cond: ...}
        /// })
        /// ==>
        /// not({cond: ...}, "not")
        /// ```
        ///
        R not(Map<String, Object> object, CharSequence path);

        /// Called on a boolean provided as a value to `E_Cond.isNull`.
        ///
        /// Example:
        /// ```
        /// workOrder (where: {
        ///    cond: {technician: {isNull: true}}
        /// })
        /// ==>
        /// not(true, "cond.technician.isNull")
        /// ```
        ///
        R isNull(boolean value, CharSequence path);

        /// Called on an input object provided as a value to `E_Cond.cond`.
        ///
        /// Example:
        /// ```
        /// workOrder (where: {
        ///     cond: object
        /// })
        /// ==>
        /// fieldCond(object, "cond")
        /// ```
        ///
        R fieldCond(Map<String, Object> object, CharSequence path);

        /// Called on an input object provided as a value to field `propName` of `E_Cond_cond`.
        ///
        /// Example:
        /// ```
        /// workOrer (where: {
        ///     cond: {technician: {isNull: true}}
        /// })
        /// ==>
        /// prop("technician", {isNull: true}, Technician_Cond, "cond")
        /// ```
        ///
        /// @param type  The GraphQL type of `object`
        ///
        R prop(CharSequence propName, Object object, GraphQLInputType type, CharSequence prefix);

    }

    /// Base visitor class that implements [#entityCond], [#fieldCond], and [#prop].
    ///
    /// Method [#prop] dispatches to one of the value-typed methods, such as [#stringCond].
    ///
    /// To use this visitor, instantiate it with an entity condition type representing the "root type" (i.e., the type of `where`).
    /// Then, call [#rootEntityCond] on an object of that type (the value provided to `where`).
    ///
    static abstract class AbstractEntityCondVisitor<R> implements EntityCondVisitor<R> {

        private final GraphQLInputObjectType rootCondType;

        /// @param rootCondType  root entity condition type whose object will be processed by this visitor
        ///
        protected AbstractEntityCondVisitor(final GraphQLInputObjectType rootCondType) {
            this.rootCondType = rootCondType;
        }

        /// Root entity condition type.
        ///
        protected GraphQLInputObjectType rootCondType() {
            return rootCondType;
        }

        @Override
        public R entityCond(final Map<String, Object> object, final CharSequence path) {
            // All E_Cond types are @oneof, so at most one field must be present.
            final var cond = (Map<String, Object>) object.get(COND);
            if (cond != null) {
                return fieldCond(cond, mkPath(path, COND));
            }
            final var and = (List<Map<String, Object>>) object.get(AND);
            if (and != null) {
                return and(and, mkPath(path, AND));
            }
            final var or = (List<Map<String, Object>>) object.get(OR);
            if (or != null) {
                return or(or, mkPath(path, OR));
            }
            final var not = (Map<String, Object>) object.get(NOT);
            if (not != null) {
                return not(not, mkPath(path, NOT));
            }
            final var isNull = (Boolean) object.get(IS_NULL);
            if (isNull != null) {
                return isNull(isNull, mkPath(path, IS_NULL));
            }
            throw new WebApiException("No recognised fields are present in an E_cond object: %s".formatted(object));
        }

        @Override
        public R fieldCond(final Map<String, Object> object, final CharSequence path) {
            return combine(object.entrySet().stream().map(ent -> prop(ent.getKey(), ent.getValue(), typeOf(rootCondType(), mkPath(path, ent.getKey())), path)));
        }

        /// Dispatches the condition on property `propName` by its condition type `type`.
        ///
        /// * A value condition type is visited by the corresponding value condition method.
        /// * An entity condition type (the type of an entity-typed property) is visited by [#entityCond(Map, CharSequence)],
        ///   which makes the nested condition apply to the referenced entity.
        /// * An explicit `null` imposes no condition, and is visited as an empty combination.
        ///
        @Override
        public R prop(
                final CharSequence propName,
                final Object object,
                final GraphQLInputType type,
                final CharSequence prefix)
        {
            if (object == null) {
                return combine(Stream.empty());
            }

            final var path = mkPath(prefix, propName);
            if (!(type instanceof GraphQLInputObjectType condType)) {
                throw new WebApiException("Unrecognised type [%s] at [%s].".formatted(type, path));
            }

            final var condObject = (Map<String, Object>) object;
            return switch (condType.getName()) {
                case STRING_COND -> stringCond(condObject, path);
                case BOOLEAN_COND -> booleanCond(condObject, path);
                case INT_COND -> intCond(condObject, path);
                case LONG_COND -> longCond(condObject, path);
                case BIG_DECIMAL_COND -> bigDecimalCond(condObject, path);
                case MONEY_COND -> moneyCond(condObject, path);
                case DATE_COND -> dateCond(condObject, path);
                case HYPERLINK_COND -> hyperlinkCond(condObject, path);
                case COLOUR_COND -> colourCond(condObject, path);
                case String _ when isEntityCondType(condType) -> entityCond(condObject, path);
                default -> throw new WebApiException("Unrecognised type [%s] at [%s].".formatted(condType.getName(), path));
            };
        }

        abstract R stringCond(Map<String, Object> object, CharSequence path);

        abstract R booleanCond(Map<String, Object> object, CharSequence path);

        abstract R intCond(Map<String, Object> object, CharSequence path);

        abstract R longCond(Map<String, Object> object, CharSequence path);

        abstract R bigDecimalCond(Map<String, Object> object, CharSequence path);

        abstract R moneyCond(Map<String, Object> object, CharSequence path);

        abstract R dateCond(Map<String, Object> object, CharSequence path);

        abstract R hyperlinkCond(Map<String, Object> object, CharSequence path);

        abstract R colourCond(Map<String, Object> object, CharSequence path);

        /// Constructs a path from provided elements.
        /// Empty elements are omitted.
        ///
        protected String mkPath(final CharSequence first, final CharSequence... rest) {
            return concat(Stream.of(first), Arrays.stream(rest)).filter(s -> !s.isEmpty()).collect(joining("."));
        }

        /// Returns the type of the object located at `path` starting at `rootType`.
        ///
        /// ```
        /// typeOf(WorkOrder_Cond, "") ==> WorkOrder_Cond
        /// typeOf(WorkOrder_Cond, "cond") ==> WorkOrder_Cond_cond
        /// typeOf(WorkOrder_Cond, "and") ==> [WorkOrder_Cond]
        /// typeOf(WorkOrder_Cond, "and.cond.technician.isNull") ==> Boolean
        /// ```
        ///
        protected GraphQLInputType typeOf(final GraphQLInputObjectType rootType, final CharSequence path) {
            if (path.isEmpty()) {
                return rootType;
            }
            return typedPath(rootType, path).last().map((type, elt) -> getField(type, elt).getType());
        }

        /// Maps a path starting at `rootType` into a corresponding property path by retaining only those elements that designate entity properties.
        ///
        /// [#rootCondType()] can be used to access the root entity condition type, and use it as an argument for `rootType`.
        ///
        /// ```
        /// toPropertyPath(WorkOrder_Cond, "cond.technician.isNull")
        /// ==>
        /// "technician"
        ///
        /// toPropertyPath(x, "")
        /// ==>
        /// ""
        /// ```
        ///
        protected String toPropertyPath(final GraphQLInputObjectType rootType, final CharSequence path) {
            return typedPath(rootType, path)
                    .filter(t2 -> isCondEntityCondType(t2._1()))
                    .map(T2::_2)
                    .collect(joining("."));
        }

        /// Given a path starting at `rootType`, returns a list of pairs `(declaringType, prop)`.
        ///
        /// ```
        /// typedPath(WorkOrder_Cond, "cond.technician.isNull")
        /// ==>
        /// [(WorkOrder_Cond, "cond"), (WorkOrder_Cond_cond, "technician"), (Technician_Cond, "isNull")]
        ///
        /// typedPath(x, "")
        /// ==>
        /// []
        /// ```
        ///
        protected io.vavr.collection.List<T2<GraphQLInputType, String>> typedPath(final GraphQLInputObjectType rootType, final CharSequence path) {
            if (path.isEmpty()) {
                return io.vavr.collection.List.of();
            }

            return foldLeft(splitPropPath(path), io.vavr.collection.List.<T2<GraphQLInputType, String>>of(),
                            (acc, elt) -> acc.isEmpty()
                                    ? acc.prepend(t2(rootType, elt))
                                    : acc.prepend(t2(getField(acc.head()._1(), acc.head()._2()).getType(), elt)))
                    .reverse();
        }

        private static GraphQLInputObjectField getField(final GraphQLInputType type, final String name) {
            final var field = switch (type) {
                case GraphQLInputFieldsContainer it -> it.getFieldDefinition(name);
                case GraphQLList it -> getField((GraphQLInputType) it.getWrappedType(), name);
                default -> throw new WebApiException("Not a field container: %s".formatted(type));
            };
            if (field == null) {
                throw new WebApiException("No such field [%s] in [%s].".formatted(name, type));
            }
            return field;
        }

    }

}
