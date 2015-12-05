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
                Arrays.asList("id", "name"),
                Arrays.asList(Arrays.asList("1", "Bob")));

        CsvFileManagerStartingImpl csvFileWriter =
                new CsvFileManagerStartingImpl(new AuditClientMockImpl());

        csvFileWriter.writeCsv(SOME_USER_ID, tabularData);

    }
}