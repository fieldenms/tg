package ua.com.fielden.platform.serialisation.jackson;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.function.Supplier;

/// Handles errors that occur in the process of deserialising property values and assigning them.
///
/// The default implementation is [#standard].
///
public interface PropertyDeserialisationErrorHandler {

    String ERR_DESERIALISATION = "Failed to deserialise property [%s.%s]. Serialised property value: [%s].";

    /// Handles `error` that occured during deserialisation and assignment of a value to `property` in `entity`.
    ///
    /// @param inputValueSupplier computes a string representation of the input for property deserialisation
    ///
    void handle(AbstractEntity<?> entity, String property, Supplier<String> inputValueSupplier, RuntimeException error);

    /// Returns a handler that first calls this handler and then calls `handler`.
    ///
    default PropertyDeserialisationErrorHandler and(final PropertyDeserialisationErrorHandler handler) {
        return (entity, property, inputValueSupplier, error) -> {
            this.handle(entity, property, inputValueSupplier, error);
            handler.handle(entity, property, inputValueSupplier, error);
        };
    }

    PropertyDeserialisationErrorHandler throwing = (_, _, _, error) -> {throw error;};

    PropertyDeserialisationErrorHandler standard = throwing;

    static String makeMessage(final AbstractEntity<?> entity, final String property, final Supplier<String> inputValueSupplier) {
        return ERR_DESERIALISATION.formatted(entity.getType().getSimpleName(), property, inputValueSupplier.get());
    }

}
