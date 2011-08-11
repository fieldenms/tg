package ua.com.fielden.platform.expression.automata;

import ua.com.fielden.platform.expression.exception.RecognitionException;

/**
 * An automata related exception indicating the failure to process an input sequence of characters.
 *
 * @author TG Team
 *
 */
public class SequenceRecognitionFailed extends RecognitionException {
    public final String pretendant;
    public final NoTransitionAvailable transitionException;


    public SequenceRecognitionFailed(final String pretendant, final NoTransitionAvailable transitionException, final Integer errorPosition) {
	super(transitionException.getMessage(), errorPosition);
	this.pretendant = pretendant;
	this.transitionException = transitionException;

    }

    public SequenceRecognitionFailed(final String pretendant, final Integer errorPosition) {
	super(pretendant, errorPosition);
	this.pretendant = pretendant;
	this.transitionException = null;
    }
}
