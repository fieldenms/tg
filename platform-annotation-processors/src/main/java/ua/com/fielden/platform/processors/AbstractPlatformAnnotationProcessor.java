package ua.com.fielden.platform.processors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.joining;
import static javax.tools.Diagnostic.Kind.NOTE;

import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import org.joda.time.DateTime;

import com.google.common.base.Stopwatch;

/**
 * An abstract platform-level annotation processor to be extended by specific implementations. 
 * It provides common processor behaviour mostly concerned with initialization and per-round logging.
 * <p>
 * Subclasses are responsible for implementing {@link #processRound(Set, RoundEnvironment)}, which is called by a known method
 * {@link #process(Set, RoundEnvironment)} in order to provide per-round logging.
 *
 * @author TG Team
 */
abstract public class AbstractPlatformAnnotationProcessor extends AbstractProcessor {

    // processing environment
    protected Messager messager;
    protected Filer filer;
    protected Elements elementUtils;
    protected Types typeUtils;
    protected Map<String, String> options;
    protected DateTime initDateTime;

    // logging-related
    private final String classSimpleName = this.getClass().getSimpleName();
    private int roundNumber;
    /**
     * A counter for the round number that makes sense during incremental compilation, when starting to process affected sources.
     * Gets reset after a "batch" of affected sources has been processed, since there might be more of them.
     */
    private int batchRoundNumber;
    /** Indicates whether the last round of processing initial inputs has already been passed. Makes sense during incremental compilation. */
    private boolean pastLastRound;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv); 
        this.initDateTime = DateTime.now();
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.options = processingEnv.getOptions();
        this.roundNumber = this.batchRoundNumber = 0; 
        this.pastLastRound = false;

        printNote("%s initialized.", classSimpleName);
        if (!this.options.isEmpty()) {
            printNote("Options: [%s]", 
                    options.entrySet().stream().map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue())).collect(joining(", ")));
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        this.roundNumber = this.roundNumber + 1;
        this.batchRoundNumber = this.batchRoundNumber + 1;
        final Stopwatch stopwatchProcess = Stopwatch.createStarted();
        final boolean processingOver = roundEnv.processingOver();

        // processing initial inputs
        if (!pastLastRound) {
            printNote(">>> %s: PROCESSING ROUND %d START >>>", classSimpleName, roundNumber);
            if (processingOver) {
                printNote("Last round of processing initial inputs.");
            }
        }
        // processing affected sources
        else {
            printNote(">>> %s: PROCESSING ROUND %d (%d) START >>>", classSimpleName, roundNumber, batchRoundNumber);
            if (processingOver) {
                printNote("Last round of processing sources affected by previous inputs.");
            }
            else {
                printNote("Processing affected sources.");
            }
        }
        printNote("annotations: [%s]", annotations.stream().map(Element::getSimpleName).map(Name::toString).sorted().collect(joining(", ")));
        printNote("rootElements: [%s]", roundEnv.getRootElements().stream().map(Element::getSimpleName).map(Name::toString).sorted().collect(joining(", ")));

        final boolean claimAnnotations = processRound(annotations, roundEnv);

        stopwatchProcess.stop();
        printNote("<<< %s: PROCESSING ROUND %d END [%s millis] <<<", classSimpleName, roundNumber, stopwatchProcess.elapsed(MILLISECONDS));

        if (roundEnv.processingOver()) {
            this.pastLastRound = true;
            this.batchRoundNumber = 0; // reset affected sources batch round counter
        }

        return claimAnnotations;
    }

    /**
     * Performs a single round of annotation processing. This method should contain the core logic of an annotation processor.
     * Subclasses should implement this method instead of {@link #process(Set, RoundEnvironment)} to benefit from extra per-round logging
     * performed by the abstraction.
     * <p>
     * For more details refer to the documentation of {@link #process(Set, RoundEnvironment)}.
     * 
     * @param annotations
     * @param roundEnv
     * @return
     */
    protected abstract boolean processRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);

    /**
     * Prints a single diagnostic message using {@link Messager} instance of message kind {@link NOTE}.
     * @param msg string to be printed
     * @param args optional string format arguments
     */
    protected void printNote(final String msg, final Object... args) {
        messager.printMessage(NOTE, msg.formatted(args));
    }

    /**
     * Prints a single diagnostic message using {@link Messager} instance of message kind {@link WARNING}.
     * @param msg string to be printed
     * @param args optional string format arguments
     */
    protected void printWarning(final String msg, final Object... args) {
        messager.printMessage(Diagnostic.Kind.WARNING, msg.formatted(args));
    }

    /**
     * Prints a single diagnostic message using {@link Messager} instance of message kind {@link ERROR}.
     * @param msg string to be printed
     * @param args optional string format arguments
     */
    protected void printError(final String msg, final Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg.formatted(args));
    }

    /**
     * Prints a single diagnostic message using {@link Messager} instance of message kind {@link MANDATORY_WARNING}.
     * @param msg string to be printed
     * @param args optional string format arguments
     */
    protected void printMandatoryWarning(final String msg, final Object... args) {
        messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg.formatted(args));
    }

    protected int getRoundNumber() {
        return roundNumber;
    }

}