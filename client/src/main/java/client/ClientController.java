package client;


import files.AbstractMessage;
import files.FileListRequest;
import files.FileMessage;
import files.FileRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class ClientController implements Initializable {
    public Button delete;

    public Button refresh;
    public Button sendLocal;
    public Button sendNetwork;

    @FXML
    private ListView<String> serverList;

    @FXML
    private ListView<String> localList;

    private String sb = "";

    FileListRequest flr = new FileListRequest();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NetworkService.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = NetworkService.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("local_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        getFileList();
                    }
                    if (am instanceof FileListRequest){
                        serverList.setItems(FXCollections.observableArrayList(((FileListRequest) am).getServerList()));
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                NetworkService.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        getFileList();
        delete.setOnAction((event -> {
            System.out.println("Button Clicked");
        }));
        refresh.setOnAction((event -> {
            getFileList();
        }));
        sendLocal.setOnAction((event) -> {
            try {
                sendFile(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        sendNetwork.setOnAction((event) -> {
            try {
                sendFile(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }

    public void getFileList() {

        if (Platform.isFxApplicationThread()) {
                streamFileList();
        } else {
            Platform.runLater(this::streamFileList);
        }
    }

    private void streamFileList() {
        try (Stream<Path> stream = Files.list(Paths.get("local_storage"))){
            localList.getItems().clear();
            stream.map(p -> p.getFileName().toString()).forEach(o -> localList.getItems().add(o));
            serverList.getItems().clear();
            NetworkService.sendMsg(flr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(Boolean to) throws IOException { // (yes - to server ; no - to local machine)

        if (to){
            if(isChecked(serverList)) {
                for (String s: localList.getItems()){
                 if (s.equals(sb)){
                     sb = "";
                     break;
                 }
                }
                if (!sb.equals("")) {
                    NetworkService.sendMsg(new FileRequest(sb));
                }
            }
        } else {
            if(isChecked(localList)) {
                NetworkService.sendMsg(new FileMessage(Paths.get("local_storage/" + sb)));
            }
        }
        getFileList();
    }

    public boolean isChecked(ListView<String> list){
        if (!list.getSelectionModel().getSelectedItems().toString().equals("[]")){
            sb = list.getSelectionModel().getSelectedItem();
            System.out.println(sb);
            return true;
        } else {
            try {
                Application.changeScene("/notselected.fxml");
            } catch (IOException e){
                e.printStackTrace();
            }
            System.out.println("Not Selected");
            return false;
        }
    }


}
