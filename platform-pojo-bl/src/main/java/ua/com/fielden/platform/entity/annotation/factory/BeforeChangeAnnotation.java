package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;

/**
 * A factory for convenient instantiation of {@link BeforeChange} annotations, which mainly should be used for dynamic property creation.
 * 
 * @author TG Team
 * 
 */
public class BeforeChangeAnnotation {

    private Handler[] value = new Handler[] {};

    public BeforeChangeAnnotation(final Handler[] value) {
        this.value = value;
    }

    public BeforeChange newInstance() {
        return new BeforeChange() {

            @Override
            public Class<BeforeChange> annotationType() {
                return BeforeChange.class;
            }

            @Override
            public Handler[] value() {
                return value;
            }
        };
    }

    public BeforeChange copyFrom(final BeforeChange bch) {
        return new BeforeChange() {

            @Override
            public Class<BeforeChange> annotationType() {
                return BeforeChange.class;
            }

            @Override
            public Handler[] value() {
                return bch.value();
            }
        };
    }
}
