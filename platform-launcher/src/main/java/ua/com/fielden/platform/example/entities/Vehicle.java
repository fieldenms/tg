package ua.com.fielden.platform.example.entities;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@EntityTitle(value = "Vehicle", desc = "Vehicle entity")
@KeyTitle("Vehicle No")
@DescTitle("Description")
@KeyType(String.class)
public class Vehicle extends AbstractEntity<String> {

    private static final long serialVersionUID = -677444311857320603L;

    @IsProperty
    @Title("Common property")
    private String commonProperty;
    @IsProperty
    @Title("Replacing")
    private Vehicle replacing;
    @IsProperty
    @Title("Replaced by")
    private Vehicle replacedBy;
    @IsProperty
    @Title(value = "Eqp. Class", desc = "Equipment class")
    private EqClass eqClass;
    @IsProperty
    @Title("Nested workorder entity")
    private Workorderable nestedWorkorder;
    @IsProperty
    @Title("Init. Date")
    private Date initDate;
    @IsProperty
    @Title("Number property")
    private BigDecimal numValue;

    public String getCommonProperty() {
	return commonProperty;
    }

    @Observable
    public void setCommonProperty(final String commonProperty) {
	this.commonProperty = commonProperty;
    }

    public Vehicle getReplacing() {
	return replacing;
    }

    @Observable
    public void setReplacing(final Vehicle replacing) {
	this.replacing = replacing;
    }

    public Vehicle getReplacedBy() {
	return replacedBy;
    }

    @Observable
    public void setReplacedBy(final Vehicle replacedBy) {
	this.replacedBy = replacedBy;
    }

    public EqClass getEqClass() {
	return eqClass;
    }

    @Observable
    public void setEqClass(final EqClass eqClass) {
	this.eqClass = eqClass;
    }

    public Workorderable getNestedWorkorder() {
	return nestedWorkorder;
    }

    @Observable
    public void setNestedWorkorder(final Workorderable nestedWorkorder) {
	this.nestedWorkorder = nestedWorkorder;
    }

    public Date getInitDate() {
	return initDate;
    }

    @Observable
    public void setInitDate(final Date initDate) {
	this.initDate = initDate;
    }

    public BigDecimal getNumValue() {
	return numValue;
    }

    @Observable
    public void setNumValue(final BigDecimal numValue) {
	this.numValue = numValue;
    }

}
