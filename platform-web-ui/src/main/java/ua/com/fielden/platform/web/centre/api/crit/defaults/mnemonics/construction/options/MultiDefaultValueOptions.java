package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options;

import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.multi.IMultiCritBooleanValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.multi.IMultiCritStringValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.multi.MultiCritBooleanValueMnemonicBuilder;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.multi.MultiCritStringValueMnemonicBuilder;

public class MultiDefaultValueOptions {

    public IMultiCritBooleanValueMnemonic bool() {
        return new MultiCritBooleanValueMnemonicBuilder();
    }

    public IMultiCritStringValueMnemonic string() {
        return new MultiCritStringValueMnemonicBuilder();
    }
}
