package files;

public class FileDeleteResponse extends AbstractMessage{
    public boolean deleted;

    public FileDeleteResponse(boolean deleted) {
        this.deleted = deleted;
    }
}
