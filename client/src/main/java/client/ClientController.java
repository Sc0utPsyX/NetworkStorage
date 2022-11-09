package client;


import files.AbstractMessage;
import files.FileMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    @FXML
    private ListView<String> serverList;

    @FXML
    private ListView<String> localList;




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
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                NetworkService.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        localList.setItems(FXCollections.observableArrayList());
        getFileList();
    }

    public void getFileList() {

        if (Platform.isFxApplicationThread()) {
            try {
                localList.getItems().clear();
                Files.list(Paths.get("local_storage")).map(p -> p.getFileName().toString()).forEach(o -> localList.getItems().add(o));
                serverList.getItems().clear();
                Files.list(Paths.get("cloud_storage")).map(p -> p.getFileName().toString()).forEach(o -> serverList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    localList.getItems().clear();
                    Files.list(Paths.get("local_storage")).map(p -> p.getFileName().toString()).forEach(o -> localList.getItems().add(o));
                    serverList.getItems().clear();
                    Files.list(Paths.get("cloud_storage")).map(p -> p.getFileName().toString()).forEach(o -> serverList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
