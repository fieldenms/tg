package ua.com.fielden.platform.keygen;

/**
 * Contract to support manual way of generating unique numbers such as WONO.
 * 
 * @author 01es
 * 
 */
public interface IKeyNumberGenerator {
    Integer nextNumber(final String key);

    Integer currNumber(final String key);
}
