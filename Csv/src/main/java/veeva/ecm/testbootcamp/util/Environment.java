package veeva.ecm.testbootcamp.util;

/**
 * Created by Eric Batzdorff on 12/1/2015.
 */
public class Environment {
    private final VeevaConfiguration config;

    /**
     * Environment that uses the default configuration.
     */
    public Environment() {
        config = VeevaConfiguration.getDefaultConfiguration();
    }


    public String getCsvOutputDir() {
        return config.getConfiguration().getString("common.csvWriter.csvOutputDir");
    }

    public int getCsvMaxRecords() {
        return config.getConfiguration().getInt("common.csvWriter.csvMaxRecords");
    }
}
