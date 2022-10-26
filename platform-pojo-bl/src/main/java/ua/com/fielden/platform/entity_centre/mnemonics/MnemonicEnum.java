package ua.com.fielden.platform.entity_centre.mnemonics;

/**
 * Represents mnemonics for atomic date range widths (days, weeks, .., quarters, .., years), and some number mnemonics.
 *
 * @author TG Team
 *
 */
public enum MnemonicEnum {
    /////////////////////////////////////////
    /////// USED BY ENTITY CENTRES //////////
    /////////////////////////////////////////
    DAY,
    WEEK,
    MONTH,
    YEAR, // Date type mnemonics
    QRT1,
    QRT2,
    QRT3,
    QRT4, // Year quarters
    FIN_YEAR, // Defined by IDates.finYearStartDay() and IDates.finYearStartMonth(); defaults to the 1st of July.

    ///////////////////////////////////////////
    /////// NOT USED BY ENTITY CENTRES ////////
    ///////////////////////////////////////////
    PERCENTAGE, // Number type mnemonics
    /** Day without right limit */
    DAY_AND_BEFORE,
    /** Day without left limit */
    DAY_AND_AFTER

}