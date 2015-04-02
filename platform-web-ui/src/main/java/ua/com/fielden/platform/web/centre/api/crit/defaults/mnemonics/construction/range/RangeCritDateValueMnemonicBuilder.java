package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.range;

import java.util.Date;
import java.util.Optional;

import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritDateValueMnemonic;
import ua.com.fielden.snappy.DateRangeConditionEnum;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

public class RangeCritDateValueMnemonicBuilder
implements  IRangeCritDateValueMnemonic,
            IRangeCritDateValueMnemonic1ToValue,
            IRangeCritDateValueMnemonic2PeriodMenmonics,
            IRangeCritDateValueMnemonic3ExcludeFrom {

    private Optional<Date> fromValue = Optional.empty();
    private Optional<Date> toValue = Optional.empty();

    private Optional<DateRangePrefixEnum> prefix = Optional.empty();
    private Optional<MnemonicEnum> period = Optional.empty();
    private Optional<DateRangeConditionEnum> beforeOrAfter = Optional.empty();

    private Optional<Boolean> excludeFrom = Optional.empty();
    private Optional<Boolean> excludeTo = Optional.empty();

    private boolean checkForMissingValue = false;
    private boolean negateCondition = false;


    ///////////////////////////////////////////////////////////
    ///////// COMMON TO EXPLICIT AND MNEMONIC PERIODS /////////
    ///////////////////////////////////////////////////////////

    @Override
    public IRangeCritDateValueMnemonic0ValueOrPeriodSelector not() {
        this.negateCondition = true;
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic6Value canHaveNoValue() {
        this.checkForMissingValue = true;
        return this;
    }

    @Override
    public RangeCritDateValueMnemonic value() {
        return new RangeCritDateValueMnemonic(
                fromValue,
                toValue,
                prefix,
                period,
                beforeOrAfter,
                excludeFrom,
                excludeTo,
                checkForMissingValue,
                negateCondition
                );
    }

    ////////////////////////////////////////////////
    /////////// EXPLICIT PERIOD VALUES /////////////
    ////////////////////////////////////////////////

    @Override
    public IRangeCritDateValueMnemonic1ToValue setFromValue(final Date from) {
        if (from == null) {
            throw new IllegalArgumentException("The from date should have a value.");
        }
        this.fromValue = Optional.of(from);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic1ToValue setFromValueExclusive(final Date from) {
        if (from == null) {
            throw new IllegalArgumentException("The from date should have a value.");
        }
        this.fromValue = Optional.of(from);
        this.excludeFrom = Optional.of(true);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic5MissingValue setToValue(final Date to) {
        if (to == null) {
            throw new IllegalArgumentException("The to date should have a value.");
        }
        this.toValue = Optional.of(to);
        return this;

    }

    @Override
    public IRangeCritDateValueMnemonic5MissingValue setToValueExclusive(final Date to) {
        if (to == null) {
            throw new IllegalArgumentException("The to date should have a value.");
        }
        this.toValue = Optional.of(to);
        this.excludeTo = Optional.of(true);
        return this;
    }

    ////////////////////////////////////////////////
    ////////////// PERIOD MNEMONICS ////////////////
    ////////////////////////////////////////////////

    @Override
    public IRangeCritDateValueMnemonic2PeriodMenmonics prev() {
        this.prefix = Optional.of(DateRangePrefixEnum.PREV);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic2PeriodMenmonics curr() {
        this.prefix = Optional.of(DateRangePrefixEnum.CURR);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic2PeriodMenmonics next() {
        this.prefix = Optional.of(DateRangePrefixEnum.NEXT);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom day() {
        this.period = Optional.of(MnemonicEnum.DAY);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom dayAndBefore() {
        this.period = Optional.of(MnemonicEnum.DAY);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom dayAndAfter() {
        this.period = Optional.of(MnemonicEnum.DAY);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom week() {
        this.period = Optional.of(MnemonicEnum.WEEK);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom weekAndBefore() {
        this.period = Optional.of(MnemonicEnum.WEEK);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom weekAndAfter() {
        this.period = Optional.of(MnemonicEnum.WEEK);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom month() {
        this.period = Optional.of(MnemonicEnum.MONTH);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom monthAndBefore() {
        this.period = Optional.of(MnemonicEnum.MONTH);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom monthAndAfter() {
        this.period = Optional.of(MnemonicEnum.MONTH);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom year() {
        this.period = Optional.of(MnemonicEnum.YEAR);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom yearAndBefore() {
        this.period = Optional.of(MnemonicEnum.YEAR);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom yearAndAfter() {
        this.period = Optional.of(MnemonicEnum.YEAR);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;

    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom quarter1() {
        this.period = Optional.of(MnemonicEnum.QRT1);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom quarter1AndBefore() {
        this.period = Optional.of(MnemonicEnum.QRT1);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom quarter1AndAfter() {
        this.period = Optional.of(MnemonicEnum.QRT1);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom quarter2() {
        this.period = Optional.of(MnemonicEnum.QRT2);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom quarter2AndBefore() {
        this.period = Optional.of(MnemonicEnum.QRT2);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom quarter2AndAfter() {
        this.period = Optional.of(MnemonicEnum.QRT2);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom quarter3() {
        this.period = Optional.of(MnemonicEnum.QRT3);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom quarter3AndBefore() {
        this.period = Optional.of(MnemonicEnum.QRT3);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom quarter3AndAfter() {
        this.period = Optional.of(MnemonicEnum.QRT3);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom quarter4() {
        this.period = Optional.of(MnemonicEnum.QRT4);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom quarter4AndBefore() {
        this.period = Optional.of(MnemonicEnum.QRT4);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom quarter4AndAfter() {
        this.period = Optional.of(MnemonicEnum.QRT4);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom finYear() {
        this.period = Optional.of(MnemonicEnum.OZ_FIN_YEAR);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom finYearAndBefore() {
        this.period = Optional.of(MnemonicEnum.OZ_FIN_YEAR);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic3ExcludeFrom finYearAndAfter() {
        this.period = Optional.of(MnemonicEnum.OZ_FIN_YEAR);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic4ExcludeTo exclusiveFrom() {
        this.excludeFrom = Optional.of(true);
        return this;
    }

    @Override
    public IRangeCritDateValueMnemonic5MissingValue exclusiveTo() {
        this.excludeTo = Optional.of(true);
        return this;
    }

}
