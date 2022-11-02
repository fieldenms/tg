package ua.com.fielden.platform.processors.test_utils;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.annotation.processing.Processor;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Implementation of {@link org.junit.rules.TestRule} that compiles java sources at runtime, storing them in memory, and provides access to instances of {@link Elements} and {@link Types} for further analysis of both input java sources and those that were generated during annotation processing.
 * 
 * @author TG Team
 */
public final class CompilationRule implements TestRule {
    private static final JavaFileObject PLACEHOLDER = InMemoryJavaFileObjects.createJavaSource("Placeholder", "final class Placeholder {}");

    private Collection<? extends JavaFileObject> javaSources;
    private Processor processor;
    private Elements elements;
    private Types types;

    /**
     * Only a single annotation processor is allowed per rule to ensure that the processing environment is not shared with other processors, which could lead to unexpected behaviour.
     * 
     * @param javaSources java sources to compile
     * @param processor annotation processor to use during compilation
     */
    public CompilationRule(final Collection<? extends JavaFileObject> javaSources, final Processor processor) {
        this.javaSources = javaSources.isEmpty() || javaSources == null ? List.of(PLACEHOLDER) : javaSources;
        this.processor = processor;
    }

    public CompilationRule(final Collection<? extends JavaFileObject> javaSources) {
        this(javaSources, null);
    }

    public CompilationRule() {
        this(List.of(), null);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                final InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(compiler.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8)); 
                // perform only annotation processing, without subsequent compilation
                final List<String> options = List.of("-proc:only");
                final Compilation compilation = new Compilation(javaSources, processor, compiler, fileManager, options);
                compilation.compileAndEvaluatef((procEnv) -> {
                    elements = procEnv.getElementUtils();
                    types = procEnv.getTypeUtils();
                    base.evaluate();
                });
            }
        };
    }

    public Elements getElements() {
        return elements;
    }

    public Types getTypes() {
        return types;
    }

}