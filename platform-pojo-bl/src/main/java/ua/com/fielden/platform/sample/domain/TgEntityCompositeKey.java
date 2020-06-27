package ua.com.fielden.platform.sample.domain;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @CompositeKeyMember(1)
    @Optional
    private String stringKey;
    
    @IsProperty
    @MapTo
    @CompositeKeyMember(2)
    @Optional
    private Date dateKey;
    
    @IsProperty
    @MapTo
    @CompositeKeyMember(3)
    @Optional
    private Integer integerKey;
    
    @IsProperty
    @MapTo
    @CompositeKeyMember(4)
    @Optional
    private Long longKey;
    
    @IsProperty(precision = 18, scale = 4)
    @MapTo
    @CompositeKeyMember(5)
    @Optional
    private BigDecimal bigDecimalKey;
    
    @IsProperty
    @MapTo
    @CompositeKeyMember(6)
    @Optional
    private Money moneyKey;
    
    @IsProperty
    @MapTo
    @CompositeKeyMember(7)
    @Optional
    private Colour colourKey;
    
    @IsProperty
    @MapTo
    @CompositeKeyMember(8)
    @Optional
    private Hyperlink hyperlinkKey;
    
    @IsProperty
    @MapTo
    @CompositeKeyMember(9)
    @Optional
    private TgEntityStringKey entityKey;
    
    @IsProperty
    @MapTo
    @CompositeKeyMember(10)
    @Optional
    private TgEntityCompositeKey selfKey;
    
    @IsProperty(TgEntityStringKey.class)
    private final Set<TgEntityCompositeKey> setOfEntitiesProp = new HashSet<>();
    
    @IsProperty(String.class)
    private final List<String> listOfStringsProp = new ArrayList<>();
    
    @IsProperty(TgEntityTwoEntityKeys.class)
    private final Set<TgEntityTwoEntityKeys> shortCollectionalProp = new HashSet<>();
    
    @Observable
    protected TgEntityCompositeKey setShortCollectionalProp(final Set<TgEntityTwoEntityKeys> shortCollectionalProp) {
        this.shortCollectionalProp.clear();
        this.shortCollectionalProp.addAll(shortCollectionalProp);
        return this;
    }
    
    public Set<TgEntityTwoEntityKeys> getShortCollectionalProp() {
        return unmodifiableSet(shortCollectionalProp);
    }
    
    @Observable
    protected TgEntityCompositeKey setListOfStringsProp(final List<String> listProp) {
        this.listOfStringsProp.clear();
        this.listOfStringsProp.addAll(listProp);
        return this;
    }
    
    public List<String> getListOfStringsProp() {
        return unmodifiableList(listOfStringsProp);
    }
    
    @Observable
    protected TgEntityCompositeKey setSetOfEntitiesProp(final Set<TgEntityCompositeKey> setOfEntitiesProp) {
        this.setOfEntitiesProp.clear();
        this.setOfEntitiesProp.addAll(setOfEntitiesProp);
        return this;
    }
    
    public Set<TgEntityCompositeKey> getSetOfEntitiesProp() {
        return unmodifiableSet(setOfEntitiesProp);
    }
    
    @Observable
    public TgEntityCompositeKey setSelfKey(final TgEntityCompositeKey selfKey) {
        this.selfKey = selfKey;
        return this;
    }
    
    public TgEntityCompositeKey getSelfKey() {
        return selfKey;
    }
    
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
    public TgEntityCompositeKey setBigDecimalKey(final BigDecimal bigDecimalKey) {
        this.bigDecimalKey = bigDecimalKey;
        return this;
    }
    
    public BigDecimal getBigDecimalKey() {
        return bigDecimalKey;
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
    public TgEntityCompositeKey setStringKey(final String stringKey) {
        this.stringKey = stringKey;
        return this;
    }
    
    public String getStringKey() {
        return stringKey;
    }
    
}