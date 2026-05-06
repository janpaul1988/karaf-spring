package com.example.osgi.bundle4;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(HelloController.class)
public class SpringBootBundleActivator implements BundleActivator {

    private volatile ConfigurableApplicationContext appContext;
    private Thread startupThread;

    @Override
    public void start(BundleContext bundleContext) {
        // Capture the bundle classloader before the thread is created so Spring Boot's
        // classpath scanning and SpringFactoriesLoader use it instead of the OSGi
        // system classloader, where our embedded Spring JARs are not visible.
        ClassLoader bundleClassLoader = this.getClass().getClassLoader();

        // Spring Boot 3.x startup can exceed Karaf's bundle activation timeout, so we
        // start it on a daemon thread and let activation return immediately.
        startupThread = new Thread(() -> {
            Thread.currentThread().setContextClassLoader(bundleClassLoader);
            appContext = SpringApplication.run(SpringBootBundleActivator.class);
        }, "spring-boot-startup");
        startupThread.setDaemon(true);
        startupThread.start();
    }

    @Override
    public void stop(BundleContext bundleContext) {
        if (appContext != null) {
            SpringApplication.exit(appContext, () -> 0);
        }
    }
}
