package ua.com.fielden.platform.domaintree.centre;

import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;

/**
 * This interface defines how domain tree can be managed for <b>locators</b>. <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * Consists of all {@link ICentreDomainTreeManager} logic and some special indicators that are specific for locators, like <code>searchBy</code> or <code>useForAutocompletion</code> etc.
 *
 * @author TG Team
 *
 */
public interface ILocatorDomainTreeManager extends ICentreDomainTreeManager {
    /**
     * A <i>domain tree manager<i> with <i>enhancer</i> inside.
     *
     * @author TG Team
     *
     */
    public interface ILocatorDomainTreeManagerAndEnhancer extends ICentreDomainTreeManagerAndEnhancer, ILocatorDomainTreeManager {
    }

    /**
     * Returns a domain representation that is able to change domain representation rules. See {@link IDomainTreeRepresentation} documentation for more details.
     *
     * @return
     */
    ILocatorDomainTreeRepresentation getRepresentation();

    /**
     * An enumeration that indicates by what locator can be searched.
     *
     * @author TG Team
     *
     */
    public enum SearchBy {
	/**
	 * An enumeration item that indicates to search only by key. This is a default value for locators.
	 */
	KEY,
	/**
	 * An enumeration item that indicates to search only by description.
	 */
	DESC,
	/**
	 * An enumeration item that indicates to search by description and key.
	 */
	DESC_AND_KEY
    }

    /**
     * Gets a <i>searchBy</i> flag for locator manager (see {@link SearchBy} for more details).<br><br>
     *
     * @return
     */
    SearchBy getSearchBy();

    /**
     * Sets a <i>searchBy</i> flag for locator manager (see {@link SearchBy} for more details). <br><br>
     *
     * @param searchBy -- a flag to set
     * @return -- a locator manager
     */
    ILocatorDomainTreeManager setSearchBy(final SearchBy searchBy);

    /**
     * Gets an <i>useForAutocompletion</i> flag for locator manager.<br><br>
     *
     * @return
     */
    boolean isUseForAutocompletion();

    /**
     * Sets an <i>useForAutocompletion</i> flag for locator manager. <br><br>
     *
     * @param useForAutocompletion -- a flag to set
     * @return -- a locator manager
     */
    ILocatorDomainTreeManager setUseForAutocompletion(final boolean useForAutocompletion);
}