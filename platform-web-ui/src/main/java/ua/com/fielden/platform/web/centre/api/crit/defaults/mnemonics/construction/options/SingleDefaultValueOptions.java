package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options;

import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.single.ISingleCritDateValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.single.SingleCritDateValueMnemonicBuilder;

public class SingleDefaultValueOptions {
    //void text();
    //void entity();
    //void integer();
    //void decimal();

    public ISingleCritDateValueMnemonic date() {
        return new SingleCritDateValueMnemonicBuilder();
    }
}
