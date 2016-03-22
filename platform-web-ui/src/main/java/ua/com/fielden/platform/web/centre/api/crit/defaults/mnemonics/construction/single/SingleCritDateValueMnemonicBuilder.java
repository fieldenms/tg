package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.single;

import java.util.Date;
import java.util.Optional;

import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritDateValueMnemonic;
import ua.com.fielden.snappy.DateRangeConditionEnum;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

public class SingleCritDateValueMnemonicBuilder
implements  ISingleCritDateValueMnemonic,
            ISingleCritDateValueMnemonic1PeriodMenmonics,
            ISingleCritDateValueMnemonic2ExcludeFrom,
            ISingleCritDateValueMnemonic4MissingValue {

    private Optional<Date> value = Optional.empty();

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
    public ISingleCritDateValueMnemonic0ValueOrPeriodSelector not() {
        this.negateCondition = true;
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic5Value canHaveNoValue() {
        this.checkForMissingValue = true;
        return this;
    }

    @Override
    public SingleCritDateValueMnemonic value() {
        return new SingleCritDateValueMnemonic(
                value,
                prefix,
                period,
                beforeOrAfter,
                excludeFrom,
                excludeTo,
                checkForMissingValue,
                negateCondition
                );
    }

    ////////////////////////////////////////
    /////////// EXPLICIT VALUE /////////////
    ////////////////////////////////////////

    @Override
    public ISingleCritDateValueMnemonic4MissingValue setValue(final Date value) {
        if (value == null) {
            throw new IllegalArgumentException("The date value cannot be null.");
        }
        this.value = Optional.of(value);
        return this;
    }

    ////////////////////////////////////////////////
    ////////////// PERIOD MNEMONICS ////////////////
    ////////////////////////////////////////////////

    @Override
    public ISingleCritDateValueMnemonic1PeriodMenmonics prev() {
        this.prefix = Optional.of(DateRangePrefixEnum.PREV);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic1PeriodMenmonics curr() {
        this.prefix = Optional.of(DateRangePrefixEnum.CURR);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic1PeriodMenmonics next() {
        this.prefix = Optional.of(DateRangePrefixEnum.NEXT);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom day() {
        this.period = Optional.of(MnemonicEnum.DAY);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom dayAndBefore() {
        this.period = Optional.of(MnemonicEnum.DAY);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom dayAndAfter() {
        this.period = Optional.of(MnemonicEnum.DAY);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom week() {
        this.period = Optional.of(MnemonicEnum.WEEK);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom weekAndBefore() {
        this.period = Optional.of(MnemonicEnum.WEEK);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom weekAndAfter() {
        this.period = Optional.of(MnemonicEnum.WEEK);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom month() {
        this.period = Optional.of(MnemonicEnum.MONTH);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom monthAndBefore() {
        this.period = Optional.of(MnemonicEnum.MONTH);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom monthAndAfter() {
        this.period = Optional.of(MnemonicEnum.MONTH);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom year() {
        this.period = Optional.of(MnemonicEnum.YEAR);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom yearAndBefore() {
        this.period = Optional.of(MnemonicEnum.YEAR);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom yearAndAfter() {
        this.period = Optional.of(MnemonicEnum.YEAR);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;

    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom quarter1() {
        this.period = Optional.of(MnemonicEnum.QRT1);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom quarter1AndBefore() {
        this.period = Optional.of(MnemonicEnum.QRT1);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom quarter1AndAfter() {
        this.period = Optional.of(MnemonicEnum.QRT1);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom quarter2() {
        this.period = Optional.of(MnemonicEnum.QRT2);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom quarter2AndBefore() {
        this.period = Optional.of(MnemonicEnum.QRT2);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom quarter2AndAfter() {
        this.period = Optional.of(MnemonicEnum.QRT2);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom quarter3() {
        this.period = Optional.of(MnemonicEnum.QRT3);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom quarter3AndBefore() {
        this.period = Optional.of(MnemonicEnum.QRT3);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom quarter3AndAfter() {
        this.period = Optional.of(MnemonicEnum.QRT3);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom quarter4() {
        this.period = Optional.of(MnemonicEnum.QRT4);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom quarter4AndBefore() {
        this.period = Optional.of(MnemonicEnum.QRT4);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom quarter4AndAfter() {
        this.period = Optional.of(MnemonicEnum.QRT4);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom finYear() {
        this.period = Optional.of(MnemonicEnum.OZ_FIN_YEAR);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom finYearAndBefore() {
        this.period = Optional.of(MnemonicEnum.OZ_FIN_YEAR);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.BEFORE);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic2ExcludeFrom finYearAndAfter() {
        this.period = Optional.of(MnemonicEnum.OZ_FIN_YEAR);
        this.beforeOrAfter = Optional.of(DateRangeConditionEnum.AFTER);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic3ExcludeTo exclusiveFrom() {
        this.excludeFrom = Optional.of(true);
        return this;
    }

    @Override
    public ISingleCritDateValueMnemonic4MissingValue exclusiveTo() {
        this.excludeTo = Optional.of(true);
        return this;
    }

}
