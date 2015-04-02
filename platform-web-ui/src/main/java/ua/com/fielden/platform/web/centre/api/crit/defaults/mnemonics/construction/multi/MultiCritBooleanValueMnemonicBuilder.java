package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.multi;

import java.util.Optional;

import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritBooleanValueMnemonic;

public class MultiCritBooleanValueMnemonicBuilder implements IMultiCritBooleanValueMnemonic {
    private Optional<Boolean> isValue = Optional.empty();
    private Optional<Boolean> isNotValue = Optional.empty();

    private boolean checkForMissingValue = false;
    private boolean negateCondition = false;


    @Override
    public MultiCritBooleanValueMnemonic value() {
        return new MultiCritBooleanValueMnemonic(isValue, isNotValue, checkForMissingValue, negateCondition);
    }


    @Override
    public IMultiCritBooleanValueMnemonic1 setIsValue(final boolean value) {
        isValue = Optional.of(value);
        return this;
    }


    @Override
    public IMultiCritBooleanValueMnemonic2 setIsNotValue(final boolean value) {
        isNotValue = Optional.of(value);
        return this;
    }


    @Override
    public IMultiCritBooleanValueMnemonic3 canHaveNoValue() {
        checkForMissingValue = true;
        return this;
    }


    @Override
    public IMultiCritBooleanValueMnemonic0 not() {
        negateCondition = true;
        return this;
    }


}
