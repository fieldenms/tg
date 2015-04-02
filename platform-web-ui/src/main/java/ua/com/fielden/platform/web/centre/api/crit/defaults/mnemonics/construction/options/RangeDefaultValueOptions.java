package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options;

import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.range.IRangeCritDateValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.range.RangeCritDateValueMnemonicBuilder;

public class RangeDefaultValueOptions {
    public IRangeCritDateValueMnemonic date() {
        return new RangeCritDateValueMnemonicBuilder();
    }
    //void decimal();
    //void integer();
}
