package ua.com.fielden.platform.web.rx.eventsources;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.rx.observables.ProcessingProgressSubject;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.sse.AbstractEventSource;

/**
 * This is a generic event source that should be used to update on events from subject of type {@link ProcessingProgressSubject}.
 *
 * @author TG Team
 *
 */
public class ProcessingProgressEventSource extends AbstractEventSource<Integer, ProcessingProgressSubject> {

    public ProcessingProgressEventSource(final ProcessingProgressSubject observableKind, final ISerialiser serialiser) {
        super(observableKind, serialiser);
    }

    @Override
    protected Map<String, Object> eventToData(final Integer event) {
        final Map<String, Object> data = new HashMap<>();
        data.put("prc", event);
        return data;
    }

}
