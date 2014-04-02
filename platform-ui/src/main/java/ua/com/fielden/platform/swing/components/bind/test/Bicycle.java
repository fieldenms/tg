/**
 *
 */
package ua.com.fielden.platform.swing.components.bind.test;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.DomainValidation;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.types.Money;

/**
 * Dummy entity for Entity Grid Inspector usage example ({@link EntityGridInspectorExample})
 * 
 * @author Yura
 */
@KeyType(String.class)
public class Bicycle extends AbstractEntity<String> {

    private static final long serialVersionUID = 1L;

    @IsProperty
    private Bicycle friendly;

    @IsProperty
    private String frameName;

    @IsProperty
    private Integer year;

    @IsProperty
    private Money price;

    @IsProperty
    private Date date;

    @IsProperty
    private boolean inStock = true;

    public Bicycle() {
    }

    public Integer getYear() {
        return year;
    }

    @Observable
    @NotNull
    public Bicycle setYear(final Integer year) {
        this.year = year;
        return this;
    }

    public String getFrameName() {
        return frameName;
    }

    @NotNull
    @Observable
    public Bicycle setFrameName(final String frameName) {
        this.frameName = frameName;
        return this;
    }

    public Money getPrice() {
        return price;
    }

    @NotNull
    @Observable
    public Bicycle setPrice(final Money cost) {
        this.price = cost;
        return this;
    }

    public Date getDate() {
        return date;
    }

    @NotNull
    @Observable
    public Bicycle setDate(final Date date) {
        this.date = date;
        return this;
    }

    public Bicycle getFriendly() {
        return friendly;
    }

    @NotNull
    @Observable
    public Bicycle setFriendly(final Bicycle friendly) {
        this.friendly = friendly;
        return this;
    }

    public boolean isInStock() {
        return inStock;
    }

    @Observable
    @DomainValidation
    public Bicycle setInStock(final boolean inStock) {
        this.inStock = inStock;
        return this;
    }

    @Override
    public String toString() {
        return "Bicycle : " + super.toString();
    }

}