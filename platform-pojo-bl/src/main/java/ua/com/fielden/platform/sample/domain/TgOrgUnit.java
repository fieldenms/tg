package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@EntityTitle(value = "Підрозділ", desc = "Організаційний підрозділ.")
@KeyTitle(value = "Назва", desc = "Назва організаційного підрозділу.")
@CompanionObject(ITgOrgUnit.class)
@MapEntityTo
@DescTitle("Опис")
public class TgOrgUnit extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Sap R/3 код", desc = "Код у системі Sap R/3")
    private String sapId;

    @IsProperty
    @MapTo
    @Title(value = "Sap R/3 назва", desc = "Назва організаційного підрозділу у системі Sap R/3")
    private String sapName;

    @Observable
    public TgOrgUnit setSapName(final String sapName) {
	this.sapName = sapName;
	return this;
    }

    public String getSapName() {
	return sapName;
    }

    @Observable
    public TgOrgUnit setSapId(final String sapId) {
	this.sapId = sapId;
	return this;
    }

    public String getSapId() {
	return sapId;
    }

}