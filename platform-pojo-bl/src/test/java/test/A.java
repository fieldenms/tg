package test;

import ua.com.fielden.platform.entity.annotation.Observable;

public class A<T extends Comparable<?>> {
    @Observable
    public void setKey(final T test){

    }
}
