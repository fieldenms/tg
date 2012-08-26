package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.case_when.end.EndTokenAutomata;

public class EndTokenAutomataTest {
    private EndTokenAutomata automata = new EndTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "END", automata.recognisePartiallyFromStart("END ", 0));
	assertEquals("Incorrect recognition result", "END", automata.recognisePartiallyFromStart("    eNd  ", 0));
	assertEquals("Incorrect recognition result", "END", automata.recognisePartiallyFromStart("\t\nEND\t", 0));
    }

    @Test
    public void test_recognition_of_partially_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "END", automata.recognisePartiallyFromStart(" END property )", 0));
	assertEquals("Incorrect recognition result", "END", automata.recognisePartiallyFromStart("\tEND\t 1 + 3\"", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
	try {
	    automata.recognisePartiallyFromStart("", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("(END", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart(" END", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("_END", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("EN D ", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
    }

}
