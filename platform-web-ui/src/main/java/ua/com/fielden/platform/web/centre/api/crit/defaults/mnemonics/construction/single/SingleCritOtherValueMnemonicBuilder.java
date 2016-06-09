package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.single;

import java.util.Optional;

import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritOtherValueMnemonic;

public class SingleCritOtherValueMnemonicBuilder<V>
        implements ISingleCritOtherValueMnemonic<V>,
        ISingleCritOtherValueMnemonic1MissingValue<V> {

    private Optional<V> value = Optional.empty();

    private boolean checkForMissingValue = false;
    private boolean negateCondition = false;

    @Override
    public SingleCritOtherValueMnemonic<V> value() {
        return new SingleCritOtherValueMnemonic<V>(
                value,
                checkForMissingValue,
                negateCondition
                );
    }

    @Override
    public ISingleCritOtherValueMnemonic0Value<V> not() {
        this.negateCondition = true;
        return this;
    }

    @Override
    public ISingleCritOtherValueMnemonic2Value<V> canHaveNoValue() {
        this.checkForMissingValue = true;
        return this;
    }

    @Override
    public ISingleCritOtherValueMnemonic1MissingValue<V> setValue(final V value) {
        if (value == null) {
            throw new IllegalArgumentException("The value cannot be null.");
        }
        this.value = Optional.of(value);
        return this;
    }

}
