package ua.com.fielden.platform.eql.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.eql.s1.elements.EntProp1;

public class PropResolver {


    private PropResolution resolvePropAgainstSource(final SourceInfo source, final EntProp1 entProp) {
        final ResolutionPath asIsResolution = source.getEntityInfo().resolve(entProp.getName());
        if (source.getAlias() != null && source.isAliasingAllowed() && entProp.getName().startsWith(source.getAlias() + ".")) {
            final String aliasLessPropName = entProp.getName().substring(source.getAlias().length() + 1);
            final ResolutionPath aliasLessResolution = source.getEntityInfo().resolve(aliasLessPropName);
            if (!aliasLessResolution.isEmpty() /*!= null*/) {
                if (asIsResolution.isEmpty() /* == null*/) {
                    return new PropResolution(true, source.getSource(), aliasLessResolution, entProp);
                } else {
                    throw new IllegalStateException("Ambiguity while resolving prop [" + entProp.getName() + "]. Both [" + entProp.getName() + "] and [" + aliasLessPropName
                            + "] are resolvable against given source.");
                }
            }
        }
        return !asIsResolution.isEmpty() /*!= null*/ ? new PropResolution(false, source.getSource(), asIsResolution, entProp) : null;
    }

    PropResolution resolveProp(final Collection<SourceInfo> sources, final EntProp1 entProp) {
        final List<PropResolution> result = new ArrayList<>();
        for (final SourceInfo pair : sources) {
            final PropResolution resolution = resolvePropAgainstSource(pair, entProp);
            if (resolution != null) {
                result.add(resolution);
            }
        }

        if (result.size() > 1) {
            throw new IllegalStateException("Ambiguity while resolving prop [" + entProp.getName() + "]");
        }

        return result.size() == 1 ? result.get(0) : null;
    }
}