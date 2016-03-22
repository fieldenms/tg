package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.range;

import java.util.Optional;

import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritOtherValueMnemonic;

public class RangeCritOtherValueMnemonicBuilder<V>
        implements IRangeCritOtherValueMnemonic<V>,
        IRangeCritOtherValueMnemonic1ToValue<V> {

    private Optional<V> fromValue = Optional.empty();
    private Optional<V> toValue = Optional.empty();

    private Optional<Boolean> excludeFrom = Optional.empty();
    private Optional<Boolean> excludeTo = Optional.empty();

    private boolean checkForMissingValue = false;
    private boolean negateCondition = false;

    @Override
    public RangeCritOtherValueMnemonic<V> value() {
        return new RangeCritOtherValueMnemonic<V>(
                fromValue,
                toValue,
                excludeFrom,
                excludeTo,
                checkForMissingValue,
                negateCondition
                );
    }

    @Override
    public IRangeCritOtherValueMnemonic0FromValue<V> not() {
        this.negateCondition = true;
        return this;
    }

    @Override
    public IRangeCritOtherValueMnemonic3Value<V> canHaveNoValue() {
        this.checkForMissingValue = true;
        return this;
    }

    @Override
    public IRangeCritOtherValueMnemonic1ToValue<V> setFromValue(final V from) {
        if (from == null) {
            throw new IllegalArgumentException("The from value cannot be null.");
        }
        this.fromValue = Optional.of(from);
        return this;
    }

    @Override
    public IRangeCritOtherValueMnemonic1ToValue<V> setFromValueExclusive(final V from) {
        if (from == null) {
            throw new IllegalArgumentException("The from value cannot be null.");
        }
        this.fromValue = Optional.of(from);
        this.excludeFrom = Optional.of(true);
        return this;
    }

    @Override
    public IRangeCritOtherValueMnemonic2MissingValue<V> setToValue(final V to) {
        if (to == null) {
            throw new IllegalArgumentException("The to value cannot be null.");
        }
        this.toValue = Optional.of(to);
        return this;
    }

    @Override
    public IRangeCritOtherValueMnemonic2MissingValue<V> setToValueExclusive(final V to) {
        if (to == null) {
            throw new IllegalArgumentException("The to value cannot be null.");
        }
        this.toValue = Optional.of(to);
        this.excludeTo = Optional.of(true);
        return this;
    }

}
