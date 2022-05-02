package ua.com.fielden.platform.processors.meta_model;

/**
 * A contract for constructs that can be converted to a dot-noted paths of properties.
 * 
 * @author TG Team
 *
 */
public interface IConvertableToPath {

    String toPath();

}