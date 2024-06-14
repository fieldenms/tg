package ua.com.fielden.platform.entity.query;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;
import ua.com.fielden.platform.dao.exceptions.DbException;
import ua.com.fielden.platform.dao.exceptions.EntityAlreadyExists;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion.PropColumnInfo;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.StreamUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Provides a way to save new entities using raw JDBC batch insertion.
 * <p>
 * <b>Warning:</b>
 * <ul>
 * <li>No entity validation takes place. The responsibility for this is with the developer using batch insertion.
 * <li>Batch insertion should only be used in situations where concurrent saving of the same entities is not possible. For example, generated entities are a good candidate.
 * </ul>
 *
 * @author TG Team
 */
public class EntityBatchInsertOperation {

    private final IDbVersionProvider dbVersionProvider;
    private final EntityBatchInsertTables entityBatchInsertTables;
    private final Supplier<TransactionalExecution> trExecSupplier;

    @Inject
    EntityBatchInsertOperation(final IDomainMetadata domainMetadata,
                               final IDbVersionProvider dbVersionProvider,
                               final EntityBatchInsertTables entityBatchInsertTables,
                               @Assisted final Supplier<TransactionalExecution> trExecSupplier) {
        this.dbVersionProvider = dbVersionProvider;
        this.entityBatchInsertTables = entityBatchInsertTables;
        this.trExecSupplier = trExecSupplier;
    }

    public interface Factory {
        EntityBatchInsertOperation create(final Supplier<TransactionalExecution> trExecSupplier);
    }
    
    /**
     * Inserts streaming entities in batches of {@code batchSize},
     * Any persisted or non-persistent entities are skipped.
     * From this point of view, this function is different than {@link #batchInsert(List, int)}, which throws a runtime exception in such cases.
     */
    public <T extends AbstractEntity<?>> int batchInsert(final Stream<T> stream, final int batchSize) {
        return StreamUtils.windowed(stream.filter(ent -> ent.isPersistent() && !ent.isPersisted()), batchSize).mapToInt(xs -> batchInsert(xs, xs.size())).sum();
    }
    
    /**
     * Inserts listed entities in batches of {@code batchSize}.
     * Any persisted entities in {@code entities} lead to runtime exception {@link EntityAlreadyExists}.
     */
    public <T extends AbstractEntity<?>> int batchInsert(final List<T> entities, final int batchSize) {
        if (entities.isEmpty()) {
            return 0;
        }

        if (entities.stream().anyMatch(ent -> ent.isPersisted())) {
            throw new EntityAlreadyExists("Trying to perform batch insert for persisted entities.");
        }

        final TableStructForBatchInsertion table = entityBatchInsertTables.getTableStructsForBatchInsertion(entities.get(0).getType());
        final String tableName = table.name();
        final List<String> columnNames = table.columns().stream().flatMap(x -> x.columnNames().stream()).collect(toList());
        final String insertStmt = generateInsertStmt(tableName, columnNames, dbVersionProvider.dbVersion());

        final AtomicInteger insertedCount = new AtomicInteger(0);

        final TransactionalExecution trEx = trExecSupplier.get(); // this is a single transaction that will save all batches. 

        Iterators.partition(entities.iterator(), batchSize > 0 ? batchSize : 1) //
                .forEachRemaining(batch -> { //
                    trEx.exec(conn -> {
                        try (final PreparedStatement pst = conn.prepareStatement(insertStmt)) {
                            // batch insert statements
                            batch.forEach(entity -> {
                                final SessionImplementor sessionImpl = (SessionImplementor) trEx.getSession();
                                try {
                                    int paramIndex = 1; // JDBC parameters start their count from 1
                                    for (final PropColumnInfo propInfo : table.columns()) {
                                        final Object value = entity.get(propInfo.leafPropName());
                                        if (propInfo.hibType() instanceof UserType) {
                                            ((UserType) propInfo.hibType()).nullSafeSet(pst, value, paramIndex, sessionImpl);
                                        } else if (propInfo.hibType() instanceof CompositeUserType) {
                                            ((CompositeUserType) propInfo.hibType()).nullSafeSet(pst, value, paramIndex, sessionImpl);
                                        } else {
                                            ((Type) propInfo.hibType()).nullSafeSet(pst, value, paramIndex, sessionImpl);
                                        }
                                        paramIndex = paramIndex + propInfo.columnNames().size();
                                    }

                                    pst.addBatch();
                                } catch (final SQLException ex) {
                                    final String error = format("Could not create insert for [%s].", entity);
                                    throw new DbException(error, ex);
                                }
                            });
                            final int[] batchCounts = pst.executeBatch();
                            insertedCount.addAndGet(IntStream.of(batchCounts).sum());
                        } catch (final SQLException ex) {
                            throw new DbException("Could not batch insert generated entities.", ex);
                        }
                    });
                });

        return insertedCount.get(); 
    }

    private static String generateInsertStmt(final String tableName, final List<String> columns, final DbVersion dbVersion) {
        return format("INSERT INTO %s(%s, %s, %s) VALUES(%s, 0, %s);",
                tableName,
                dbVersion.idColumnName(),
                dbVersion.versionColumnName(),
                columns.stream().collect(joining(", ")),
                dbVersion.nextSequenceValSql(),
                join(", ", nCopies(columns.size(), "?")));
    }

    /**
     * An abstraction for representing a DB table, used to store an entity, which is specific for batch insertion
     * purposes.
     *
     * @author TG Team
     */
    public record TableStructForBatchInsertion(String name, List<PropColumnInfo> columns) {
        public TableStructForBatchInsertion(final String name, final List<PropColumnInfo> columns) {
            this.name = name;
            this.columns = ImmutableList.copyOf(columns);
        }

        /**
         * Represents a table column or columns in case of a component, to which an entity property is mapped.
         */
        public record PropColumnInfo(String leafPropName, Set<String> columnNames, Object hibType) {
            public PropColumnInfo(final String leafPropName, final String columnName, final Object hibType) {
                this(leafPropName, ImmutableSet.of(columnName), hibType);
            }

            public PropColumnInfo(final String leafPropName, final List<String> columnNames, final Object hibType) {
                this(leafPropName, unmodifiableSet(new LinkedHashSet<>(columnNames)), hibType);
            }
        }
    }

}
