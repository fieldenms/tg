package ua.com.fielden.platform.processors.verify;

import ua.com.fielden.platform.processors.AbstractPlatformAnnotationProcessor;
import ua.com.fielden.platform.processors.ProcessorOptionDescriptor;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;
import ua.com.fielden.platform.processors.verify.verifiers.IVerifier;
import ua.com.fielden.platform.processors.verify.verifiers.companion.SaveWithFetchVerifier;
import ua.com.fielden.platform.processors.verify.verifiers.entity.EssentialPropertyVerifier;
import ua.com.fielden.platform.processors.verify.verifiers.entity.KeyTypeVerifier;
import ua.com.fielden.platform.processors.verify.verifiers.entity.UnionEntityVerifier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.processors.appdomain.ApplicationDomainProcessor.APP_DOMAIN_EXTENSION_OPT_DESC;
import static ua.com.fielden.platform.processors.appdomain.ApplicationDomainProcessor.APP_DOMAIN_PKG_OPT_DESC;

/**
 * Annotation processor responsible for verifying source definitions in a domain model.
 * <p>
 * The processor itself does not define any specific verification logic. Instead it delegates to implementations of the
 * {@link IVerifier} interface, providing them its own inputs and respective processing/round environments.
 *
 * @author TG Team
 */
@SupportedAnnotationTypes("*")
public class VerifyingProcessor extends AbstractPlatformAnnotationProcessor {

    private final List<Function<ProcessingEnvironment, IVerifier>> registeredVerifiersProviders = new ArrayList<>();
    private final List<IVerifier> registeredVerifiers = new ArrayList<>();

    /** Round-cumulative indicator of whether all verifiers were passed. */
    private boolean passed;

    @Override
    public Set<String> getSupportedOptions() {
        return Stream.concat(super.getSupportedOptions().stream(),
                        Stream.of(APP_DOMAIN_PKG_OPT_DESC, APP_DOMAIN_EXTENSION_OPT_DESC).map(ProcessorOptionDescriptor::name))
                .collect(toSet());
    }

    public VerifyingProcessor() {
        // specify default verifiers here
        this.registeredVerifiersProviders.add(procEnv -> new KeyTypeVerifier(procEnv));
        this.registeredVerifiersProviders.add(procEnv -> new EssentialPropertyVerifier(procEnv));
        this.registeredVerifiersProviders.add(procEnv -> new UnionEntityVerifier(procEnv));
        this.registeredVerifiersProviders.add(procEnv -> new SaveWithFetchVerifier(procEnv));
    }

    /**
     * Creates an instance of this processor that will use the specified verifiers.
     * This constructor is required for unit testing of verifiers.
     * @param verifierProviders
     */
    VerifyingProcessor(final Collection<Function<ProcessingEnvironment, IVerifier>> verifierProviders) {
        this.registeredVerifiersProviders.addAll(verifierProviders);
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
        if (roundEnv.getRootElements().isEmpty()) {
            printNote("Nothing to verify.");
        } else {
            final boolean roundPassed = verify(roundEnv);
            if (roundPassed) {
                printNote("All verifiers were passed.");
            }
            passed = passed && roundPassed;
        }

        if (!passed) {
            printMandatoryWarning("Claiming this round's (no. %s) annotations to disable other processors.", getRoundNumber());
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

        for (final IVerifier verifier : registeredVerifiers) {
            final List<ViolatingElement> erronousElements = verifier.verify(roundEnv).stream()
                    .filter(ve -> !SkipVerification.Factory.shouldSkipVerification(ve.element()))
                    .filter(ViolatingElement::hasError)
                    .toList();
            if (!erronousElements.isEmpty()) {
                roundPassed = false;
                printError(errVerifierNotPassedBy(verifier.getClass().getSimpleName(),
                        erronousElements.stream()
                            .map(ve -> ve.element().getSimpleName())
                            .distinct()
                            .map(Name::toString)
                            .sorted() /* sort to have a predictable order */
                            .toList()));
            }
        }

        return roundPassed;
    }

    /**
     * Constructs an error message about a verifier that was not passed by certain elements. Element names are sorted beforehand.
     */
    public static String errVerifierNotPassedBy(final String verifierSimpleName, final Collection<String> elementSimpleNames) {
        return "%s was not passed by: [%s]".formatted(
                verifierSimpleName, elementSimpleNames.stream().sorted().collect(joining(", ")));
    }

    /**
     * Constructs an error message about a verifier that was not passed by certain elements.
     */
    public static String errVerifierNotPassedBy(final String verifierSimpleName, final String... elementSimpleNames) {
        return errVerifierNotPassedBy(verifierSimpleName, List.of(elementSimpleNames));
    }

}
