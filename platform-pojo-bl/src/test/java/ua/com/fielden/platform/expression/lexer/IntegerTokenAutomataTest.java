package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.integer.IntegerTokenAutomata;

public class IntegerTokenAutomataTest {
    private IntegerTokenAutomata automata = new IntegerTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "023689", automata.recognisePartiallyFromStart("023689", 0));
	assertEquals("Incorrect recognition result", "896", automata.recognisePartiallyFromStart("   896 ", 0));
	assertEquals("Incorrect recognition result", "96003", automata.recognisePartiallyFromStart("\t\n96003\t", 0));
    }

    @Test
    public void test_recognition_of_partially_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "123", automata.recognisePartiallyFromStart("123 property1.subProperty1+", 0));
	assertEquals("Incorrect recognition result", "456", automata.recognisePartiallyFromStart(" 456 + property", 0));
	assertEquals("Incorrect recognition result", "456", automata.recognisePartiallyFromStart(" 456 98", 0));
	assertEquals("Incorrect recognition result", "456", automata.recognisePartiallyFromStart(" 456.", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
	try {
	    automata.recognisePartiallyFromStart("", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("+12346", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("bn12", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
    }

}
