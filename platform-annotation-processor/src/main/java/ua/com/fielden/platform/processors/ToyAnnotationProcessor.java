package ua.com.fielden.platform.processors;

import java.util.*;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.tools.Diagnostic.Kind;

import ua.com.fielden.platform.annotations.ClassLevelAnnotation;

@SupportedAnnotationTypes(value = { "*" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ToyAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment env) {

	for (final Element element : env.getRootElements()) {
	    if (element.getSimpleName().toString().startsWith("Toy")) {
		processingEnv.getMessager().printMessage(Kind.WARNING, "Do not play with toys!", element);
	    }

	    if (element.getAnnotation(ClassLevelAnnotation.class) != null) {
		processingEnv.getMessager().printMessage(Kind.WARNING, "Be carefull with this annotations... it bites!", element);
	    }
	}

//	for (final TypeElement element : annotations) {
//	    System.out.println(element.getQualifiedName());
//	}

	return false;
    }
}