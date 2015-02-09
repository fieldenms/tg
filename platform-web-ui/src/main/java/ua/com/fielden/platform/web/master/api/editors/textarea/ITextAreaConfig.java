package ua.com.fielden.platform.web.master.api.editors.textarea;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface ITextAreaConfig<T extends AbstractEntity<?>> extends ITextAreaConfig0<T> {
    /** Indicates whether the textarea should be resizable or not.
     * IMPLEMENTATION HINT: should add value <code>resize: none;</code> to the element's CSS. */
    ITextAreaConfig0<T> resizable();
}
