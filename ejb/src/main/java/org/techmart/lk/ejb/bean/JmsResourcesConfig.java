package org.techmart.lk.ejb.bean;

import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup
public class JmsResourcesConfig {
    // JMS resources are now declared in glassfish-resources.xml to ensure early bind before EJB loading
}
