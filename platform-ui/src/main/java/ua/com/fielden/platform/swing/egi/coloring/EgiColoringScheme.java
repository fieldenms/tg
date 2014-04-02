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

    @Override
    public Color getBgColour(final T entity, final String propertyName) {
        final IColouringScheme<T> propColouringScheme = bgPropertyColouringSchemes.get(propertyName) != null ? //
        bgPropertyColouringSchemes.get(propertyName)
                : bgRowColouringScheme;
        if (propColouringScheme == null) {
            return null;
        } else {
            T ent;
            try {
                ent = ensureCorrectEntityType(entity);
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
                ent = entity;
            }
            final Color propertyColor = propColouringScheme.getColor(ent);
            if (propertyColor != null) {
                return propertyColor;
            } else {
                return bgRowColouringScheme != null ? bgRowColouringScheme.getColor(ent) : null;
            }
        }
    }

    public Color getFgColour(final T entity, final String propertyName) {
        final IColouringScheme<T> propColouringScheme = fgPropertyColouringSchemes.get(propertyName) != null ? //
        fgPropertyColouringSchemes.get(propertyName)
                : fgRowColouringScheme;
        if (propColouringScheme == null) {
            return null;
        } else {
            T ent;
            try {
                ent = ensureCorrectEntityType(entity);
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
                ent = entity;
            }
            final Color propertyColour = propColouringScheme.getColor(ent);
            if (propertyColour != null) {
                return propertyColour;
            } else {
                return fgRowColouringScheme != null ? fgRowColouringScheme.getColor(ent) : null;
            }
        }
    }

    private T ensureCorrectEntityType(final T entity) throws ClassNotFoundException {
        // TODO this conversion from enhanced entity to original is placed here to support execution of type-parameterised colouring schemes
        // that would otherwise throw a runtime exception.
        // However, this type compatibility problem exists only due to incompatibility of the original and enhanced types.
        // Such situations may occur elsewhere, and thus needs to be resolved at the type level during generation of new types.
        // This place is just a quick and dirty patch.

        //	if (!DynamicEntityClassLoader.isEnhanced(entity.getClass())) {
        //	    return entity;
        //	} else {
        //	    final String originalType = DynamicEntityClassLoader.getOriginalType(entity.getClass()).getName();
        //	    final String genType = entity.getType().getName();
        //	    final String suggestedName = genType + "_adapted";
        //
        //	    Class<? extends AbstractEntity> newType;
        //	    try {
        //		newType = (Class<? extends AbstractEntity>) ClassLoader.getSystemClassLoader().loadClass(suggestedName);
        //	    } catch (final ClassNotFoundException e) {
        //		final DynamicEntityClassLoader cl = (DynamicEntityClassLoader) entity.getClass().getClassLoader();
        //		newType = (Class<? extends AbstractEntity>)//
        //			    cl.startModification(genType).//
        //			    modifyTypeName(suggestedName).//
        //			    modifySupertypeName(originalType).//
        //			    endModification();
        //	    }
        //
        //	    return (T) entity.copy(newType);
        //	}

        //	return !DynamicEntityClassLoader.isEnhanced(entity.getClass()) ? (T) entity :
        //		(T) entity.copy(DynamicEntityClassLoader.getOriginalType(entity.getClass()));
        return entity;
    }

}
