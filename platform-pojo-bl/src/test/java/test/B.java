package test;

import ua.com.fielden.platform.entity.annotation.Observable;

public class B extends A<String> {

    @Override
    @Observable
    public void setKey(final String test) {
    }
}
