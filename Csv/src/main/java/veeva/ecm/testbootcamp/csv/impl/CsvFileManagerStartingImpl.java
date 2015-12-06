package veeva.ecm.testbootcamp.csv.impl;

import org.apache.commons.lang.StringUtils;
import veeva.ecm.testbootcamp.csv.CsvFileManager;
import veeva.ecm.testbootcamp.env.impl.EnvironmentImpl;
import veeva.ecm.testbootcamp.env.Environment;
import veeva.ecm.testbootcamp.auditclient.AuditClient;
import veeva.ecm.testbootcamp.util.TabularData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

public class CsvFileManagerStartingImpl implements CsvFileManager {

    private final Environment environment = new EnvironmentImpl();

    private final AuditClient auditClient;

    public CsvFileManagerStartingImpl(AuditClient auditClient){
        this.auditClient = auditClient;
    }

    @Override
    public String writeCsv(String userId, TabularData tabularData){
        if (tabularData.getData().size() > environment.getCsvMaxRecords()){
            throw new IllegalArgumentException();
        }
        String csvOutputDir = environment.getCsvOutputDir();
        String fileName = UUID.randomUUID().toString() + ".csv";
        PrintWriter pw = createPrintWriter(csvOutputDir, fileName);
        // print header
        pw.println(asCommaSeparatedString(tabularData.getHeader()));

        // print each data record
        for (List<String> record : tabularData.getData()){
            pw.println(asCommaSeparatedString(record));
        }
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
        return StringUtils.join(data,",");
    }

    @Override
    public TabularData readCsv(String userId, String fileId) {
        // TODO
        return null;
    }
}
