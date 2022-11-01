package ua.com.fielden.platform.processors.test_utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.junit.runners.model.Statement;

import ua.com.fielden.platform.types.try_wrapper.ThrowableConsumer;

/**
 * An abstraction for compiling java sources that is based on {@link CompilationTask} with the primary purpose of evaluating additional statements in the annotation processing environment.
 * <p>
 * Makes a convenient annotation processor testing utility.
 * 
 * @author TG Team
 *
 */
public final class Compilation {
    private Collection<? extends JavaFileObject> javaSources;
    private Processor processor;
    private JavaCompiler compiler;
    private JavaFileManager fileManager;
    private Iterable<String> options;
    private final List<Diagnostic<? extends JavaFileObject>> diagnostics = new ArrayList<>();

    /**
     * Only a single annotation processor is allowed to ensure that the processing environment is not shared with other processors, which could lead to unexpected behaviour.
     * 
     * @param javaSources java sources to compile
     * @param processor annotation processor to use during compilation
     * @param compiler
     * @param fileManager
     * @param options
     */
    public Compilation(final Collection<? extends JavaFileObject> javaSources, final Processor processor, final JavaCompiler compiler, final JavaFileManager fileManager, final Iterable<String> options) {
        this.javaSources = javaSources;
        this.processor = processor;
        this.compiler = compiler == null ? ToolProvider.getSystemJavaCompiler() : compiler;
        this.fileManager = fileManager == null ? this.compiler.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8) : fileManager;
        this.options = options;
    }

    public Compilation(final Collection<? extends JavaFileObject> javaSources) {
        this(javaSources, null, null, null, null);
    }

    /**
     * Similar to {@link #compileAndEvaluate}, but accepts a {@link ThrowableConsumer}.
     * <p>
     * Use whenever {@code evaluator} might throw.
     *
     * @param evaluator
     * @return
     * @throws Throwable
     */
    public boolean compileAndEvaluatef(final ThrowableConsumer<ProcessingEnvironment> evaluator) throws Throwable {
        final EvaluatingProcessor evaluatingProcessor = new EvaluatingProcessor(evaluator);
        final boolean success = compile(evaluatingProcessor);
        evaluatingProcessor.throwIfStatementThrew();
        return success;
    }
    
    /**
     * Performs compilation and applies {@code evaluator} during the last round of annotation processing.
     *
     * @param evaluator
     * @return
     * @throws Throwable
     */
    public boolean compileAndEvaluate(final Consumer<ProcessingEnvironment> evaluator) throws Throwable {
        return compileAndEvaluatef((procEnv) -> evaluator.accept(procEnv));
    }

    private boolean compile(final EvaluatingProcessor processor) {
        final DiagnosticCollector<JavaFileObject> diagnosticListener = new DiagnosticCollector<>();
        final CompilationTask task = compiler.getTask(
                null, // Writer for additional output from the compiler (null => System.err)                
                fileManager,
                diagnosticListener,
                options,
                null, // names of classes to be processed by annotation processing (?)
                javaSources);
        task.setProcessors(List.of(processor));
        final boolean success = task.call();

        this.diagnostics.addAll(diagnosticListener.getDiagnostics());

        return success;
    }

    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    /**
     * An annotation processor that wraps {@code Compilation.processor}, passed during instantiation, in order to to be able to control its execution and error reporting.
     *
     */
    private final class EvaluatingProcessor extends AbstractProcessor {

        private final ThrowableConsumer<ProcessingEnvironment> evaluator;
        private Throwable thrown;

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
                    thrown = ex;
                }
            }
            return false;
        }

        /** 
         * Throws what {@code base} {@link Statement} threw, if anything. 
         */
        void throwIfStatementThrew() throws Throwable {
            if (thrown != null) {
                throw thrown;
            }
        }
    }

}