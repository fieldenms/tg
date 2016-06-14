package ua.com.fielden.platform.ioc;

import com.google.inject.Binder;
import com.google.inject.Module;

import ua.com.fielden.platform.security.user.INewUserNotifier;
import ua.com.fielden.platform.security.user.User;

/**
 * IoC module to mock {@link INewUserNotifier} for unit testing purposes.
 * 
 * @author TG Team
 *
 */
public class NewUserNotifierMockBindingModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(INewUserNotifier.class).toInstance(new INewUserNotifier() {
            @Override
            public void notify(final User user) {
            }
            
        });
    }

}
