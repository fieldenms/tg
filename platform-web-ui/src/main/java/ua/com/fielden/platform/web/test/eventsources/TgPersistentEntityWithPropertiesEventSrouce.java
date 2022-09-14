package ua.com.fielden.platform.web.test.eventsources;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;

import rx.Observable;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.observables.TgPersistentEntityWithPropertiesChangeSubject;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.sse.AbstractEventSource;

/**
 * This is a demo event source listening to changes and creation of new instance of type {@link TgPersistentEntityWithProperties}.
 * <p>
 * It is interesting in a way that it demonstrates the application on data stream transformation by applying filtering.
 * In this example, the resultant stream receives only changes to entities with property <code>bigDecimalProp</code> of value > than 50.
 *
 * @author TG Team
 *
 */
public class TgPersistentEntityWithPropertiesEventSrouce extends AbstractEventSource<TgPersistentEntityWithProperties, TgPersistentEntityWithPropertiesChangeSubject> {

    @Inject
    protected TgPersistentEntityWithPropertiesEventSrouce(final TgPersistentEntityWithPropertiesChangeSubject observableKind, final ISerialiser serialiser) {
        super(observableKind, serialiser);
    }

    /**
     * Overridden to provide derived from the original stream that filters events.
     */
    @Override
    protected Observable<TgPersistentEntityWithProperties> getStream() {
        final Observable<TgPersistentEntityWithProperties> ob = super.getStream()
                .filter(v -> v.getBigDecimalProp() != null && v.getBigDecimalProp().compareTo(new BigDecimal(50)) > 0);
        return ob;
    }

    @Override
    protected Map<String, Object> eventToData(final TgPersistentEntityWithProperties event) {
        final Map<String, Object> data = new HashMap<>();
        data.put("id", event.getId());
        data.put("key", event.getKey());
        data.put("changeDate", new Date());
        return data;
    }

}
