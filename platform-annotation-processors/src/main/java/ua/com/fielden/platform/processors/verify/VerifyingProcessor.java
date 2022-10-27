package ua.com.fielden.platform.processors.verify;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.google.auto.service.AutoService;
import com.google.common.base.Stopwatch;

import ua.com.fielden.platform.processors.verify.verifiers.KeyTypeVerifier;
import ua.com.fielden.platform.processors.verify.verifiers.Verifier;

/**
 * Annotation processor responsible for verifying source definitions in a domain model.
 * 
 * @author TG Team
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
public class VerifyingProcessor extends AbstractProcessor {
    
    private final String classSimpleName = this.getClass().getSimpleName();
    private final Set<Verifier> registeredVerifiers = new HashSet<>();

    private Messager messager;
    private Map<String, String> options;
    private int roundNumber;
    /** Round-cumulative indicator of whether all verifiers were passed. */
    private boolean passed;
    
    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.roundNumber = 0;
        this.passed = true;
        this.messager = processingEnv.getMessager();
        this.options = processingEnv.getOptions();
        // TODO declarative dependency injection
        registeredVerifiers.add(new KeyTypeVerifier(processingEnv));

        messager.printMessage(Kind.NOTE, format("%s initialized.", classSimpleName));
        if (!options.isEmpty()) {
            messager.printMessage(Kind.NOTE, format("Options: [%s]",
                    options.keySet().stream().map(k -> format("%s=%s", k, options.get(k))).sorted().collect(joining(", "))));
        }
        if (!registeredVerifiers.isEmpty()) {
            messager.printMessage(Kind.NOTE, format("Registered verifiers: [%s]",
                    registeredVerifiers.stream().map(v -> v.getClass().getSimpleName()).sorted().collect(joining(", "))));
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * If verification failed, returns true to prevent other annotation processors from running in that round by claiming all annotations ("*").
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        roundNumber = roundNumber + 1;
        final Stopwatch stopwatchProcess = Stopwatch.createStarted();

        messager.printMessage(Kind.NOTE, format(">>> %s: PROCESSING ROUND %d START >>>", classSimpleName, roundNumber));
        
        final Set<? extends Element> rootElements = roundEnv.getRootElements();
        if (rootElements.isEmpty()) {
            messager.printMessage(Kind.NOTE, "Nothing to verify.");
        }
        else {
            messager.printMessage(Kind.NOTE, format("annotations: [%s]",
                    annotations.stream().map(el -> el.getSimpleName().toString()).sorted().collect(joining(", "))));
            messager.printMessage(Kind.NOTE, format("rootElements: [%s]",
                    rootElements.stream().map(el -> el.getSimpleName().toString()).sorted().collect(joining(", "))));

            final boolean roundPassed = verify(roundEnv);
            if (roundPassed) {
                messager.printMessage(Kind.NOTE, "All verifiers were passed.");
            }
            passed = passed && roundPassed;
        }

        if (!passed) {
            messager.printMessage(Kind.NOTE, "Claiming this round's annotations to disable other processors.");
        }

        stopwatchProcess.stop();
        messager.printMessage(Kind.NOTE, format("<<< %s: PROCESSING ROUND %d END [%s millis] <<<", classSimpleName, roundNumber,
                stopwatchProcess.elapsed(TimeUnit.MILLISECONDS)));

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
                    messager.printMessage(Kind.NOTE, format("%s was not passed by: [%s]", verifier.getClass().getSimpleName(),
                            violatingElements.stream().map(el -> el.getSimpleName().toString()).sorted().collect(joining(", "))));
                }
            }
        }
        return roundPassed;
    }

}
