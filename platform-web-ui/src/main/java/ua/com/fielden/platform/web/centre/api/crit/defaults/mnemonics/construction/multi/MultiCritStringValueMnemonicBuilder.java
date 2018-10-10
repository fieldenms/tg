package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.multi;

import static java.util.Arrays.asList;
import static java.util.Optional.of;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritStringValueMnemonic;

public class MultiCritStringValueMnemonicBuilder implements IMultiCritStringValueMnemonic, IMultiCritStringValueMnemonic1MissingValue {
    private Optional<List<String>> values = Optional.empty();

    private boolean checkForMissingValue = false;
    private boolean negateCondition = false;

    @Override
    public MultiCritStringValueMnemonic value() {
        return new MultiCritStringValueMnemonic(values, checkForMissingValue, negateCondition);
    }

    @Override
    public IMultiCritStringValueMnemonic1MissingValue setValues(final String... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("At least one values is expected.");
        }
        
        // Simple ArrayList must be used here instead of Arrays$ArrayList.
        // This is due to generic collection copying in EntityUtils.copyCollectionalValue (used in MetaPropertyFull.setOriginalValue) that is implemented through obtaining of empty constructor and addAll method.
        // Unfortunately Arrays$ArrayList does not have empty constructor.
        final List<String> list = new ArrayList<>(asList(values));
        this.values = of(list);
        return this;
    }

    @Override
    public IMultiCritStringValueMnemonic2Value canHaveNoValue() {
        this.checkForMissingValue = true;
        return this;
    }

    @Override
    public IMultiCritStringValueMnemonic0Values not() {
        this.negateCondition = true;
        return this;
    }

}
