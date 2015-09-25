package ua.com.fielden.platform.sample.domain;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Colour;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgEntityForColourMaster.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgEntityForColourMaster extends AbstractEntity<String> {
    @IsProperty
    @MapTo
    @Title(value = "String prop", desc = "String prop description")
    private String stringProp;

    @IsProperty
    @MapTo
    @Title(value = "Boolean prop", desc = " Boolean prop description")
    private Boolean booleanProp;

    @IsProperty
    @MapTo
    @Title(value = "Date prop", desc = "Date prop desc")
    private Date dateProp;

    @IsProperty
    @MapTo
    @Title(value = "Colour prop", desc = "Colour prop description")
    private Colour colourProp;

    @IsProperty
    @MapTo
    @Title(value = "Producer initialised prop", desc = "Producer initialised prop desc")
    private TgEntityForColourMaster producerInitProp;

    @IsProperty
    @MapTo
    @Title(value = "Domain initialised prop", desc = "The property that was initialised directly inside Entity type definition Java class")
    private String domainInitProp = "ok";

    @Observable
    public TgEntityForColourMaster setColourProp(final Colour colourProp) {
        this.colourProp = colourProp;
        return this;
    }

    public Colour getColourProp() {
        return colourProp;
    }

    @Observable
    public TgEntityForColourMaster setDateProp(final Date dateProp) {
        this.dateProp = dateProp;
        return this;
    }

    public Date getDateProp() {
        return dateProp;
    }

    @Observable
    public TgEntityForColourMaster setBooleanProp(final Boolean booleanProp) {
        this.booleanProp = booleanProp;
        return this;
    }

    public Boolean getBooleanProp() {
        return booleanProp;
    }

    @Observable
    public TgEntityForColourMaster setStringProp(final String stringProp) {
        this.stringProp = stringProp;
        return this;
    }

    public String getStringProp() {
        return stringProp;
    }

    @Observable
    public TgEntityForColourMaster setDomainInitProp(final String domainInitProp) {
        this.domainInitProp = domainInitProp;
        return this;
    }

    public String getDomainInitProp() {
        return domainInitProp;
    }

    @Observable
    public TgEntityForColourMaster setProducerInitProp(final TgEntityForColourMaster producerInitProp) {
        this.producerInitProp = producerInitProp;
        return this;
    }

    public TgEntityForColourMaster getProducerInitProp() {
        return producerInitProp;
    }

}