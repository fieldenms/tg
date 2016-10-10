package ua.com.fielden.platform.web.test.eventsources;

import java.util.Date;

import com.google.inject.Inject;

import rx.Observable;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.observables.TgMessageChangeSubject;
import ua.com.fielden.platform.web.sse.AbstractEventSource;

/**
 * Event source to notify changes to DDS entries.
 *
 * @author TG Team
 *
 */
public class TgMessageEventSource extends AbstractEventSource<TgMessage, TgMessageChangeSubject> {

    @Inject
    protected TgMessageEventSource(final TgMessageChangeSubject observableKind) {
        super(observableKind);
    }

    /**
     * Overridden to provide derived from the original stream that filters events.
     */
    @Override
    protected Observable<TgMessage> getStream() {
        final Observable<TgMessage> ob = super.getStream();
        return ob;
    }

    @Override
    protected String eventToData(final TgMessage event) {
        return String.format("{\"id\": %s, \"key\": \"%s\", \"changeDate\": \"%s\"}", event.getMachine().getId(), event.getKey(), new Date());
    }

}
