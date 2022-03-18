package ua.com.fielden.platform.migration;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;

public class RetrieverSqlProducer {

    public static String getSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        return getSql(retriever, true);
    }

    public static String getSqlWithoutOrdering(final IRetriever<? extends AbstractEntity<?>> retriever) {
        return getSql(retriever, false);
    }
	
    private static String getSql(final IRetriever<? extends AbstractEntity<?>> retriever, final boolean withOrdering) {
        final StringBuffer sb = new StringBuffer();
        sb.append(allYieldsSql(retriever.resultFields()));
        sb.append(coreSql(retriever));
        if (withOrdering) {
            sb.append(orderBySql(retriever));    
        }
        return sb.toString();
    }

    public static String getKeyResultsOnlySql(final IRetriever<? extends AbstractEntity<?>> retriever, final List<String> keyProps) {
        final StringBuffer sb = new StringBuffer();
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
        final StringBuffer sb = new StringBuffer();
        sb.append(fromSql(retriever));
        sb.append(whereSql(retriever));
        sb.append(groupBySql(retriever));
        return sb.toString();
    }

    public static String allYieldsSql(final SortedMap<String, String> resultProps) {
        return "\nSELECT ALL " + resultProps.entrySet().stream().map(e -> e.getValue() + " \"" + e.getKey() + "\"").collect(joining(", "));
    }

    private static String fromSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        return "\nFROM " + retriever.fromSql();
    }

    private static StringBuffer whereSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final StringBuffer sb = new StringBuffer();

        if (retriever.whereSql() != null) {
            sb.append("\nWHERE ");
            sb.append(retriever.whereSql());
        }
    
        return sb;
    }
    
    private static StringBuffer groupBySql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final StringBuffer sb = new StringBuffer();

        if (retriever.groupSql() != null) {
            sb.append("\nGROUP BY ");
            sb.append(retriever.groupSql().stream().collect(joining(", ")));
        }

        return sb;
    }
    
    private static StringBuffer orderBySql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final StringBuffer sb = new StringBuffer();

        if (retriever.orderSql() != null) {
            sb.append("\nORDER BY ");
            sb.append(retriever.orderSql().stream().collect(joining(", ")));
        }

        return sb;
    }
}