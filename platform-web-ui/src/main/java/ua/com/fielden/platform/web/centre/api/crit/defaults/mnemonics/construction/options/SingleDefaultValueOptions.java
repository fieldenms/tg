package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.single.ISingleCritDateValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.single.ISingleCritOtherValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.single.SingleCritDateValueMnemonicBuilder;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.single.SingleCritOtherValueMnemonicBuilder;

public class SingleDefaultValueOptions {

    public ISingleCritOtherValueMnemonic<String> text() {
        return new SingleCritOtherValueMnemonicBuilder<String>();
    }

    public ISingleCritOtherValueMnemonic<Boolean> bool() {
        return new SingleCritOtherValueMnemonicBuilder<Boolean>();
    }

    public <V extends AbstractEntity<?>> ISingleCritOtherValueMnemonic<V> entity(final Class<V> type) {
        return new SingleCritOtherValueMnemonicBuilder<V>();
    }

    public ISingleCritOtherValueMnemonic<Integer> integer() {
        return new SingleCritOtherValueMnemonicBuilder<Integer>();
    }

    public ISingleCritOtherValueMnemonic<BigDecimal> decimal() {
        return new SingleCritOtherValueMnemonicBuilder<BigDecimal>();
    }

    public ISingleCritDateValueMnemonic date() {
        return new SingleCritDateValueMnemonicBuilder();
    }
}
