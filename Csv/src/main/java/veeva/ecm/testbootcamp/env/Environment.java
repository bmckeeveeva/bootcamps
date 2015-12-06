package veeva.ecm.testbootcamp.env;

/**
 * Veeva Environment
 */
public interface Environment {

    /**
     * Returns the output directory for CSV files
     *
     * @return the output directory for CSV files
     */
    String getCsvOutputDir();

    /**
     * Returns the max number of data rows that can be written to a csv file
     *
     * @return the max number of data rows that can be written to a csv file
     */
    int getCsvMaxRecords();


}
