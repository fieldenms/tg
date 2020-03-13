package ua.com.fielden.platform.processors;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.google.auto.service.AutoService;

import ua.com.fielden.platform.annotations.companion.Category;

@AutoService(Processor.class)
public class CategoryAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment env) {

        for (final Element element : env.getRootElements()) {
            if (element.getSimpleName().toString().startsWith("Toy")) {
                processingEnv.getMessager().printMessage(Kind.WARNING, "Do not play with toys!", element);
            }

            if (element.getAnnotation(Category.class) != null) {
                processingEnv.getMessager().printMessage(Kind.WARNING, "Be carefull with this annotations... it bites!", element);
            }
        }

        //	for (final TypeElement element : annotations) {
        //	    System.out.println(element.getQualifiedName());
        //	}

        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotations = new LinkedHashSet<String>();
        annotations.add(Category.class.getCanonicalName());
        return annotations;
    }
}