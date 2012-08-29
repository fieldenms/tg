package ua.com.fielden.platform.expression.lexer.date;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.BaseNonDeterministicAutomata;

/**
 * NDA for recognising DATE token of the expression language.
 *
 * @author TG Team
 *
 */
public class DateTokenAutomata extends BaseNonDeterministicAutomata {

    public DateTokenAutomata() {
	super(EgTokenCategory.DATE, TEXT_POST_PROCESSING.TRIM,
		new State00_Quote(), // start
		new State01_Year(), new State02_Year(), new State03_Year(), new State04_Year(), // year
		new State05_Dash(),//
		new State06_Month(), new State07_Month(), new State08_Month(), // month
		new State09_Dash(),//
		new State10_Day(), new State11_Day(), new State12_Day(), new State13_Day(), // year
		new State14_PreFinal_for_Date_Portion(), //
		new State15_Hour(), new State16_Hour(), new State17_Hour(), // hour
		new State18_Colon(), //
		new State19_Minute(), new State20_Minute(), // minute
		new State21_Colon(), //
		new State22_Second(), new State23_Second(), // second
		new State24_PreFinal_for_Date_and_Time_Portion(),//
		new State25_Grave(), new State26_Final()
		);
    }

    @Override
    public String recognisePartiallyFromStart(final String input, final Integer posInOriginalSequence) throws SequenceRecognitionFailed {
	final String recognised = super.recognisePartiallyFromStart(input, posInOriginalSequence);
	final String pureDateStr = recognised.replace("'", "");

	// let's now check if we can covert recognised sequence to date
	if (pureDateStr.split(" ").length == 2) { // has time portion
	    final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	    formatter.parseDateTime(pureDateStr);
	} else { // has only the date portion
	    final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
	    formatter.parseDateTime(pureDateStr);
	}

	// return the recognised sequence including single quotes
	return recognised;
    }

    @Override
    public String toString() {
	return "DATE token automata";
    }

}
