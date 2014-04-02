package ua.com.fielden.platform.migration;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Updater;
import ua.com.fielden.platform.reflection.AnnotationReflector;

/**
 * A base class for all concrete retrievers.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public abstract class AbstractRetriever<T extends AbstractEntity<?>> implements IRetriever<T> {
    protected final IEntityDao<T> dao;

    protected AbstractRetriever(final IEntityDao<T> dao) {
        this.dao = dao;
    }

    public Class<T> type() {
        return dao.getEntityType();
    }

    @Override
    public String whereSql() {
        return null;
    }

    @Override
    public List<String> groupSql() {
        return null;
    }

    @Override
    public List<String> orderSql() {
        return null;
    }

    public static FieldMapping field(final String key, final String stmt) {
        return new FieldMapping(key, stmt);
    }

    protected static class FieldMapping {
        String key;
        String stmt;

        public FieldMapping(final String key, final String stmt) {
            this.key = key;
            this.stmt = stmt;
        }

        protected String getKey() {
            return key;
        }

        protected String getStmt() {
            return stmt;
        }

    }

    public static SortedMap<String, String> map(final FieldMapping... pairs) {
        final SortedMap<String, String> result = new TreeMap<String, String>();
        for (final FieldMapping pair : pairs) {
            if (result.containsKey(pair.getKey())) {
                throw new IllegalArgumentException("Duplicate stmts for property [" + pair.getKey() + "]");
            }
            result.put(pair.getKey(), pair.getStmt());
        }
        return result;
    }

    public static List<String> list(final String... stmts) {
        return Arrays.asList(stmts);
    }

    public String convertToBoolean(final String propName) {
        return "COALESCE(CASE UPPER(LTRIM(RTRIM(" + propName + "))) WHEN '' THEN 'N' " + //
                "WHEN 'A' THEN 'Y' WHEN 'Y' THEN 'Y' WHEN 'YES' THEN 'Y' WHEN '1' THEN 'Y' WHEN 'TRUE' THEN 'Y' WHEN 'T' THEN 'Y' " + //
                "WHEN 'B' THEN 'N' WHEN 'N' THEN 'N' WHEN 'NO' THEN 'N' WHEN '0' THEN 'N' WHEN 'FALSE' THEN 'N' WHEN 'F' THEN 'N' ELSE NULL END, 'N')";
    }

    @Override
    public final boolean isUpdater() {
        return AnnotationReflector.isAnnotationPresentForClass(Updater.class, getClass());
    }
}