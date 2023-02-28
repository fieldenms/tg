package ua.com.fielden.platform.entity.annotation.factory;

import java.util.Arrays;

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

    public static BeforeChange from(final Handler... handlers) {
        return new BeforeChangeAnnotation(handlers).newInstance();
    }

    /**
     * Returns the result of merging {@link BeforeChange} annotation instances.
     * Handlers of the resulting annotation are ordered in the same way as given annotations. No deduplication takes place.
     * <p>
     * If a single annotation is given, then it is simply returned back.
     *
     * @param annotations {@link BeforeChange} instances to be merged
     * @return merged instance of the {@link BeforeChange} annotation
     */
    public static BeforeChange merged(final BeforeChange... annotations) {
        if (annotations.length == 1) return annotations[0];
        final Handler[] handlers = Arrays.stream(annotations).flatMap(bch -> Arrays.stream(bch.value())).toArray(Handler[]::new);
        return new BeforeChangeAnnotation(handlers).newInstance();
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
