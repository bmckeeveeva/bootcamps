package veeva.ecm.testbootcamp.csv.impl;

import veeva.ecm.testbootcamp.csv.CsvFileManager;
import veeva.ecm.testbootcamp.util.Environment;
import veeva.ecm.testbootcamp.auditclient.AuditClient;
import veeva.ecm.testbootcamp.util.TabularData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CsvFileManagerStartingImpl implements CsvFileManager {

    private AuditClient auditClient;

    public CsvFileManagerStartingImpl(AuditClient auditClient){
        this.auditClient = auditClient;
    }

    @Override
    public String writeCsv(String userId, TabularData tabularData){
        Environment environment = new Environment();
        if (tabularData.getData().size() > environment.getCsvMaxRecords()){
            throw new RuntimeException();
        }
        String csvOutputDir = environment.getCsvOutputDir();
        String fileName = UUID.randomUUID().toString() + ".csv";
        PrintWriter pw = createPrintWriter(csvOutputDir, fileName);
        // print header
        pw.println(asCommaSeparatedString(tabularData.getHeader()));

        // print each data record
        tabularData.getData().stream().forEach(this::asCommaSeparatedString);
        pw.close();
        auditClient.auditFileWriteAction(userId, fileName);
        return fileName;
    }


    private PrintWriter createPrintWriter(String csvOutputDir, String fileName){
        String filePath = csvOutputDir + File.separator + fileName;
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filePath));
            return pw;
        } catch (IOException e) {
            throw new RuntimeException(e);
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
