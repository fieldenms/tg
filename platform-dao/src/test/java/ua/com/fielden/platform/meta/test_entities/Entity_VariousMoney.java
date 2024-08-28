package ua.com.fielden.platform.meta.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.persistence.types.MoneyWithTaxAmountType;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.ISimpleMoneyType;

@MapEntityTo
@KeyType(String.class)
public class Entity_VariousMoney extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @PersistentType(userType = ISimpleMoneyType.class)
    private Money simpleMoney;

    @IsProperty
    @MapTo
    @PersistentType(userType = MoneyWithTaxAmountType.class)
    private Money moneyWithTax;

    @IsProperty
    @Calculated("simpleMoney")
    private Money calcSimpleMoney;

    @IsProperty
    @Calculated("moneyWithTax")
    private Money calcMoneyWithTax;

    public Money getCalcMoneyWithTax() {
        return calcMoneyWithTax;
    }

    @Observable
    public Entity_VariousMoney setCalcMoneyWithTax(final Money calcMoneyWithTax) {
        this.calcMoneyWithTax = calcMoneyWithTax;
        return this;
    }

    @Observable
    protected Entity_VariousMoney setCalcSimpleMoney(final Money calcSimpleMoney) {
        this.calcSimpleMoney = calcSimpleMoney;
        return this;
    }

    public Money getCalcSimpleMoney() {
        return calcSimpleMoney;
    }

    public Money getMoneyWithTax() {
        return moneyWithTax;
    }

    @Observable
    public Entity_VariousMoney setMoneyWithTax(final Money moneyWithTax) {
        this.moneyWithTax = moneyWithTax;
        return this;
    }

    public Money getSimpleMoney() {
        return simpleMoney;
    }

    @Observable
    public Entity_VariousMoney setSimpleMoney(final Money simpleMoney) {
        this.simpleMoney = simpleMoney;
        return this;
    }

}
