package ua.com.fielden.platform.reflection.testclasses;

import ua.com.fielden.platform.reflection.testannotation.BaseClassAnnotation;
import ua.com.fielden.platform.reflection.testannotation.MethodInBaseClassAnnotation;

@BaseClassAnnotation
public class BaseClass {

    @MethodInBaseClassAnnotation
    public void baseMethod() {

    }
}
