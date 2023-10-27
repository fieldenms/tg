package ua.com.fielden.platform.processors.test_utils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Set;

/**
 * An abstract annotation processor that processes only the selected rounds. It is designed primarily for testing purposes.
 *
 * @author TG Team
 */
public abstract class SelectedRoundsProcessor extends AbstractProcessor {

    private int roundNumber = 0;
    private final Set<Integer> selectedRounds = new HashSet<>();

    public SelectedRoundsProcessor(final Set<Integer> selectedRounds) {
        this.selectedRounds.addAll(selectedRounds);
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        this.roundNumber++;
        if (selectedRounds.contains(roundNumber)) {
            return processRound(annotations, roundEnv, roundNumber);
        }
        return false;
    }

    protected abstract boolean processRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv, final int roundNumber);


    /**
     * An abstract annotation processor that processes only the first round. It is designed primarily for testing purposes.
     */
    public static abstract class FirstRoundProcessor extends SelectedRoundsProcessor {
        private static final Set<Integer> SELECTED_ROUNDS = Set.of(1);

        private FirstRoundProcessor(final Set<Integer> selectedRounds) {
            super(selectedRounds);
        }

        public FirstRoundProcessor() {
            this(SELECTED_ROUNDS);
        }
    }

}
