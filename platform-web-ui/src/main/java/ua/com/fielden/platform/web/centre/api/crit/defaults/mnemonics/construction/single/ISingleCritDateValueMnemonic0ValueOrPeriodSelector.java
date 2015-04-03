package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.single;

import java.util.Date;

public interface ISingleCritDateValueMnemonic0ValueOrPeriodSelector {
    ISingleCritDateValueMnemonic4MissingValue setValue(final Date from);
    ISingleCritDateValueMnemonic1PeriodMenmonics prev();
    ISingleCritDateValueMnemonic1PeriodMenmonics curr();
    ISingleCritDateValueMnemonic1PeriodMenmonics next();
    ISingleCritDateValueMnemonic5Value canHaveNoValue();
}
