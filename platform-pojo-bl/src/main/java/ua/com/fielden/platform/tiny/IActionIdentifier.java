package ua.com.fielden.platform.tiny;

import static ua.com.fielden.platform.entity.exceptions.InvalidArgumentException.requireNotNullArgument;

/// Represents an identifier associated with an action configuration.
///
/// Action identifiers must be unique, but this interface by itself does not enforce uniqueness.
///
/// The recommended method of defining action identifiers is to create an enum implementing this interface.
///
/// ```java
/// /// Example of an application-level enum.
/// ///
/// public enum ActionIdentifiers implements IActionIdentifier {
///
///     COPY_WO_TOP,
///     COPY_WO_EMBEDDED,
///     ...;
/// }
/// ```
///
public interface IActionIdentifier extends CharSequence {

    /// Returns the identifier's name (i.e., its content).
    ///
    /// For convenience, this method need not be overriden in enums due to the existence of [Enum#name()].
    ///
    /// If this method is overriden, it must return the same result as [#toString()].
    ///
    CharSequence name();

    /// Creates a new action identifier with the specified name.
    ///
    /// This method is provided for convenient use in those cases where action identifiers are created dynamically.
    ///
    static IActionIdentifier of(final CharSequence name) {
        requireNotNullArgument(name, "name");

        final var str = name.toString();

        return new IActionIdentifier() {
            @Override
            public CharSequence name() {
                return str;
            }

            @Override
            public String toString() {
                return str;
            }
        };
    }

    @Override
    default int length() {
        return name().length();
    }

    @Override
    default char charAt(final int index) {
        return name().charAt(index);
    }

    @Override
    default CharSequence subSequence(final int start, final int end) {
        return name().subSequence(start, end);
    }

}
