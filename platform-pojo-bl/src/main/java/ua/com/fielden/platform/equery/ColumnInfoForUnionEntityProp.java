package ua.com.fielden.platform.equery;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.equery.interfaces.IEntityMapper;

/**
 * Represents the following sql related information about the entity property: column name, column alias for external reference, column sql type, enity type/CompositeUserType (if
 * property is entity/CustomUserType), list of enity types (if property is polymorphic entity)
 *
 * @author nc
 *
 */
public class ColumnInfoForUnionEntityProp extends ColumnInfoForPrimProp {
    private Map<String, Class> polymorphicTypes = new HashMap<String, Class>(); // key = 'WA' value = 'fielden.rma.Wagon'

    public ColumnInfoForUnionEntityProp(final String columnName, final String hibernateType, final Map<String, Class> polymorphicTypes, final IEntityMapper parent) {
	super(columnName, hibernateType, parent);
	this.polymorphicTypes.putAll(polymorphicTypes);
    }

    @Override
    public String toString() {
	return "[" + getColumnName() + " : " + getHibernateType() + " : " + polymorphicTypes + "]";
    }

    public Map<String, Class> getPolymorphicTypes() {
	return polymorphicTypes;
    }

    @Override
    public ColumnInfoForUnionEntityProp clon(final String newColumnName, final IEntityMapper newParent) {
	return new ColumnInfoForUnionEntityProp(newColumnName, getHibernateType(), polymorphicTypes, newParent);
    }
}
