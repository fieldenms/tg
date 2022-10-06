package ua.com.fielden.platform.test_config;

import static java.lang.String.format;
import static ua.com.fielden.platform.dao.HibernateMappingsGenerator.ID_SEQUENCE_NAME;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of convenient PostgreSQL-related utilities such as to generate DDL.
 *
 * @author TG PSA Team
 */

public class PostgresqlDbUtils {

    private PostgresqlDbUtils() {}

    /**
     * Postgresql specific utility, which prepends the drop statements for dropping all tables and to create the sequence for ID generation.
     *
     * @param ddl
     * @return
     */
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
}
