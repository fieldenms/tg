package ua.com.fielden.platform.entity.annotation.factory;

import java.util.Objects;
import java.util.stream.Stream;

import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

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

    /**
     * A factory method instantiating {@link BeforeChange} with {@code handlers}.
     *
     * @param handlers
     * @return
     */
    public static BeforeChange newInstance(final Handler... handlers) {
        return new BeforeChangeAnnotation(handlers).newInstance();
    }

    /**
     * Returns an instance of {@link BeforeChange} with {@code value} containing all handlers from {@code annotations}.
     * Handlers of the resulting annotation are ordered in the same way as in {@code annotations}, but no deduplication of handlers takes place.
     *
     * @param annotations {@link BeforeChange} instances to be merged
     * @return merged instance of the {@link BeforeChange} annotation
     */
    public static BeforeChange merge(final BeforeChange... annotations) {
        if (annotations == null || annotations.length == 0) {
            throw new InvalidArgumentException("There are no BeforeChange annotations to merge.");
        }
        if (Stream.of(annotations).anyMatch(Objects::isNull)) {
            throw new InvalidArgumentException("No annotation can be null.");
        }

        if (annotations.length == 1) {
            return annotations[0];
        }
        final Handler[] mergedHandlers = Stream.of(annotations).flatMap(bch -> Stream.of(bch.value())).toArray(Handler[]::new);
        return new BeforeChangeAnnotation(mergedHandlers).newInstance();
    }

    /**
     * Creates a new instance of {@link BeforeChange}.
     *
     * @return
     */
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

}