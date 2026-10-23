package ua.com.fielden.platform.domaintree;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.domaintree.impl.DomainManager;
import ua.com.fielden.platform.security.user.User;

/// Various functionality pertaining to domain enhancement.
///
@ImplementedBy(DomainManager.class)
public interface IDomainManager {

    /// Returns a domain tree enhancer associated with the specified user.
    /// The returned domain enhancer covers the whole application domain.
    ///
    IDomainTreeEnhancer domainTreeEnhancer(User user);

}
