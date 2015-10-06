package ua.com.fielden.platform.web.view.master.api.helpers;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.IMaster;

/**
 *
 * A contract that completes Master building process.
 * Its single method <code>done</code> returns an immutable instance of {@link IMaster} that is
 * used for HTML/JS rendering.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IComplete<T extends AbstractEntity<?>> {
    IMaster<T> done();
}
