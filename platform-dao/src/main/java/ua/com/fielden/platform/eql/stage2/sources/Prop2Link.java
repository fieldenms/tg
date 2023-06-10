package ua.com.fielden.platform.eql.stage2.sources;

import java.util.Objects;

/**
 * Lightweight representation of the respective Prop2 instance -- contains all ingredients of Prop2 identity.
 * 
 * Used within the process of building associations between Prop2 and the corresponding Prop3 item.
 * 
 * @author TG Team
 *
 */
public class Prop2Link {
	public final String name; // name from the respective Prop2 instance
	public final Integer sourceId; // source.id() from the respective Prop2 instance
	
	public Prop2Link (final String name, final Integer sourceId) {
		this.name = name;
		this.sourceId = sourceId;
	}
	
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + sourceId.hashCode();
        result = prime * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Prop2Link)) {
            return false;
        }

        final Prop2Link other = (Prop2Link) obj;

        return Objects.equals(name, other.name) && Objects.equals(sourceId, other.sourceId);
    }
}