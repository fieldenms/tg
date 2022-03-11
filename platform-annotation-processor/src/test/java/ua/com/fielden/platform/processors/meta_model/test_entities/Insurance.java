package ua.com.fielden.platform.processors.meta_model.test_entities;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.annotations.GenerateMetaModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@GenerateMetaModel
public class Insurance extends AbstractEntity<String> {
    @IsProperty
    @MapTo
    @Title(value = "Cost", desc = "Insurance cost.")
    private BigDecimal cost;

    @IsProperty
    @MapTo
    @Title(value = "Expiration date", desc = "Date of expiration of this insurance.")
    private Date expirationDate;

    @Observable
    public Insurance setExpirationDate(final Date expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    @Observable
    public Insurance setCost(final BigDecimal cost) {
        this.cost = cost;
        return this;
    }

    public BigDecimal getCost() {
        return cost;
    }

    

}
