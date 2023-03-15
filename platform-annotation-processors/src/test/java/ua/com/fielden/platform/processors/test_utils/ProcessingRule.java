package ua.com.fielden.platform.processors.test_utils;

import static ua.com.fielden.platform.processors.test_utils.Compilation.OPTION_PROC_ONLY;

import java.util.Collection;
import java.util.List;

import javax.annotation.processing.Processor;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Implementation of {@link org.junit.rules.TestRule} that runs test methods during the last round of annotation processing.
 * <p>
 * This rule can be used for:
 * <ul>
 *   <li>Testing a specific annotation processor, while optionally providing a set of sources to be processed.</li>
 *   <li>Simply obtaining access to the processing environment in the form of instances of {@link Elements} and {@link Types}.</li>
 * </ul>
 * Any files generated during annotation processing will be stored in memory. 
 * 
 * @author TG Team
 */
public class ProcessingRule implements TestRule {
    private static final JavaFileObject PLACEHOLDER = InMemoryJavaFileObjects.createJavaSource("Placeholder", "final class Placeholder {}");

    private Collection<? extends JavaFileObject> javaSources;
    private Processor processor;
    private Elements elements;
    private Types types;

    /**
     * Only a single annotation processor is allowed per rule to ensure that the processing environment is not shared with other processors, 
     * which could lead to unexpected behaviour.
     * 
     * @param javaSources java sources to compile (may be empty or {@code null})
     * @param processor annotation processor instance to use
     */
    public ProcessingRule(final Collection<? extends JavaFileObject> javaSources, final Processor processor) {
        this.javaSources = javaSources.isEmpty() || javaSources == null ? List.of(PLACEHOLDER) : javaSources;
        this.processor = processor;
    }

    /**
     * Initializes this rule with the a collection of java sources to be processed.
     * 
     * @param javaSources java sources to compile (may be empty or {@code null})
     */
    public ProcessingRule(final Collection<? extends JavaFileObject> javaSources) {
        this(javaSources, null);
    }

    /**
     * Initializes this rule with the minimum required inputs to get into the processing environment. 
     */
    public ProcessingRule() {
        this(List.of(), null);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final Compilation compilation = Compilation.newInMemory(javaSources)
                        .setProcessor(processor)
                        // perform only annotation processing without subsequent compilation
                        .setOptions(OPTION_PROC_ONLY);

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