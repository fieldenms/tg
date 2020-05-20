package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.master.MasterInfo;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.exceptions.MissingEntityTypeException;
import ua.com.fielden.platform.web.view.master.MasterInfoProvider;

/**
 * Web resource for that gets the {@link MasterInfo}
 *
 * @author TG Team
 *
 */
public class MasterInfoProviderResource extends AbstractWebResource {

    private final MasterInfoProvider masterInfoProvider;
    private final RestServerUtil restUtil;

    public MasterInfoProviderResource(final MasterInfoProvider masterInfoProvider, final RestServerUtil restUtil, final IDeviceProvider deviceProvider, final IDates dates, final Context context, final Request request, final Response response) {
        super(context, request, response, deviceProvider, dates);
        this.masterInfoProvider = masterInfoProvider;
        this.restUtil = restUtil;
    }

    @Get
    @Override
    public Representation get() {
        return handleUndesiredExceptions(getResponse(), () -> restUtil.singleJsonMasterRepresentation(masterInfoProvider.getMasterInfo(getEntityType()), getRequest().getAttributes().get("entityType").toString()), restUtil);
    }

    /**
     * Returns the entity type as class. If entity type attribute of URI can not be converted to class {@link MissingEntityTypeException} exception will be thrown.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    private Class<? extends AbstractEntity<?>> getEntityType() {
        final String entityType = getRequest().getAttributes().get("entityType").toString();
        try {
            return (Class<? extends AbstractEntity<?>>) Class.forName(entityType);
        } catch (final ClassNotFoundException e) {
            throw new MissingEntityTypeException(String.format("The entity type class is missing for type: %s", entityType), e);
        }
    }

}
