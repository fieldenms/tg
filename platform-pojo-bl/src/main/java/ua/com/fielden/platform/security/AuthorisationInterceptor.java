package ua.com.fielden.platform.security;

import com.google.inject.Provider;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.AnnotationReflector;

import java.lang.reflect.Method;

import static java.lang.ThreadLocal.withInitial;

/// Interceptor that validates calls to methods annotated with `@Authorise` against the application authorisation model before execution.
/// If authorisation fails, a `Result` exception is thrown, which is handled specially by UI controllers and is suitable for both companion methods and entity setters.
///
/// The authorisation model is configurable and can be supplied to the interceptor at construction time.
/// In most cases it is expected to be bound and injected via an IoC configuration module.
///
public class AuthorisationInterceptor implements MethodInterceptor {

    private final ThreadLocal<IAuthorisationModel> authModel;

    public AuthorisationInterceptor(final Provider<IAuthorisationModel> authModelProvider) {
        this.authModel = withInitial(authModelProvider::get);
    }

    public IAuthorisationModel getModel() {
        return authModel.get();
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        if (getModel().isStarted()) { // this is a subsequent interception of the method requiring authentication, and thus there is no need to check access permission
            return invocation.proceed();
        } else { // this is the first intercepted method call requiring authorisation
            getModel().start();
            try {
                final Method method = invocation.getMethod();
                final Authorise annotation = AnnotationReflector.getAnnotation(method, Authorise.class);
                final Result result = getModel().authorise(annotation.value());
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
