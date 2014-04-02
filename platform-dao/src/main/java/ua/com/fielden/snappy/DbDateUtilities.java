package ua.com.fielden.snappy;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;

public class DbDateUtilities extends DateUtilities {
    /**
     * This method initializes all date parameters that exist in "query" and are relative to previously retrieved "currentDbDate". </br> For instance : "BEG_PREV_MONTH",
     * "MID_CURR_YEAR" or "END_NEXT_DAY" etc.
     * 
     * @param query
     * @param currentDbDate
     * @return
     */
    public Query initializeSnappyDateParameters(final Query query, final Date currentDbDate) {
        final List<String> namedParameters = Arrays.asList(query.getNamedParameters());
        for (final String param : namedParameters) {
            final String[] paramParts = param.split("_");
            try {
                final DateRangeSelectorEnum begMidEnd = DateRangeSelectorEnum.valueOf(paramParts[0]);
                final DateRangePrefixEnum prevCurrNext = DateRangePrefixEnum.valueOf(paramParts[1]);
                final MnemonicEnum rangeWidth = MnemonicEnum.valueOf(paramParts[2]);
                final Date date = dateOfRangeThatIncludes(currentDbDate, begMidEnd, prevCurrNext, rangeWidth);
                query.setDate(param, date);
                System.err.println("param :" + param + " == " + date);
            } catch (final Exception e) {
                throw new RuntimeException("Enumeration that represents date related stuff could not be retrived from date parameter  : " + param + "======= " + e.getMessage());
            }
        }
        return query;
    }
}
