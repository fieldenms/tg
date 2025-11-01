package ua.com.fielden.platform.migration;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.stream.Collectors.joining;

@Singleton
final class RetrieverSqlProducer {

    private final static String SELECT = "\nSELECT ALL ";
    private final static String FROM = "\nFROM ";
    private final static String WHERE = "\nWHERE ";
    private final static String GROUP_BY = "\nGROUP BY ";
    private final static String ORDER_BY = "\nORDER BY ";

    @Inject
    RetrieverSqlProducer() {}
    
    public String getSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        return getSql(retriever, true);
    }

    public String getSqlWithoutOrdering(final IRetriever<? extends AbstractEntity<?>> retriever) {
        return getSql(retriever, false);
    }
	
    private String getSql(final IRetriever<? extends AbstractEntity<?>> retriever, final boolean withOrdering) {
        final var sb = new StringBuilder();
        sb.append(allYieldsSql(retriever.resultFields()));
        sb.append(coreSql(retriever));
        if (withOrdering) {
            sb.append(orderBySql(retriever));    
        }
        return sb.toString();
    }

    public String getKeyResultsOnlySql(final IRetriever<? extends AbstractEntity<?>> retriever, final List<String> keyProps) {
        final var sb = new StringBuilder();
        sb.append(allYieldsSql(getSpecifiedPropsYields(retriever, keyProps)));
        sb.append(coreSql(retriever));
        return sb.toString();
    }
    
    private static SortedMap<String, String> getSpecifiedPropsYields(final IRetriever<? extends AbstractEntity<?>> retriever, final List<String> specifiedProps) {
        final SortedMap<String, String> result = new TreeMap<String, String>();
        for (final Map.Entry<String, String> resultField : retriever.resultFields().entrySet()) {
            if (specifiedProps.contains(resultField.getKey())) {
                result.put(resultField.getKey(), resultField.getValue());
            }
        }
        return result;
    }
    
    private static String coreSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final var sb = new StringBuilder();
        sb.append(fromSql(retriever));
        sb.append(whereSql(retriever));
        sb.append(groupBySql(retriever));
        return sb.toString();
    }

    public String allYieldsSql(final SortedMap<String, String> resultProps) {
        return SELECT + resultProps.entrySet().stream().map(e -> "%s \"%s\"".formatted(e.getValue(), e.getKey())).collect(joining(", "));
    }

    private static String fromSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        return FROM + retriever.fromSql();
    }

    private static String whereSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        return retriever.whereSql() != null ? WHERE + retriever.whereSql() : "";
    }
    
    private static String groupBySql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        return retriever.groupSql() != null ? GROUP_BY + retriever.groupSql().stream().collect(joining(", ")) : "";
    }
    
    private static String orderBySql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        return retriever.orderSql() != null ? ORDER_BY + retriever.orderSql().stream().collect(joining(", ")) : "";
    }
}
