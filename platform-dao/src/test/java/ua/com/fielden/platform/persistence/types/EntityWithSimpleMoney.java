package ua.com.fielden.platform.persistence.types;

import ua.com.fielden.platform.dao.EntityWithSimpleMoneyDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.types.Money;

/**
 * This is a test entity, which is currently used for testing of class {@link Money} in simple (without currency) mapping configuration.
 *
 * @author 01es
 *
 */
@KeyType(String.class)
@DescTitle("Description")
@MapEntityTo("SIMPLE_MONEY_CLASS_TABLE")
@CompanionObject(EntityWithSimpleMoneyDao.class)
public class EntityWithSimpleMoney extends AbstractEntity<String> {

    @IsProperty
    @MapTo("MONEY")
    private Money money;

    protected EntityWithSimpleMoney() {}

    public EntityWithSimpleMoney(final String key, final String desc, final Money money) {
        super(null, key, desc);
        setMoney(money);
    }

    @Observable
    public EntityWithSimpleMoney setMoney(final Money money) {
        if (money == null) {
            throw new IllegalArgumentException("money should not be null");
        }
        this.money = money;
        return this;
    }

    public Money getMoney() {
        return money;
    }
}
