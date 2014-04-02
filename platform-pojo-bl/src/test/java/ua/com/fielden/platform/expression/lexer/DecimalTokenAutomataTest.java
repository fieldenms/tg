package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.decimal.DecimalTokenAutomata;

public class DecimalTokenAutomataTest {
    private DecimalTokenAutomata automata = new DecimalTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_sequences() throws SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "023689.", automata.recognisePartiallyFromStart("023689.", 0));
        assertEquals("Incorrect recognition result", "0.12", automata.recognisePartiallyFromStart("  0.12", 0));
        assertEquals("Incorrect recognition result", ".23", automata.recognisePartiallyFromStart("\t\n.23", 0));
    }

    @Test
    public void test_recognition_of_partially_correct_sequences() throws SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "123.", automata.recognisePartiallyFromStart("123. +", 0));
        assertEquals("Incorrect recognition result", "123.", automata.recognisePartiallyFromStart("123. 6", 0));
        assertEquals("Incorrect recognition result", ".456", automata.recognisePartiallyFromStart("  .456  ", 0));
        assertEquals("Incorrect recognition result", "456.23", automata.recognisePartiallyFromStart(" 456.23+", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
        try {
            automata.recognisePartiallyFromStart("", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("+12.346", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("+.12", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart(" . 56", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("56", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
    }

}
