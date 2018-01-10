package aspects;

import beans.AuthenticatedRequestBean;
import beans.SessionNavigationBean;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.javarosa.core.model.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import services.BrowserValuesProvider;

/**
 * Created by amstone326 on 1/8/18.
 */
@Aspect
@Order(3)
public class SetBrowserValuesAspect {

    @Autowired
    private BrowserValuesProvider browserValuesProvider;

    @Before(value = "@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void setValues(JoinPoint joinPoint) throws Throwable {
        Object beanArg = joinPoint.getArgs()[0];
        if (beanArg instanceof AuthenticatedRequestBean) {
            browserValuesProvider.setTimezoneOffset((AuthenticatedRequestBean)beanArg);
        }
        DateUtils.setTimezoneProvider(browserValuesProvider);
    }

}
