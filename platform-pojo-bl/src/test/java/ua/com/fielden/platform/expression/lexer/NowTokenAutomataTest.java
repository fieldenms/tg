package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.function.now.NowTokenAutomata;

public class NowTokenAutomataTest {

    private NowTokenAutomata automata() {
	return new NowTokenAutomata();
    }

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "NOW", automata().recognisePartiallyFromStart("NOW", 0));
	assertEquals("Incorrect recognition result", "NOW", automata().recognisePartiallyFromStart("    nOw  ", 0));
	assertEquals("Incorrect recognition result", "NOW", automata().recognisePartiallyFromStart("\t\nNow\t)", 0));
	assertEquals("Incorrect recognition result", "NOW", automata().recognisePartiallyFromStart(" Now))", 0));
	assertEquals("Incorrect recognition result", "NOW", automata().recognisePartiallyFromStart(" Now+", 0));
	assertEquals("Incorrect recognition result", "NOW", automata().recognisePartiallyFromStart("Now/", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
	try {
	    automata().recognisePartiallyFromStart("", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata().recognisePartiallyFromStart("(NOW", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata().recognisePartiallyFromStart("(NOW_", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata().recognisePartiallyFromStart("NOWed", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata().recognisePartiallyFromStart("NOW1", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata().recognisePartiallyFromStart("NOW.", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata().recognisePartiallyFromStart("NOW.lala", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata().recognisePartiallyFromStart("_NOW", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata().recognisePartiallyFromStart("NO W ", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
    }

}
