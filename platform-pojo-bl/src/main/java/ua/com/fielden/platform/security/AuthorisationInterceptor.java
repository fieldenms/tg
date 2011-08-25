package ua.com.fielden.platform.security;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import ua.com.fielden.platform.error.Result;

import com.google.inject.Injector;

/**
 * Method interceptor ensuring that all calls to methods annotated with Authorise are validated against an application authorisation model first. If authorisation does not succeed
 * then a Result type exception is thrown. This is a convenient exception type for both controller method as well as for entity setters since it is processed in a special way by UI
 * controllers.
 * <p>
 * Authorisation model is configurable and can be passed into the interceptor during the construction phase. It is envisaged that in most cases it will be bound (and thus injected)
 * as part of a Guice configuration module.
 *
 * @author TG Team
 */
public class AuthorisationInterceptor implements MethodInterceptor {

    private ThreadLocal<IAuthorisationModel> authModel;

    public void setInjector(final Injector injector) {
	authModel = new ThreadLocal<IAuthorisationModel>() {
		public IAuthorisationModel initialValue() {
		    return injector.getInstance(IAuthorisationModel.class);
		}
	    };
    }

    public IAuthorisationModel getModel() {
	return authModel.get();
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
	if (getModel().isStarted()) { // this is a subsequent interception of the method requiring authentication, and thus there is no need to check access permission
	    try {
		return invocation.proceed();
	    } finally {
		getModel().stop();
	    }
	} else { // this is the first intercepted method call requiring authorisation
	    getModel().start();
	    final Method method = invocation.getMethod();
	    final Authorise annotation = method.getAnnotation(Authorise.class);
	    final Result result = getModel().authorise(annotation.value());
	    try {
		if (result.isSuccessful()) {
		    return invocation.proceed();
		}
		throw result;
	    } finally {
		getModel().stop();
	    }
	}
    }

}
