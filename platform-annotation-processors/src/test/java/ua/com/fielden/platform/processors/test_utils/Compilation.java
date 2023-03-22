package ua.com.fielden.platform.processors.test_utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.junit.runners.model.Statement;

import ua.com.fielden.platform.processors.test_utils.exceptions.CompilationException;
import ua.com.fielden.platform.types.try_wrapper.ThrowableConsumer;

/**
 * An abstraction for compiling java sources that is based on {@link CompilationTask} with the primary purpose of evaluating additional statements in the annotation processing environment.
 * <p>
 * In order to obtain independent results it is recommended to use a fresh instance for every invokation of a method responsible for compilation.
 * In other words, compiling more than once with the same instance of this class does not guarantee the result will be independent of the
 * previous compilation.
 * <p>
 * Makes a convenient annotation processor testing utility.
 *
 * @author TG Team
 */
public final class Compilation {
    /** Java compiler option to perform only annotation processing (without subsequent compilation) */
    public static final String OPTION_PROC_ONLY = "-proc:only";

    private Collection<? extends JavaFileObject> javaSources;
    private Processor processor;
    private JavaCompiler compiler;
    private JavaFileManager fileManager;
    private List<String> options = new LinkedList<>();
    private DiagnosticCollector<JavaFileObject> diagnosticListener = new DiagnosticCollector<>();

    /**
     * Creates a new instance that stores the compiled sources in memory by using a preconfigured {@link #compiler} and {@link #fileManager}.
     *
     * @param javaSources   java sources to be compiled
     * @return              a new compilation instance
     */
    public static Compilation newInMemory(final Collection<? extends JavaFileObject> javaSources) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(compiler.getStandardFileManager(null, null, null));

        return new Compilation(javaSources).setCompiler(compiler).setFileManager(fileManager);
    }

    /**
     * Only a single annotation processor is allowed to ensure that the processing environment is not shared with other processors, which could lead to unexpected behaviour.
     *
     * @param javaSources java sources to compile
     * @param processor annotation processor to use during compilation
     * @param compiler
     * @param fileManager
     * @param options
     */
    public Compilation(final Collection<? extends JavaFileObject> javaSources) {
        this.javaSources = javaSources;
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.fileManager = compiler.getStandardFileManager(null, null, null);
    }

    public Compilation setJavaSources(final Collection<? extends JavaFileObject> javaSources) {
        this.javaSources = javaSources;
        return this;
    }

    public Compilation setProcessor(final Processor processor) {
        this.processor = processor;
        return this;
    }

    public Compilation setCompiler(final JavaCompiler compiler) {
        this.compiler = compiler;
        return this;
    }

    public Compilation setFileManager(final JavaFileManager fileManager) {
        this.fileManager = fileManager;
        return this;
    }

    public Compilation addOptions(final Iterable<String> options) {
        options.forEach(opt -> this.options.add(opt));
        return this;
    }

    public Compilation addOptions(final String... options) {
        this.options.addAll(List.of(options));
        return this;
    }

    public Compilation addProcessorOption(final String key, final String value) {
        this.options.add("-A%s=%s".formatted(key, value));
        return this;
    }

    public Compilation setDiagnosticListener(final DiagnosticCollector<JavaFileObject> diagnosticListener) {
        this.diagnosticListener = diagnosticListener;
        return this;
    }

    /**
     * Similar to {@link #compileAndEvaluate}, but accepts a {@link ThrowableConsumer}.
     * <p>
     * Use whenever {@code evaluator} might throw.
     *
     * @param evaluator
     * @return result of the compilation
     * @throws Throwable
     */
    public CompilationResult compileAndEvaluatef(final ThrowableConsumer<ProcessingEnvironment> evaluator) {
        final EvaluatingProcessor evaluatingProcessor = new EvaluatingProcessor(evaluator);
        final CompilationResult result = doCompile(evaluatingProcessor);
        evaluatingProcessor.throwIfStatementThrew();
        return result;
    }

    /**
     * Performs compilation and applies {@code evaluator} during the last round of annotation processing.
     *
     * @param evaluator
     * @return result of the compilation
     * @throws Throwable
     */
    public CompilationResult compileAndEvaluate(final Consumer<ProcessingEnvironment> evaluator) {
        return compileAndEvaluatef((procEnv) -> evaluator.accept(procEnv));
    }

    public CompilationResult compile() {
        return compileAndEvaluate((procEnv) -> {});
    }

    private CompilationResult doCompile(final EvaluatingProcessor processor) {
        final CompilationTask task = compiler.getTask(
                null, // Writer for additional output from the compiler (null => System.err)
                fileManager,
                diagnosticListener,
                options,
                null, // names of classes to be processed by annotation processing (?)
                javaSources);
        task.setProcessors(List.of(processor));
        final boolean success = task.call();

        return new CompilationResult(success, diagnosticListener.getDiagnostics());
    }

    /**
     * An annotation processor that wraps {@code Compilation.processor}, passed during instantiation, in order to to be able to control its execution and error reporting.
     *
     */
    private final class EvaluatingProcessor extends AbstractProcessor {

        private final ThrowableConsumer<ProcessingEnvironment> evaluator;
        private CompilationException thrown;

        public EvaluatingProcessor(final ThrowableConsumer<ProcessingEnvironment> evaluator) {
            this.evaluator = evaluator;
        }

        @Override
        public SourceVersion getSupportedSourceVersion() {
            return SourceVersion.latest();
        }

        @Override
        public Set<String> getSupportedAnnotationTypes() {
            return Set.of("*");
        }

        @Override
        public synchronized void init(ProcessingEnvironment processingEnv) {
            super.init(processingEnv);
            if (processor != null) {
                processor.init(processingEnv);
            }
        }

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            if (processor != null) {
                processor.process(annotations, roundEnv);
            }
            if (roundEnv.processingOver()) {
                try {
                    evaluator.accept(processingEnv);
                } catch (final Throwable ex) {
                    thrown = new CompilationException(ex);
                }
            }
            return false;
        }

        /**
         * Throws what {@code base} {@link Statement} threw, if anything.
         */
        void throwIfStatementThrew() {
            if (thrown != null) {
                throw thrown;
            }
        }
    }

}