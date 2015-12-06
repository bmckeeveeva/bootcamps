package veeva.ecm.testbootcamp.csv.impl;

import org.junit.Test;
import veeva.ecm.testbootcamp.auditclient.AuditClientMockImpl;
import veeva.ecm.testbootcamp.util.TabularData;

import java.util.Arrays;

public class CsvFileManagerStartingImplTestU {
    private static final String SOME_USER_ID = "bob@bob.com";

    @Test
    public void testWriteCsv(){
        TabularData tabularData = new TabularData(
                Arrays.asList("id", "name"),    // header
                        Arrays.asList(          // collection of tabular data
                                Arrays.asList("1", "Bob"),
                                Arrays.asList("2", "Bill")));

        CsvFileManagerStartingImpl csvFileManager =
                new CsvFileManagerStartingImpl(new AuditClientMockImpl());

        csvFileManager.writeCsv(SOME_USER_ID, tabularData);

        // TODO:  verifications
    }
}