package ua.com.fielden.platform.domaintree.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation.IAddToResultTickRepresentation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 *
 * @author TG Team
 *
 */
public class CentreManagerConfigurator {

    private final Class<?> menuItemClass;
    private final Class<?> root;

    public CentreManagerConfigurator(final Class<?> menuItemClass) {
	this.menuItemClass = menuItemClass;
	this.root = getEntityTypeForMenuItemClass(menuItemClass);
    }

    public Class<?> getMenuItemClass() {
	return menuItemClass;
    }

    public ICentreDomainTreeManagerAndEnhancer configCentre(final ICentreDomainTreeManagerAndEnhancer cdtme) {
	return cdtme;
    }

    public CentreManagerConfigurator addCriteria(final ICentreDomainTreeManagerAndEnhancer cdtme, final String prop) {
	cdtme.getFirstTick().check(root, prop, true);
	return this;
    }

    public CentreManagerConfigurator addColumn(final ICentreDomainTreeManagerAndEnhancer cdtme, final String prop) {
	final IAddToResultTickRepresentation rtr = cdtme.getRepresentation().getSecondTick();
	cdtme.getSecondTick().check(root, prop, true);
	cdtme.getSecondTick().setWidth(root, prop, rtr.getWidthByDefault(root, prop));
	return this;
    }

    public CentreManagerConfigurator addColumn(final ICentreDomainTreeManagerAndEnhancer cdtme, final String prop, final int width) {
	cdtme.getSecondTick().check(root, prop, true);
	cdtme.getSecondTick().setWidth(root, prop, width);
	return this;
    }

    public CentreManagerConfigurator addTotal(final ICentreDomainTreeManagerAndEnhancer cdtme, final String title, final String description, final String formula, final String relatedProperty) {
	cdtme.getEnhancer().addCalculatedProperty(root, "", formula, title, description, CalculatedPropertyAttribute.NO_ATTR, relatedProperty);
	cdtme.getEnhancer().apply();
	cdtme.getSecondTick().check(root, getNameForTitle(title), true);
	return this;
    }

    private String getNameForTitle(final String title) {
	return StringUtils.uncapitalize(WordUtils.capitalize(title.trim()).
		/*remove non-words, but keep digits and underscore*/replaceAll("[^\\p{L}\\d_]", "").
		/*remove the digit at the beginning of the word*/replaceFirst("\\d*", ""));
    }

    private Class<?> getEntityTypeForMenuItemClass(final Class<?> menuItemType){
	final EntityType etAnnotation = menuItemType.getAnnotation(EntityType.class);
	if (etAnnotation == null || etAnnotation.value() == null || !AbstractEntity.class.isAssignableFrom(etAnnotation.value())) {
	    throw new IllegalArgumentException("The menu item type " + menuItemType.getSimpleName() + " has no 'EntityType' annotation, which must specify the entity type type of the centre.");
	}
	return etAnnotation.value();
    }
}
