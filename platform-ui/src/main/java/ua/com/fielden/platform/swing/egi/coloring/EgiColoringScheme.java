package ua.com.fielden.platform.swing.egi.coloring;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.utils.DummyBuilder;

import com.jidesoft.grid.CellStyleProvider;

/**
 * EGI colouring scheme class which uses instances of {@link IColouringScheme} interface, associated with property names to determine colour of a entity's property.<br>
 * <br>
 * <b>Note:</b> if colouring does not work, please make sure {@link EntityGridInspector} doesn't have {@link CellStyleProvider} set using
 * {@link EntityGridInspector#setCellStyleProvider(CellStyleProvider)} method. Gray-white cell style is automatically set, so one should not use {@link DummyBuilder#csp()} provider
 * again.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class EgiColoringScheme<T extends AbstractEntity> implements IEgiColoringScheme<T> {

    private IColouringScheme<T> bgRowColouringScheme;
    private IColouringScheme<T> fgRowColouringScheme;

    private final Map<String, IColouringScheme<T>> bgPropertyColouringSchemes = new HashMap<String, IColouringScheme<T>>();
    private final Map<String, IColouringScheme<T>> fgPropertyColouringSchemes = new HashMap<String, IColouringScheme<T>>();

    public EgiColoringScheme(final IColouringScheme<T> bgRowColouringScheme, final Map<String, IColouringScheme<T>> propertyColoringScheme) {
	this.bgRowColouringScheme = bgRowColouringScheme;
	this.bgPropertyColouringSchemes.putAll(propertyColoringScheme);
    }

    public EgiColoringScheme<T> setBgRowColouringScheme(final IColouringScheme<T> bgRowColoringScheme) {
	this.bgRowColouringScheme = bgRowColoringScheme;
	return this;
    }

    public EgiColoringScheme<T> setFgRowColouringScheme(final IColouringScheme<T> fgRowColoringScheme) {
	this.fgRowColouringScheme = fgRowColoringScheme;
	return this;
    }

    public EgiColoringScheme<T> setBgColouringScheme(final String propertyName, final IColouringScheme<T> propertyColoringScheme) {
	bgPropertyColouringSchemes.put(propertyName, propertyColoringScheme);
	return this;
    }

    public EgiColoringScheme<T> setFgColouringScheme(final String propertyName, final IColouringScheme<T> propertyColoringScheme) {
	fgPropertyColouringSchemes.put(propertyName, propertyColoringScheme);
	return this;
    }

    public Color getBgColour(final T entity, final String propertyName) {
	final IColouringScheme<T> propColouringScheme = bgPropertyColouringSchemes.get(propertyName) != null ? //
	bgPropertyColouringSchemes.get(propertyName): bgRowColouringScheme;
	if (propColouringScheme == null) {
	    return null;
	} else {
	    final Color propertyColor = propColouringScheme.getColor(entity);
	    if (propertyColor != null) {
		return propertyColor;
	    } else {
		return bgRowColouringScheme != null ? bgRowColouringScheme.getColor(entity) : null;
	    }
	}
    }

    public Color getFgColour(final T entity, final String propertyName) {
	final IColouringScheme<T> propColouringScheme = fgPropertyColouringSchemes.get(propertyName) != null ? //
		fgPropertyColouringSchemes.get(propertyName): fgRowColouringScheme;
	if (propColouringScheme == null) {
	    return null;
	} else {
	    final Color propertyColour = propColouringScheme.getColor(entity);
	    if (propertyColour != null) {
		return propertyColour;
	    } else {
		return fgRowColouringScheme != null ? fgRowColouringScheme.getColor(entity) : null;
	    }
	}
    }

}
