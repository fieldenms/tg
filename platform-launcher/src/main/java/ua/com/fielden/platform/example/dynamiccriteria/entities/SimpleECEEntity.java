package ua.com.fielden.platform.example.dynamiccriteria.entities;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController2;
import ua.com.fielden.platform.example.dynamiccriteria.iao.ISimpleECEEntityDao;

@EntityTitle("Simple entity type")
@KeyType(String.class)
@KeyTitle(value = "Simple entity", desc = "Simple entity description")
@MapEntityTo("SIMPLEECEENTITY")
@DefaultController2(ISimpleECEEntityDao.class)
public class SimpleECEEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = -8516470172415858958L;

    @IsProperty
    @Title(value = "String property", desc = "String property description")
    @MapTo("STRING_PROPERTY")
    private String stringProperty;

    @IsProperty
    @Title(value = "Init. date", desc = "Date of initiation")
    @MapTo("INIT_DATE")
    private Date initDate;

    @IsProperty
    @Title(value = "active", desc = "determines the activity of simple entity.")
    @MapTo("ACTIVE")
    private boolean active = false;

    @IsProperty
    @Title(value = "Num. value", desc = "Number value ")
    @MapTo("NUM_VALUE")
    private Integer numValue;

    @MapTo("ID_NESTED_ENTITY")
    @Title(value = "Aggregated entity", desc = "Aggregated entity description")
    @IsProperty
    private SimpleNestedEntity nestedEntity;

    @IsProperty(value = SimpleCompositeEntity.class, linkProperty = "simpleEntity") @Title("Collection property") @MapTo("ID_SIMPLE_ENTITY")
    private Set<SimpleCompositeEntity> entities = new HashSet<SimpleCompositeEntity>();
    public Set<SimpleCompositeEntity> getFuelUsages() { return entities; }

    public String getStringProperty() {
	return stringProperty;
    }

    @Observable
    public void setStringProperty(final String stringProperty) {
	this.stringProperty = stringProperty;
    }

    public Date getInitDate() {
	return initDate;
    }

    @Observable
    public void setInitDate(final Date initDate) {
	this.initDate = initDate;
    }

    public boolean isActive() {
	return active;
    }

    @Observable
    public void setActive(final boolean active) {
	this.active = active;
    }

    public Integer getNumValue() {
	return numValue;
    }

    @Observable
    public void setNumValue(final Integer numValue) {
	this.numValue = numValue;
    }

    public SimpleNestedEntity getNestedEntity() {
	return nestedEntity;
    }

    @Observable
    public void setNestedEntity(final SimpleNestedEntity nestedEntity) {
	this.nestedEntity = nestedEntity;
    }
}
