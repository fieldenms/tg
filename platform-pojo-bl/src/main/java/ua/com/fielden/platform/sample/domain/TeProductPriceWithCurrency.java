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
@CompanionObject(TeProductPriceWithCurrencyCo.class)
@KeyTitle("Product and Price")
@EntityTitle("Product Price with Currency")
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

    @IsProperty
    @MapTo
    @Title("Other")
    private TeProductPriceWithCurrency other;

    public TeProductPriceWithCurrency getOther() {
        return other;
    }

    @Observable
    public TeProductPriceWithCurrency setOther(final TeProductPriceWithCurrency other) {
        this.other = other;
        return this;
    }

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
