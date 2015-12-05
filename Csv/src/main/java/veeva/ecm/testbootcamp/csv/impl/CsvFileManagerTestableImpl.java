package veeva.ecm.testbootcamp.csv.impl;

import veeva.ecm.testbootcamp.auditclient.AuditClient;
import veeva.ecm.testbootcamp.csv.CsvFileManager;
import veeva.ecm.testbootcamp.util.Environment;
import veeva.ecm.testbootcamp.util.TabularData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CsvFileManagerTestableImpl implements CsvFileManager {

    private AuditClient auditClient;

    private Environment environment;

    private PrintFactory printFactory;

    CsvFileManagerTestableImpl(AuditClient auditClient,
                               Environment environment,
                               PrintFactory printFactory){
        this.auditClient = auditClient;
        this.environment = environment;
        this.printFactory = printFactory;
    }

    public CsvFileManagerTestableImpl(AuditClient auditClient){
        this.environment = new Environment();
        this.printFactory = new PrintFactory();
        this.auditClient = auditClient;
    }

    @Override
    public String writeCsv(String userId, TabularData tabularData){

        if (tabularData.getData().size() > environment.getCsvMaxRecords()){
            throw new IllegalArgumentException();
        }
        String csvOutputDir = environment.getCsvOutputDir();
        String fileName = UUID.randomUUID().toString() + ".csv";
        try(PrintWriter pw = createPrintWriter(csvOutputDir, fileName)){
            // print header
            pw.println(asCommaSeparatedString(tabularData.getHeader()));

            // print each data record
            tabularData.
                    getData().
                    stream().
                    map(this::asCommaSeparatedString).
                    forEach(pw::println);
        }

        auditClient.auditFileWriteAction(userId, fileName);
        return fileName;
    }


    private PrintWriter createPrintWriter(String csvOutputDir, String fileName){
        return printFactory.createPrintWriter(csvOutputDir, fileName);
    }

    static class PrintFactory{
        PrintWriter createPrintWriter(String csvOutputDir, String fileName){
            String filePath = csvOutputDir + File.separator + fileName;
            try {
                PrintWriter pw = new PrintWriter(new FileWriter(filePath));
                return pw;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String asCommaSeparatedString(List<String> data){
        String result =
                data.stream().collect(Collectors.joining(","));
        return result;
    }


    @Override
    public TabularData readCsv(String userId, String fileId) {
        // TODO
        return null;
    }
}
