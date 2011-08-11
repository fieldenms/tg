package ua.com.fielden.platform.ui.config;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * A base type designed for storing an arbitrary configuration in a binary format.
 *
 * @author TG Team
 *
 */
public abstract class AbstractConfiguration<KEY extends Comparable<KEY>> extends AbstractEntity<KEY> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Configuration body", desc = "The binary representation of the configuration.")
    @MapTo("BODY")
    private byte[] configBody = new byte[]{};

    public byte[] getConfigBody() {
	return configBody;
    }

    @Observable
    public void setConfigBody(final byte[] configBody) {
	this.configBody = configBody;
    }

}
