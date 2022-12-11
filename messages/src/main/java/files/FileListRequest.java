package files;

public class FileListRequest extends AbstractMessage{
    private String directory;

    public FileListRequest(String directory) {
        this.directory = directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }
}
