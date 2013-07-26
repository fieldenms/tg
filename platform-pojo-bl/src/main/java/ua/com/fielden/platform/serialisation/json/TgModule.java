package ua.com.fielden.platform.serialisation.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * {@link SimpleModule} for tg related serialisation into JSON format.
 *
 * @author TG Team
 *
 */
public class TgModule extends SimpleModule {

    private static final long serialVersionUID = -2741883920547263539L;

    public TgModule(){
	super("Tg Jackson module", new Version(1, 0, 0, null, null, null));
    }
}
