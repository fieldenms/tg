package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.sample.domain.TrivialPersistentEntity;
import ua.com.fielden.platform.types.Money;

import static ua.com.fielden.platform.entity.annotation.CritOnly.Type.MULTI;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

/**
 * Entity type for tests in {@link PersistentEntitySaverTest}.
 * <p>
 * Property naming convention is to use property nature as a prefix.
 */
@MapEntityTo
@KeyType(String.class)
@CompanionObject(PersistentEntityWithAllKindsOfPropertiesDao.class)
public class PersistentEntityWithAllKindsOfProperties extends AbstractPersistentEntity<String> {

    @IsProperty
    private TrivialPersistentEntity plainEntity;

    @IsProperty
    private String plainString;

    @IsProperty
    private Money plainMoney;

    @IsProperty
    @MapTo
    private TrivialPersistentEntity persistEntity;

    @IsProperty
    @MapTo
    private String persistString;

    @IsProperty
    @MapTo
    private Money persistMoney;

    @IsProperty
    @Calculated
    private String calcString;
    protected static final ExpressionModel calcString_ = expr().prop("persistString").model();

    @IsProperty
    @Calculated
    private Money calcMoney;
    protected static final ExpressionModel calcMoney_ = expr().prop("persistMoney").model();

    @IsProperty
    @CritOnly(MULTI)
    private String critString;

    @Observable
    public PersistentEntityWithAllKindsOfProperties setCritString(final String critString) {
        this.critString = critString;
        return this;
    }

    public String getCritString() {
        return critString;
    }

    @Observable
    protected PersistentEntityWithAllKindsOfProperties setCalcMoney(final Money calcMoney) {
        this.calcMoney = calcMoney;
        return this;
    }

    public Money getCalcMoney() {
        return calcMoney;
    }


    public String getCalcString() {
        return calcString;
    }

    @Observable
    public PersistentEntityWithAllKindsOfProperties setCalcString(final String calcString) {
        this.calcString = calcString;
        return this;
    }

    public Money getPersistMoney() {
        return persistMoney;
    }

    @Observable
    public PersistentEntityWithAllKindsOfProperties setPersistMoney(final Money persistMoney) {
        this.persistMoney = persistMoney;
        return this;
    }

    public TrivialPersistentEntity getPersistEntity() {
        return persistEntity;
    }

    @Observable
    public PersistentEntityWithAllKindsOfProperties setPersistEntity(final TrivialPersistentEntity persistEntity) {
        this.persistEntity = persistEntity;
        return this;
    }

    public String getPersistString() {
        return persistString;
    }

    @Observable
    public PersistentEntityWithAllKindsOfProperties setPersistString(final String persistString) {
        this.persistString = persistString;
        return this;
    }

    public Money getPlainMoney() {
        return plainMoney;
    }

    @Observable
    public PersistentEntityWithAllKindsOfProperties setPlainMoney(final Money plainMoney) {
        this.plainMoney = plainMoney;
        return this;
    }

    public String getPlainString() {
        return plainString;
    }

    @Observable
    public PersistentEntityWithAllKindsOfProperties setPlainString(final String plainString) {
        this.plainString = plainString;
        return this;
    }

    public TrivialPersistentEntity getPlainEntity() {
        return plainEntity;
    }

    @Observable
    public PersistentEntityWithAllKindsOfProperties setPlainEntity(final TrivialPersistentEntity plainEntity) {
        this.plainEntity = plainEntity;
        return this;
    }

}
