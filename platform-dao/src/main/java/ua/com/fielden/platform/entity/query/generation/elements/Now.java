package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Collections;
import java.util.List;


public class Now implements ISingleOperand {

    @Override
    public String sql() {
	return "NOW()";
    }

    public Now() {
    }

    @Override
    public List<EntProp> getLocalProps() {
	return Collections.emptyList();
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	return Collections.emptyList();
    }

    @Override
    public List<EntValue> getAllValues() {
	return Collections.emptyList();
    }

    @Override
    public boolean ignore() {
	return false;
    }

    @Override
    public Class type() {
	return null;
    }

    @Override
    public Object hibType() {
	return null;
    }

    @Override
    public boolean isNullable() {
	return true;
    }
}