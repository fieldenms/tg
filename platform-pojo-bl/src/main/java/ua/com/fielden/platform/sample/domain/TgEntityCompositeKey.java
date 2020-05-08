package ua.com.fielden.platform.sample.domain;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Optional;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;

/**
 * Master entity object.
 *
 * @author TG Team
 *
 */
@KeyType(value = DynamicEntityKey.class, keyMemberSeparator= ":")
@CompanionObject(ITgEntityCompositeKey.class)
@MapEntityTo
public class TgEntityCompositeKey extends AbstractPersistentEntity<DynamicEntityKey> {
    
    @IsProperty
    @MapTo
    @Title("String Key")
    @CompositeKeyMember(1)
    @Optional
    private String stringKey;
    
    @IsProperty
    @MapTo
    @Title("Boolean Key")
    @CompositeKeyMember(2)
    @Optional
    private boolean booleanKey;
    
    @IsProperty
    @MapTo
    @Title("Date Key")
    @CompositeKeyMember(3)
    @Optional
    private Date dateKey;
    
    @IsProperty
    @MapTo
    @Title("Integer Key")
    @CompositeKeyMember(4)
    @Optional
    private Integer integerKey;
    
    @IsProperty
    @MapTo
    @Title("Long Key")
    @CompositeKeyMember(5)
    @Optional
    private Long longKey;
    
    @IsProperty
    @MapTo
    @Title("Money Key")
    @CompositeKeyMember(6)
    @Optional
    private Money moneyKey;
    
    @IsProperty
    @MapTo
    @Title("Colour Key")
    @CompositeKeyMember(7)
    @Optional
    private Colour colourKey;
    
    @IsProperty
    @MapTo
    @Title("Hyperlink Key")
    @CompositeKeyMember(8)
    @Optional
    private Hyperlink hyperlinkKey;
    
    @IsProperty
    @MapTo
    @Title("Entity Key")
    @CompositeKeyMember(9)
    @Optional
    private TgEntityStringKey entityKey;
    
    @Observable
    public TgEntityCompositeKey setEntityKey(final TgEntityStringKey entityKey) {
        this.entityKey = entityKey;
        return this;
    }
    
    public TgEntityStringKey getEntityKey() {
        return entityKey;
    }
    
    @Observable
    public TgEntityCompositeKey setHyperlinkKey(final Hyperlink hyperlinkKey) {
        this.hyperlinkKey = hyperlinkKey;
        return this;
    }
    
    public Hyperlink getHyperlinkKey() {
        return hyperlinkKey;
    }
    
    @Observable
    public TgEntityCompositeKey setColourKey(final Colour colourKey) {
        this.colourKey = colourKey;
        return this;
    }
    
    public Colour getColourKey() {
        return colourKey;
    }
    
    @Observable
    public TgEntityCompositeKey setMoneyKey(final Money moneyKey) {
        this.moneyKey = moneyKey;
        return this;
    }
    
    public Money getMoneyKey() {
        return moneyKey;
    }
    
    @Observable
    public TgEntityCompositeKey setLongKey(final Long longKey) {
        this.longKey = longKey;
        return this;
    }
    
    public Long getLongKey() {
        return longKey;
    }
    
    @Observable
    public TgEntityCompositeKey setIntegerKey(final Integer integerKey) {
        this.integerKey = integerKey;
        return this;
    }
    
    public Integer getIntegerKey() {
        return integerKey;
    }
    
    @Observable
    public TgEntityCompositeKey setDateKey(final Date dateKey) {
        this.dateKey = dateKey;
        return this;
    }
    
    public Date getDateKey() {
        return dateKey;
    }
    
    @Observable
    public TgEntityCompositeKey setBooleanKey(final boolean booleanKey) {
        this.booleanKey = booleanKey;
        return this;
    }
    
    public boolean getBooleanKey() {
        return booleanKey;
    }
    
    @Observable
    public TgEntityCompositeKey setStringKey(final String stringKey) {
        this.stringKey = stringKey;
        return this;
    }
    
    public String getStringKey() {
        return stringKey;
    }
    
}