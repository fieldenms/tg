package ua.com.fielden.platform.domaintree.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation.IAddToResultTickRepresentation;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 *
 * @author TG Team
 *
 */
public class CentreManagerConfigurator {
    private final Class<?> root;

    public CentreManagerConfigurator(final Class<? extends AbstractEntity<?>> root) {
	this.root = root;
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
	cdtme.getSecondTick().check(root, CalculatedProperty.generateNameFrom(title), true);
	return this;
    }

    /** Root type, which domain tree is configured by this configurator. */
    protected Class<?> root() {
	return root;
    }

    /** Convenient entity strings conversion method. */
    protected static List<String> entityVal(final String ... strs) {
	return Arrays.asList(strs);
    }

    /** Convenient date conversion method in format 'yyyy-MM-dd HH:mm:ss'. */
    protected static Date dateVal(final String str) {
	final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	final DateTime dt = formatter.parseDateTime(str);
	return dt.toDate();
    }
}
