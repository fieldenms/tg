package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.IMoneyType;

/// Like [TeProductPrice] but [#price] contains currency.
///
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionIsGenerated
public class TeProductPriceWithCurrency extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private String product;

    @IsProperty
    @MapTo
    @PersistentType(userType = IMoneyType.class)
    @CompositeKeyMember(2)
    private Money price;

    @Observable
    public TeProductPriceWithCurrency setProduct(final String product) {
        this.product = product;
        return this;
    }

    public String getProduct() {
        return product;
    }

    @Observable
    public TeProductPriceWithCurrency setPrice(final Money price) {
        this.price = price;
        return this;
    }

    public Money getPrice() {
        return price;
    }

}
