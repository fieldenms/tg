package ua.com.fielden.platform.migration;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.EntityUtils;

public class DataValidatorUtils {
    public static String getKeyUniquenessViolationSql(final Class<? extends AbstractEntity<?>> entityType, final List<CompiledRetriever> entityTypeRetrievers) {
        final List<String> keyProps = MigrationUtils.keyPaths(entityType);
        final StringBuffer sb = new StringBuffer();
        var from = entityTypeRetrievers.stream().map(r -> RetrieverSqlProducer.getKeyResultsOnlySql(r.retriever, keyProps)).collect(joining("\nUNION ALL"));
        var props = keyProps.stream().map(k ->  " \"" + k + "\"").collect(joining(", "));
        sb.append("SELECT 1 WHERE EXISTS (\nSELECT *, COUNT(*) FROM (" + from + ") T GROUP BY " + props + " HAVING COUNT(*) > 1\n)");
        return sb.toString();
    }
    
    public static List<T3<String, String, String>> produceValidationSql(final List<CompiledRetriever> retrieversJobs) {
    	var result = new ArrayList<T3<String, String, String>>();
    	
    	for (final CompiledRetriever retriever : retrieversJobs) {
    		var retrieverSql = RetrieverSqlProducer.getSqlWithoutOrdering(retriever.retriever);
    		for (PropInfo pi : retriever.getContainers()) {
				var pmd = retriever.md.props().stream().filter(p -> p.name().equals(pi.propName())).findFirst().get(); 
				if (pmd.required()) {
						final List<String> leafProps = EntityUtils.isPersistedEntityType(pi.propType()) ? pmd.leafProps() : CollectionUtil.listOf(pmd.name());
						var cond = leafProps.stream().map(s -> "R. \"" + s + "\" IS NULL").collect(Collectors.joining(" AND "));
						var sql = "SELECT COUNT(*) FROM (" + retrieverSql + ") R WHERE " + cond;
						result.add(T3.t3(retriever.retriever.getClass().getSimpleName(), pi.propName() + ":" + pi.propType().getSimpleName(), sql));
				}
			}
        }

    	return result;
    }
    
    public static List<T3<String, String, String>> produceValidationSql(final List<CompiledRetriever> retrieversJobs, final Map<Class<? extends AbstractEntity<?>>, List<CompiledRetriever>> entityTypeRetrievers) {
    	var result = new ArrayList<T3<String, String, String>>();
    	
    	for (final CompiledRetriever retriever : retrieversJobs) {
    		var retrieverSql = RetrieverSqlProducer.getSqlWithoutOrdering(retriever.retriever);
    		for (PropInfo pi : retriever.getContainers()) {
				if (EntityUtils.isPersistedEntityType(pi.propType())) {
					final List<String> keyProps = MigrationUtils.keyPaths((Class<? extends AbstractEntity>) pi.propType());
					final List<String> leafProps = retriever.md.props().stream().filter(p -> p.name().equals(pi.propName())).findFirst().get().leafProps();
					var domainRets = entityTypeRetrievers.get(pi.propType());
					var from = domainRets == null ? "NONE" : domainRets.stream().map(r -> RetrieverSqlProducer.getKeyResultsOnlySql(r.retriever, keyProps)).collect(joining("\nUNION ALL"));
					var cond = "(" + leafProps.stream().map(s -> "R. \"" + s + "\" IS NOT NULL").collect(Collectors.joining(" OR ")) + ")";
					var sql = "SELECT COUNT(*) FROM (" + retrieverSql + ") R WHERE " + cond + " AND NOT EXISTS (SELECT * FROM (" + from + ") D WHERE " + 
					composeCondition(leafProps, keyProps, "R", "D") + ")";
					result.add(T3.t3(retriever.retriever.getClass().getSimpleName(), pi.propName() + ":" + pi.propType().getSimpleName(), sql));
				}
			}
        }

    	return result;
    }
    
    private static String composeCondition(final List<String> props, final List<String> keyProps, final String retAlias, final String domainAlias) {
    	final List<T2<String, String>> pairs = new ArrayList<>();
    	
    	for (int i = 0; i < props.size(); i++)	{
    	    pairs.add(T2.t2(props.get(i), keyProps.get(i)));
    	}
    	
    	return pairs.stream().map(e -> "(" + retAlias + ".\"" + e._1 + "\" IS NULL AND " + domainAlias + ".\"" + e._2 + "\" IS NULL OR " + retAlias+ ".\"" + e._1 + "\" = " + domainAlias + ".\"" + e._2 + "\")").collect(Collectors.joining(" AND "));
    }

}
