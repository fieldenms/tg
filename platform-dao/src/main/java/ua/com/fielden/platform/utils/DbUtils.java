package ua.com.fielden.platform.utils;

import static java.lang.String.format;
import static ua.com.fielden.platform.dao.HibernateMappingsGenerator.ID_SEQUENCE_NAME;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaExport.Action;
import org.hibernate.tool.schema.TargetType;

import ua.com.fielden.platform.ddl.MetadataProvider;

/**
 * A collection of convenient DB related utilities such as to generate DDL and obtain the next value for sequence by name. 
 * 
 * @author TG Team
 *
 */
public class DbUtils {
    
    private DbUtils() {}

    /**
     * Returns the next sequence value using DB independent way that utilises the Hibernate's Dialect support. 
     * 
     * @param seqName
     * @param session
     * @return
     */
    public static Long nextIdValue(final String seqName, final Session session) {
        final ReturningWork<Optional<Long>> maxReturningWork = new ReturningWork<Optional<Long>>() {
            @Override
            public Optional<Long> execute(Connection connection) throws SQLException {
                final Dialect dialect =  new StandardDialectResolver().resolveDialect(new DatabaseMetaDataDialectResolutionInfoAdapter(connection.getMetaData()));
                try (final PreparedStatement preparedStatement = connection.prepareStatement( dialect.getSequenceNextValString(seqName));
                     final ResultSet rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.ofNullable(rs.getLong(1));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        };
        return session.doReturningWork(maxReturningWork).orElseThrow(() -> new HibernateException(format("Could not obtain the next value for ID based on sequence [%s].", seqName)));
    }

    /**
     * Utilises Hibernate for DDL generation. 
     * <p>
     * This implementation depends on proper registration of {@link MetadataProvider} as the implementation for <code>org.hibernate.boot.spi.SessionFactoryBuilderFactory</code>.
     * Please refer {@link MetadataProvider}'s Javadoc for more details.
     * 
     * @return
     * @throws IOException
     */
    public static List<String> generateSchemaByHibernate() throws IOException {
        final List<String> ddl;
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // redirect sysout to a stream to capture the output as a string...
            System.setOut(new PrintStream(baos, /* autoFlush = */ true, /* encoding = */ "UTF8"));
            new SchemaExport().setDelimiter(";").setHaltOnError(true).execute(EnumSet.of(TargetType.STDOUT), Action.CREATE, MetadataProvider.getMetadata());
            final String generatedDdl = baos.toString("UTF8");
            ddl = Arrays.asList(generatedDdl.split("\n"));
        } finally {
            // revert sysout back to STD
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        }
        return ddl;
    }

    /**
     * Microsoft SQL Server specific utility, which prepends the drop statements for dropping all tables and to create the sequence for ID generation.
     *   
     * @param ddl
     * @return
     */
    public static List<String> prependDropDdlForSqlServer(final List<String> ddl) {
        final List<String> ddlWithDrop = new ArrayList<>();
        // drop all tables from the target database
        ddlWithDrop.add("EXEC sp_msforeachtable \"ALTER TABLE ? NOCHECK CONSTRAINT all\";");
        ddlWithDrop.add(
                "WHILE(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'FOREIGN KEY'))"+
                        "BEGIN"+
                        "    DECLARE @sql_alterTable_fk NVARCHAR(4000)"+
                        ""+
                        "    SELECT  TOP 1 @sql_alterTable_fk = ('ALTER TABLE ' + TABLE_SCHEMA + '.[' + TABLE_NAME + '] DROP CONSTRAINT [' + CONSTRAINT_NAME + ']')"+
                        "    FROM    INFORMATION_SCHEMA.TABLE_CONSTRAINTS"+
                        "    WHERE   CONSTRAINT_TYPE = 'FOREIGN KEY'"+
                        ""+
                        "    EXEC (@sql_alterTable_fk)"+
                "END");
        ddlWithDrop.add("EXEC sp_MSforeachtable @command1 = \"DROP TABLE ?\";");
        
        // create sequence for ID generation
        ddlWithDrop.add(format("IF EXISTS(SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'%s') AND type = 'SO') DROP SEQUENCE %s;", ID_SEQUENCE_NAME, ID_SEQUENCE_NAME));
        ddlWithDrop.add(format("CREATE SEQUENCE %s START WITH 1 INCREMENT BY 1 MINVALUE 1 CACHE  3;", ID_SEQUENCE_NAME));
        
        // now add the passed in DDL
        ddlWithDrop.addAll(ddl);
        return ddlWithDrop;
    }

    /**
     * H2 specific utility, which prepends the drop statements for dropping all objects and to create the sequence for ID generation.
     *   
     * @param ddl
     * @return
     */
    public static List<String> prependDropDdlForH2(final List<String> ddl) {
        final List<String> ddlWithDrop = new ArrayList<>();
        // drop all tables from the target database
        ddlWithDrop.add("DROP ALL OBJECTS;");
        
        // create sequence for ID generation
        ddlWithDrop.add(format("DROP SEQUENCE IF EXISTS %s;", ID_SEQUENCE_NAME));
        ddlWithDrop.add(format("CREATE SEQUENCE %s START WITH 1 INCREMENT BY 1 MINVALUE 1 CACHE  3;", ID_SEQUENCE_NAME));
        
        // now add the passed in DDL
        ddlWithDrop.addAll(ddl);
        return ddlWithDrop;
    }

    /**
     * A convenient procedure for executing a list of SQL statements using the provided Hibernate session.
     * All statements are executed in a single transaction.
     * <p>
     * The primary intent for this procedure was to execute DB schema creation DDL.
     *  
     * @param sqlStatements
     * @param session
     */
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

}
