package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IEntityMapper;


/**
 * Represents the following sql related information about the entity property: column name, column alias for external reference, column sql type, enity type/CompositeUserType (if
 * property is entity/CustomUserType), list of enity types (if property is polymorphic entity)
 *
 * @author nc
 *
 */
public class ColumnInfo {
    private String columnName;
    private String columnAlias; // should be generated sequentially in the runtime
    private IEntityMapper parent;

    public ColumnInfo(final String columnName, final String columnAlias, final IEntityMapper parent) {
	this.columnName = columnName;
	this.columnAlias = columnAlias;
	this.parent = parent;
    }

    public String getColumnName() {
	return columnName;
    }

    public void setColumnName(final String columnName) {
	this.columnName = columnName;
    }

    @Override
    public String toString() {
	return "[" + columnName + "]";
    }

    public String getColumnAlias() {
	return columnAlias;
    }

    public IEntityMapper getParent() {
	return parent;
    }

    public void setColumnAlias(final String columnAlias) {
	this.columnAlias = columnAlias;
    }

    public String getSqlColumn() {
	return parent != null ? parent.getSqlAlias() + "." + getColumnName() : getColumnName();
    }

    public ColumnInfo clon(final String newColumnName, final IEntityMapper newParent) {
	return new ColumnInfo(newColumnName, columnAlias, newParent);
    }
}
