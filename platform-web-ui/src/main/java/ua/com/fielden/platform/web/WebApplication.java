package ua.com.fielden.platform.web;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.web.WebView;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.SingleDomElement;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.component.WebComponent;
import ua.com.fielden.platform.web.master.EntityMaster;

/**
 * The abstraction that represents the web application. It also play role of dependency manager and configures devices and
 * routes.
 *
 * @author TG Team
 *
 */
public class WebApplication{

    /*
     * Properties related to device configuration (i.e. their minimal and maximal width). The default values are in pt (1/72 inch).
     * This values will be used to configure ng-matchmedia module.
     */
    private float desktopMinWidth = 770;
    private float tabletMaxWidth = 770, tabletMinWidth = 300;
    private float phoneMaxWidth = 300;

    /*
     * The properties below represent essential tg views: entity centres, entity masters. Also it is possible to specify custom view.
     */
    private final List<EntityCentre> centres = new ArrayList<>();
    private final List<EntityMaster> masters = new ArrayList<>();
    private final List<WebView> customViews = new ArrayList<>();

    /**
     * Set the minimal screen width of the desktop device.
     *
     * @param minWidth
     * @return
     */
    public WebApplication desktop(final float minWidth) {
	this.desktopMinWidth = minWidth;
	return this;
    }

    /**
     * Set the minimal and maximal screen width of the tablet device.
     *
     * @param minWidth
     * @param maxWidth
     * @return
     */
    public WebApplication tablet(final float minWidth, final float maxWidth) {
	if (maxWidth < minWidth) {
	    throw new IllegalArgumentException("The min tablet width can not be greater then the max tablet width: " + minWidth + " > " + maxWidth);
	}
	this.tabletMaxWidth = maxWidth;
	this.tabletMinWidth = minWidth;
	return this;
    }

    /**
     * Set the maximal screen width of the phone device.
     *
     * @param maxWidth
     * @return
     */
    public WebApplication phone(final float maxWidth) {
	this.phoneMaxWidth = maxWidth;
	return this;
    }

    /**
     * Returns the minimal screen width of the desktop device.
     *
     * @return
     */
    public float desktopMinWidth() {
	return desktopMinWidth;
    }

    /**
     * Returns the maximal screen width of the tablet device.
     *
     * @return
     */
    public float tabletMaxWidth() {
	return tabletMaxWidth;
    }

    /**
     * Returns the minimal screen width of the tablet device.
     *
     * @return
     */
    public float tabletMinWidth() {
	return tabletMinWidth;
    }

    /**
     * Returns the maximal screen width of the phone device.
     *
     * @return
     */
    public float phoneMaxWidth() {
	return phoneMaxWidth;
    }

    /**
     * Renders the componentToShow component and wraps it into html page with configured dependencies routes and screens.
     *
     * @param componentToShow
     * @return
     */
    public String run(final WebComponent componentToShow) {
	//See whether screen configurations are correct: desktopMinWidth < tabletMaxWidth < tabletMinWidth < phoneMaxWidth.
	if (desktopMinWidth < tabletMaxWidth) {
	    throw new IllegalStateException("The desktop screen width can not be less then tablet screen width: " + desktopMinWidth + " < " + tabletMaxWidth);
	}
	if (tabletMinWidth < phoneMaxWidth) {
	    throw new IllegalStateException("The tablet screen width can not be less then phone screen width: " + tabletMinWidth + " < " + phoneMaxWidth);
	}
	final DomElement head = new DomElement("head").
		add(new SingleDomElement("meta").attr("charset", "UTF-8"));
	final DomElement body = componentToShow.render(new DomElement("body"));
	return new SingleDomElement("!DOCTYPE").attr("html", null).toString() + "\n"
		+ new DomElement("html").attr("ng-app", "tgAppModule").
		add(head).add(body);
    }
}
