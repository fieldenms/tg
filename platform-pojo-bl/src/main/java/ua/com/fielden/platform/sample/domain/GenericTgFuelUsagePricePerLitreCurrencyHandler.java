package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.validation.AbstractDependentMoneyCurrencyHandler;
import ua.com.fielden.platform.types.either.Either;

import java.util.Currency;
import java.util.Locale;

import static java.lang.String.format;
import static ua.com.fielden.platform.sample.domain.TgFuelUsage.LOCATION_TO_CURRENCY;
import static ua.com.fielden.platform.types.either.Either.left;
import static ua.com.fielden.platform.types.either.Either.right;

public class GenericTgFuelUsagePricePerLitreCurrencyHandler<T> extends AbstractDependentMoneyCurrencyHandler<TgFuelUsage, T> {

    public static final String ERR_CANNOT_DETERMINE_FROM_LOCATION = "Currency cannot be determined from location [%s].";

    protected GenericTgFuelUsagePricePerLitreCurrencyHandler() {
        super("pricePerLitre");
    }

    @Override
    protected Either<String, Currency> currencyFrom(final TgFuelUsage entity) {
        if (entity.getLocation() == null) {
            return right(Currency.getInstance(Locale.getDefault()));
        }

        if (!LOCATION_TO_CURRENCY.containsKey(entity.getLocation())) {
            return left(format(ERR_CANNOT_DETERMINE_FROM_LOCATION, entity.getLocation()));
        }

        return right(Currency.getInstance(LOCATION_TO_CURRENCY.get(entity.getLocation())));
    }

}
