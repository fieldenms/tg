package ua.com.fielden.platform.domaintree.master;

import ua.com.fielden.platform.domaintree.ILocatorManager;

/**
 * This interface defines how domain tree can be managed for <b>entity masters</b>. <br>
 * <br>
 * 
 * Domain tree consists of a tree of properties. <br>
 * <br>
 * 
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br>
 * <br>
 * 
 * 1. Entity master can be configured in means of its locators. 2. TODO In future : properties checking, moving, etc. (entity master contents design)
 * 
 * @author TG Team
 */
public interface IMasterDomainTreeManager extends ILocatorManager {
}
