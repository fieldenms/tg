package ua.com.fielden.platform.web.view.master.api.helpers;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.ISimpleMasterConfig;

/**
 *
 * A contract that completes Simple Master building process.
 * Its single method <code>done</code> returns an immutable instance of {@link ISimpleMasterConfig} that
 * can be used for rendering and accessing constituents of a the returned master configuration.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IComplete<T extends AbstractEntity<?>> {
    IMaster<T> done();
}
