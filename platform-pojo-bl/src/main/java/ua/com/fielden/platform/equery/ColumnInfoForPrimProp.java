package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IEntityMapper;

/**
 * Represents the following sql related information about the entity property: column name, column alias for external reference, column sql type, enity type/CompositeUserType (if
 * property is entity/CustomUserType), list of enity types (if property is polymorphic entity)
 *
 * @author nc
 *
 */
public class ColumnInfoForPrimProp extends ColumnInfo {

    private String hibernateType;

    public ColumnInfoForPrimProp(final String columnName, final String hibernateType, final IEntityMapper parent) {
	super(columnName, null, parent);
	this.hibernateType = hibernateType;
    }

    public String getHibernateType() {
	return hibernateType;
    }

    @Override
    public String toString() {
	return "[" + getColumnName() + " : " + hibernateType + "]";
    }

    @Override
    public ColumnInfoForPrimProp clon(final String newColumnName, final IEntityMapper newParent) {
	return new ColumnInfoForPrimProp(newColumnName, hibernateType, newParent);
    }
}
