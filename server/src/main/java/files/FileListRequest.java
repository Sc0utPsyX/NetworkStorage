package files;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class FileListRequest extends AbstractMessage {
    public String getDirectory() {
        return directory;
    }

    private String directory;
    private ArrayList<String> serverList = new ArrayList<>();

    public FileListRequest(String directory){
        this.directory = directory;
    }

    public void update(String directory){
        try (Stream<Path> stream = Files.list(Paths.get(System.getProperty("user.dir") + File.separator + "server_storage" + File.separator + directory + File.separator))) {
            stream.map(p -> p.getFileName().toString()).forEach(o -> serverList.add(o));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getServerList(){
        update(directory);
        return serverList;
    }

}
