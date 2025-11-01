package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Updater;
import ua.com.fielden.platform.reflection.AnnotationReflector;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/// A base class for all concrete retrievers.
///
/// To denote the update function of the implementing class, it should be annotated with [Updater].
///
/// To implement [#resultFields()], the following methods are provided: [#map(FieldMapping...)], [#field(CharSequence,String)].
///
public abstract class AbstractRetriever<T extends AbstractEntity<?>> implements IRetriever<T> {

    protected final Class<T> entityType;

    /// Prefer [#AbstractRetriever(Class)] instead of this constructor.
    ///
    @Deprecated(forRemoval = true)
    protected AbstractRetriever(final IEntityDao<T> dao) {
        this(dao.getEntityType());
    }

    protected AbstractRetriever(final Class<T> entityType) {
        this.entityType = entityType;
    }

    @Override
    public Class<T> type() {
        return entityType;
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

    protected static FieldMapping field(final CharSequence key, final String stmt) {
        return new FieldMapping(key.toString(), stmt);
    }

    protected record FieldMapping (String key, String statement) {}

    protected static SortedMap<String, String> map(final FieldMapping... mappings) {
        final SortedMap<String, String> result = new TreeMap<>();
        for (final FieldMapping mapping : mappings) {
            if (result.containsKey(mapping.key())) {
                throw new IllegalArgumentException("Duplicate mappings for property [%s]".formatted(mapping.key()));
            }
            result.put(mapping.key(), mapping.statement());
        }
        return result;
    }

    protected static List<String> list(final String... stmts) {
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
