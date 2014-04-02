/**
 *
 */
package ua.com.fielden.platform.persistence.types;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.PersistedType;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.ISimplyMoneyWithTaxAndExTaxAmountType;

/**
 * This class is the same as ancestor one, but this class has another mapping, which uses {@link SimplyMoneyWithTaxAndExTaxAmountType} to map {@link Money} property.
 * 
 * @author Yura
 */
@KeyType(String.class)
@DescTitle("Description")
@MapEntityTo("SIMPLE_TAX_AND_EX_TAX_MONEY_CLASS_TABLE")
public class EntityWithExTaxAndTaxMoney extends AbstractEntity<String> {

    private static final long serialVersionUID = 6986558260127684377L;

    @IsProperty
    @MapTo("MONEY")
    @PersistedType(userType = ISimplyMoneyWithTaxAndExTaxAmountType.class)
    private Money money;

    protected EntityWithExTaxAndTaxMoney() {
    }

    @Observable
    public void setMoney(final Money money) {
        if (money == null) {
            throw new IllegalArgumentException("money should not be null");
        }
        if (money.getTaxAmount() == null) {
            throw new IllegalArgumentException("money should tax sensitive");
        }
        this.money = money;
    }

    public Money getMoney() {
        return money;
    }

}
