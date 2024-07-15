package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * Entity for {@link IWebApi} testing.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@CompanionObject(ITgWebApiEntity.class)
@MapEntityTo
@DescTitle("Description")
@DisplayDescription
public class TgWebApiEntity extends ActivatableAbstractEntity<String> {
    
    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgWebApiEntity.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();
    
    @IsProperty
    @MapTo
    @Title(value = "Int Prop", desc = "Integer property.")
    private Integer intProp;
    
    @IsProperty
    @MapTo
    @Title(value = "Long Prop", desc = "Long property.")
    private Long longProp;
    
    @IsProperty(precision = 10, scale = 4)
    @MapTo
    @Title(value = "BigDecimal Prop", desc = "BigDecimal property.")
    private BigDecimal bigDecimalProp;
    
    @IsProperty
    @MapTo
    @Title(value = "Money Prop", desc = "Money property.")
    private Money moneyProp;
    
    @IsProperty
    @MapTo
    @Title(value = "Date Prop", desc = "Date property.")
    private Date dateProp;
    
    @IsProperty
    @MapTo
    @Title(value = "Hyperlink Prop", desc = "Hyperlink property.")
    private Hyperlink hyperlinkProp;
    
    @IsProperty
    @MapTo
    @Title(value = "Colour Prop", desc = "Colour property.")
    private Colour colourProp;
    
    @IsProperty
    @MapTo
    @Title("Model")
    private TgVehicleModel model;
    
    public TgVehicleModel getModel() {
        return model;
    }
    
    @Observable
    public TgWebApiEntity setModel(final TgVehicleModel model) {
        this.model = model;
        return this;
    }
    
    @Observable
    public TgWebApiEntity setColourProp(final Colour colourProp) {
        this.colourProp = colourProp;
        return this;
    }
    
    public Colour getColourProp() {
        return colourProp;
    }
    
    @Observable
    public TgWebApiEntity setHyperlinkProp(final Hyperlink hyperlinkProp) {
        this.hyperlinkProp = hyperlinkProp;
        return this;
    }
    
    public Hyperlink getHyperlinkProp() {
        return hyperlinkProp;
    }
    
    @Observable
    public TgWebApiEntity setDateProp(final Date dateProp) {
        this.dateProp = dateProp;
        return this;
    }
    
    public Date getDateProp() {
        return dateProp;
    }
    
    @Observable
    public TgWebApiEntity setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
        return this;
    }
    
    public Money getMoneyProp() {
        return moneyProp;
    }
    
    @Observable
    public TgWebApiEntity setBigDecimalProp(final BigDecimal bigDecimalProp) {
        this.bigDecimalProp = bigDecimalProp;
        return this;
    }
    
    public BigDecimal getBigDecimalProp() {
        return bigDecimalProp;
    }
    
    @Observable
    public TgWebApiEntity setLongProp(final Long longProp) {
        this.longProp = longProp;
        return this;
    }
    
    public Long getLongProp() {
        return longProp;
    }
    
    @Observable
    public TgWebApiEntity setIntProp(final Integer intProp) {
        this.intProp = intProp;
        return this;
    }
    
    public Integer getIntProp() {
        return intProp;
    }
    
    @Observable
    @Override
    public TgWebApiEntity setActive(final boolean active) {
        super.setActive(active);
        return this;
    }
    
    @Observable
    @Override
    public TgWebApiEntity setKey(final String key) {
        super.setKey(key);
        return this;
    }
    
}