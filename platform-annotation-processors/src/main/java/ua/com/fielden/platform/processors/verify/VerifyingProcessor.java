package ua.com.fielden.platform.processors.verify;

import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import ua.com.fielden.platform.processors.AbstractPlatformAnnotationProcessor;
import ua.com.fielden.platform.processors.verify.verifiers.Verifier;
import ua.com.fielden.platform.processors.verify.verifiers.entity.KeyTypeVerifier;

/**
 * Annotation processor responsible for verifying source definitions in a domain model.
 * <p>
 * The processor itself does not define any specific verification logic. Instead it delegates to implementations of the {@link Verifier} interface,
 * providing them its own inputs and respective processing/round environments.
 * 
 * @author TG Team
 */
@SupportedAnnotationTypes("*")
public class VerifyingProcessor extends AbstractPlatformAnnotationProcessor {
    
    private final Set<Function<ProcessingEnvironment, Verifier>> registeredVerifiersProviders;
    private final Set<Verifier> registeredVerifiers = new HashSet<>();

    /** Round-cumulative indicator of whether all verifiers were passed. */
    private boolean passed;

    public VerifyingProcessor() {
        // specify default verifiers here
        this.registeredVerifiersProviders = Set.of((procEnv) -> new KeyTypeVerifier(procEnv));
    }

    /**
     * Creates an instance of this processor that will use the specified verifiers.
     * This constructor is required for unit testing of verifiers.
     * @param verifierProviders
     */
    VerifyingProcessor(final Collection<Function<ProcessingEnvironment, Verifier>> verifierProviders) {
        this.registeredVerifiersProviders = verifierProviders.stream().collect(Collectors.toUnmodifiableSet());
    }
    
    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.passed = true;
        // instantiate registered verifiers
        registeredVerifiers.addAll(registeredVerifiersProviders.stream().map(p -> p.apply(processingEnv)).toList());
        if (!registeredVerifiers.isEmpty()) {
            printNote("Registered verifiers: [%s]",
                    registeredVerifiers.stream().map(v -> v.getClass().getSimpleName()).sorted().collect(joining(", ")));
        }
    }

    /**
     * If verification failed, returns true to prevent other annotation processors from running in that round by claiming all annotations ("*").
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean processRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final Set<? extends Element> rootElements = roundEnv.getRootElements();
        if (rootElements.isEmpty()) {
            printNote("Nothing to verify.");
        }
        else {
            final boolean roundPassed = verify(roundEnv);
            if (roundPassed) {
                printNote("All verifiers were passed.");
            }
            passed = passed && roundPassed;
        }

        if (!passed) {
            printMandatoryWarning("Claiming this round's annotations to disable other processors.");
        }

        return !passed;
    }

    /**
     * Performs verification in the current round. Returns {@code true} only if all verifiers were passed or there were no verifiers to run.
     * @param roundEnv
     * @return
     */
    private boolean verify(final RoundEnvironment roundEnv) {
        boolean roundPassed = true;
        for (final Verifier verifier : registeredVerifiers) {
            final boolean verifierPassed = verifier.verify(roundEnv);
            roundPassed = roundPassed && verifierPassed;
            if (!verifierPassed) {
                final Set<Element> violatingElements = verifier.getViolatingElements();
                if (!violatingElements.isEmpty()) {
                    printError("%s was not passed by: [%s]", verifier.getClass().getSimpleName(),
                            violatingElements.stream().map(el -> el.getSimpleName().toString()).sorted().collect(joining(", ")));
                }
            }
        }
        return roundPassed;
    }

}