package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.case_when.else_.ElseTokenAutomata;

public class ElseTokenAutomataTest {
    private ElseTokenAutomata automata = new ElseTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "ELSE", automata.recognisePartiallyFromStart("ELSE ", 0));
	assertEquals("Incorrect recognition result", "ELSE", automata.recognisePartiallyFromStart("    eLse  ", 0));
	assertEquals("Incorrect recognition result", "ELSE", automata.recognisePartiallyFromStart("\t\nELSE\t\"string\"", 0));
    }

    @Test
    public void test_recognition_of_partially_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "ELSE", automata.recognisePartiallyFromStart(" ELSE property )", 0));
	assertEquals("Incorrect recognition result", "ELSE", automata.recognisePartiallyFromStart("\tELSE\t 1 + 3\"", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
	try {
	    automata.recognisePartiallyFromStart("", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("(ELSE", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart(" ELSE", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("_ELSE", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("EL SE ", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
    }

}
