package com.att.training.spring.boot.demo.tc;

import lombok.Getter;
import net.ttddyy.dsproxy.asserts.ProxyTestDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import javax.sql.DataSource;
import java.lang.reflect.Method;

@TestComponent
public class DatasourceProxyBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    private ApplicationContext appContext;

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) {
        if (bean instanceof DataSource && !(bean instanceof ProxyDataSource)) {
            final ProxyFactory factory = new ProxyFactory(bean);
            factory.setProxyTargetClass(true);
            var advice = new ProxyDataSourceInterceptor((DataSource) bean);
            factory.addAdvice(advice);
            registerProxy(advice.getDataSource());
            return factory.getProxy();
        }
        return bean;
    }

    private void registerProxy(ProxyTestDataSource proxyTestDataSource) {
        ConfigurableWebApplicationContext configContext = (ConfigurableWebApplicationContext) appContext;
        DefaultListableBeanFactory beanRegistry = (DefaultListableBeanFactory) configContext.getBeanFactory();
        beanRegistry.registerSingleton("proxyTestDataSource", proxyTestDataSource);
    }
    public static class ProxyDataSourceInterceptor implements MethodInterceptor {
        @Getter
        private final ProxyTestDataSource dataSource;

        public ProxyDataSourceInterceptor(final DataSource dataSource) {
            this.dataSource = new ProxyTestDataSource(ProxyDataSourceBuilder
                    .create(dataSource)
                    .name("datasource-proxy")
                    .logQueryBySlf4j()
                    .countQuery()
                    .build());
        }

        @Override
        public Object invoke(final MethodInvocation invocation) throws Throwable {
            final Method proxyMethod = ReflectionUtils.findMethod(this.dataSource.getClass(),
                    invocation.getMethod().getName());
            if (proxyMethod != null) {
                return proxyMethod.invoke(this.dataSource, invocation.getArguments());
            }
            return invocation.proceed();
        }
    }

}
