package ua.com.fielden.platform.processors;

import com.google.common.base.Stopwatch;
import com.squareup.javapoet.AnnotationSpec;
import ua.com.fielden.platform.processors.metamodel.elements.utils.TypeElementCache;
import ua.com.fielden.platform.processors.utils.CodeGenerationUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.tools.Diagnostic.Kind.NOTE;
import static ua.com.fielden.platform.processors.ProcessorOptionDescriptor.newBooleanOptionDescriptor;
import static ua.com.fielden.platform.processors.ProcessorOptionDescriptor.parseOptionFrom;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.findAnnotationMirror;

/**
 * An abstract platform-level annotation processor to be extended by specific implementations.
 * It provides common processor behaviour mostly concerned with initialization and per-round logging.
 * <p>
 * Subclasses are responsible for implementing {@link #processRound(Set, RoundEnvironment)}, which is called by a known method
 * {@link #process(Set, RoundEnvironment)} in order to provide per-round logging.
 * <p>
 * Supported options by this base type:
 * <ul>
 *   <li>{@code cacheStats} -- if set to {@code true} enables recording of type element cache statistics (see {@link TypeElementCache#getStats()}). </li>
 * </ul>
 *
 * @author TG Team
 */
abstract public class AbstractPlatformAnnotationProcessor extends AbstractProcessor {

    // processing environment
    protected Messager messager;
    protected Filer filer;
    protected Elements elementUtils;
    protected Types typeUtils;

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

    // --- supported options ---

    public static final ProcessorOptionDescriptor<Boolean> CACHE_STATS_OPT_DESC = newBooleanOptionDescriptor("cacheStats", false);
    private boolean reportCacheStats;

    @Override
    public Set<String> getSupportedOptions() {
        return Stream.of(CACHE_STATS_OPT_DESC).map(ProcessorOptionDescriptor::name).collect(Collectors.toSet());
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.roundNumber = this.batchRoundNumber = 0;
        this.pastLastRound = false;

        final Map<String, String> options = processingEnv.getOptions();
        if (!options.isEmpty()) {
            printNote("Options: " + options);
        }
        parseOptions(options);
        printNote("%s initialized.", classSimpleName);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * Performs parsing of options that were passed to this processor.
     * Subclasses should call the super implementation when overriding this method.
     */
    protected void parseOptions(final Map<String, String> options) {
        reportCacheStats = parseOptionFrom(options, CACHE_STATS_OPT_DESC);
        if (reportCacheStats)
            TypeElementCache.recordStats();
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
        printNote(formatSequence("annotations", annotations.stream().map(Element::getSimpleName).iterator()));
        printNote(formatSequence("rootElements", roundEnv.getRootElements().stream().map(Element::getSimpleName).iterator()));

        final boolean claimAnnotations = processRound(annotations, roundEnv);

        stopwatchProcess.stop();
        printNote("<<< %s: PROCESSING ROUND %d END [%s millis] <<<", classSimpleName, roundNumber, stopwatchProcess.elapsed(MILLISECONDS));

        if (processingOver) {
            this.pastLastRound = true;
            this.batchRoundNumber = 0; // reset affected sources batch round counter
            postProcess();
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
     * Performs some post-processing work at the end of the last round.
     * Subclasses might wish to call the super implementation when overriding this method.
     */
    protected void postProcess() {
        if (reportCacheStats) {
            printNote("Type element cache statistics: " + TypeElementCache.getStats());
        }
    }

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

    /// Prints a message on the first annotation of type `annotType` that is present on `element`.
    /// If no such annotation is present, the message is printed on `element` itself.
    ///
    protected void printMessageOn(
            final Diagnostic.Kind kind,
            final CharSequence message,
            final Element element,
            final Class<? extends Annotation> annotType)
    {
        findAnnotationMirror(element, annotType)
                .ifPresentOrElse(am -> messager.printMessage(kind, message, element, am),
                                 () -> messager.printMessage(kind, message, element));
    }

    /**
     * Returns the current processing round number. Numbering starts at 1.
     * @return
     */
    protected int getRoundNumber() {
        return roundNumber;
    }

    protected static String formatSequence(String name, final Iterator<?> iterator, final String separator) {
        final StringJoiner sj = new StringJoiner(",");

        int size = 0;
        while (iterator.hasNext()) {
            sj.add(iterator.next().toString());
            size += 1;
        }

        return "%s[%s]: [%s]".formatted(name, size, sj);
    }

    protected static String formatSequence(String name, final Iterator<?> iterator) {
        return formatSequence(name, iterator, ",");
    }

    protected static String formatSequence(String name, final Iterable<?> iterable, final String separator) {
        return formatSequence(name, iterable.iterator(), separator);
    }

    protected static String formatSequence(String name, final Iterable<?> iterable) {
        return formatSequence(name, iterable.iterator(), ",");
    }

    protected AnnotationSpec buildAtGenerated(String date) {
        return CodeGenerationUtils.buildAnnotationGenerated(this.getClass().getCanonicalName(), date);
    }

}
