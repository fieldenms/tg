package ua.com.fielden.platform.reflection.testclasses;

import ua.com.fielden.platform.reflection.testannotation.DerivedClassAnnotation;
import ua.com.fielden.platform.reflection.testannotation.MethodInDerivedClassAnnotation;

@DerivedClassAnnotation
public class GrandSonClass extends DerivedClass {

    @MethodInDerivedClassAnnotation
    public void derivedMethod() {

    }
}
