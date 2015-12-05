package veeva.ecm.testbootcamp.util;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

/**
 * This class wraps the Commons Configuration API. It uses the XML configuration facilities to
 * enable an inheritance type scheme which allows for overriding of specific config values.<p/>
 *
 * It will look for two different files: <br>
 *     - EnvironmentDefault.xml (required)<br>
 *     - Environment-(value from ENVIRONMENT.ID system var).xml <br>
 *         (optional for overriding values in EnvironmentDefault.xml). Ex. Environment-production.xml<br>
 * <p/>
 * By default, the system will look in the classpath for the existence of these XML files. This behavior can be overriden by supplying the CONFIG.BASEPATH system variable on
 * startup, and specifying a folder on the file system. This will tell Commons Configuration to look in the specified directory for those files. If the files are not found here,
 * it will look in the classpath.
 */
public class VeevaConfiguration {
    private static final Log LOGGER = LogFactory.getLog(VeevaConfiguration.class.getName());

    private static CompositeConfiguration config = null;

    public static final String COMMON_PORT_NAMESPACE = "common";
    public static final String SOLR_PORT_NAMESPACE = "solr";

    public static VeevaConfiguration getDefaultConfiguration() {
        return new VeevaConfiguration();
    }

    public static VeevaConfiguration getConfiguration(final String portNamespace) {
        return new VeevaConfiguration(portNamespace, false);
    }

    /**
     * Default constructor to build the VeevaConfiguration object from the config files. We default to the "common"
     * port namespace and we use a single, global configuration that is read only once - as such this is the more
     * efficient way of getting a configuration.
     */
    public VeevaConfiguration() {
        this(COMMON_PORT_NAMESPACE, true);
    }

    /**
     * Builds a VeevaConfiguration from the configuration files. This will re-read the configuration file
     * so this should not be done in a loop or repeatedly.
     *
     * @param portNamespace If running under Tomcat we will determine the port on which it is running and will
     *                      add "portNamespace.webserver.http" and "portNamespace.webserver.https" to the
     *                      underlying configuration.
     */
    public VeevaConfiguration(String portNamespace, boolean useGlobal) {

        if (config == null || !useGlobal) {
            String basePath = System.getProperty("CONFIG.BASEPATH");
            CompositeConfiguration localConfig = new CompositeConfiguration();

            String nodeId = System.getProperty("NODE.ID");
            if ((nodeId != null) && (!"".equals(nodeId))) {

                if (doesXmlConfigExist("Environment-" + nodeId + ".xml", basePath)) {
                    try {
                        XMLConfiguration xmlConfig = buildXmlConfig("Environment-" + nodeId + ".xml", basePath);
                        xmlConfig.load();
                        localConfig.addConfiguration(xmlConfig);
                    } catch (ConfigurationException e) {
                        throw new IllegalArgumentException("Not able to load node specific configuration. NODE.ID=" + nodeId, e);
                    }
                } else {
                    throw new IllegalArgumentException("Not able to load node specific configuration. NODE.ID=" + nodeId + ". The file does not exist");
                }
            }

            // Pod specific configuration.
            String podId = System.getProperty("POD.ID");
            if (StringUtils.isNotBlank(podId)) {
                if (!"pod".equals(podId)) {
                    if (doesXmlConfigExist("Environment-" + podId + ".xml", basePath)) {
                        try {
                            XMLConfiguration xmlConfig = buildXmlConfig("Environment-" + podId + ".xml", basePath);
                            xmlConfig.load();
                            localConfig.addConfiguration(xmlConfig);
                        } catch (ConfigurationException e) {
                            throw new IllegalArgumentException("Not able to load POD specific configuration. POD.ID=" + podId, e);
                        }
                    } else {
                        throw new IllegalArgumentException("Not able to load POD specific configuration. POD.ID=" + podId + ". The file does not exist.");
                    }
                }

                // Generic pod configuration
                if (doesXmlConfigExist("Environment-pod.xml", basePath)) {
                    try {
                        XMLConfiguration xmlConfig = buildXmlConfig("Environment-pod.xml", basePath);
                        xmlConfig.load();
                        localConfig.addConfiguration(xmlConfig);
                    } catch (ConfigurationException e) {
                        throw new IllegalArgumentException("Not able to load POD generic configuration.", e);
                    }
                } else {
                    throw new IllegalArgumentException("Not able to load POD generic configuration. The file does not exist.");
                }
            }

            String envId = System.getProperty("ENVIRONMENT.ID");
            if ((envId != null) && (!"".equals(envId))) {
                if (doesXmlConfigExist("Environment-" + envId + ".xml", basePath)) {
                    try {
                        XMLConfiguration xmlConfig = buildXmlConfig("Environment-" + envId + ".xml", basePath);
                        xmlConfig.load();
                        localConfig.addConfiguration(xmlConfig);
                    } catch (ConfigurationException e) {
                        throw new IllegalArgumentException("Not able to load environment specific configuration. ENVIRONMENT.ID=" + envId, e);
                    }
                } else {
                    LOGGER.warn("Not able to load environment specific configuration. ENVIRONMENT.ID=" + envId + ". The file does not exist.");
                }
            }

            // Inject the Tomcat ports
            long httpPort = 0;
            long httpsPort = 0;

            try {
                Object server = null;
                List<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
                // DEV-71798 Do not use VeevaListUtils.isNotNullOrEmptyCollection() - NoClassDefFoundError on Google's Predicate class
                if (mBeanServers != null && mBeanServers.size() > 0) {
                    MBeanServer mBeanServer = mBeanServers.get(0);
                    ObjectName name = new ObjectName("Catalina", "type", "Server");
                    server = mBeanServer.getAttribute(name, "managedResource");
                }

                if (server == null) {
                    throw new Exception("MBeanServer did not return a server configuration");
                } else {
                    Method m = server.getClass().getMethod("findServices");
                    Object[] services = (Object[]) m.invoke(server);

                    Object service = services[0];
                    m = service.getClass().getMethod("findConnectors");
                    Object[] connectors = (Object[]) m.invoke(service);
                    for (Object connector : connectors) {

                        Object protocol = PropertyUtils.getProperty(connector, "protocol");
                        Object port = PropertyUtils.getProperty(connector, "port");
                        m = connector.getClass().getMethod("getProperty", new Class[] {String.class});
                        Object sslEnabled = m.invoke(connector, "SSLEnabled");
                        boolean isHttps = (sslEnabled != null && sslEnabled instanceof Boolean && ((Boolean) sslEnabled).booleanValue());

                        if (port instanceof Number && protocol instanceof String &&
                                ((String) protocol).toLowerCase().startsWith("http")) {
                            if (isHttps) {
                                httpsPort = ((Number) port).longValue();
                            } else {
                                httpPort = ((Number) port).longValue();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Unable to get port numbers from Tomcat configuration.", e);
            }

            Configuration infrastructureConfiguration = new HierarchicalConfiguration();
            if (httpPort != 0) {
                infrastructureConfiguration.setProperty(portNamespace + ".webserver.httpPort", Long.toString(httpPort));
            }
            if (httpsPort != 0) {
                infrastructureConfiguration.setProperty(portNamespace + ".webserver.httpsPort", Long.toString(httpsPort));
            }
            localConfig.addConfiguration(infrastructureConfiguration);

            try {
                XMLConfiguration xmlConfig = buildXmlConfig("EnvironmentHelpLinks.xml", basePath);
                xmlConfig.load();
                localConfig.addConfiguration(xmlConfig);
            } catch (ConfigurationException e) {
                LOGGER.error("VeevaConfiguration() exception with defaultXml: EnvironmentHelpLinks.xml, basePath: " + basePath, e);
                throw new IllegalArgumentException("Unable to load default configuration. Make sure you have a file named EnvironmentHelpLinks.xml somewhere in the classpath.");
            }

            try {
                XMLConfiguration defaultXmlConfig = buildXmlConfig("EnvironmentDefault.xml", basePath);
                defaultXmlConfig.load();
                localConfig.addConfiguration(defaultXmlConfig);
            } catch (ConfigurationException e) {
                LOGGER.error("VeevaConfiguration() exception with defaultXml: EnvironmentDefault.xml, basePath: " + basePath, e);
                throw new IllegalArgumentException("Unable to load default configuration. Make sure you have a file named EnvironmentDefault.xml somewhere in the classpath.");
            }
            setConfig(localConfig);
        }
    }
    
    private synchronized static void setConfig(final CompositeConfiguration localConfig) {
    	config = localConfig;
	}

    private static boolean doesXmlConfigExist(final String fileName, final String basePath) {

        // Note that ConfigurationUtils.locate will look for the file on the file system under the basePath. It will
        // also look under the user's home directory and in the classpath, which is important - in some cases we
        // resolve to the XML configuration file as a resource in the classpath.
        URL url = ConfigurationUtils.locate(basePath, fileName);

        return (url != null);
    }

	private static XMLConfiguration buildXmlConfig(final String fileName, final String basePath) {
        XMLConfiguration xmlConfig = new XMLConfiguration();
        xmlConfig.setReloadingStrategy(new FileChangedReloadingStrategy());

        URL url = ConfigurationUtils.locate(basePath, fileName);

        xmlConfig.setURL(url);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Configuration file: " + xmlConfig.getURL().toExternalForm());
        }
        return xmlConfig;
    }

    /**
     * Returns the Commons Configuration object. IMPORTANT: If you want to take advantage of the dynamic reloading
     * capabilities for your configuration parameter, be sure not to store the value in a cache or field.
     * @return Configuration
     */
    public Configuration getConfiguration() {
        return config;
    }

}
