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

/**
 * Entity that is used in CentreUpdaterTest for testing centre diff serialisation; used as property value in {@link TgCentreDiffSerialisation}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@CompanionObject(ITgCentreDiffSerialisationPersistentChild.class)
@DescTitle("Desc")
@MapEntityTo
public class TgCentreDiffSerialisationPersistentChild extends AbstractEntity<String> {
    
    @IsProperty
    @Title("String Prop")
    @MapTo
    private String stringProp;
    
    @IsProperty
    @Title("Date Prop")
    @MapTo
    private Date dateProp;
    
    @Observable
    public TgCentreDiffSerialisationPersistentChild setDateProp(final Date dateProp) {
        this.dateProp = dateProp;
        return this;
    }
    
    public Date getDateProp() {
        return dateProp;
    }
    
    @Observable
    public TgCentreDiffSerialisationPersistentChild setStringProp(final String stringProp) {
        this.stringProp = stringProp;
        return this;
    }
    
    public String getStringProp() {
        return stringProp;
    }
    
}