package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options;

/**
 * This class represents a convenient starting point for defining default values for selection criteria.
 *
 * @author TG Team
 *
 */
public class DefaultValueOptions {
    public static MultiDefaultValueOptions multi() {
        return new MultiDefaultValueOptions();
    }

    public static SingleDefaultValueOptions single() {
        return new SingleDefaultValueOptions();
    }

    public static RangeDefaultValueOptions range() {
        return new RangeDefaultValueOptions();
    }
}
