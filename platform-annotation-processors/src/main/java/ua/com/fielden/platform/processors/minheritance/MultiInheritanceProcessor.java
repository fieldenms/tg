package ua.com.fielden.platform.processors.minheritance;

import ua.com.fielden.platform.entity.annotation.Extends;
import ua.com.fielden.platform.processors.AbstractPlatformAnnotationProcessor;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/// An annotation processor for the [Extends] annotation.
///
@SupportedAnnotationTypes("*")
public class MultiInheritanceProcessor extends AbstractPlatformAnnotationProcessor {

    @Override
    protected boolean processRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        return false;
    }

}
