package ua.com.fielden.platform.processors.security.tokens;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import ua.com.fielden.platform.processors.utils.ElementFinder;
import ua.com.fielden.platform.security.ISecurityToken;

public class SecurityTokenFinder extends ElementFinder {
    private TypeMirror iSecurityToken = null;

    public SecurityTokenFinder(final Elements elements, final Types types) {
        super(elements, types);
    }

    public TypeMirror getISecurityToken() {
        if (iSecurityToken == null) {
            // TODO handle not found
            iSecurityToken = elements.getTypeElement(ISecurityToken.class.getName()).asType();
        }
        return iSecurityToken;
    }

    /**
     * Tests whether a type is an implementation of {@link ISecurityToken} interface.
     * Returns false if {@code type} represents {@link ISecurityToken} itself.
     * 
     * @param type
     * @return
     */
    public boolean isSecurityToken(final TypeMirror type) {
        return isStrictlySubtype(type, ISecurityToken.class);
    }

}
