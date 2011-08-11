package ua.com.fielden.platform.reflection.testclasses.innerpackage.nested;

import ua.com.fielden.platform.reflection.testannotation.DerivedClassAnnotation;
import ua.com.fielden.platform.reflection.testannotation.MethodInDerivedClassAnnotation;

@DerivedClassAnnotation
public class NestedClassAnnotated {

    @MethodInDerivedClassAnnotation
    public void annotatedMethod() {

    }

}
