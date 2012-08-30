package ua.com.fielden.platform.criteria.generator.impl;

import java.io.InputStream;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiser;

public class StubSerialiser implements ISerialiser {

    @Override
    public byte[] serialise(Object obj) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <T> T deserialise(byte[] content, Class<T> type) throws Exception {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <T> T deserialise(InputStream content, Class<T> type) throws Exception {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public EntityFactory factory() {
	// TODO Auto-generated method stub
	return null;
    }

}
