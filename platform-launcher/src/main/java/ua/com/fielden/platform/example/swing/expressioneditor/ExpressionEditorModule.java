package ua.com.fielden.platform.example.swing.expressioneditor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.type.YesNoType;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaGenerator;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.testing.ClassProviderForTestingPurposes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.example.dynamiccriteria.BaseUserProvider;
import ua.com.fielden.platform.example.dynamiccriteria.GeneratedEntityControllerStub;
import ua.com.fielden.platform.ioc.BasicWebServerModule;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.PropertyDescriptorType;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.swing.review.EntityMasterManager;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.types.Money;

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

    private static class NoFilter implements IFilter {
	@Override
	public <ET extends AbstractEntity<?>> ConditionModel enhance(final Class<ET> entityType, final String typeAlias, final String username) {
	    return null;
	}
    }


    public ExpressionEditorModule()
	    throws Exception {
	super(hibTypeDefaults, new ExpressionExampleDomain(), ClassProviderForTestingPurposes.class, NoFilter.class, null, createProperties());
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
	bind(IGeneratedEntityController.class).to(GeneratedEntityControllerStub.class).in(Scopes.SINGLETON);
    }
}
