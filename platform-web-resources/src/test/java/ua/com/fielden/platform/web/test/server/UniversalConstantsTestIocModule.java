package ua.com.fielden.platform.web.test.server;

import com.google.inject.Binder;
import com.google.inject.Module;
import ua.com.fielden.platform.test.ioc.DatesForTesting;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;

/// IoC module for [IUniversalConstants] for unit testing purposes.
///
public class UniversalConstantsTestIocModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(IDates.class).to(DatesForTesting.class);
        binder.bind(IUniversalConstants.class).to(UniversalConstantsForTesting.class);
    }

}
