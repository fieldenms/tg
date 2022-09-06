package ua.com.fielden.platform.web.test.eventsources;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.observables.TgMessageChangeSubject;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.sse.AbstractEventSource;

/**
 * Event source to notify changes to {@link TgMessage}.
 *
 * @author TG Team
 *
 */
public class TgMessageEventSource extends AbstractEventSource<TgMessage, TgMessageChangeSubject> {

    @Inject
    protected TgMessageEventSource(final TgMessageChangeSubject observableKind, final ISerialiser serialiser) {
        super(observableKind, serialiser);
    }

    @Override
    protected Map<String, Object> eventToData(final TgMessage event) {
        final Map<String, Object> data = new HashMap<>();
        data.put("id", event.getMachine().getId());
        data.put("key", event.getKey());
        data.put("changeDate", new Date());
        return data;
    }

}
