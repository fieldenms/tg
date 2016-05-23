package ua.com.fielden.platform.ioc;

import com.google.inject.Binder;
import com.google.inject.Module;

import ua.com.fielden.platform.security.user.INewUserNotifier;
import ua.com.fielden.platform.security.user.NewUserNotifierByEmail;

/**
 * IoC module to bind {@link INewUserNotifier} to an email based notification implementation.
 * 
 * @author TG Team
 *
 */
public class NewUserEmailNotifierBindingModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(INewUserNotifier.class).to(NewUserNotifierByEmail.class);
    }

}
