package ua.com.fielden.platform.serialisation.impl;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.ISecurityToken;

import com.google.inject.Inject;

/**
 * Default implementation of {@link ISerialisationClassProvider}, which relies on the application settings to provide the location of classes to be used in serialisation.
 * 
 * @author TG Team
 * 
 */
public class DefaultSerialisationClassProvider implements ISerialisationClassProvider {

    protected final List<Class<?>> types = new ArrayList<Class<?>>();

    @Inject
    public DefaultSerialisationClassProvider(final IApplicationSettings settings) throws Exception {
	types.addAll(ClassesRetriever.getAllNonAbstractClassesInPackageDerivedFrom(settings.classPath(), settings.packageName(), AbstractEntity.class));
	types.addAll(ClassesRetriever.getAllClassesInPackageDerivedFrom(settings.pathToSecurityTokens(), settings.securityTokensPackageName(), ISecurityToken.class));
	types.add(Exception.class);
	types.add(StackTraceElement[].class);
    }

    @Override
    public List<Class<?>> classes() {
	return types;
    }

}
