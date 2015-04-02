package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.range;

public interface IRangeCritDateValueMnemonic2PeriodMenmonics {
    IRangeCritDateValueMnemonic3ExcludeFrom day();
    IRangeCritDateValueMnemonic3ExcludeFrom dayAndBefore();
    IRangeCritDateValueMnemonic3ExcludeFrom dayAndAfter();

    IRangeCritDateValueMnemonic3ExcludeFrom week();
    IRangeCritDateValueMnemonic3ExcludeFrom weekAndBefore();
    IRangeCritDateValueMnemonic3ExcludeFrom weekAndAfter();

    IRangeCritDateValueMnemonic3ExcludeFrom month();
    IRangeCritDateValueMnemonic3ExcludeFrom monthAndBefore();
    IRangeCritDateValueMnemonic3ExcludeFrom monthAndAfter();

    IRangeCritDateValueMnemonic3ExcludeFrom year();
    IRangeCritDateValueMnemonic3ExcludeFrom yearAndBefore();
    IRangeCritDateValueMnemonic3ExcludeFrom yearAndAfter();

    IRangeCritDateValueMnemonic3ExcludeFrom quarter1();
    IRangeCritDateValueMnemonic3ExcludeFrom quarter1AndBefore();
    IRangeCritDateValueMnemonic3ExcludeFrom quarter1AndAfter();

    IRangeCritDateValueMnemonic3ExcludeFrom quarter2();
    IRangeCritDateValueMnemonic3ExcludeFrom quarter2AndBefore();
    IRangeCritDateValueMnemonic3ExcludeFrom quarter2AndAfter();

    IRangeCritDateValueMnemonic3ExcludeFrom quarter3();
    IRangeCritDateValueMnemonic3ExcludeFrom quarter3AndBefore();
    IRangeCritDateValueMnemonic3ExcludeFrom quarter3AndAfter();

    IRangeCritDateValueMnemonic3ExcludeFrom quarter4();
    IRangeCritDateValueMnemonic3ExcludeFrom quarter4AndBefore();
    IRangeCritDateValueMnemonic3ExcludeFrom quarter4AndAfter();


    IRangeCritDateValueMnemonic3ExcludeFrom finYear();
    IRangeCritDateValueMnemonic3ExcludeFrom finYearAndBefore();
    IRangeCritDateValueMnemonic3ExcludeFrom finYearAndAfter();

}
