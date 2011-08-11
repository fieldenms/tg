package ua.com.platform.swing.review;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.Ignore;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.ResultOnly;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle(value = "key entity", desc = "key entity description")
@DescTitle(value = "desc title", desc = "desc title description")
public class KeyEntity extends AbstractEntity<String> {

    /**
     * 
     */
    private static final long serialVersionUID = -5321235058448207436L;

    @IsProperty
    @Title(value = "another property", desc = "another property description")
    private EntityWithEntityKey anotherProperty;

    @IsProperty
    @CritOnly
    @Title(value = "criteria property", desc = "criteira property description")
    private KeyEntity critEntityProperty;

    @IsProperty
    @ResultOnly
    @Title(value = "resultant property", desc = "resultant property description")
    private KeyEntity resultEntityProperty;

    @IsProperty
    @Title(value = "composite key property", desc = "composite key property description")
    private EntityWithCompositeKey compositeKeyEntity;

    @IsProperty
    @Ignore
    @Title(value = "string property title", desc = "string property desc")
    private String entityString;

    @IsProperty
    @Invisible
    @Title(value = "invisible string property title", desc = "invisible string property desc")
    private String invisibleEntityString;

    @IsProperty(String.class)
    @Title(value = "list string property title", desc = "list string property desc")
    private List<String> listPropertyString;

    @IsProperty(String.class)
    @Title(value = "enum string property title", desc = "enum string property desc")
    private PropertyEnum enumPropertyString;

    @IsProperty
    @Title(value = "key property title", desc = " key property description")
    private String keyProperty;

    public EntityWithEntityKey getAnotherProperty() {
	return anotherProperty;
    }

    public EntityWithCompositeKey getCompositeKeyEntity() {
	return compositeKeyEntity;
    }

    public KeyEntity getCritEntityProperty() {
	return critEntityProperty;
    }

    public KeyEntity getResultEntityProperty() {
	return resultEntityProperty;
    }

    public String getEntityString() {
	return entityString;
    }

    public String getInvisibleEntityString() {
	return invisibleEntityString;
    }

    public List<String> getListPropertyString() {
	return listPropertyString;
    }

    public PropertyEnum getEnumPropertyString() {
	return enumPropertyString;
    }

    public String getKeyProperty() {
	return keyProperty;
    }

    @Observable
    public void setAnotherProperty(final EntityWithEntityKey anotherProperty) {
	this.anotherProperty = anotherProperty;
    }

    @Observable
    public void setCompositeKeyEntity(final EntityWithCompositeKey compositeKeyEntity) {
	this.compositeKeyEntity = compositeKeyEntity;
    }

    @Observable
    public void setCritEntityProperty(final KeyEntity critEnttiyProperty) {
	this.critEntityProperty = critEnttiyProperty;
    }

    @Observable
    public void setResultEntityProperty(final KeyEntity resultEntityProperty) {
	this.resultEntityProperty = resultEntityProperty;
    }

    @Observable
    public void setEntityString(final String entityString) {
	this.entityString = entityString;
    }

    @Observable
    public void setInvisibleEntityString(final String invisibleEntityString) {
	this.invisibleEntityString = invisibleEntityString;
    }

    @Observable
    public void setListPropertyString(final List<String> listPropertyString) {
	this.listPropertyString = listPropertyString;
    }

    @Observable
    public void setEnumPropertyString(final PropertyEnum enumPropertyString) {
	this.enumPropertyString = enumPropertyString;
    }

    @Observable
    public void setKeyProperty(final String keyProperty) {
	this.keyProperty = keyProperty;
    }
}
