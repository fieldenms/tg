package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.case_when.then_.ThenTokenAutomata;

public class ThenTokenAutomataTest {
    private ThenTokenAutomata automata = new ThenTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "THEN", automata.recognisePartiallyFromStart("THEN ", 0));
	assertEquals("Incorrect recognition result", "THEN", automata.recognisePartiallyFromStart("    ThEn  ", 0));
	assertEquals("Incorrect recognition result", "THEN", automata.recognisePartiallyFromStart("\t\nTHEN\t(", 0));
    }

    @Test
    public void test_recognition_of_partially_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "THEN", automata.recognisePartiallyFromStart(" THEN property )", 0));
	assertEquals("Incorrect recognition result", "THEN", automata.recognisePartiallyFromStart("\tTHEN\t 1 + 3\"", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
	try {
	    automata.recognisePartiallyFromStart("", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("(THEN", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart(" THEN", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("_THEN", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("THE N ", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
    }

}
