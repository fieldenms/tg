package ua.com.fielden.platform.eql.dbschema;

import java.sql.Types;

sealed interface SqlType {

    record Named(String name) implements SqlType {}

    /**
     * SQL type code from {@link Types}.
     */
    record TypeCode(int code) implements SqlType {}

}
