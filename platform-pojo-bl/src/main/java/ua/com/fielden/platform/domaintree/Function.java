package ua.com.fielden.platform.domaintree;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Money;

/**
 * Represents a "function" (of single argument) concept for properties in domain tree representations. <br>
 * <br>
 * 
 * Function represents a :<br>
 * 1) simple expression applied to simple / collectional property<br>
 * 2) aggregated expression applied to simple / collectional property<br>
 * 3) attributed collectional property<br>
 * <br>
 * 
 * An expression should be defined in eQuery manner as simply query string (see {@link #equeryString(String)} method). To complete function definition the result type should be
 * defined (it can differ from type of property, see {@link #resultType(Class)} method).
 * 
 * @author TG Team
 * 
 */
public enum Function {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////// EXPRESSION FUNCTIONS ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * The function which represents the same property. Returns value of the same type as argument type.
     * 
     */
    SELF("Self", "The same property", null) {
        @Override
        public String equeryString(final String property) {
            return "([" + property + "])";
        }

        @SuppressWarnings("serial")
        @Override
        public Set<Class<?>> argumentTypes() {
            return new HashSet<Class<?>>() {
                {
                    add(AbstractEntity.class);
                    add(String.class);
                    add(Money.class);
                    add(Number.class);
                    add(boolean.class);
                    add(Date.class);
                }
            };
        }
    },
    /**
     * The function which represents a year of date property. Returns {@link Integer} values. (e.g. "1999", "2012" etc.)
     */
    YEAR("Year", "Year of date property", "YEAR") {
        @Override
        public Class<?> resultType(final Class<?> argumentType) {
            super.resultType(argumentType);
            return Integer.class;
        }

        @SuppressWarnings("serial")
        @Override
        public Set<Class<?>> argumentTypes() {
            return new HashSet<Class<?>>() {
                {
                    add(Date.class);
                }
            };
        }
    },

    /**
     * The function which represents a month of date property. Returns {@link Integer} values. (e.g. "199911", "201212" etc.)
     */
    MONTH("Month", "Month of date property", "MONTH") {
        @Override
        public String equeryString(final String property) {
            return YEAR.getEqueryName() + "([" + property + "]) * 100 + " + getEqueryName() + "([" + property + "])";
        }

        @Override
        public Class<?> resultType(final Class<?> argumentType) {
            super.resultType(argumentType);
            return Integer.class;
        }

        @SuppressWarnings("serial")
        @Override
        public Set<Class<?>> argumentTypes() {
            return new HashSet<Class<?>>() {
                {
                    add(Date.class);
                }
            };
        }
    },

    /**
     * The function which represents a day of date property. Returns {@link Integer} values. (e.g. "19991125", "20121221" etc.)
     */
    DAY("Day", "Day of date property", "DAY") {
        @Override
        public String equeryString(final String property) {
            return YEAR.getEqueryName() + "([" + property + "]) * 10000 + " + MONTH.getEqueryName() + "([" + property + "]) * 100 + " + getEqueryName() + "([" + property + "])";
        }

        @Override
        public Class<?> resultType(final Class<?> argumentType) {
            super.resultType(argumentType);
            return Integer.class;
        }

        @SuppressWarnings("serial")
        @Override
        public Set<Class<?>> argumentTypes() {
            return new HashSet<Class<?>>() {
                {
                    add(Date.class);
                }
            };
        }
    },

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////// AGGREGATED EXPRESSION FUNCTIONS /////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * The function which represents a count of distinct values of property. Returns {@link Integer} values.
     * 
     * IMPORTANT : Distinct Count function can be applicable to all Finite types of arguments (Boolean, {@link AbstractEntity} type, properties like "year of date property",
     * "count of collection" etc.).
     * 
     */
    COUNT_DISTINCT("Count", "Count of distinct values", "COUNT(DISTINCT") {
        @Override
        public String equeryString(final String property) {
            return super.equeryString(property) + ")";
        }

        @Override
        public Class<?> resultType(final Class<?> argumentType) {
            super.resultType(argumentType);
            return Integer.class;
        }

        @SuppressWarnings("serial")
        @Override
        public Set<Class<?>> argumentTypes() {
            return new HashSet<Class<?>>() {
                {
                    add(boolean.class);
                    add(AbstractEntity.class);
                    add(Integer.class); /* please pay attention to cases of Integer usage. */
                }
            };
        }
    },

    /**
     * The function which represents a summary value of property. Returns value of the same type as argument type.
     */
    SUM("Sum", "Sum of property values", "SUM") {
        @SuppressWarnings("serial")
        @Override
        public Set<Class<?>> argumentTypes() {
            return new HashSet<Class<?>>() {
                {
                    add(Number.class);
                    add(Money.class);
                }
            };
        }
    },

    /**
     * The function which represents an average value of property. Returns value of the same type as argument type.
     */
    AVG("Average", "Average of property values", "AVG") {
        @SuppressWarnings("serial")
        @Override
        public Set<Class<?>> argumentTypes() {
            return new HashSet<Class<?>>() {
                {
                    add(Number.class);
                    add(Money.class);
                }
            };
        }
    },

    /**
     * The function which represents a minimum value of property. Returns value of the same type as argument type.
     */
    MIN("Minimum", "Minimum of property values", "MIN") {
        @SuppressWarnings("serial")
        @Override
        public Set<Class<?>> argumentTypes() {
            return new HashSet<Class<?>>() {
                {
                    add(Number.class);
                    add(Money.class);
                    add(Date.class);
                    add(String.class);
                }
            };
        }
    },

    /**
     * The function which represents a maximum value of property. Returns value of the same type as argument type.
     */
    MAX("Maximum", "Maximum of property values", "MAX") {
        @SuppressWarnings("serial")
        @Override
        public Set<Class<?>> argumentTypes() {
            return new HashSet<Class<?>>() {
                {
                    add(Number.class);
                    add(Money.class);
                    add(Date.class);
                    add(String.class);
                }
            };
        }
    },

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// ATTRIBUTED COLLECTIONAL EXPRESSION FUNCTIONS /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * The function which represents all values of collectional property. Returns value of the same type as argument type.
     * 
     */
    ALL("All", "All property values", "ALL") {
        @SuppressWarnings("serial")
        @Override
        public Set<Class<?>> argumentTypes() {
            return new HashSet<Class<?>>() {
                {
                    add(AbstractEntity.class);
                    add(String.class);
                    add(Money.class);
                    add(Number.class);
                    add(boolean.class);
                    add(Date.class);
                }
            };
        }
    },

    /**
     * The function which represents some value of collectional property. Returns value of the same type as argument type.
     */
    ANY("Any", "Some (any) property value", "ANY") {
        @SuppressWarnings("serial")
        @Override
        public Set<Class<?>> argumentTypes() {
            return new HashSet<Class<?>>() {
                {
                    add(AbstractEntity.class);
                    add(String.class);
                    add(Money.class);
                    add(Number.class);
                    add(boolean.class);
                    add(Date.class);
                }
            };
        }
    };

    private final String title, desc, equeryName;

    /**
     * Constructor for creating {@link Function}.
     * 
     * @param title
     *            -- a title of this function
     * @param desc
     *            -- a description of this function
     * @param equeryName
     *            -- strict eQuery name for function
     */
    private Function(final String title, final String desc, final String equeryName) {
        this.title = title;
        this.desc = desc;
        this.equeryName = equeryName;
    }

    /**
     * Returns the title of this function.
     * 
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the description of this function.
     * 
     * @return
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Returns the strict name of this function in eQuery manner.
     * 
     * @return
     */
    public String getEqueryName() {
        return equeryName;
    }

    /**
     * Returns an expression string in eQuery manner that defines a function.
     * 
     * @param property
     *            -- a dot-notation expression that defines a property (empty property defines an entity itself).
     * @return
     */
    public String equeryString(final String property) {
        return getEqueryName() + "([" + property + "])";
    }

    /**
     * Returns a type of result for a function applied to property of <code>argumentType</code>.
     * 
     * @param argumentType
     *            -- a type of property for which a function is applied.
     * @return
     */
    public Class<?> resultType(final Class<?> argumentType) {
        FunctionUtils.check(argumentType, argumentTypes());
        return argumentType;
    }

    /**
     * Returns a set of argument types that are applicable to function.
     * 
     * @return
     */
    public Set<Class<?>> argumentTypes() {
        return null; // should be overridden
    }

    //  /**
    //  * The function which represents a count of distinct years of date property. Returns {@link Integer} values.
    //  */
    // COUNT_DISTINCT_YEAR("Distinct count (Month)", "Count of distinct months", "COUNT(DISTINCT") {
    //	@Override
    //	public String equeryString(final String property) {
    //	    return getEqueryName() + "(" + DateFunction.YEAR.equeryString(property) + "))";
    //	}
    //
    //	@Override
    //	public Class<?> resultType(final Class<?> argumentType) {
    //	    FunctionUtils.check(argumentType, argumentTypes());
    //	    return Integer.class;
    //	}
    //
    //	@Override
    //	public Set<Class<?>> argumentTypes() {
    //	    return new HashSet<Class<?>>() { { add(Date.class); } };
    //	}
    // },
    //
    // /**
    //  * The function which represents a count of distinct days of date property. Returns {@link Integer} values.
    //  */
    // COUNT_DISTINCT_MONTH("Distinct count (Month)", "Count of distinct months", "COUNT(DISTINCT") {
    //	@Override
    //	public String equeryString(final String property) {
    //	    return getEqueryName() + "(" + DateFunction.MONTH.equeryString(property) + "))";
    //	}
    //
    //	@Override
    //	public Class<?> resultType(final Class<?> argumentType) {
    //	    FunctionUtils.check(argumentType, argumentTypes());
    //	    return Integer.class;
    //	}
    //
    //	@Override
    //	public Set<Class<?>> argumentTypes() {
    //	    return new HashSet<Class<?>>() { { add(Date.class); } };
    //	}
    // },
    //
    // /**
    //  * The function which represents a count of distinct days of date property. Returns {@link Integer} values.
    //  */
    // COUNT_DISTINCT_DAY("Distinct count (Day)", "Count of distinct days", "COUNT(DISTINCT") {
    //	@Override
    //	public String equeryString(final String property) {
    //	    return getEqueryName() + "(" + DateFunction.DAY.equeryString(property) + "))";
    //	}
    //
    //	@Override
    //	public Class<?> resultType(final Class<?> argumentType) {
    //	    FunctionUtils.check(argumentType, argumentTypes());
    //	    return Integer.class;
    //	}
    //
    //	@Override
    //	public Set<Class<?>> argumentTypes() {
    //	    return new HashSet<Class<?>>() { { add(Date.class); } };
    //	}
    // };
}
