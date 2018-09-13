package com.bj58.qf.dubbolight.core.listener;

import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;


/**
 * Created by Administrator on 2017/12/6.
 */
public class DubboLightContextLoaderListener extends ContextLoaderListener{
    public DubboLightContextLoaderListener() {
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        System.setProperty("dubbo.application.logger", "slf4j");
        super.contextInitialized(event);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        super.contextDestroyed(event);
    }
}
