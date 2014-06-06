package ua.com.fielden.uds.designer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import edu.umd.cs.piccolo.util.PObjectOutputStream;

/**
 * This class represents type information, such as class name and properties.
 * 
 * @author 01es
 * 
 */
public class MetaType implements Comparable<MetaType>, Serializable {
    private static final long serialVersionUID = 4661662654298269821L;

    private String name; // this is a class name, e.g. WorkOrder, String etc.
    private String id; // if a MetaType instance represents a property (i.e. not a high level class) then id would contain a name of this property
    private Set<MetaType> properties = new TreeSet<MetaType>(); // containts properties sorted alphabetically, which can be used for the rule composition

    private static int seq = 0;

    private MetaType parentType; // if this instance is a property then parentType is an enclosing type; otherwise it is null

    public MetaType(String name) {
        setName(name);
        setId(Integer.toString(seq++));
    }

    public MetaType(String name, String id) {
        this(name);
        setId(id);
    }

    public MetaType(MetaType type, String id) {
        this(type.getName(), id, type.getProperties());
    }

    public MetaType(String name, Set<MetaType> properties) {
        this(name);

        if (properties != null) {
            setProperties(properties);
        }
    }

    public MetaType(String name, String id, Set<MetaType> properties) {
        this(name, properties);
        setId(id);
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public Set<MetaType> getProperties() {
        return Collections.unmodifiableSet(properties);
    }

    private void setProperties(Set<MetaType> properties) {
        this.properties.clear();
        for (MetaType prop : properties) {
            prop.setParentType(this);
            this.properties.add(prop);
        }

    }

    public int compareTo(MetaType cmpTo) {
        if (cmpTo == null) {
            return +1;
        }

        int res = getName().compareTo(cmpTo.getName());
        if (res == 0) {
            if (getId() != null) {
                res = getId().compareTo(cmpTo.getId());
            } else if (cmpTo.getId() != null) {
                res = -1;
            }
        }

        return res;
    }

    public String toString() {
        return (getId() != null ? getId() + ": " : "") + getName();
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof MetaType))
            return false;

        MetaType cmpTo = (MetaType) obj;
        return compareTo(cmpTo) == 0;
    }

    public int hashCode() {
        return getName().hashCode() * 37;
    }

    public String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = id;
    }

    public MetaType getParentType() {
        return parentType;
    }

    public void setParentType(MetaType parentType) {
        this.parentType = parentType;
    }

    public MetaType getTopParentType(StringBuffer path) {
        if (getParentType() == null) { // this condition indicates that the highest hierarchical level is reached
            return this;
        } else {
            if (path != null && !"".equals(path.toString())) {
                path.append("-> ");
            }
            if (path != null) {
                path.append("(" + this + ") ");
            }
            return getParentType().getTopParentType(path);
        }
    }

    public boolean isHighLevel() {
        return getParentType() == null;
    }

    public MetaType clone() {
        try {
            byte[] ser = PObjectOutputStream.toByteArray(this);
            MetaType clone = (MetaType) new ObjectInputStream(new ByteArrayInputStream(ser)).readObject();
            clone.setId(Integer.toString(seq++));
            return clone;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void main(String[] args) {
        // hierarchy: WorkOrder
        Set<MetaType> datePeriodProps = new HashSet<MetaType>();
        MetaType fromDate = new MetaType("Date", "fromDate");
        datePeriodProps.add(fromDate);
        datePeriodProps.add(new MetaType("Date", "toDate"));
        MetaType highLevelMetaTypeDatePeriod = new MetaType("DatePeriod", datePeriodProps);

        Set<MetaType> typeProps = new HashSet<MetaType>();
        typeProps.add(new MetaType("String", "id"));
        MetaType earlyPeriod = new MetaType(highLevelMetaTypeDatePeriod, "earlyPeriod");
        typeProps.add(earlyPeriod);
        typeProps.add(new MetaType("DatePeriod", "actualPeriod"));
        MetaType highLevelMetaType = new MetaType("WorkOrder", typeProps);

        StringBuffer path = new StringBuffer();
        /*	System.out.println(fromDate.getTopParentType(path));
        	System.out.println("Path: " + path);
        	System.out.println(highLevelMetaType.getTopParentType(null));
        */
        // hierarchy: DatePeriod
        datePeriodProps = new HashSet<MetaType>();
        fromDate = new MetaType("Date", "fromDate");
        datePeriodProps.add(fromDate);
        datePeriodProps.add(new MetaType("Date", "toDate"));
        highLevelMetaTypeDatePeriod = new MetaType("DatePeriod", datePeriodProps);
        //	System.out.println(fromDate.getTopParentType(null));

    }
}
