package ua.com.fielden.platform.expression.ast.visitor.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Collections.unmodifiableList;

@KeyType(String.class)
public class EntityLevel2 extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "String Property", desc = "Property to test string functions in expression language")
    private String strProperty;

    @IsProperty
    private Integer intProperty;

    @IsProperty(EntityLevel3.class)
    private final List<EntityLevel3> collectional = new ArrayList<>();

    @IsProperty
    @MapTo
    @Title(value = "Self", desc = "Self")
    private EntityLevel2 selfProperty;

    @Observable
    public EntityLevel2 setSelfProperty(final EntityLevel2 selfProperty) {
        this.selfProperty = selfProperty;
        return this;
    }

    public EntityLevel2 getSelfProperty() {
        return selfProperty;
    }

    @IsProperty
    @MapTo
    @Title(value = "Date Property", desc = "Property to test date functions in expression language.")
    private Date dateProperty;

    @Observable
    public EntityLevel2 setStrProperty(final String strProperty) {
        this.strProperty = strProperty;
        return this;
    }

    public String getStrProperty() {
        return strProperty;
    }

    @Observable
    public EntityLevel2 setDateProperty(final Date dateProperty) {
        this.dateProperty = dateProperty;
        return this;
    }

    public Date getDateProperty() {
        return dateProperty;
    }

    public Integer getIntProperty() {
        return intProperty;
    }

    @Observable
    public void setIntProperty(final Integer strProperty) {
        this.intProperty = strProperty;
    }

    public List<EntityLevel3> getCollectional() {
        return unmodifiableList(collectional);
    }

    @Observable
    public EntityLevel2 setCollectional(final List<EntityLevel3> collectional) {
        this.collectional.clear();
        this.collectional.addAll(collectional);
        return this;
    }

}
