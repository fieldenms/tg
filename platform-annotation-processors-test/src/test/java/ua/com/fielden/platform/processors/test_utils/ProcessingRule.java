package ua.com.fielden.platform.processors.test_utils;

import com.google.common.collect.ImmutableList;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.util.Collection;
import java.util.List;

import static ua.com.fielden.platform.processors.test_utils.Compilation.OPTION_PROC_ONLY;

/**
 * Implementation of {@link TestRule} that runs test methods during the last round of annotation processing.
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
    private ProcessingEnvironment processingEnvironment;

    /**
     * Only a single annotation processor is allowed per rule to ensure that the processing environment is not shared with other processors, 
     * which could lead to unexpected behaviour.
     * 
     * @param javaSources java sources to compile (may be empty or {@code null})
     * @param processor annotation processor instance to use
     */
    public ProcessingRule(final Collection<? extends JavaFileObject> javaSources, final Processor processor) {
        this.javaSources = javaSources == null || javaSources.isEmpty() ? List.of(PLACEHOLDER) : javaSources;
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
                        .setProcessors(processor == null ? ImmutableList.of() : ImmutableList.of(processor))
                        // perform only annotation processing without subsequent compilation
                        .addOptions(OPTION_PROC_ONLY);

                compilation.compileAndEvaluatef((procEnv) -> {
                    processingEnvironment = procEnv;
                    base.evaluate();
                });
            }
        };
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return processingEnvironment;
    }

    public Elements getElements() {
        return processingEnvironment.getElementUtils();
    }

    public Types getTypes() {
        return processingEnvironment.getTypeUtils();
    }

}
