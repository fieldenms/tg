package ua.com.fielden.platform.client.svg.combining;

public class FileBegin {

    private final String fileBegin;

    public String getFileBegin() {
        return fileBegin;
    }

    public FileBegin(final String name, final String size) {

        this.fileBegin = "<link rel=\"import\" href=\"/resources/polymer/iron-icon/iron-icon.html\">" +
                "\n <link rel=\"import\" href=\"/resources/polymer/iron-iconset-svg/iron-iconset-svg.html\">" +
                "\n <iron-iconset-svg name=\"" + name + "\" size=\"" + size + "\">" + "\n <svg>" + "\n <defs>; \n";
    }

}
