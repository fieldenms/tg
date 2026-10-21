package ua.com.fielden.platform.web_api;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity_centre.review.DynamicPropertyAnalyser;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.Streams.stream;
import static java.lang.Byte.parseByte;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNullElse;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.*;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.ASCENDING;
import static ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering.DESCENDING;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createConditionProperty;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createJoinCondition;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.fetchNotInstrumentedWithKeyAndDesc;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;
import static ua.com.fielden.platform.web_api.FieldSchema.ORDER;
import static ua.com.fielden.platform.web_api.GraphQLCommon.*;

/**
 * Contains querying utility methods for root fields in GraphQL query / mutation schemas.
 *
 * @author TG Team
 *
 */
public class RootEntityUtils {

    /**
     * The name for built-in data introspection field returning the name of actual data object type in runtime.<br>
     * This is a part of GraphQL spec.
     */
    private static final String __TYPENAME = "__typename";
    static final String QUERY_TYPE_NAME = "Query";

    public static final String WARN_ORDER_PRIORITIES_ARE_NOT_DISTINCT = "Order priorities are not distinct.";

    private static final Logger LOGGER = getLogger(RootEntityUtils.class);

    /// Creates an EQL model for retrieving the root field of `environment` and its selection set, together with an optional warning about ordering.
    ///
    public static <T extends AbstractEntity<?>> T2<Optional<String>, QueryExecutionModel<T, EntityResultQueryModel<T>>> generateQueryModelFrom(
            final DataFetchingEnvironment environment,
            final Class<T> entityType)
    {
        final var selectedFields = allSelectedFields(environment);
        validateDuplicateFields(selectedFields);
        final var selectedOrders = Stream.concat(
                        Stream.of(createSelectedOrder("", environment.getArguments())),
                        selectedFields.stream().map(RootEntityUtils::createSelectedOrder))
                .flatMap(Optional::stream)
                .toList();
        final Optional<String> optionalWarning = selectedOrders.stream().mapToInt(SelectedOrder::priority).distinct().count() < selectedOrders.size()
                ? of(WARN_ORDER_PRIORITIES_ARE_NOT_DISTINCT)
                : empty();
        final var orders = selectedOrders.isEmpty() ? List.of(new SelectedOrder("", ASCENDING, 1)) : selectedOrders;
        final var qem = from(createJoinCondition(entityType).where()
                                     .condition(EntityCondToEqlCompiler.compile(environment))
                                     .model()
                                     .setFilterable(true)) // filterable to support IFilter part of the model
                // KEY_AND_DESC strategy for root entities is required for loading collectional associations
                // linked through key of root entity; explicit fetching of all keys in GraphQL query does not always work.
                .with(fetchNotInstrumentedWithKeyAndDesc(entityType)
                      .with(selectedFields.stream()
                                    // Exclude built-in data introspection.
                                    .filter(fld -> !fld.getName().equals(__TYPENAME))
                                    .map(GraphQLCommon::toPropertyPath)
                                    .collect(toSet()))
                      .fetchModel())
                .with(mkOrderingModel(orders, entityType))
                .lightweight() // avoid fetching instrumented entities
                .model();
        return t2(optionalWarning, qem);
    }

    /// Validates selected fields to ensure that there are no identical fields selected under different aliases.
    ///
    /// @throws WebApiException  if validation is unsuccessful
    ///
    public static void validateDuplicateFields(final Iterable<SelectedField> fields) {
        stream(fields)
        .collect(groupingBy(SelectedField::getQualifiedName, LinkedHashMap::new, toList()))
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().size() > 1)
        .findFirst()
        .ifPresent(entry -> {
            throw new WebApiException("Selected non-root fields must be unique. Duplicate field [%s] under aliases: %s.".formatted(
                    propertyPathFromSelectedFieldName(entry.getKey()),
                    entry.getValue().stream().map(field -> requireNonNullElse(field.getAlias(), "<none>")).collect(joining(", "))));
        });
    }

    private static @Nullable OrderingModel mkOrderingModel(
            final List<SelectedOrder> orders,
            final Class<? extends AbstractEntity<?>> entityType)
    {
        if (orders.isEmpty()) {
            return null;
        }

        final var sortedOrders = orders.stream().sorted(comparing(SelectedOrder::priority)).toList();
        return foldLeft(sortedOrders.subList(1, sortedOrders.size()),
                        appendPropOrder(orderBy(), sortedOrders.getFirst(), entityType),
                        (acc, order) -> appendPropOrder(acc, order, entityType))
                .model();
    }

    private static IOrderingItemCloseable appendPropOrder(
            final IOrderingItem model,
            final SelectedOrder order,
            final Class<? extends AbstractEntity<?>> entityType)
    {
        // We use `prop` rather than `yield` -- ordering by yield is only required for ad-hoc calculated properties that are not supported in Web API yet.
        // `createConditionProperty` is required because of the need to have property prepended with alias.
        final var part = model.prop(createConditionProperty(new DynamicPropertyAnalyser(entityType, order.property()).getCriteriaFullName()));
        return ASCENDING.equals(order.ordering()) ? part.asc() : part.desc();
    }

    /// Creates [SelectedOrder] for a selected field named `property` with specified arguments `arguments`.
    /// Returns an empty optional if the arguments specify no order.
    ///
    private static Optional<SelectedOrder> createSelectedOrder(
            final CharSequence property,
            final Map<String, Object> arguments)
    {
        final var orderVal = arguments.get(ORDER);
        if (orderVal == null) {
            return empty();
        }
        // "ASC_1", "DESC_1", "ASC_2", "DESC_2" and so on
        final var order = (String) orderVal;
        final byte priority = parseByte(order.substring(order.length() - 1));
        return of(new SelectedOrder(property.toString(),
                                    "ASC".equals(order.substring(0, order.length() - 2)) ? ASCENDING : DESCENDING,
                                    priority));
    }

    /// Creates [SelectedOrder] for a selected field.
    ///
    private static Optional<SelectedOrder> createSelectedOrder(final SelectedField field) {
        return createSelectedOrder(GraphQLCommon.toPropertyPath(field), field.getArguments());
    }

    private record SelectedOrder (String property, Ordering ordering, int priority) {}

}
