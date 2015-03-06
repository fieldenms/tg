package ua.com.fielden.platform.web.resources.webui;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Resource for integration test of Java and JavaScript serialisation.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 */
public class EgiExampleResource extends ServerResource {
    private final RestServerUtil restUtil;
    private final List<AbstractEntity<?>> entities;

    public EgiExampleResource(final EntityFactory entityFactory, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        init(context, request, response);
        this.restUtil = restUtil;
        this.entities = createEntities(entityFactory);
    }

    /**
     * Handles sending of the serialised testing entities to the Web UI client (GET method).
     */
    @Override
    protected Representation get() throws ResourceException {
        return restUtil.listJSONRepresentation(this.entities);
    }

    private static List<AbstractEntity<?>> createEntities(final EntityFactory entityFactory) {
        return Arrays.asList(
                entityFactory.newByKey(TgPersistentEntityWithProperties.class, "KEY1").setIntegerProp(43)
                        .setDesc("Description for entity with key 1. This is a relatively long description to demonstrate how well does is behave during value autocompletion."),
                entityFactory.newByKey(TgPersistentEntityWithProperties.class, "KEY2").setIntegerProp(14).setDesc("Description for entity with key 2."),
                entityFactory.newByKey(TgPersistentEntityWithProperties.class, "KEY3").setIntegerProp(15).setDesc("Description for entity with key 3."),
                entityFactory.newByKey(TgPersistentEntityWithProperties.class, "KEY4").setIntegerProp(63).setMoneyProp(new Money(new BigDecimal(23.0))).setDesc("Description for entity with key 4."),
                entityFactory.newByKey(TgPersistentEntityWithProperties.class, "KEY5").setBigDecimalProp(new BigDecimal(23.0)).setDesc("Description for entity with key 5."),
                entityFactory.newByKey(TgPersistentEntityWithProperties.class, "KEY6").setIntegerProp(61).setStringProp("ok").setDesc("Description for entity with key 6."),
                entityFactory.newByKey(TgPersistentEntityWithProperties.class, "KEY7").setBooleanProp(true).setDesc("Description for entity with key 7."),
                entityFactory.newByKey(TgPersistentEntityWithProperties.class, "KEY8").setDateProp(new DateTime(9999L).toDate()).setDesc("Description for entity with key 8."),
                entityFactory.newByKey(TgPersistentEntityWithProperties.class, "DEFAULT_KEY").setIntegerProp(7).setMoneyProp(new Money(new BigDecimal(7))).setBigDecimalProp(new BigDecimal(7.7))
                        .setStringProp("ok_def").setBooleanProp(true).setDateProp(new DateTime(7777L).toDate()).setDesc("Default entity description"),
                entityFactory.newByKey(TgPersistentEntityWithProperties.class, "KEY10").setConflictingProp("initial").setNonConflictingProp("initial").setDesc("Description for entity with key 10."),
                entityFactory.newByKey(TgPersistentEntityWithProperties.class, "KEY11").setStringProp("ok").setIntegerProp(43).setBigDecimalProp(new BigDecimal(23.0)).setDateProp(new DateTime(960000L).toDate()).setBooleanProp(true).setDesc("Description for entity with key 11.")
                );
    }
}
