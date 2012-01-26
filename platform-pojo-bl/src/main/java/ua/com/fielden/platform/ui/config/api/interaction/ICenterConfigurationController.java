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
     * Returns the key for the principle entity centre.
     *
     * @param forType
     *            - type for which entity centre key should be generated.
     * @return
     */
    String generateKeyForPrincipleCenter(final Class<?> forType);

    /**
     * Returns the key for the non principle entity centre specified with appropriate principle entity center and name of the non principle center.
     *
     * @param principleCenterKey
     *            - key for appropriate principle entity centre.
     * @param nonPrincipleCenterName
     *            - the name of the non principle entity centre.
     * @return
     */
    String generateKeyForNonPrincipleCenter(final String principleCenterKey, final String nonPrincipleCenterName);

    /**
     * Returns all non principle entity centres for principle specified with principleCenterKey parameter.
     *
     * @param principleCenterKey
     *            - specifies principle entity centre for which non principle centres should be retrieved.
     * @return
     */
    List<String> getNonPrincipleCenters(final String principleCenterKey);

    /**
     * Determines whether specified non principle entity centre name is valid or not.
     *
     * @param principleCenterKey
     *            - the principle entity centre key for which nonPrincipleCenterName must be validated.
     * @param nonPrincipleCenterName
     *            - Non principle entity centre name.
     */
    boolean isNonPrincipleCenterNameValid(final String principleCenterKey, final String nonPrincipleCenterName);

}
