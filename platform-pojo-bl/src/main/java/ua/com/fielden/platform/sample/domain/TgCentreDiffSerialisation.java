package ua.com.fielden.platform.sample.domain;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Entity that is used in CentreUpdaterTest for testing centre diff serialisation.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@CompanionObject(ITgCentreDiffSerialisation.class)
@DescTitle("Desc")
public class TgCentreDiffSerialisation extends AbstractEntity<String> {
    
    @IsProperty
    @Title("String Prop")
    private String stringProp;
    
    @IsProperty
    @Title("Date Prop")
    private Date dateProp;
    
    @IsProperty
    @Title("Date Prop Default")
    private Date datePropDefault;
    
    @IsProperty
    @Title("Date Prop Default Mnemonics")
    private Date datePropDefaultMnemonics;
    
    @Observable
    public TgCentreDiffSerialisation setDatePropDefaultMnemonics(final Date datePropDefaultMnemonics) {
        this.datePropDefaultMnemonics = datePropDefaultMnemonics;
        return this;
    }
    
    public Date getDatePropDefaultMnemonics() {
        return datePropDefaultMnemonics;
    }
    
    @Observable
    public TgCentreDiffSerialisation setDatePropDefault(final Date datePropDefault) {
        this.datePropDefault = datePropDefault;
        return this;
    }
    
    public Date getDatePropDefault() {
        return datePropDefault;
    }
    
    @Observable
    public TgCentreDiffSerialisation setDateProp(final Date dateProp) {
        this.dateProp = dateProp;
        return this;
    }
    
    public Date getDateProp() {
        return dateProp;
    }
    
    @Observable
    public TgCentreDiffSerialisation setStringProp(final String stringProp) {
        this.stringProp = stringProp;
        return this;
    }
    
    public String getStringProp() {
        return stringProp;
    }
    
}