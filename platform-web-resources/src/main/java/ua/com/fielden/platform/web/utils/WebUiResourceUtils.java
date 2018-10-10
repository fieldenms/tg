package ua.com.fielden.platform.web.utils;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web.resources.RestServerUtil;

public class WebUiResourceUtils {
    private final static Logger LOGGER = Logger.getLogger(WebUiResourceUtils.class);

    /**
     * Restores the holder of modified properties into the map [propertyName; webEditorSpecificValue].
     *
     * @param envelope
     * @return
     */
    public static Map<String, Object> restoreModifiedPropertiesHolderFrom(final Representation envelope, final RestServerUtil restUtil) {
        return (Map<String, Object>) restUtil.restoreJSONMap(envelope);
    }

    /**
     * Restores the holder of context and criteria entity.
     *
     * @param envelope
     * @return
     */
    public static CentreContextHolder restoreCentreContextHolder(final Representation envelope, final RestServerUtil restUtil) {
        return restUtil.restoreJSONEntity(envelope, CentreContextHolder.class);
    }

    /**
     * Restores the {@link Result} from JSON envelope.
     *
     * @param envelope
     * @return
     */
    public static Result restoreJSONResult(final Representation envelope, final RestServerUtil restUtil) {
        return restUtil.restoreJSONResult(envelope);
    }

    /**
     * Restores the holder of saving information (modified props + centre context, if any).
     *
     * @param envelope
     * @return
     */
    public static SavingInfoHolder restoreSavingInfoHolder(final Representation envelope, final RestServerUtil restUtil) {
        return restUtil.restoreJSONEntity(envelope, SavingInfoHolder.class);
    }

    /**
     * This method wraps the function of representation creation to handle properly <b>undesired</b> server errors.
     * <p>
     * Please note that all <b>expected</b> exceptional situations should be handled inside the respective 'representationCreator' and one should not rely on this method for such
     * errors.
     *
     * @param representationCreator
     * @return
     */
    public static Representation handleUndesiredExceptions(final Response response, final Supplier<Representation> representationCreator, final RestServerUtil restUtil) {
        try {
            return representationCreator.get();
        } catch (final Exception undesiredEx) {
            LOGGER.error(undesiredEx.getMessage(), undesiredEx);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            return restUtil.errorJSONRepresentation(undesiredEx);
        }
    }

}
