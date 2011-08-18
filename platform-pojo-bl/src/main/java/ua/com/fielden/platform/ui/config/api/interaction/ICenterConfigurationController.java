package ua.com.fielden.platform.ui.config.api.interaction;

import java.util.List;

/**
 * A contract for configuration and management of entity centres.
 *
 * @author TG Team
 *
 */
public interface ICenterConfigurationController extends IConfigurationController, ICenterConfigurationManager {

    /**
     * Returns the key for the principle entity center.
     *
     * @param forType
     *            - type for which entity center key should be generated.
     * @return
     */
    String generateKeyForPrincipleCenter(final Class<?> forType);

    /**
     * Returns the key for the non principle entity center specified with appropriate principle entity center and name of the non principle center.
     *
     * @param principleCenterKey
     *            - key for appropriate principle entity center.
     * @param nonPrincipleCenterName
     *            - the name of the non principle entity center.
     * @return
     */
    String generateKeyForNonPrincipleCenter(final String principleCenterKey, final String nonPrincipleCenterName);

    /**
     * Returns all non principle entity centers for principle specified with principleCenterKey parameter.
     *
     * @param principleCenterKey
     *            - specifies principle entity center for which non principle centers should be retrieved.
     * @return
     */
    List<String> getNonPrincipleCenters(final String principleCenterKey);

    /**
     * Determines whether specified non principle entity center name is valid or not.
     *
     * @param principleCenterKey
     *            - the principle entity center key for which nonPrincipleCenterName must be validated.
     * @param nonPrincipleCenterName
     *            - Non principle entity center name.
     */
    boolean isNonPrincipleCenterNameValid(final String principleCenterKey, final String nonPrincipleCenterName);

}
