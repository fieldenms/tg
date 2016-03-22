package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.single;

public interface ISingleCritDateValueMnemonic1PeriodMenmonics {
    ISingleCritDateValueMnemonic2ExcludeFrom day();
    ISingleCritDateValueMnemonic2ExcludeFrom dayAndBefore();
    ISingleCritDateValueMnemonic2ExcludeFrom dayAndAfter();

    ISingleCritDateValueMnemonic2ExcludeFrom week();
    ISingleCritDateValueMnemonic2ExcludeFrom weekAndBefore();
    ISingleCritDateValueMnemonic2ExcludeFrom weekAndAfter();

    ISingleCritDateValueMnemonic2ExcludeFrom month();
    ISingleCritDateValueMnemonic2ExcludeFrom monthAndBefore();
    ISingleCritDateValueMnemonic2ExcludeFrom monthAndAfter();

    ISingleCritDateValueMnemonic2ExcludeFrom year();
    ISingleCritDateValueMnemonic2ExcludeFrom yearAndBefore();
    ISingleCritDateValueMnemonic2ExcludeFrom yearAndAfter();

    ISingleCritDateValueMnemonic2ExcludeFrom quarter1();
    ISingleCritDateValueMnemonic2ExcludeFrom quarter1AndBefore();
    ISingleCritDateValueMnemonic2ExcludeFrom quarter1AndAfter();

    ISingleCritDateValueMnemonic2ExcludeFrom quarter2();
    ISingleCritDateValueMnemonic2ExcludeFrom quarter2AndBefore();
    ISingleCritDateValueMnemonic2ExcludeFrom quarter2AndAfter();

    ISingleCritDateValueMnemonic2ExcludeFrom quarter3();
    ISingleCritDateValueMnemonic2ExcludeFrom quarter3AndBefore();
    ISingleCritDateValueMnemonic2ExcludeFrom quarter3AndAfter();

    ISingleCritDateValueMnemonic2ExcludeFrom quarter4();
    ISingleCritDateValueMnemonic2ExcludeFrom quarter4AndBefore();
    ISingleCritDateValueMnemonic2ExcludeFrom quarter4AndAfter();


    ISingleCritDateValueMnemonic2ExcludeFrom finYear();
    ISingleCritDateValueMnemonic2ExcludeFrom finYearAndBefore();
    ISingleCritDateValueMnemonic2ExcludeFrom finYearAndAfter();

}
