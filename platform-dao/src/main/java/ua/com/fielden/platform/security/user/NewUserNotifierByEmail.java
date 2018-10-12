package ua.com.fielden.platform.security.user;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isEmpty;

import com.google.inject.Inject;

import ua.com.fielden.platform.mail.SmtpEmailSender;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.annotations.AppUri;

/**
 * 
 * A notifier that notifies newly created users via an email.
 * 
 * @author TG Team
 *
 */
public class NewUserNotifierByEmail implements INewUserNotifier {

    private final IUniversalConstants constants;
    private final String appUri;
    
    @Inject
    public NewUserNotifierByEmail(
            final IUniversalConstants constants,
            final @AppUri String applicationUri) {
        this.constants = constants;
        this.appUri = applicationUri;
    }
    
    @Override
    public void notify(final UserSecret secret) {
        // let's perform some basic assertions
        if (secret == null || secret.getKey() == null) {
            throw new SecurityException("No user was provided to notify of their registration as a new application user.");
        }
        
        final User user = secret.getKey();
        if (!user.isPersisted()) {
            throw new SecurityException(format("New user [%s] cannot be notified if their information if not persisted.", user.getKey()));
        } else if (!isEmpty(secret.getPassword())) {
            throw new SecurityException(format("New user [%s] cannot be notified if their password has a value.", user.getKey()));
        } else if (isEmpty(secret.getResetUuid())) {
            throw new SecurityException(format("New user [%s] cannot be notified if their reset UUID has not been assigned.", user.getKey()));
        } else if (isEmpty(user.getEmail())) {
            throw new SecurityException(format("New user [%s] cannot be notified if their email address has not been assigned.", user.getKey()));
        } 
        
        final String emailBody = makePasswordRestEmail(constants.appName(), appUri, secret.getResetUuid());
        final SmtpEmailSender sender = new SmtpEmailSender(constants.smtpServer());
        sender.sendPlainMessage(constants.fromEmailAddress(), 
                                user.getEmail(), 
                                format("[%s] You have been registered as a new user", constants.appName()), 
                                emailBody);

    }
    
    private String makePasswordRestEmail(final String appName, final String appUri, final String resetUuid) {
        final StringBuilder builder = new StringBuilder();
        builder.append(format("You have been registered as a new user in %s.%n%n", appName));
        builder.append("In order to log into the system you need to use the following link within the next day to setup your password:\n\n");
        builder.append(format("%sreset_password/%s%n%n", appUri, resetUuid));
        builder.append(format("If you don’t use this link within 24 hours, it will expire. To get a new password setup link, visit %sforgotten%n%n", appUri));
        builder.append("Thanks,\n");
        builder.append("Your support team");

        return builder.toString();
    }

}
