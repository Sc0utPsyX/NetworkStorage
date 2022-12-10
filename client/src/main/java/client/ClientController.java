package client;


import files.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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
    static String conDirectory = "user1" + File.separator;


    FileListRequest flr = new FileListRequest(conDirectory);
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    NetworkService.start(LogonController.getLoginMessage().serverIp, LogonController.getLoginMessage().serverPort);
                    AbstractMessage am = NetworkService.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get(System.getProperty("user.dir") + File.separator + conDirectory + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        getFileList();
                    }
                    if (am instanceof FileListRequest){
                        System.out.println(((FileListRequest) am).getServerList());
                        Platform.runLater(() -> {
                            try (Stream<String> stream = ((FileListRequest) am).getServerList().stream()){
                                serverList.getItems().clear();
                                stream.distinct().forEach((o) -> serverList.getItems().add(o));
                            } catch (RuntimeException e)
                            {
                                e.printStackTrace();
                                }
                            });
                    }
                    if (am instanceof LoginMessage){
                        conDirectory = ((LoginMessage) am).directory;
                        if (conDirectory == null){
                            Application.changeScene("/logon.fxml");
                        }
                        flr = new FileListRequest(conDirectory);
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
            NetworkService.sendMsg(LogonController.getLoginMessage());
            getFileList();
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
        try (Stream<Path> stream = Files.list(Paths.get(System.getProperty("user.dir") + File.separator))){
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
                NetworkService.sendMsg(new FileMessage(Paths.get(System.getProperty("user.dir") + File.separator + sb)));
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
