package veeva.ecm.testbootcamp.csv.impl;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import veeva.ecm.testbootcamp.auditclient.AuditClient;
import veeva.ecm.testbootcamp.csv.CsvFileManager;
import veeva.ecm.testbootcamp.util.Environment;
import veeva.ecm.testbootcamp.util.TabularData;
import veeva.ecm.testbootcamp.csv.impl.CsvFileManagerTestableImpl.PrintFactory;

import java.io.PrintWriter;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class CsvFileManagerTestableImplTestU {
    private static final String SOME_USER_ID = "bob@bob.com";

    private static final int DEFAULT_CSV_LIMIT = 10;

    @Test
    public void testWriteCsv(){
        // assemble
        TabularData tabularData = new TabularData(
                Arrays.asList("id", "name"),
                Arrays.asList(Arrays.asList("1", "Bob")));
        PrintWriter mockPrintWriter = mock(PrintWriter.class);

        // act
        CsvFileManager csvFileWriter =
                createCsvFileManager(mockPrintWriter);
        csvFileWriter.writeCsv(SOME_USER_ID, tabularData);

        // asserts
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockPrintWriter, times(2)).println(stringArgumentCaptor.capture());
        assertEquals("Expected header data in comma-delimited format",
                "id,name", stringArgumentCaptor.getAllValues().get(0));
        assertEquals("Expected record data in comma-delimited format",
                "1,Bob", stringArgumentCaptor.getAllValues().get(1));
    }

    @Test
    public void testWriteCsv_TestLimits(){
        // assemble
        TabularData tabularData = new TabularData(
                Arrays.asList("id", "name"),
                Arrays.asList(
                        Arrays.asList("1", "Bob"),
                        Arrays.asList("2", "Bill")));
        Environment environment = mockEnvironmentWithLimitOf(1);
        AuditClient auditClient = mock(AuditClient.class);
        PrintWriter printWriter = mock(PrintWriter.class);

        try{
            // act
            CsvFileManager csvFileWriter =
                    createCsvFileManager(
                            auditClient,
                            environment,
                            mockPrintWriterFactory(printWriter));
            csvFileWriter.writeCsv(SOME_USER_ID, tabularData);
            fail("Expected IllegalArgumentException");
        }catch(IllegalArgumentException e){
            // assert
            verifyZeroInteractions(auditClient, printWriter);
        }
    }

    @Test
    public void testWriteCsv_Audit(){
        // assemble
        TabularData tabularData =
                createArbitraryTabularData();
        AuditClient auditClient = mock(AuditClient.class);

        // act
        CsvFileManager csvFileWriter = createCsvFileManager(auditClient);
        csvFileWriter.writeCsv(SOME_USER_ID, tabularData);

        // assert
        verify(auditClient, times(1)).auditFileWriteAction(eq(SOME_USER_ID), anyString());
    }

    @Test
    public void testWriteCsv_PrintWriterClosedOnException(){
        // assemble
        TabularData tabularData = createArbitraryTabularData();

        PrintWriter mockPrintWriter = mock(PrintWriter.class);
        doThrow(new RuntimeException()).when(mockPrintWriter).println(anyString());

        try{
            // act
            CsvFileManager csvFileWriter =
                    createCsvFileManager(mockPrintWriter);
            csvFileWriter.writeCsv(SOME_USER_ID, tabularData);
            fail("expected runtimeexception");
        }catch(RuntimeException e){
            // assert
            verify(mockPrintWriter, times(1)).close();
        }
    }

    private PrintFactory mockPrintWriterFactory(){
        return mockPrintWriterFactory(mock(PrintWriter.class));
    }

    private PrintFactory mockPrintWriterFactory(PrintWriter printWriter){
        PrintFactory printFactory = mock(PrintFactory.class);
        when(printFactory.createPrintWriter(anyString(), anyString())).thenReturn(printWriter);
        return printFactory;
    }

    private Environment mockEnvironmentWithLimitOf(int limit){
        Environment environment = mock(Environment.class);
        when(environment.getCsvMaxRecords()).thenReturn(limit);
        return environment;
    }

    private AuditClient mockAuditClient(){
        return mock(AuditClient.class);
    }

    private CsvFileManager createCsvFileManager(PrintWriter printWriter){
        return new CsvFileManagerTestableImpl(
                mockAuditClient(),
                mockEnvironmentWithLimitOf(DEFAULT_CSV_LIMIT),
                mockPrintWriterFactory(printWriter));
    }

    private CsvFileManager createCsvFileManager(AuditClient auditClient){
        return new CsvFileManagerTestableImpl(
                auditClient,
                mockEnvironmentWithLimitOf(DEFAULT_CSV_LIMIT),
                mockPrintWriterFactory());
    }

    private CsvFileManager createCsvFileManager(AuditClient auditClient,
                                                Environment environment,
                                                PrintFactory printFactory){
        return new CsvFileManagerTestableImpl(
                auditClient,
                environment,
                printFactory);
    }

    private TabularData createArbitraryTabularData(){
        return new TabularData(
                Arrays.asList("id", "name"),
                Arrays.asList(Arrays.asList("1", "Bob")));
    }



}