package veeva.ecm.testbootcamp.env.impl;

import veeva.ecm.testbootcamp.env.Environment;
import veeva.ecm.testbootcamp.util.VeevaConfiguration;

public class EnvironmentImpl implements  Environment{
    private final VeevaConfiguration config = VeevaConfiguration.getDefaultConfiguration();

    public String getCsvOutputDir() {
            return config.getConfiguration().getString("common.csvWriter.csvOutputDir");
        }

    public int getCsvMaxRecords() {
            return config.getConfiguration().getInt("common.csvWriter.csvMaxRecords");
        }
}
