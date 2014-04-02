package ua.com.fielden.platform.security;

import java.util.Arrays;

import ua.com.fielden.platform.entity.annotation.KeyTitle;

/**
 * A convenient utility class for obtaining static security tone information such as key, description, super token etc.
 * 
 * @author 01es
 * 
 */
public class SecurityTokenInfo {

    /**
     * Determines security token's key, which represents its short description.
     * 
     * @param token
     * @return
     */
    public static String shortDesc(final Class<? extends ISecurityToken> token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null.");
        } else if (!token.isAnnotationPresent(KeyTitle.class)) {
            throw new IllegalStateException("Security token " + token.getName() + " is malformed: missing KeyTitle annotation.");
        }

        return token.getAnnotation(KeyTitle.class).value();
    }

    /**
     * Determines security token's description, which represents its long description.
     * 
     * @param token
     * @return
     */
    public static String longDesc(final Class<? extends ISecurityToken> token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null.");
        } else if (!token.isAnnotationPresent(KeyTitle.class)) {
            throw new IllegalStateException("Security token " + token.getName() + " is malformed: missing KeyTitle annotation.");
        }

        return token.getAnnotation(KeyTitle.class).desc();
    }

    /**
     * Determines whether security token is a top level one.
     * 
     * @param token
     * @return
     */
    public static boolean isTopLevel(final Class<? extends ISecurityToken> token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null.");
        } else if (!token.isAnnotationPresent(KeyTitle.class)) {
            throw new IllegalStateException("Security token " + token.getName() + " is malformed: missing KeyTitle annotation.");
        }

        return Arrays.asList(token.getInterfaces()).contains(ISecurityToken.class);
    }

    /**
     * Determines super token for the provided one. The simplest way to obtain a super token is to directly invoke token.getSuperclass(). However, this method provides an added
     * value by returning ISecurityToken class if the top level token is passed into the method. This is convenient when traversing token classes to determine when the top level is
     * reached (alternatively method isTopLevel can be used).
     * 
     * @param token
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends ISecurityToken> superToken(final Class<? extends ISecurityToken> token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null.");
        } else if (!token.isAnnotationPresent(KeyTitle.class)) {
            throw new IllegalStateException("Security token " + token.getName() + " is malformed: missing KeyTitle annotation.");
        }

        return isTopLevel(token) ? ISecurityToken.class : (Class<? extends ISecurityToken>) token.getSuperclass();
    }

    /**
     * Checks whether <code>superToken</code> is a super token if <code>token</code>.
     * 
     * @param supertToken
     * @param token
     * @return
     */
    public static boolean isSuperTokenOf(final Class<? extends ISecurityToken> supertToken, final Class<? extends ISecurityToken> token) {
        return superToken(token).equals(supertToken);
    }

}
