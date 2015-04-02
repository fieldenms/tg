package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.range;

import java.util.Date;

public interface IRangeCritDateValueMnemonic1ToValue extends IRangeCritDateValueMnemonic5MissingValue {
    IRangeCritDateValueMnemonic5MissingValue setToValue(final Date to);
    IRangeCritDateValueMnemonic5MissingValue setToValueExclusive(final Date to);
}
