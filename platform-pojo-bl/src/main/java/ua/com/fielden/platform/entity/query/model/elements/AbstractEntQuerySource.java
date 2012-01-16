package ua.com.fielden.platform.entity.query.model.elements;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractEntQuerySource implements IEntQuerySource {
    private final String alias; // can be also dot.notated, but should stick to property alias naming rules (e.g. no dots in beginning/end
    private final List<EntProp> referencingProps = new ArrayList<EntProp>();

    public AbstractEntQuerySource(final String alias) {
	this.alias = alias;
    }

    public String getAlias() {
	return alias;
    }

    protected String dealiasPropName(final String dotNotatedPropName, final String propAlias) {
	return propAlias == null ? dotNotatedPropName : (!dotNotatedPropName.startsWith(propAlias + ".") ? dotNotatedPropName : dotNotatedPropName.substring(propAlias.length() + 1));
    }

    protected abstract Class determinePropertyType(final String dotNotatedPropName);

    @Override
    public boolean hasProperty(final EntProp prop) {
	final Class propType = determinePropertyType(prop.getName());
	final boolean result = propType != null;
	if (result) {
	    referencingProps.add(prop);
	    prop.setPropType(propType);

	}
	return result;
    }
}