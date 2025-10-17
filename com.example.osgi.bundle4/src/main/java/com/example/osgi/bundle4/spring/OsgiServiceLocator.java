package com.example.osgi.bundle4.spring;

import org.osgi.framework.*;

public class OsgiServiceLocator implements BundleActivator {

    private static BundleContext bundleContext;

    public static <T> T getService(Class<T> clazz) {
        if(bundleContext == null) {
            return null;
        }
        ServiceReference<T> ref = bundleContext.getServiceReference(clazz);
        if (ref != null) {
            return bundleContext.getService(ref);
        }
        return null;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        this.bundleContext = null;
    }
}
