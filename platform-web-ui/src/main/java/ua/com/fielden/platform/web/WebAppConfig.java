package ua.com.fielden.platform.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.web.WebView;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.master.EntityMaster;

/**
 * The abstraction that represents the web application. It also play role of dependency manager and configures devices and
 * routes.
 *
 * @author TG Team
 *
 */
public class WebAppConfig{

    private final String appName;

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
    private final Map<String, EntityCentre> centres = new LinkedHashMap<>();
    private final List<EntityMaster> masters = new ArrayList<>();
    private final List<WebView> customViews = new ArrayList<>();

    /**
     * Creates web application instance and initialises it with application name.
     *
     * @param appName
     */
    public WebAppConfig(final String appName) {
	this.appName = appName;
    }

    /**
     * Set the minimal screen width of the desktop device.
     *
     * @param minWidth
     * @return
     */
    public WebAppConfig desktop(final float minWidth) {
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
    public WebAppConfig tablet(final float minWidth, final float maxWidth) {
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
    public WebAppConfig phone(final float maxWidth) {
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

    public void addCentre(final EntityCentre centre) {
	centres.put(centre.getMenuItemType().getName(), centre);
    }

    /**
     * Returns registered entity centres.
     *
     * @return
     */
    public Map<String, EntityCentre> getCentres() {
	return Collections.unmodifiableMap(centres);
    }

    /**
     * Runs the web application by sending the html file for this application.
     *
     * @return
     */
    public String run() {
	// TODO should check whether user is authentic first.
	return ResourceLoader.getText("ua/com/fielden/platform/web/app.html").
		replace("@appName", appName).
		replace("@menuItems", generateMenu());
    }

    /**
     * Generates the menu for the application. This generation of menu items is based on the information about the entity centres.
     *
     * @return
     */
    private String generateMenu() {
	// TODO generate menu on entity masters and custom views (not only entity centre).
	// TODO consider the ability to create menu item template for menu generator.
	final List<String> menuBuilder = new ArrayList<>();
	centres.forEach((key, value) -> {
	   menuBuilder.add("<li ng-class=\"navClass('centre/"+
		   		key + "')\"><a href=\"#/centre/"+
		   key + "\">" + value.getName() + "</a></li>");
	});
	return StringUtils.join(menuBuilder, "\n");
    }

//    /**
//     * Renders the componentToShow component and wraps it into html page with configured dependencies routes and screens.
//     *
//     * @param componentToShow
//     * @return
//     */
//    public String run(final WebComponent componentToShow) {
//	//See whether screen configurations are correct: desktopMinWidth < tabletMaxWidth < tabletMinWidth < phoneMaxWidth.
//	if (desktopMinWidth < tabletMaxWidth) {
//	    throw new IllegalStateException("The desktop screen width can not be less then tablet screen width: " + desktopMinWidth + " < " + tabletMaxWidth);
//	}
//	if (tabletMinWidth < phoneMaxWidth) {
//	    throw new IllegalStateException("The tablet screen width can not be less then phone screen width: " + tabletMinWidth + " < " + phoneMaxWidth);
//	}
//	final DomElement head = new DomElement("head").
//		add(new SingleDomElement("meta").attr("charset", "UTF-8"));
//	final DomElement body = componentToShow.render(new DomElement("body"));
//	return new SingleDomElement("!DOCTYPE").attr("html", null).toString() + "\n"
//		+ new DomElement("html").attr("ng-app", "tgAppModule").
//		add(head).add(body);
//    }
}
