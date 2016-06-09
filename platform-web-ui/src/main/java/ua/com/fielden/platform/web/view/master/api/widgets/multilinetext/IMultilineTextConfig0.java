package ua.com.fielden.platform.web.view.master.api.widgets.multilinetext;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IMultilineTextConfig0<T extends AbstractEntity<?>> extends IMultilineTextConfig1<T> {
    /**
     * Indicates whether the textarea should be resizable or not.
     * IMPLEMENTATION HINT: should add value <code>resize: none;</code> to the element's CSS.
     */
    IMultilineTextConfig1<T> resizable();
}