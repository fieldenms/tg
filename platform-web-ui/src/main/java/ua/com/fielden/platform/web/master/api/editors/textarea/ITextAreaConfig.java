package ua.com.fielden.platform.web.master.api.editors.textarea;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface ITextAreaConfig<T extends AbstractEntity<?>> extends ITextAreaConfig0<T> {
    /** Indicates whether the hard word wrapping should be enforced.
     * Hard wrapping requires the number of cols to be specified. */
    ITextAreaConfig0<T> hardWrap(final int cols);
}
