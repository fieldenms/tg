package ua.com.fielden.platform.expression.ast.visitor.entities;

import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
public class EntityLevel2 extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "String Property", desc = "Property to test string functions in expression language")
    private String strProperty;

    @Observable
    public EntityLevel2 setStrProperty(final String strProperty) {
	this.strProperty = strProperty;
	return this;
    }

    public String getStrProperty() {
	return strProperty;
    }


    @IsProperty
    @MapTo
    @Title(value = "Date Property", desc = "Property to test date functions in expression language.")
    private Date dateProperty;

    @Observable
    public EntityLevel2 setDateProperty(final Date dateProperty) {
	this.dateProperty = dateProperty;
	return this;
    }

    public Date getDateProperty() {
	return dateProperty;
    }

    @IsProperty
    private Integer intProperty;

    @IsProperty(EntityLevel3.class)
    private List<EntityLevel3> collectional;

    public Integer getIntProperty() {
	return intProperty;
    }

    @Observable
    public void setIntProperty(final Integer strProperty) {
	this.intProperty = strProperty;
    }

    public List<EntityLevel3> getCollectional() {
        return collectional;
    }

    @Observable
    public void setCollectional(final List<EntityLevel3> collectional) {
        this.collectional = collectional;
    }
}
