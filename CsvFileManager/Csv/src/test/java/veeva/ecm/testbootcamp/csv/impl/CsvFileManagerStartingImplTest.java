package veeva.ecm.testbootcamp.csv.impl;

import org.junit.Test;
import veeva.ecm.testbootcamp.auditclient.AuditClientMockImpl;
import veeva.ecm.testbootcamp.util.TabularData;

import java.util.Arrays;

public class CsvFileManagerStartingImplTest {
    private static final String SOME_USER_ID = "bob@bob.com";

    @Test
    public void testWriteCsv(){
        TabularData tabularData = new TabularData(
            /* Header */
            Arrays.asList("id", "name"),
            /* Rows in table */
            Arrays.asList(
                /* Cells in a row */
                Arrays.asList("1", "Bob"),
                Arrays.asList("2", "Bill")));

        CsvFileManagerStartingImpl csvFileManager =
            new CsvFileManagerStartingImpl(new AuditClientMockImpl());

        csvFileManager.writeCsv(SOME_USER_ID, tabularData);

        // TODO:  verifications
    }
}