package ua.com.fielden.platform.web.rx.eventsources;

import ua.com.fielden.platform.rx.observables.ProcessingProgressSubject;
import ua.com.fielden.platform.web.sse.AbstractEventSource;

/**
 * This is a generic event source that should be used to update on events from subject of type {@link ProcessingProgressSubject}.
 * 
 * @author TG Team
 *
 */
public class ProcessingProgressEventSrouce extends AbstractEventSource<Integer, ProcessingProgressSubject> {

    public ProcessingProgressEventSrouce(final ProcessingProgressSubject observableKind) {
        super(observableKind);
    }

    @Override
    protected String eventToData(final Integer event) {
        return String.format("{\"prc\": %s}", event);
    }

}
