package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.range;

import java.util.Date;

public interface IRangeCritDateValueMnemonic0FromValueOrPeriodSelector {
    IRangeCritDateValueMnemonic1ToValue setFromValue(final Date from);
    IRangeCritDateValueMnemonic1ToValue setFromValueExclusive(final Date from);
    IRangeCritDateValueMnemonic5MissingValue setToValue(final Date to);
    IRangeCritDateValueMnemonic5MissingValue setToValueExclusive(final Date to);
    IRangeCritDateValueMnemonic2PeriodMenmonics prev();
    IRangeCritDateValueMnemonic2PeriodMenmonics curr();
    IRangeCritDateValueMnemonic2PeriodMenmonics next();
    IRangeCritDateValueMnemonic6Value canHaveNoValue();
}
