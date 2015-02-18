package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IAlso;

/**
 * This is a configuration for a widget that expects a plain HTML. The basic idea for using it, is to create large textual labels as part of any master.
 * It could be conveniently used to displaying some inplace explanation or a comment.
 *
 * It would be great if it could support some simple templating that would enable injection of property values (and potentially their meta-data such as titles and descriptions)
 * into the label text.
 *
 *
 * @author TG Team
 *
 */
public interface IHtmlText<T extends AbstractEntity<?>> extends IAlso<T> {

}
