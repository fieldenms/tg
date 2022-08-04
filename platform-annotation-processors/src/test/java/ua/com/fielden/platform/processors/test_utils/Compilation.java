package ua.com.fielden.platform.processors.test_utils;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
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
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.junit.runners.model.Statement;

import ua.com.fielden.platform.types.try_wrapper.FailableConsumer;

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

    /**
     * Only a single annotation processor is allowed to ensure that the processing environment is not shared with other processors, which could lead to unexpected behaviour.
     * 
     * @param javaSources java sources to compile
     * @param processor annotation processor to use during compilation
     * @param compiler
     * @param fileManager
     */
    public Compilation(final Collection<? extends JavaFileObject> javaSources, final Processor processor, final JavaCompiler compiler, final JavaFileManager fileManager) {
        this.javaSources = javaSources;
        this.processor = processor;
        this.compiler = compiler == null ? ToolProvider.getSystemJavaCompiler() : compiler;
        this.fileManager = fileManager == null ? this.compiler.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8) : fileManager;
    }

    public Compilation(final Collection<? extends JavaFileObject> javaSources) {
        this(javaSources, null, null, null);
    }

    /**
     * Similar to {@link #compileAndEvaluate}, but accepts a {@link FailableConsumer}.
     * <p>
     * Use whenever <code>evaluator</code> might throw.
     * @param evaluator
     * @return
     * @throws Throwable
     */
    public boolean compileAndEvaluatef(final FailableConsumer<ProcessingEnvironment> evaluator) throws Throwable {
            final EvaluatingProcessor evaluatingProcessor = new EvaluatingProcessor(evaluator);
            boolean success = compile(evaluatingProcessor);
            evaluatingProcessor.throwIfStatementThrew();
            return success;
    }
    
    /**
     * Performs compilation and applies <code>evaluator</code> during the last round of annotation processing.
     * @param evaluator
     * @return
     * @throws Throwable
     */
    public boolean compileAndEvaluate(final Consumer<ProcessingEnvironment> evaluator) throws Throwable {
        return compileAndEvaluatef((procEnv) -> evaluator.accept(procEnv));
    }

    private boolean compile(final EvaluatingProcessor processor) {
        final CompilationTask task = compiler.getTask(
                null, // Writer for additional output from the compiler (null => System.err)                
                fileManager,
                null, // diagnostic listener
                null, // compiler options
                null, // names of classes to be processed by annotation processing (?)
                javaSources);
        task.setProcessors(List.of(processor));
        return task.call();
    }

    private final class EvaluatingProcessor extends AbstractProcessor {

        final FailableConsumer<ProcessingEnvironment> evaluator;
        Throwable thrown;

        EvaluatingProcessor(final FailableConsumer<ProcessingEnvironment> evaluator) {
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
                } catch (Throwable e) {
                    thrown = e;
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