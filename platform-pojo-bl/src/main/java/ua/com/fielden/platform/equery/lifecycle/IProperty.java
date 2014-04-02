package ua.com.fielden.platform.equery.lifecycle;

import org.joda.time.ReadablePeriod;

/**
 * Property interface for lifecycle distribution.
 * 
 * @author TG Team
 * 
 */
public interface IProperty {

    /**
     * Property title.
     * 
     * @return
     */
    String getTitle();

    /**
     * Property description.
     * 
     * @return
     */
    String getDesc();

    /**
     * Property for property-value-distribution.
     * 
     * @author TG Team
     * 
     */
    public interface IValueProperty extends IProperty {
        /**
         * A name of value-property by which a data should be distributed.
         * 
         * @return
         */
        String getName();
    }

    /**
     * Simple {@link IValueProperty} implementation.
     * 
     * @author TG Team
     * 
     */
    public class ValueProperty implements IValueProperty {
        private final String name, title, desc;

        public ValueProperty(final String name, final String title, final String desc) {
            this.name = name;
            this.title = title;
            this.desc = desc;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getDesc() {
            return desc;
        }

        @Override
        public String toString() {
            return getTitle();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final ValueProperty other = (ValueProperty) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }
    }

    /**
     * Property for time-distribution.
     * 
     * @author TG Team
     * 
     */
    public interface ITimeProperty extends IProperty {

        /**
         * Period by which a data should be distributed.
         */
        ReadablePeriod getPeriod();
    }

}
