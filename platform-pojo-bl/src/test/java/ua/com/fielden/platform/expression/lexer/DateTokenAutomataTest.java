package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.date.DateTokenAutomata;

public class DateTokenAutomataTest {
    private DateTokenAutomata automata = new DateTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_date_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "'2001-01-02'", automata.recognisePartiallyFromStart("'2001-01-02' ", 0));
	assertEquals("Incorrect recognition result", "'2001-01-02'", automata.recognisePartiallyFromStart("    '2001-01-02'  ", 0));
	assertEquals("Incorrect recognition result", "'2001-01-02'", automata.recognisePartiallyFromStart("\t\n'2001-01-02'\t(", 0));
    }

    @Test
    public void test_full_recognition_of_correct_date_time_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "'2001-01-02 23:59:10'", automata.recognisePartiallyFromStart("'2001-01-02 23:59:10' ", 0));
	assertEquals("Incorrect recognition result", "'2001-01-02 23:59:10'", automata.recognisePartiallyFromStart("    '2001-01-02 23:59:10'  ", 0));
	assertEquals("Incorrect recognition result", "'2001-01-02 23:59:10'", automata.recognisePartiallyFromStart("\t\n'2001-01-02 23:59:10'\t(", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
	try {
	    automata.recognisePartiallyFromStart("", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("'2001-01-02 24:59:10'", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart(" '2001-01-02", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart(" '2001-01-02 23:59:10", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("2001-01-02 22:59:10'", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("'2001-00-02'", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
    }

}
