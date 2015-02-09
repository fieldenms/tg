package ua.com.fielden.platform.web.master.api.editors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.editors.multilinetext.IMultilineTextConfig0;

public interface IMultilineTextConfig<T extends AbstractEntity<?>> extends IMultilineTextConfig0<T> {
    /** Indicates whether the textarea should be resizable or not.
     * IMPLEMENTATION HINT: should add value <code>resize: none;</code> to the element's CSS. */
    IMultilineTextConfig0<T> resizable();
}
