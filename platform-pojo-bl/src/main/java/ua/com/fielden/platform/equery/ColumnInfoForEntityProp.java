package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IEntityMapper;

/**
 * Represents the following sql related information about the entity property: column name, column alias for external reference, column sql type, enity type/CompositeUserType (if
 * property is entity/CustomUserType), list of enity types (if property is polymorphic entity)
 *
 * @author nc
 *
 */
public class ColumnInfoForEntityProp extends ColumnInfoForPrimProp {
    private String hibernateEntityType; // in case of CompositeUserType column contains CompositeUserType name TODO find better approach

    public ColumnInfoForEntityProp(final String columnName, final String hibernateType, final String hibernateEntityType, final IEntityMapper parent) {
	super(columnName, hibernateType, parent);
	this.hibernateEntityType = hibernateEntityType;
    }

    @Override
    public String toString() {
	return "[" + getColumnName() + " : " + getHibernateType() + " : " + hibernateEntityType + "]";
    }

    public String getHibernateEntityType() {
	return hibernateEntityType;
    }

    public void setHibernateEntityType(final String hibernateEntityType) {
	this.hibernateEntityType = hibernateEntityType;
    }

    @Override
    public ColumnInfoForEntityProp clon(final String newColumnName, final IEntityMapper newParent) {
	return new ColumnInfoForEntityProp(newColumnName, getHibernateType(), hibernateEntityType, newParent);
    }
}
