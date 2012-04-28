package ua.com.fielden.platform.example.swing.expressioneditor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.type.YesNoType;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.testing.EntityWithoutKeyType;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.example.dynamiccriteria.BaseUserProvider;
import ua.com.fielden.platform.ioc.BasicWebServerModule;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.PropertyDescriptorType;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.impl.DefaultSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.swing.review.EntityMasterManager;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.types.Money;

import com.google.inject.Inject;
import com.google.inject.Scopes;

/**
 * An example module.
 * 
 * @author TG Team
 *
 */
public class ExpressionEditorModule extends BasicWebServerModule{

    private static final Map<Class, Class> hibTypeDefaults = new HashMap<Class, Class>();
    static {
	hibTypeDefaults.put(boolean.class, YesNoType.class);
	hibTypeDefaults.put(Boolean.class, YesNoType.class);
	hibTypeDefaults.put(Date.class, DateTimeType.class);
	hibTypeDefaults.put(PropertyDescriptor.class, PropertyDescriptorType.class);
	hibTypeDefaults.put(Money.class, SimpleMoneyType.class);
    }

    private static class ExpressionEditorExampleClassProvider extends DefaultSerialisationClassProvider{

	@Inject
	public ExpressionEditorExampleClassProvider(final IApplicationSettings settings) throws Exception {
	    super(settings);
	    types.addAll(TgKryo.typesForRegistration("../platform-pojo-bl/target/test-classes", "ua.com.fielden.platform.domaintree", AbstractDomainTree.DOMAIN_TREE_TYPES));
	    types.addAll(TgKryo.typesForRegistration("../platform-pojo-bl/target/classes", "ua.com.fielden.platform.domaintree.testing", AbstractDomainTree.DOMAIN_TREE_TYPES));
	    types.remove(EntityWithoutKeyType.class);
	}

    }

    private static class NoFilter implements IFilter {

	@Override
	public <T extends AbstractEntity<?>> EntityResultQueryModel<T> enhance(final Class<T> entityType, final String username) {
	    return null;
	}
    }


    public ExpressionEditorModule()
	    throws Exception {
	super(hibTypeDefaults, new ArrayList<Class<? extends AbstractEntity<?>>>(), ExpressionEditorExampleClassProvider.class, NoFilter.class, null, createProperties());
    }

    public static Properties createProperties(){
	FileInputStream in;
	try {
	    in = new FileInputStream("src/main/resources/expression_editor_example.properties");
	    final Properties props = new Properties();
	    props.load(in);
	    in.close();
	    return props;
	} catch (final FileNotFoundException e) {
	    e.printStackTrace();
	} catch (final IOException e) {
	    e.printStackTrace();
	}
	return null;
    }

    @Override
    protected void configure() {
	super.configure();
	bind(IUserProvider.class).to(BaseUserProvider.class).in(Scopes.SINGLETON);
	bind(ICriteriaGenerator.class).to(CriteriaGenerator.class).in(Scopes.SINGLETON);
	bind(IGlobalDomainTreeManager.class).to(GlobalDomainTreeManager.class).in(Scopes.SINGLETON);
	bind(IEntityMasterManager.class).to(EntityMasterManager.class).in(Scopes.SINGLETON);
    }
}
