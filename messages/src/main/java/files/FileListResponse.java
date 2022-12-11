package files;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class FileListResponse extends AbstractMessage {
    public String getDirectory() {
        return directory;
    }
    private String activeUser;
    private String directory;
    private ArrayList<String> serverList = new ArrayList<>();

    public FileListResponse(String directory, String activeUser){
        this.directory = directory;
        this.activeUser = activeUser;
        update();
    }

    public void update(){
        try (Stream<Path> stream = Files.list(Paths.get(directory))) {
            serverList.clear();
            if (!directory.equals(activeUser)){
                serverList.add("../");
            }
            stream.map(p -> p.getFileName().toString()).forEach(o -> serverList.add(o));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getServerList(){
        return serverList;
    }

}
