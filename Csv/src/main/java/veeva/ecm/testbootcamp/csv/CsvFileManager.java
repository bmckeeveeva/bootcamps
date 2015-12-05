package veeva.ecm.testbootcamp.csv;

import veeva.ecm.testbootcamp.util.Environment;
import veeva.ecm.testbootcamp.util.TabularData;

public interface CsvFileManager {

    /**
     * Writes the specified tabular data to persistent storage in CSV format.
     * <p/>
     * The maximum number of data records allowed is as specified in the Vault configuration,
     * i.e. {@link Environment#getCsvMaxRecords()}.
     * <p/>
     * All calls to this method will be audited in the Vault audit sub-system.
     *
     * @param userId      id of the user making the request
     * @param tabularData tabular data to write
     * @return id of the file in persistent storage.  This id can be used to recall the file at a later point in time
     * @throws IllegalArgumentException in case that the number of records in the tabular data exceeds the
     *                                  maximum allowed by the Vault configuration
     */
    String writeCsv(String userId,
                    TabularData tabularData) throws IllegalArgumentException;

    /**
     * Retrieves the specified file from persistent storage as tabular data (i.e. {@link TabularData}
     *
     * @param userId id of the user making the request
     * @param fileId id of the file to retrieve
     * @return the requested file as TabularData
     */
    TabularData readCsv(String userId, String fileId);
}
