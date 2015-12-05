package veeva.ecm.testbootcamp.util;

import java.util.List;

public class TabularData {
    private List<String> header;
    private List<List<String>> data;
    public TabularData(List<String> header,
                       List<List<String>> data) {
        this.header = header;
        this.data = data;
    }

    public List<String> getHeader() {
        return header;
    }

    public List<List<String>> getData() {
        return data;
    }
}
