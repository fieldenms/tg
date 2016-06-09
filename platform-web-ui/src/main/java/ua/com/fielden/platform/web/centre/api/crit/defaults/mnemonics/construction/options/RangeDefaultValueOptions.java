package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options;

import java.math.BigDecimal;

import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.range.IRangeCritDateValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.range.IRangeCritOtherValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.range.RangeCritDateValueMnemonicBuilder;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.range.RangeCritOtherValueMnemonicBuilder;

public class RangeDefaultValueOptions {
    public IRangeCritDateValueMnemonic date() {
        return new RangeCritDateValueMnemonicBuilder();
    }

    public IRangeCritOtherValueMnemonic<BigDecimal> decimal() {
        return new RangeCritOtherValueMnemonicBuilder<BigDecimal>();
    }

    public IRangeCritOtherValueMnemonic<Integer> integer() {
        return new RangeCritOtherValueMnemonicBuilder<Integer>();
    }
}
