package ua.com.fielden.platform.migration;

import static java.util.stream.Collectors.joining;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;

public class RetrieverSqlProducer {

    public static String getSql(final IRetriever<? extends AbstractEntity<?>> retriever, final boolean withOrdering) {
        final StringBuffer sb = new StringBuffer();
        sb.append(getAllYieldsSql(retriever.resultFields()));
        sb.append(getCoreSql(retriever));
        if (withOrdering) {
            sb.append(getOrderBySql(retriever));    
        }
        return sb.toString();
    }

    public static String getKeyUniquenessViolationSql(final IRetriever<? extends AbstractEntity<?>> retriever, final DomainMetadataAnalyser dma) {
        final List<String> keyProps = MigrationUtils.keyPathes(retriever.type());

        final StringBuffer sb = new StringBuffer();
        final String baseSql = getKeyResultsOnlySql(retriever, keyProps);
        sb.append("SELECT 1 WHERE EXISTS (SELECT * FROM (" + baseSql + ") MT WHERE 1 < (SELECT COUNT(*) FROM (" + baseSql + ") AA WHERE ");
        for (final Iterator<String> iterator = keyProps.iterator(); iterator.hasNext();) {
            final String keyPropName = iterator.next();
            sb.append("AA.\"" + keyPropName + "\" = MT.\"" + keyPropName + "\"" + (iterator.hasNext() ? " AND " : ""));
        }
        sb.append("))");
        return sb.toString();
    }

    public static String getCoreSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final StringBuffer sb = new StringBuffer();
        sb.append(getFromSql(retriever));
        sb.append(getWhereSql(retriever));
        sb.append(getGroupBySql(retriever));
        return sb.toString();
    }

    public static String getCoreSqlWithCriteria(final IRetriever<? extends AbstractEntity<?>> retriever, final Map<String, String> criteria) {
        final StringBuffer sb = new StringBuffer();
        sb.append(getFromSql(retriever));
        sb.append(getWhereWithAdditionalCriteriaSql(retriever, criteria));
        sb.append(getGroupBySql(retriever));
        return sb.toString();
    }

    private static String getKeyResultsOnlySql(final IRetriever<? extends AbstractEntity<?>> retriever, final List<String> keyProps) {
        final StringBuffer sb = new StringBuffer();
        sb.append(getAllYieldsSql(getSpecifiedPropsYields(retriever, keyProps)));
        sb.append(getCoreSql(retriever));
        return sb.toString();
    }

    public static SortedMap<String, String> getSpecifiedPropsYields(final IRetriever<? extends AbstractEntity<?>> retriever, final List<String> specifiedProps) {
        final SortedMap<String, String> result = new TreeMap<String, String>();
        for (final Map.Entry<String, String> resultField : retriever.resultFields().entrySet()) {
            if (specifiedProps.contains(resultField.getKey())) {
                result.put(resultField.getKey(), resultField.getValue());
            }
        }
        return result;
    }

    public static String getAllYieldsSql(final SortedMap<String, String> resultProps) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\nSELECT ALL ");
        sb.append(getYieldsSql(resultProps));
        return sb.toString();
    }

    private static String getYieldsSql(final SortedMap<String, String> resultProps) {
        final StringBuffer sb = new StringBuffer();
        for (final Iterator<Entry<String, String>> iterator = resultProps.entrySet().iterator(); iterator.hasNext();) {
            final Map.Entry<String, String> keyPropName = iterator.next();
            sb.append(keyPropName.getValue() + " \"" + keyPropName.getKey() + "\"" + (iterator.hasNext() ? ", " : ""));
        }
        return sb.toString();
    }

    public static String getDistinctYieldsSql(final SortedMap<String, String> resultProps) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\nSELECT DISTINCT ");
        sb.append(getYieldsSql(resultProps));
        return sb.toString();
    }

    private static StringBuffer getWhereSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final StringBuffer sb = new StringBuffer();
        if (retriever.whereSql() != null) {
            sb.append("\nWHERE ");
            sb.append(retriever.whereSql());
        }
        return sb;
    }

    private static StringBuffer getWhereWithAdditionalCriteriaSql(final IRetriever<? extends AbstractEntity<?>> retriever, final Map<String, String> criteria) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\nWHERE ");
        if (retriever.whereSql() != null) {
            sb.append("(" + retriever.whereSql() + ") AND ");
        }
        sb.append("(");
        for (final Iterator<Entry<String, String>> iterator = criteria.entrySet().iterator(); iterator.hasNext();) {
            final Entry<String, String> entry = iterator.next();
            sb.append("(" + retriever.resultFields().get(entry.getKey()) + ") = (" + entry.getValue() + ")");
            sb.append(iterator.hasNext() ? " AND " : ")");
        }

        return sb;
    }

    private static StringBuffer getOrderBySql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final StringBuffer sb = new StringBuffer();

        if (retriever.orderSql() != null) {
            sb.append("\nORDER BY ");
            sb.append(retriever.orderSql().stream().collect(joining(", ")));
        }

        return sb;
    }

    private static StringBuffer getFromSql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\nFROM ");
        sb.append(retriever.fromSql());
        return sb;
    }

    private static StringBuffer getGroupBySql(final IRetriever<? extends AbstractEntity<?>> retriever) {
        final StringBuffer sb = new StringBuffer();

        if (retriever.groupSql() != null) {
            sb.append("\nGROUP BY ");
            sb.append(retriever.groupSql().stream().collect(joining(", ")));
        }

        return sb;
    }
}