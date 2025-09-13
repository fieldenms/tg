package ua.com.fielden.platform.serialisation.jackson.serialisers;

import com.fasterxml.jackson.core.JsonGenerator;
import ua.com.fielden.platform.continuation.NeedMoreDataException;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;

import java.io.IOException;

import static ua.com.fielden.platform.continuation.NeedMoreDataException.CONTINUATION_PROPERTY;
import static ua.com.fielden.platform.continuation.NeedMoreDataException.CONTINUATION_TYPE_STR;

/// [NeedMoreDataException] standard Jackson serialiser.
/// Serialises additional data comparing to the data in [Result] base type.
///
public class NeedMoreDataExceptionJsonSerialiser extends ResultJsonSerialiser {

    /// Creates [NeedMoreDataExceptionJsonSerialiser].
    ///
    public NeedMoreDataExceptionJsonSerialiser(final TgJackson tgJackson) {
        super(tgJackson);
    }

    /// Write additional [NeedMoreDataException] data comparing to the data in [Result] base type.
    ///
    /// Skip the properties that are not needed in client-side representation.
    /// These include [NeedMoreDataException#continuationType] and [NeedMoreDataException#maybeContinuation].
    ///
    @Override
    protected void writeResultData(final Result result, final JsonGenerator generator) throws IOException {
        super.writeResultData(result, generator);

        final var needDataEx = (NeedMoreDataException) result;
        generator.writeFieldName(CONTINUATION_TYPE_STR);
        generator.writeObject(needDataEx.continuationTypeStr);
        generator.writeFieldName(CONTINUATION_PROPERTY);
        generator.writeObject(needDataEx.continuationProperty);
    }

}