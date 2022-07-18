package ua.com.fielden.platform.entity.query;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.dao.HibernateMappingsGenerator.ID_SEQUENCE_NAME;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;

import com.google.common.collect.Iterators;

import ua.com.fielden.platform.dao.exceptions.DbException;
import ua.com.fielden.platform.dao.exceptions.EntityAlreadyExists;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.eql.meta.Table;
import ua.com.fielden.platform.eql.meta.Table.PropColumnInfo;

public class EntityBatchInsertOperation {
    private final DomainMetadata dm;
    private final Supplier<TransactionalExecution> trExecSupplier;
    
    public EntityBatchInsertOperation(final DomainMetadata dm, final Supplier<TransactionalExecution> trExecSupplier) {
        this.dm = dm;
        this.trExecSupplier = trExecSupplier;
    }
    
    public int batchInsert(final List<? extends AbstractEntity<?>> entities, final int batchSize) {
        if (entities.isEmpty()) {
            return 0;
        }
        
        if (entities.stream().anyMatch(ent -> ent.isPersisted())) {
            throw new EntityAlreadyExists("Trying to perform batch insert for persisted entities.");
        }
        
        final Table table = dm.eqlDomainMetadata.getTableForEntityType(entities.get(0).getType());
        final String tableName = table.name;
        final List<String> columnNames = table.columns.stream().flatMap(x -> x.columnNames.stream()).collect(toList());
        final String insertStmt = generateInsertStmt(tableName, columnNames);
        
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
                                    for (final PropColumnInfo propInfo : table.columns) {
                                        final Object value = entity.get(propInfo.leafPropName);
                                        if (propInfo.hibType instanceof UserType) {
                                            ((UserType) propInfo.hibType).nullSafeSet(pst, value, paramIndex, sessionImpl);
                                        } else if (propInfo.hibType instanceof CompositeUserType) {
                                            ((CompositeUserType) propInfo.hibType).nullSafeSet(pst, value, paramIndex, sessionImpl);
                                        } else {
                                            ((Type) propInfo.hibType).nullSafeSet(pst, value, paramIndex, sessionImpl);
                                        }
                                        paramIndex = paramIndex + propInfo.columnNames.size();
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

    private static String generateInsertStmt(final String tableName, final List<String> columns) {
        return format("INSERT INTO %s(_ID, _VERSION, %s) VALUES(NEXT VALUE FOR %s, 0, %s);", //
                tableName, //
                columns.stream().collect(joining(", ")), // 
                ID_SEQUENCE_NAME, //
                join(", ", nCopies(columns.size(), "?")));
    }


}