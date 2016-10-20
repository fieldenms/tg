package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

/**
 * Represent's menu item view.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@CompanionObject(IView.class)
public class View extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title("Import")
    private String htmlImport;

    @IsProperty
    @Title("Element Name")
    private String elementName;

    @IsProperty
    @Title("View Type")
    private String viewType;

    @IsProperty
    @Title("Attributes")
    private AbstractView attrs;

    @Observable
    @EntityExists(AbstractView.class)
    public View setAttrs(final AbstractView attrs) {
        this.attrs = attrs;
        return this;
    }

    public AbstractView getAttrs() {
        return attrs;
    }

    @Observable
    public View setViewType(final String viewType) {
        this.viewType = viewType;
        return this;
    }

    public String getViewType() {
        return viewType;
    }

    @Observable
    public View setElementName(final String elementName) {
        this.elementName = elementName;
        return this;
    }

    public String getElementName() {
        return elementName;
    }

    @Observable
    public View setHtmlImport(final String htmlImport) {
        this.htmlImport = htmlImport;
        return this;
    }

    public String getHtmlImport() {
        return htmlImport;
    }
}