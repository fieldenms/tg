package ua.com.fielden.uds.designer.zui.component.generic.value;

/**
 * This is a string value with one special feature -- its clone method returns a reference to itself. At the same time it is possible to instantiate different instances, so it is
 * not a singleton.
 * 
 * @author 01es
 * 
 */
public class SingularityStringValue extends StringValue implements Cloneable {
    private static final long serialVersionUID = 4924091634276713297L;

    public SingularityStringValue() {
    }

    public SingularityStringValue(String value) {
	super(value);
    }

    public Object clone() {
	return this;
    }

    public static void main(String[] args) throws CloneNotSupportedException {
	SingularityStringValue value = new SingularityStringValue("xxx");
	SingularityStringValue clone = (SingularityStringValue) value.clone();
	clone.setValue("xx");

	value = new SingularityStringValue("xxx");
	clone = (SingularityStringValue) value.clone();
	clone.setValue("xx");
    }
}
