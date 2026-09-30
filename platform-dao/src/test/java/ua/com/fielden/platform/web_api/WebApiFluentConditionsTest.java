package ua.com.fielden.platform.web_api;

import org.junit.Test;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.web_api.FluentConditions.*;
import static ua.com.fielden.platform.web_api.GraphQLCommon.*;
import static ua.com.fielden.platform.web_api.WebApiUtils.*;

/// Test for the condition types that [FluentConditions] adds to the schema, and for the `where` argument of root fields that refers to them.
///
public class WebApiFluentConditionsTest extends AbstractDaoTestCase {

    private static final Set<String> VALUE_COND_TYPES = Set.of(
            STRING_COND, BOOLEAN_COND, INT_COND, LONG_COND, BIG_DECIMAL_COND, MONEY_COND, DATE_COND, HYPERLINK_COND, COLOUR_COND);

    /// Selects the name and kind of an input type, unwrapping up to two levels of `NON_NULL` and `LIST`.
    ///
    private static final String TYPE_REF = "type{name kind ofType{name kind ofType{name kind}}}";

    private final IWebApi webApi = getInstance(IWebApi.class);
    private final IApplicationDomainProvider appDomainProvider = getInstance(IApplicationDomainProvider.class);

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Entity condition types
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void condition_types_are_created_for_all_entity_types_that_have_a_GraphQL_type() {
        final var schemaTypeNames = querySchemaTypeNames();

        final var entityTypeNamesWithGraphQlType = streamVisibleTypes(appDomainProvider)
                .map(GraphQLCommon::graphQlTypeNameForEntity)
                .filter(schemaTypeNames::contains)
                .collect(toSet());
        assertThat(entityTypeNamesWithGraphQlType)
                .as("Queryable and union types are expected to have GraphQL types.")
                .contains(graphQlTypeNameForEntity(TgWebApiEntity.class), graphQlTypeNameForEntity(TgUnion.class));

        final var entityTypeNamesWithCondType = schemaTypeNames.stream()
                .filter(name -> !VALUE_COND_TYPES.contains(name))
                .flatMap(graphQlTypeName -> entityTypeNameFromGraphQlCondEntityType(graphQlTypeName).stream())
                .collect(toSet());
        assertThat(entityTypeNamesWithCondType).isEqualTo(entityTypeNamesWithGraphQlType);

        final var entityTypeNamesWithCondCondType = schemaTypeNames.stream()
                .flatMap(graphQlTypeName -> entityTypeNameFromGraphQlCondEntityCondType(graphQlTypeName).stream())
                .collect(toSet());
        assertThat(entityTypeNamesWithCondCondType).isEqualTo(entityTypeNamesWithGraphQlType);
    }

    @Test
    public void entity_condition_type_is_a_oneOf_input_object_type_with_cond_and_or_not_isNull() {
        final var condType = queryType(graphQlTypeNameForCondEntity(TgWebApiEntity.class));
        assertThat(condType.get("kind")).isEqualTo("INPUT_OBJECT");
        assertThat(condType.get("isOneOf")).isEqualTo(true);

        final var condTypeName = graphQlTypeNameForCondEntity(TgWebApiEntity.class);
        assertThat(inputFieldTypes(condType)).isEqualTo(Map.of(
                COND, graphQlTypeNameForCondEntityCond(TgWebApiEntity.class),
                AND, "[%s]".formatted(condTypeName),
                OR, "[%s]".formatted(condTypeName),
                NOT, condTypeName,
                IS_NULL, "Boolean"));
    }

    @Test
    public void property_condition_type_constrains_each_property_by_the_condition_type_of_its_type() {
        final var condCondType = queryType(graphQlTypeNameForCondEntityCond(TgWebApiEntity.class));
        assertThat(condCondType.get("kind")).isEqualTo("INPUT_OBJECT");
        assertThat(condCondType.get("isOneOf")).isEqualTo(false);

        assertThat(inputFieldTypes(condCondType)).containsAllEntriesOf(mapOf(
                t2("key", STRING_COND),
                t2("desc", STRING_COND),
                t2("active", BOOLEAN_COND),
                t2("intProp", INT_COND),
                t2("longProp", LONG_COND),
                t2("bigDecimalProp", BIG_DECIMAL_COND),
                t2("moneyProp", MONEY_COND),
                t2("dateProp", DATE_COND),
                t2("hyperlinkProp", HYPERLINK_COND),
                t2("colourProp", COLOUR_COND),
                t2("model", graphQlTypeNameForCondEntity(TgVehicleModel.class))));
    }

    @Test
    public void property_condition_type_has_fields_only_for_fields_of_the_GraphQL_type() {
        final var objectFieldNames = namesOf(queryType(graphQlTypeNameForEntity(TgWebApiEntity.class)), "fields");
        final var condFieldNames = namesOf(queryType(graphQlTypeNameForCondEntityCond(TgWebApiEntity.class)), "inputFields");

        assertThat(condFieldNames).isNotEmpty();
        assertThat(objectFieldNames).containsExactlyInAnyOrderElementsOf(condFieldNames);
    }

    @Test
    public void property_condition_type_has_no_fields_for_crit_only_properties() {
        Map.of(TgWebApiEntitySyntheticSingle.class, "date",
               TgWebApiEntitySyntheticMulti.class, "datePeriod")
           .forEach((entityType, critOnlyProp) -> {
               final var objectFieldNames = namesOf(queryType(graphQlTypeNameForEntity(entityType)), "fields");
               final var condFieldNames = namesOf(queryType(graphQlTypeNameForCondEntityCond(entityType)), "inputFields");

               assertThat(objectFieldNames).as("Fields of [%s].", entityType.getSimpleName()).contains(critOnlyProp);
               assertThat(condFieldNames).as("Input fields of the property condition type of [%s].", entityType.getSimpleName())
                       .isNotEmpty()
                       .doesNotContain(critOnlyProp);
           });
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Value condition types
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void String_Cond_has_equality_membership_pattern_and_isNull_operators() {
        assertThat(inputFieldTypes(queryType(STRING_COND))).isEqualTo(Map.of(
                EQ, "String", NE, "String",
                IN, "[String!]", NOT_IN, "[String!]",
                LIKE, "String", NOT_LIKE, "String", I_LIKE, "String", NOT_I_LIKE, "String",
                IS_NULL, "Boolean"));
    }

    @Test
    public void Boolean_Cond_has_only_the_eq_operator() {
        assertThat(inputFieldTypes(queryType(BOOLEAN_COND))).isEqualTo(Map.of(EQ, "Boolean"));
    }

    @Test
    public void numeric_condition_types_have_equality_comparison_membership_and_isNull_operators_over_their_scalar() {
        Map.of(INT_COND, "Int", LONG_COND, "Long", BIG_DECIMAL_COND, "BigDecimal", MONEY_COND, "Money").forEach((condTypeName, scalar) -> {
            final var listType = "[%s!]".formatted(scalar);
            assertThat(inputFieldTypes(queryType(condTypeName))).as(condTypeName).isEqualTo(Map.of(
                    EQ, scalar, NE, scalar,
                    LT, scalar, LE, scalar, GT, scalar, GE, scalar,
                    IN, listType, NOT_IN, listType,
                    IS_NULL, "Boolean"));
        });
    }

    @Test
    public void Date_Cond_has_equality_comparison_and_isNull_operators() {
        assertThat(inputFieldTypes(queryType(DATE_COND))).isEqualTo(Map.of(
                EQ, "Date", NE, "Date",
                LT, "Date", LE, "Date", GT, "Date", GE, "Date",
                IS_NULL, "Boolean"));
    }

    @Test
    public void Hyperlink_Cond_and_Colour_Cond_have_only_the_isNull_operator() {
        assertThat(inputFieldTypes(queryType(HYPERLINK_COND))).isEqualTo(Map.of(IS_NULL, "Boolean"));
        assertThat(inputFieldTypes(queryType(COLOUR_COND))).isEqualTo(Map.of(IS_NULL, "Boolean"));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Root fields
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void every_entity_root_field_has_argument_where_of_the_entity_condition_type() {
        final var rootFields = querySchema("{__schema{queryType{fields{name args{name %s}}}}}".formatted(TYPE_REF))
                .get("queryType");
        @SuppressWarnings("unchecked")
        final var argsByRootField = ((List<Map<String, Object>>) ((Map<String, Object>) rootFields).get("fields")).stream()
                .collect(toMap(f -> (String) f.get("name"), f -> inputFieldTypes(f, "args")));

        final var queryableTypes = streamQueryableTypes(appDomainProvider).toList();
        assertThat(queryableTypes).isNotEmpty();
        queryableTypes.forEach(ty -> assertThat(argsByRootField.get(rootFieldName(ty)))
                .as("Arguments of root field [%s].", rootFieldName(ty))
                .containsEntry(WHERE, graphQlTypeNameForCondEntity(ty)));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @SuppressWarnings("unchecked")
    private Set<String> querySchemaTypeNames() {
        return ((List<Map<String, Object>>) querySchema("{__schema{types{name}}}").get("types")).stream()
                .map(ty -> (String) ty.get("name"))
                .collect(toSet());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> querySchema(final String query) {
        final var result = webApi.execute(input(query));
        assertNoErrors(result);
        return (Map<String, Object>) data(result).get("__schema");
    }

    /// Queries the specified type, selecting its kind, `isOneOf`, and the names of its fields and input fields together with their types.
    ///
    @SuppressWarnings("unchecked")
    private Map<String, Object> queryType(final String typeName) {
        final var result = webApi.execute(input("{__type(name: \"%s\"){name kind isOneOf fields{name} inputFields{name %s}}}".formatted(typeName, TYPE_REF)));
        assertNoErrors(result);
        final var type = (Map<String, Object>) data(result).get("__type");
        assertThat(type).as("Type [%s] is not present in the schema.", typeName).isNotNull();
        return type;
    }

    private static Map<String, String> inputFieldTypes(final Map<String, Object> type) {
        return inputFieldTypes(type, "inputFields");
    }

    /// Maps the names of fields listed under `key` in `type` to their types in the SDL notation.
    ///
    @SuppressWarnings("unchecked")
    private static Map<String, String> inputFieldTypes(final Map<String, Object> type, final String key) {
        return ((List<Map<String, Object>>) type.get(key)).stream()
                .collect(toMap(f -> (String) f.get("name"), f -> typeToString((Map<String, Object>) f.get("type"))));
    }

    @SuppressWarnings("unchecked")
    private static List<String> namesOf(final Map<String, Object> type, final String key) {
        return ((List<Map<String, Object>>) type.get(key)).stream().map(f -> (String) f.get("name")).toList();
    }

    @SuppressWarnings("unchecked")
    private static String typeToString(final Map<String, Object> type) {
        return switch ((String) type.get("kind")) {
            case "NON_NULL" -> typeToString((Map<String, Object>) type.get("ofType")) + "!";
            case "LIST" -> "[%s]".formatted(typeToString((Map<String, Object>) type.get("ofType")));
            default -> (String) type.get("name");
        };
    }

    private static void assertNoErrors(final Map<String, Object> result) {
        final var errors = errors(result);
        assertThat(errors).as(errors.toString()).isEmpty();
    }

}
