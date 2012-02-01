package ua.com.fielden.platform.persistence;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.utils.EntityUtils;

public class DdlGenerator {

    public final static String id = "_ID";
    public final static String version = "_VERSION";
    public final static String key = "KEY_";
    public final static String desc = "DESC_";
    protected final String _id = " _ID INT NOT NULL PRIMARY KEY";
    protected final String _version = " _VERSION INT NOT NULL DEFAULT 0";
    protected final String key_ = "KEY_ VARCHAR(255)";
    protected final String desc_ = "DESC_ VARCHAR(255)";

    public String generateDdl(final List<Class> entityTypes) {
	final StringBuffer sb = new StringBuffer();
	for (final Class entityType : entityTypes) {
	    sb.append(generateTableDdl(entityType));
	}
	return sb.toString();
    }

    private String getSqlTypeStr(final Class propType) {
	// if abstract entity the INT/LONG/BIGINT
	return null;
    }

    private String generateTableDdl(final Class entityType) {
	final StringBuffer sb = new StringBuffer();
	sb.append("CREATE TABLE ");
	sb.append(getTableClause(entityType));
	sb.append("(");
	sb.append(_id + ", ");
	sb.append(_version + ", ");

//	for (final Class entityType : entityTypes) {
//
//	}
	sb.append(");");
	return sb.toString();

    }

    public String getTableClause(final Class entityType) {
	if (!EntityUtils.isPersistedEntityType(entityType)) {
	    throw new IllegalArgumentException("Trying to determine table name for not-persisted entity type [" + entityType + "]");
	}
	final String providedTableName = AnnotationReflector.getAnnotation(MapEntityTo.class, entityType).value();
	if (!StringUtils.isEmpty(providedTableName)) {
	    return providedTableName;
	} else {
	    return entityType.getSimpleName().toUpperCase() + "_";
	}
    }
}
