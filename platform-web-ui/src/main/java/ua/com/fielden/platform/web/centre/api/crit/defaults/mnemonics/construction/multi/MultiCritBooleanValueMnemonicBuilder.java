package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.multi;

import java.util.Optional;

import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritBooleanValueMnemonic;

public class MultiCritBooleanValueMnemonicBuilder implements IMultiCritBooleanValueMnemonic, IMultiCritBooleanValueMnemonic1IsNotValue, IMultiCritBooleanValueMnemonic2MissingValue {
    private Optional<Boolean> isValue = Optional.empty();
    private Optional<Boolean> isNotValue = Optional.empty();

    private boolean checkForMissingValue = false;
    private boolean negateCondition = false;


    @Override
    public MultiCritBooleanValueMnemonic value() {
        return new MultiCritBooleanValueMnemonic(isValue, isNotValue, checkForMissingValue, negateCondition);
    }


    @Override
    public IMultiCritBooleanValueMnemonic1IsNotValue setIsValue(final boolean value) {
        isValue = Optional.of(value);
        return this;
    }


    @Override
    public IMultiCritBooleanValueMnemonic2MissingValue setIsNotValue(final boolean value) {
        isNotValue = Optional.of(value);
        return this;
    }


    @Override
    public IMultiCritBooleanValueMnemonic3Value canHaveNoValue() {
        checkForMissingValue = true;
        return this;
    }


    @Override
    public IMultiCritBooleanValueMnemonic0IsValue not() {
        negateCondition = true;
        return this;
    }


}
