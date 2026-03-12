package ua.com.fielden.platform.entity.validation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.either.Left;
import ua.com.fielden.platform.types.either.Right;

import java.lang.annotation.Annotation;
import java.util.Currency;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

/// A base class for ACE and BCE handlers (definers and validators) that supports modelling of [Money]-typed properties
/// whose currency depends on other properties of the same entity.
///
/// To illustrate how to use this base class, consider the following example:
///
/// ```
/// class PurchaseOrder {
///     @IsProperty
///     @AfterChange(GenericPurchaseOrderCostCurrencyHandler.class)
///     String location;
///
///     @IsProperty
///     @AfterChange(GenericPurchaseOrderCostCurrencyHandler.class)
///     boolean international;
///
///     @IsProperty
///     @PersistentType(value = IMoneyType.class) // [amount, currency]
///     @BeforeChange(@Handler(GenericPurchaseOrderCostCurrencyHandler.class))
///     @AfterChange(GenericPurchaseOrderCostCurrencyHandler.class)
///     Money cost;
/// }
///
/// class GenericPurchaseOrderCostCurrencyHandler<T>
///       extends AbstractDependentMoneyCurrencyHandler<PurchaseOrder, T>
/// {
///     protected GenericPurchaseOrderCostCurrencyHandler() {
///         super("cost");
///     }
///
///     protected Either<String, Currency> tryCurrency(PurchaseOrder po) { ... }
/// }
/// ```
///
/// In this example, `cost.currency` depends on `location` and `international`.
///
/// Class `GenericPurchaseOrderCostCurrencyHandler` implements determination of context-dependent currency for property `cost`.
/// It calls the super constructor which requires the name of the dependent [Money]-typed property.
/// It implements method [#currencyFrom] which determines the currency for property `cost` of a given purchase order.
///
/// Class `GenericPurchaseOrderCostCurrencyHandler` can be used as both a validator and a definer, hence generic.
/// * As a definer it is applicable to both the [Money]-typed property and the properties that determine the currency.
///   * For the dependent [Money]-typed property, it simply replaces the currency with the result of [#currencyFrom] if it returns [Right].
///   * For other properties, it revalidates the dependent [Money]-typed property and adjusts its currency based on the result of [#currencyFrom].
/// * As a validator it is applicable only to the [Money]-typed property.
///   If [#currencyFrom] returns a left value, validation fails with that value as the message; otherwise, validation succeeds.
///
/// It is possible to subclass `GenericPurchaseOrderCostCurrencyHandler` to specialise it for a particular property,
/// either as a definer or a validator.
/// The subclass must then specify the property type for type parameter `<T>`.
///
/// @param <E>  type of the entity that contains the property associated with this handler
/// @param <T>  type of the property associated with this handler.
///             This type parameter should be repeated in generic handlers.
///
public abstract class AbstractDependentMoneyCurrencyHandler<E extends AbstractEntity<?>, T>
        implements IAfterChangeEventHandler<T>,
                   IBeforeChangeEventHandler<Money>
{

    private final String dependentMoneyProp;

    protected AbstractDependentMoneyCurrencyHandler(final CharSequence dependentMoneyProp) {
        this.dependentMoneyProp = dependentMoneyProp.toString();
    }

    /// Determines the currency for the dependent [Money]-typed property.
    /// If the currency can be determined, returns [Right] containing it.
    /// Otherwise, returns [Left] with an error message.
    ///
    /// Implementations should not rely on the current value of the [Money]-typed property in `entity`.
    /// Instead, the currency should be determined from other properties.
    ///
    protected abstract Either<String, Currency> currencyFrom(E entity);

    @Override
    public void handle(final MetaProperty<T> property, final T value) {
        if (property.getName().equals(dependentMoneyProp)) {
            defineDependent((MetaProperty<Money>) property, (Money) value);
        }
        else {
            defineOther(property, value);
        }
    }

    @Override
    public Result handle(final MetaProperty<Money> property, final Money newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return successful();
        }
        return switch (currencyFrom(property.getEntity())) {
            case Left(var msg) -> failure(msg);
            case Right _ -> successful();
        };
    }

    private void defineDependent(final MetaProperty<Money> property, final Money value) {
        final E entity = property.getEntity();

        if (!entity.isInitialising() && value != null) {
            switch (currencyFrom(entity)) {
                case Left _ -> {}
                case Right(var currency) -> {
                    // This method acts as a self-reassigning definer, therefore enforcement must be avoided to prevent non-termination.
                    final var enforce = false;
                    property.setValue(new Money(value.getAmount(), currency), enforce);
                }
            }
        }
    }

    private void defineOther(final MetaProperty<T> property, final T value) {
        final E entity = property.getEntity();

        // If Money is persisted, it already has the correct currency.
        if (!entity.isInitialising()) {
            final var mpDependentMoney = entity.<Money>getProperty(dependentMoneyProp);
            final var money = !mpDependentMoney.isValid() ? mpDependentMoney.getLastInvalidValue() : mpDependentMoney.getValue();

            if (money != null) {
                final var result = mpDependentMoney.revalidate(false);
                if (result.isSuccessful()) {
                    final var newMoney = switch (currencyFrom(entity)) {
                        case Left _ -> money;
                        case Right(var currency) -> new Money(money.getAmount(), currency);
                    };
                    mpDependentMoney.setValue(newMoney);
                }
            }
        }
    }

}
