package com.vero.coreprocessor.config;


import com.vero.coreprocessor.config.i18n.*;
import com.vero.coreprocessor.exceptions.*;
import org.slf4j.*;
import org.springframework.beans.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;

@Component
public class SpringContextConfig implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(SpringContextConfig.class);

    private static ApplicationContext context;

    public static <T> T getBean(Class<T> beanClass) {
        ensureContextIsFullyBooted();

        return context.getBean(beanClass);
    }

    public static <T> T getBean(Class<T> beanClass, String qualifier) {
        ensureContextIsFullyBooted();

        return BeanFactoryAnnotationUtils.qualifiedBeanOfType(context.getAutowireCapableBeanFactory(), beanClass, qualifier);
    }

    private static void ensureContextIsFullyBooted() {
        if (context == null) {
            logger.error("Application context is not fully booted");
            throw new OmniproApplicationException(MessageCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
