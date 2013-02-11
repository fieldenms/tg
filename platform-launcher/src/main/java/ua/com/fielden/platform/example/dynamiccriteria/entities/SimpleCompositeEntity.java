package ua.com.fielden.platform.example.dynamiccriteria.entities;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;
import ua.com.fielden.platform.example.dynamiccriteria.iao.ISimpleCompositeEntityDao;

@KeyTitle("Simple Composite Entity")
@KeyType(DynamicEntityKey.class)
@MapEntityTo("SIMPLE_COMPOSITE_ENTITY")
@CompanionObject(ISimpleCompositeEntityDao.class)
public class SimpleCompositeEntity extends AbstractEntity<DynamicEntityKey> {

    private static final long serialVersionUID = 5952735860737176582L;

    @IsProperty
    @Title(value = "Simple entity", desc = "Simple entity description")
    @CompositeKeyMember(1)
    @MapTo("ID_SIMPLE_ENTITY")
    private SimpleECEEntity simpleEntity;

    @IsProperty
    @Title(value = "String key property", desc = "String proerty key description")
    @CompositeKeyMember(2)
    @MapTo("STRING_KEY")
    private String stringKey;

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

    /**
     * Constructor for the entity factory from TG.
     */
    protected SimpleCompositeEntity() {
	setKey(new DynamicEntityKey(this));
    }

    public SimpleECEEntity getSimpleEntity() {
	return simpleEntity;
    }

    @Observable
    public SimpleCompositeEntity setSimpleEntity(final SimpleECEEntity simpleEntity) {
	this.simpleEntity = simpleEntity;
	return this;
    }

    public String getStringKey() {
	return stringKey;
    }

    @Observable
    public SimpleCompositeEntity setStringKey(final String stringKey) {
	this.stringKey = stringKey;
	return this;
    }

    public Date getInitDate() {
	return initDate;
    }

    @Observable
    public SimpleCompositeEntity setInitDate(final Date initDate) {
	this.initDate = initDate;
	return this;
    }

    public boolean isActive() {
	return active;
    }

    @Observable
    public SimpleCompositeEntity setActive(final boolean active) {
	this.active = active;
	return this;
    }

    public Integer getNumValue() {
	return numValue;
    }

    @Observable
    public SimpleCompositeEntity setNumValue(final Integer numValue) {
	this.numValue = numValue;
	return this;
    }
}
