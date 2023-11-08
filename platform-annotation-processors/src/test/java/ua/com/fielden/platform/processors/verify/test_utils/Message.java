package ua.com.fielden.platform.processors.verify.test_utils;

import javax.tools.Diagnostic.Kind;
import java.lang.annotation.Retention;
import java.util.Objects;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Informs the {@link MessagePrintingVerifier} about a diagnostic message that should be reported on an element.
 *
 * @author TG Team
 */
@Retention(SOURCE)
public @interface Message {

    /** Message contents */
    String value();

    /** Message kind */
    Kind kind();

    public record Factory(String value, Kind kind) {

        public static Message create(final String value, final Kind kind) {
            return new Factory(value, kind).newInstance();
        }

        public Message newInstance() {
            return new Message() {
                @Override public Class<Message> annotationType() { return Message.class; }

                @Override public String value() { return value; }
                @Override public Kind kind() { return kind; }

                @Override
                public boolean equals(final Object other) {
                    if (this == other) {
                        return true;
                    }
                    return other instanceof Message atOther &&
                            Objects.equals(this.value(), atOther.value()) &&
                            Objects.equals(this.kind(), atOther.kind());
                }
            };
        }

    }

}
