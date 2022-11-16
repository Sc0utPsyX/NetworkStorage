package files;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class FileListRequest extends AbstractMessage {

    private ArrayList<String> serverList = new ArrayList<>();

    public FileListRequest(){
    }

    void update(){
        try (Stream<Path> stream = Files.list(Paths.get("cloud_storage"))) {
            stream.map(p -> p.getFileName().toString()).forEach(o -> serverList.add(o));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getServerList(){
        update();
        return serverList;
    }

}
