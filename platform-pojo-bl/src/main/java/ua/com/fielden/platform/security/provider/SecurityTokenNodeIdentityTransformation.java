package ua.com.fielden.platform.security.provider;

import java.util.SortedSet;

/**
 * This is the default implementation for {@link ISecurityTokenNodeTransformation}, which does not apply any transformations.    
 *
 * @author TG Air
 */
public class SecurityTokenNodeIdentityTransformation implements ISecurityTokenNodeTransformation {

    @Override
    public SortedSet<SecurityTokenNode> transform(final SortedSet<SecurityTokenNode> topLevelSecurityTokenNodes) {
        return topLevelSecurityTokenNodes;
    }

}
