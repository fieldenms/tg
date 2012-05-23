package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Money;

/**
 * Entity with arbitrarily long validation process, which makes its instance locked as long as desired.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Entity No", desc = "Key Property")
@DescTitle(value = "Description", desc = "Description Property")
@DescRequired
public class LockedEntity extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    private boolean lockPropertyValidation = true;

    @IsProperty
    @Title(value = "First Property", desc = "used for testing")
    private Integer firstProperty = null;

    @IsProperty
    @Title("Observable Property")
    private Double observableProperty = 0.0;

    @IsProperty
    private Money money;


    public Integer getFirstProperty() {
	return firstProperty;
    }

    @NotNull
    @Observable
    public LockedEntity setFirstProperty(final Integer property) {
	this.firstProperty = property;
	return this;
    }

    public Double getObservableProperty() {
	return observableProperty;
    }

    @Observable
    public LockedEntity setObservableProperty(final Double observableProperty) {
	this.observableProperty = observableProperty;
	return this;
    }

    public Money getMoney() {
	return money;
    }

    @Observable
    public void setMoney(final Money money) {
	while (lockPropertyValidation) {
            try {
		Thread.sleep(10);
	    } catch (final InterruptedException e) {
	    }
        }
	this.money = money;
    }

    @Override
    protected Result validate() {
        return lockPropertyValidation ? Result.successful(this) : new Result(new IllegalStateException());
    }

     public boolean isLockPropertyValidation() {
        return lockPropertyValidation;
    }

    public void setLockPropertyValidation(final boolean lockPropertyValidation) {
        this.lockPropertyValidation = lockPropertyValidation;
    }
}