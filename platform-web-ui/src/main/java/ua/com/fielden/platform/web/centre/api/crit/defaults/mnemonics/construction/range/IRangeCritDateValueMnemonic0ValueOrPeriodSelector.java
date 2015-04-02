package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.range;

import java.util.Date;

public interface IRangeCritDateValueMnemonic0ValueOrPeriodSelector extends IRangeCritDateValueMnemonic5MissingValue {
    IRangeCritDateValueMnemonic1ToValue setFromValue(final Date from);
    IRangeCritDateValueMnemonic1ToValue setFromValueExclusive(final Date from);
    IRangeCritDateValueMnemonic2PeriodMenmonics prev();
    IRangeCritDateValueMnemonic2PeriodMenmonics curr();
    IRangeCritDateValueMnemonic2PeriodMenmonics next();
}
