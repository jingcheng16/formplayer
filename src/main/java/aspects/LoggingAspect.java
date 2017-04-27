package aspects;

import beans.AuthenticatedRequestBean;
import com.getsentry.raven.Raven;
import com.getsentry.raven.event.BreadcrumbBuilder;
import com.getsentry.raven.event.Breadcrumbs;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ShellProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import util.SentryUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect to log the inputs and return of each API method
 */
@Aspect
public class LoggingAspect {

    private final Log log = LogFactory.getLog(LoggingAspect.class);

    @Autowired
    protected Raven raven;


    @Around(value = "@annotation(org.springframework.web.bind.annotation.RequestMapping) " +
            "&& !@annotation(annotations.NoLogging)")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        Method m = ms.getMethod();
        String requestPath = m.getAnnotation(RequestMapping.class).value()[0]; //Should be only one
        try {
            AuthenticatedRequestBean requestBean = (AuthenticatedRequestBean) joinPoint.getArgs()[0];
            log.info("Request to " + requestPath + " with bean " + requestBean);
            Map<String, String> data = new HashMap<String, String>();
            data.put("path", requestPath);
            data.put("domain", requestBean.getDomain());
            data.put("username", requestBean.getUsername());
            data.put("restoreAs", requestBean.getRestoreAs());

            BreadcrumbBuilder builder = new BreadcrumbBuilder();
            builder.setData(data);
            SentryUtils.recordBreadcrumb(builder.build());
        } catch(ArrayIndexOutOfBoundsException e) {
            // no request body
            log.info("Request to " + requestPath + " with no request body.");
        }
        Object result = joinPoint.proceed();
        log.info("Request to " + requestPath + " returned result " + result);
        return result;
    }
}
