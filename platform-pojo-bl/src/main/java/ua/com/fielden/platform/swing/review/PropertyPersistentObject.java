package ua.com.fielden.platform.swing.review;

import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

public class PropertyPersistentObject {
    private final String propertyName;
    private Object propertyValue;
    private Boolean not;
    private Boolean exclusive;
    private DateRangePrefixEnum datePrefix;
    private MnemonicEnum dateMnemonic;
    private Boolean andBefore;
    private Pair<Integer, Integer> position;
    private Boolean all, orNull;

    protected PropertyPersistentObject() {
	propertyName = null;
    }

    public PropertyPersistentObject(final String propertyName) {
	this.propertyName = propertyName;
    }

    public void setAll(final Boolean all) {
	this.all = all;
    }

    public Boolean getAll() {
	return all;
    }

    public Boolean getOrNull() {
        return orNull;
    }

    public void setOrNull(final Boolean orNull) {
        this.orNull = orNull;
    }

    public DateRangePrefixEnum getDatePrefix() {
        return datePrefix;
    }

    public void setDatePrefix(final DateRangePrefixEnum datePrefix) {
        this.datePrefix = datePrefix;
    }

    public MnemonicEnum getDateMnemonic() {
        return dateMnemonic;
    }

    public void setDateMnemonic(final MnemonicEnum dateMnemonic) {
        this.dateMnemonic = dateMnemonic;
    }

    public Boolean getAndBefore() {
        return andBefore;
    }

    public void setAndBefore(final Boolean andBefore) {
        this.andBefore = andBefore;
    }

    public Boolean getNot() {
	return not;
    }

    public void setNot(final Boolean not) {
	this.not = not;
    }

    public Boolean getExclusive() {
        return exclusive;
    }

    public void setExclusive(final Boolean exclusive) {
        this.exclusive = exclusive;
    }

    public Object getPropertyValue() {
	return propertyValue;
    }

    public void setPropertyValue(final Object propertyValue) {
	this.propertyValue = propertyValue;
    }

    public boolean positionIsInitialised() {
	return position != null;
    }

    public void setPosition(final Pair<Integer, Integer> position) {
	this.position = position;
    }

    public Integer getRow() {
	return position.getValue();
    }

    public Integer getColumn() {
	return position.getKey();
    }

    public String getPropertyName() {
	return propertyName;
    }

    public Pair<Integer, Integer> getPosition() {
	return position;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null || obj.getClass() != this.getClass()) {
	    return false;
	}
	final PropertyPersistentObject pObj = (PropertyPersistentObject) obj;
	if ((getPropertyName() == null && getPropertyName() != pObj.getPropertyName()) || (getPropertyName() != null && !getPropertyName().equals(pObj.getPropertyName()))) {
	    return false;
	}
	if ((getPropertyValue() == null && getPropertyValue() != pObj.getPropertyValue()) || (getPropertyValue() != null && !getPropertyValue().equals(pObj.getPropertyValue()))) {
	    return false;
	}
	if ((getNot() == null && getNot() != pObj.getNot()) || (getNot() != null && !getNot().equals(pObj.getNot()))) {
	    return false;
	}
	if ((getExclusive() == null && getExclusive() != pObj.getExclusive()) || (getExclusive() != null && !getExclusive().equals(pObj.getExclusive()))) {
	    return false;
	}
	if ((getDatePrefix() == null && getDatePrefix() != pObj.getDatePrefix()) || (getDatePrefix() != null && !getDatePrefix().equals(pObj.getDatePrefix()))) {
	    return false;
	}
	if ((getDateMnemonic() == null && getDateMnemonic() != pObj.getDateMnemonic()) || (getDateMnemonic() != null && !getDateMnemonic().equals(pObj.getDateMnemonic()))) {
	    return false;
	}
	if ((getAndBefore() == null && getAndBefore() != pObj.getAndBefore()) || (getAndBefore() != null && !getAndBefore().equals(pObj.getAndBefore()))) {
	    return false;
	}
	if ((getPosition() == null && getPosition() != pObj.getPosition()) || (getPosition() != null && !getPosition().equals(pObj.getPosition()))) {
	    return false;
	}
	if ((getAll() == null && getAll() != pObj.getAll()) || (getAll() != null && !getAll().equals(pObj.getAll()))) {
	    return false;
	}
	if ((getOrNull() == null && getOrNull() != pObj.getOrNull()) || (getOrNull() != null && !getOrNull().equals(pObj.getOrNull()))) {
	    return false;
	}
	return true;
    }

    @Override
    public int hashCode() {
	int result = 17;
	result = 31 * result + (getPropertyName() != null ? getPropertyName().hashCode() : 0);
	result = 31 * result + (getPropertyValue() != null ? getPropertyValue().hashCode() : 0);
	result = 31 * result + (getNot() != null ? getNot().hashCode() : 0);
	result = 31 * result + (getExclusive() != null ? getExclusive().hashCode() : 0);
	result = 31 * result + (getDatePrefix() != null ? getDatePrefix().hashCode() : 0);
	result = 31 * result + (getDateMnemonic() != null ? getDateMnemonic().hashCode() : 0);
	result = 31 * result + (getAndBefore() != null ? getAndBefore().hashCode() : 0);
	result = 31 * result + (getPosition() != null ? getPosition().hashCode() : 0);
	result = 31 * result + (getAll() != null ? getAll().hashCode() : 0);
	result = 31 * result + (getOrNull() != null ? getOrNull().hashCode() : 0);
	return result;
    }

    @Override
    public String toString() {
	return "[" + getPropertyName() + ", " + getPropertyValue() + ", " + getPosition() + ", " + getNot() + ", " + getExclusive() + ", " + getDatePrefix() + ", " + getDateMnemonic() + ", " + getAndBefore() + ", " + getAll() + ", " + getOrNull() + "]";
    }
}
