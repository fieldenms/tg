package ua.com.fielden.platform.entity.query;

import org.hibernate.type.Type;

public class PropColumn implements Comparable<PropColumn> {
    private String name;
    private String sqlAlias;
    private Type hibType;
    private IUserTypeInstantiate hibUserType;

    public PropColumn(final String name, final String sqlAlias, final Type hibType, final IUserTypeInstantiate hibUserType) {
	this.name = name;
	this.sqlAlias = sqlAlias;
	this.hibType = hibType;
	this.hibUserType = hibUserType;
    }

    @Override
    public String toString() {
        return "\nPROP_COLUMN:\n name = " + name + "\n sqlAlias = " + sqlAlias + "\n hibType = " + hibType + "\n hibUserType = " + hibUserType + ";\n";
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((hibType == null) ? 0 : hibType.hashCode());
	result = prime * result + ((hibUserType == null) ? 0 : hibUserType.hashCode());
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	result = prime * result + ((sqlAlias == null) ? 0 : sqlAlias.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof PropColumn))
	    return false;
	final PropColumn other = (PropColumn) obj;
	if (hibType == null) {
	    if (other.hibType != null)
		return false;
	} else if (!hibType.equals(other.hibType))
	    return false;
	if (hibUserType == null) {
	    if (other.hibUserType != null)
		return false;
	} else if (!hibUserType.equals(other.hibUserType))
	    return false;
	if (name == null) {
	    if (other.name != null)
		return false;
	} else if (!name.equals(other.name))
	    return false;
	if (sqlAlias == null) {
	    if (other.sqlAlias != null)
		return false;
	} else if (!sqlAlias.equals(other.sqlAlias))
	    return false;
	return true;
    }

    @Override
    public int compareTo(final PropColumn o) {
	return name.compareTo(o.name);
    }

    public String getName() {
        return name;
    }

    public String getSqlAlias() {
        return sqlAlias;
    }

    public Type getHibType() {
        return hibType;
    }

    public IUserTypeInstantiate getHibUserType() {
        return hibUserType;
    }
}