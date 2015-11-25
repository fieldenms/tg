package ua.com.fielden.platform.client.svg.combining;

public class FileEnd {

    private String fileEnd = new String();

    public FileEnd() {
        this.fileEnd = "</defs>" + "\n </svg>" + "\n </iron-iconset-svg>";
    }

    public String getFileEnd() {
        return fileEnd;
    }
}
