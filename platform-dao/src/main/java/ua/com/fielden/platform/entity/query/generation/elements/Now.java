package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.query.DbVersion;

public class Now implements ISingleOperand {
    private final DbVersion dbVersion;

    @Override
    public String sql() {
        switch (dbVersion) {
        case H2:
            return "NOW()";
        case MSSQL:
            return "GETDATE()";
        case POSTGRESQL:
            return "CURRENT_TIMESTAMP";
        default:
            throw new IllegalStateException("Function [" + getClass().getSimpleName() + "] is not yet implemented for RDBMS [" + dbVersion + "]!");
        }
    }

    public Now(final DbVersion dbVersion) {
        this.dbVersion = dbVersion;
    }

    @Override
    public List<EntProp> getLocalProps() {
        return Collections.emptyList();
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
        return Collections.emptyList();
    }

    @Override
    public List<EntValue> getAllValues() {
        return Collections.emptyList();
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public Class type() {
        return null;
    }

    @Override
    public Object hibType() {
        return null;
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dbVersion == null) ? 0 : dbVersion.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Now)) {
            return false;
        }
        final Now other = (Now) obj;
        if (dbVersion != other.dbVersion) {
            return false;
        }
        return true;
    }

}