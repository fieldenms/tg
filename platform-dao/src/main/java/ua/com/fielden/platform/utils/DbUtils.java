package ua.com.fielden.platform.utils;

import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaExport.Action;
import org.hibernate.tool.schema.TargetType;
import ua.com.fielden.platform.dao.exceptions.DbException;
import ua.com.fielden.platform.ddl.MetadataProvider;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.DbVersion.ID_SEQUENCE_NAME;


/// A collection of convenient DB related utilities such as to generate DDL and obtain the next value for sequence by name.
///
public class DbUtils {
    private static final Logger LOGGER = getLogger(DbUtils.class);

    /// A marker used by [#batchExecSql(List, Connection, int)] to force a JDBC batch boundary.
    ///
    /// When present in the input list, the marker is not added to the JDBC batch.
    /// Instead, any statements queued so far are submitted via `executeBatch()` and a new batch starts.
    /// This is used to ensure that DDL phases (e.g. `CREATE TABLE` vs. `CREATE INDEX`) are submitted as separate batches,
    /// which avoids name-resolution races in dialects (notably MS SQL Server with filtered indices) where statements within a single
    /// submitted batch may be parsed before the metadata effects of preceding statements are visible.
    ///
    /// The marker is intentionally formatted as a SQL line comment so that it remains a no-op if executed by accident
    /// and survives a round-trip through file-based DDL caching (where each list element is written as a separate line).
    ///
    public static final String PHASE_BOUNDARY_MARKER = "-- TG_DDL_PHASE_BOUNDARY";

    private DbUtils() {}

    /// Returns the next sequence value using DB independent way that utilises the Hibernate's Dialect support.
    ///
    public static Long nextIdValue(final String seqName, final Session session) {
        final ReturningWork<Optional<Long>> maxReturningWork = new ReturningWork<Optional<Long>>() {
            @Override
            public Optional<Long> execute(Connection connection) throws SQLException {
                final Dialect dialect =  new StandardDialectResolver().resolveDialect(new DatabaseMetaDataDialectResolutionInfoAdapter(connection.getMetaData()));
                try (final PreparedStatement preparedStatement = connection.prepareStatement( dialect.getSequenceNextValString(seqName));
                     final ResultSet rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return ofNullable(rs.getLong(1));
                    } else {
                        return empty();
                    }
                }
            }
        };
        return session.doReturningWork(maxReturningWork).orElseThrow(() -> new HibernateException(format("Could not obtain the next value for ID based on sequence [%s].", seqName)));
    }

    
    /// Drops and creates the specified sequence with new initial value `startWithValue`.
    /// The specified sequence must exist before using this function.
    ///
    public static void resetSequenceGenerator(final String seqName, final int startWithValue, final Session session) {
        final Work recreateSequence = new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                final Dialect dialect =  new StandardDialectResolver().resolveDialect(new DatabaseMetaDataDialectResolutionInfoAdapter(connection.getMetaData()));
                try (final Statement st = connection.createStatement()) {
                    // first try to drop sequence, it should already exist
                    st.execute(dialect.getDropSequenceStrings(seqName)[0]);
                    // then try to re-create sequence with new initial value
                    st.execute(dialect.getCreateSequenceStrings(seqName, startWithValue, 1)[0]);
                } catch (final Exception ex) {
                    LOGGER.warn(format("Could not reset sequnece [%s] due to: %s", seqName, ex.getMessage()), ex);
                    throw ex;
                }
            }
        };
        session.doWork(recreateSequence);
    }

    /// Utilises Hibernate for DDL generation.
    ///
    /// This implementation depends on proper registration of [MetadataProvider] as the implementation for `org.hibernate.boot.spi.SessionFactoryBuilderFactory`.
    /// Please refer [MetadataProvider]'s Javadoc for more details.
    ///
    public static List<String> generateSchemaByHibernate() throws IOException {
        final List<String> ddl;
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // redirect sysout to a stream to capture the output as a string...
            System.setOut(new PrintStream(baos, /* autoFlush = */ true, /* encoding = */ UTF_8));
            new SchemaExport().setDelimiter(";").setHaltOnError(true).execute(EnumSet.of(TargetType.STDOUT), Action.CREATE, MetadataProvider.getMetadata());
            final String generatedDdl = baos.toString(UTF_8);
            ddl = Arrays.asList(generatedDdl.split("\n"));
        } finally {
            // revert sysout back to STD
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        }
        return ddl;
    }

    /// PostgreSQL specific utility, which prepends the drop statements for dropping all tables and to create the sequence for ID generation.
    ///
    public static List<String> prependDropDdlForPostgresql(final List<String> ddl) {
        final List<String> ddlWithDrop = new ArrayList<>();

        // Drop all tables from the target database.
        ddlWithDrop.add(
                "DO $$ DECLARE" +
                        "    r RECORD;" +
                        "BEGIN" +
                        "    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = current_schema()) LOOP" +
                        "        EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(r.tablename) || ' CASCADE';" +
                        "    END LOOP;" +
                "END $$;");

        // Create the sequence for ID generation.
        ddlWithDrop.add(format("DROP SEQUENCE IF EXISTS %s;", ID_SEQUENCE_NAME));
        ddlWithDrop.add(format("CREATE SEQUENCE %s START WITH 0 INCREMENT BY 1 MINVALUE 0 CACHE 3;", ID_SEQUENCE_NAME));

        // Append the passed-in DDL, typically including "create table" statements.
        ddlWithDrop.addAll(ddl);

        return ddlWithDrop;
    }

    /// Microsoft SQL Server specific utility, which prepends the drop statements for dropping all tables and to create the sequence for ID generation.
    ///
    public static List<String> prependDropDdlForSqlServer(final List<String> ddl) {
        final List<String> ddlWithDrop = new ArrayList<>();

        // Drop all foreign keys in all tables.
        // Strictly speaking this is required only for the situation where FKs exist (e.g., in PopulateDb, but not in tests).
        // However, the cost of this query is negligible in situations where FKs do not exist.
        // Line continuations (\) ensure this text block produces a single line,
        // which is required to survive save/load via Files.write/readLines.
        ddlWithDrop.add(
                """
                WHILE(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'FOREIGN KEY')) \
                BEGIN \
                    DECLARE @sql_alterTable_fk NVARCHAR(4000) \
                    SELECT  TOP 1 @sql_alterTable_fk = ('ALTER TABLE ' + TABLE_SCHEMA + '.[' + TABLE_NAME + '] DROP CONSTRAINT [' + CONSTRAINT_NAME + ']') \
                    FROM    INFORMATION_SCHEMA.TABLE_CONSTRAINTS \
                    WHERE   CONSTRAINT_TYPE = 'FOREIGN KEY' \
                    EXEC (@sql_alterTable_fk) \
                END;
                """);

        // drop all tables from the target database
        ddlWithDrop.add("EXEC sp_MSforeachtable @command1 = \"DROP TABLE ?\";");

        // create sequence for ID generation
        ddlWithDrop.add(format("IF EXISTS(SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'%s') AND type = 'SO') DROP SEQUENCE %s;", ID_SEQUENCE_NAME, ID_SEQUENCE_NAME));
        ddlWithDrop.add(format("CREATE SEQUENCE %s START WITH 0 INCREMENT BY 1 MINVALUE 0 CACHE 3;", ID_SEQUENCE_NAME));

        // now add the passed in DDL
        ddlWithDrop.addAll(ddl);
        return ddlWithDrop;
    }

    /// H2 specific utility, which prepends the drop statements for dropping all objects and to create the sequence for ID generation.
    ///
    public static List<String> prependDropDdlForH2(final List<String> ddl) {
        final List<String> ddlWithDrop = new ArrayList<>();
        // drop all tables from the target database
        ddlWithDrop.add("DROP ALL OBJECTS;");
        
        // create sequence for ID generation
        ddlWithDrop.add(format("DROP SEQUENCE IF EXISTS %s;", ID_SEQUENCE_NAME));
        ddlWithDrop.add(format("CREATE SEQUENCE %s START WITH 0 INCREMENT BY 1 MINVALUE 0 CACHE 3;", ID_SEQUENCE_NAME));
        
        // now add the passed in DDL
        ddlWithDrop.addAll(ddl);
        return ddlWithDrop;
    }

    /// A convenient procedure for executing a list of SQL statements using the provided Hibernate session.
    /// All statements are executed in a single transaction.
    ///
    /// The primary intent for this procedure was to execute DB schema creation DDL.
    ///
    public static void execSql(final List<String> sqlStatements, final Session session) {
        final Transaction tr = session.beginTransaction();
        session.doWork(conn -> {
            try (final Statement st = conn.createStatement()) {
                for (final String sql : sqlStatements) {
                    st.execute(sql); // batch execution is not possible due to complex scripts with variables
                }
            }
        });
        tr.commit();
    }

    /// Executes SQL `statements` in batches of `batchSize` using the `conn` provided.
    /// All entries in list `statements` should be complete SQL statements
    /// (i.e. one statement should not be represented by several consecutive entries as if it is split on multiple lines.).
    ///
    /// If `barchSize` is 0 or negative then no batching is used (i.e. all statements are executed one-by-one).
    ///
    /// Occurrences of `PHASE_BOUNDARY_MARKER` in `statements` are not added to the JDBC batch.
    /// Instead, they force any statements queued so far to be submitted via `executeBatch()` before queuing continues.
    /// This lets callers express batch boundaries between logical DDL phases without changing the `List<String>` carrier type.
    ///
    /// It is expected that a database transaction has already been started when calling this function.
    /// Committing the transaction is the responsibility of the caller.
    ///
    public static void batchExecSql(final List<String> statements, final Connection conn, final int batchSize) {
        final int actualBatchSize = batchSize > 0 ? batchSize : 1;
        try (final Statement st = conn.createStatement()) {
            int queuedCount = 0;
            for (final String stmt : statements) {
                if (PHASE_BOUNDARY_MARKER.equals(stmt)) {
                    if (queuedCount > 0) {
                        st.executeBatch();
                        queuedCount = 0;
                    }
                } else {
                    st.addBatch(stmt);
                    queuedCount++;
                    if (queuedCount >= actualBatchSize) {
                        st.executeBatch();
                        queuedCount = 0;
                    }
                }
            }
            if (queuedCount > 0) {
                st.executeBatch();
            }
        } catch (final SQLException ex) {
            throw new DbException("Could not create statement.", ex);
        } catch (final DbException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new DbException("Could not exec batched SQL statements.", ex);
        }
    }

    /// A convenient wrapper around [#batchExecSql(List, Connection, int)] that executed all statements in a single batch.
    ///
    public static void batchExecSql(final List<String> statements, final Connection conn) {
        batchExecSql(statements, conn, statements.size());
    }

}
