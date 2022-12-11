package client;


import files.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class ClientController implements Initializable {

    @FXML
    public Button refresh;
    @FXML
    public Button sendLocal;
    @FXML
    public Button sendNetwork;
    @FXML
    public AnchorPane clientWindow;
    @FXML
    public AnchorPane loginWindow;
    @FXML
    public Text errorLogin;
    @FXML
    public Button btnRegistration;
    @FXML
    public MenuItem btnMenuConnect;
    public MenuItem btnMenuClose;
    @FXML
    public MenuItem btnMenuAbout;
    @FXML
    public AnchorPane registrationWindow;
    public TextField regName;
    public TextField regEmail;
    public TextField regPassword;
    public Button btnAcceptRegistration;
    public Text regStatus;
    public Button btnRegistrationBack;
    @FXML
    private ListView<String> serverList;
    @FXML
    private ListView<String> localList;

    private String fileName = "";
    static StringBuilder serverDirectory;
    @FXML
    public PasswordField csnPswd;
    @FXML
    public TextField csnLogin;
    @FXML
    public TextField serverPort;
    @FXML
    public TextField serverIp;
    @FXML
    public Button btnLogin;

    private static LoginMessage loginMessage;

    private StringBuilder clientDir = new StringBuilder(System.getProperty("user.dir") + File.separator);

    FileListRequest flr;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Thread t = new Thread(() -> {
            try {
                NetworkService.start(loginMessage.serverIp, loginMessage.serverPort);
                while (true) {
                    AbstractMessage am = NetworkService.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get(clientDir.toString() + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        getFileList();
                    }
                    if (am instanceof FileListResponse){
                        serverDirectory = new StringBuilder(((FileListResponse) am).getDirectory());
                        Platform.runLater(() -> {
                            try (Stream<String> stream = ((FileListResponse) am).getServerList().stream()){
                                serverList.getItems().clear();
                                stream.distinct().forEach((o) -> serverList.getItems().add(o));
                            } catch (RuntimeException e)
                            {
                                e.printStackTrace();
                                }
                            });
                    }
                    if (am instanceof LoginMessage){
                        serverDirectory = new StringBuilder(((LoginMessage) am).getDirectory());
                        if (((LoginMessage) am).getDirectory() == null){
                            try{
                                loginWindow.setVisible(true);
                                clientWindow.setVisible(false);
                                errorLogin.setText("Wrong credentials");
                                btnLogin.wait();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            flr = new FileListRequest(serverDirectory.toString());
                            getFileList();
                        }

                    }
                    if (am instanceof RegistrationMessage) {
                        if (((RegistrationMessage) am).isRegStatus()) {
                            registrationWindow.setVisible(false);
                            loginWindow.setVisible(true);
                            errorLogin.setText("Registration Successful");
                        } else {
                            regStatus.setText("Login already Exists");
                        }
                    }
                    if (am instanceof FileDeleteResponse) {
                        if (((FileDeleteResponse) am).deleted) {
                            getFileList();
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                NetworkService.stop();
            }
        });
        t.setDaemon(true);
        btnLogin.setOnAction(event -> {
            if (csnLogin.getLength() < 30
                    && csnPswd.getLength() < 30
                    && csnLogin.getLength() > 2
                    && csnPswd.getLength() > 2
                    && serverIp.getLength() > 8
                    && serverPort.getLength() > 1
                    && serverPort.getLength() < 6
                    && serverIp.getLength() < 17
            ) {
                loginMessage = new LoginMessage(csnPswd.getText(), csnLogin.getText(),
                        serverIp.getText(), Integer.parseInt(serverPort.getText()));
                if (Platform.isFxApplicationThread() && !t.isAlive()) {
                    t.start();
                } else if (!t.isAlive()){
                    Platform.runLater(t::start);
                }
                try {
                    Thread.sleep(500);
                    NetworkService.sendMsg(loginMessage);
                    System.out.println(loginMessage);
                    loginWindow.setVisible(false);
                    clientWindow.setVisible(true);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                getFileList();
            }
        });
        refresh.setOnAction((event -> getFileList()));
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
            getFileList();
        });
        localList.setOnMouseClicked((event) -> {
            String selectedItem;
            if (event.getButton() == MouseButton.SECONDARY){
                ContextMenu contextMenu = new ContextMenu();
                MenuItem delete = new MenuItem("Delete");
                contextMenu.getItems().add(delete);
                localList.setContextMenu(contextMenu);
                delete.setOnAction((event1 -> {
                    try {
                        Files.deleteIfExists(Path.of(clientDir.toString() + localList.getSelectionModel().getSelectedItem()));
                        getFileList();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
            if (event.getClickCount() == 2) {
                selectedItem = localList.getSelectionModel().getSelectedItem();
                if (isChecked(localList)){
                    if (selectedItem.equals("../")){
                        clientDir.deleteCharAt(clientDir.lastIndexOf("/"))
                                .delete(clientDir.lastIndexOf("/") + 1, clientDir.capacity() - 1);
                        getFileList();
                    } else if (Files.isDirectory(Path.of(clientDir.toString() + selectedItem))){
                        clientDir.append(selectedItem).append(File.separator);
                        getFileList();
                    } else {
                        showError("It's not directory");
                    }
                }
            }
        });
        serverList.setOnMouseClicked((event -> {
            String selectedItem;
            if (event.getButton() == MouseButton.SECONDARY){
                ContextMenu contextMenu = new ContextMenu();
                MenuItem delete = new MenuItem("Delete");
                contextMenu.getItems().add(delete);
                serverList.setContextMenu(contextMenu);
                delete.setOnAction((event1 -> {
                    if (!serverList.getSelectionModel().getSelectedItem().equals("[]")){
                    NetworkService.sendMsg(new FileDeleteRequest(serverList.getSelectionModel().getSelectedItem()));
                    }
                    }));
            }
            if (event.getClickCount() == 2) {
                selectedItem = serverList.getSelectionModel().getSelectedItem();
                if (isChecked(serverList)){
                    if (selectedItem.equals("../")){
                        serverDirectory.deleteCharAt(serverDirectory.lastIndexOf("/"))
                                .delete(serverDirectory.lastIndexOf("/") + 1, serverDirectory.capacity() - 1);
                        flr.setDirectory(serverDirectory.toString());
                        getFileList();
                    } else {
                        flr.setDirectory(serverDirectory.toString() + selectedItem + File.separator);
                        NetworkService.sendMsg(flr);
                    }
                }
            }
        }));
        btnMenuClose.setOnAction((e) -> {
            Platform.exit();
            System.exit(0);
        });
        btnMenuConnect.setOnAction((e) -> {
            loginWindow.setVisible(true);
            clientWindow.setVisible(false);
        });
        btnMenuAbout.setOnAction((event -> showAbout()));
        btnRegistration.setOnAction((event -> {
            if (serverPort.getText().equals("") && serverIp.getText().equals("")){
                showError("Server Ip or Port is empty");
            } else {
                loginWindow.setVisible(false);
                registrationWindow.setVisible(true);
                loginMessage = new LoginMessage(serverIp.getText(), Integer.parseInt(serverPort.getText()));
                if (Platform.isFxApplicationThread() && !t.isAlive()) {
                    t.start();
                } else if (!t.isAlive()){
                    Platform.runLater(t::start);
                }
            }
        }));
        btnAcceptRegistration.setOnAction((event -> {
            if (regName.getLength() > 3
                    && regName.getLength() < 30
                    && regEmail.getLength() > 3
                    && regEmail.getLength() <50
                    && regPassword.getLength() > 3
                    && regPassword.getLength() < 20){
                RegistrationMessage registrationMessage = new RegistrationMessage(regName.getText(), regEmail.getText(), regPassword.getText());
                NetworkService.sendMsg(registrationMessage);
            } else {
                regStatus.setText("Wrong Credentials");
            }
        }));
        btnRegistrationBack.setOnAction((event -> {
            loginWindow.setVisible(true);
            registrationWindow.setVisible(false);
            serverPort.setEditable(false);
            serverIp.setEditable(false);
        }));
    }


    public void getFileList() {
        if (Platform.isFxApplicationThread()) {
                streamFileList();
        } else {
            Platform.runLater(this::streamFileList);
        }
    }

    private void streamFileList() {
        try (Stream<Path> stream = Files.list(Paths.get(clientDir.toString()))){
            localList.getItems().clear();
            localList.getItems().add("../");
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
                 if (s.equals(fileName)){
                     fileName = "";
                     break;
                 }
                }
                if (!fileName.equals("")) {
                    NetworkService.sendMsg(new FileRequest(fileName));
                }
            }
        } else {
            if(isChecked(localList)) {
                NetworkService.sendMsg(new FileMessage(Paths.get(clientDir.toString() + fileName)));
            }
        }
        getFileList();
    }

    public boolean isChecked(ListView<String> list){
        if (!list.getSelectionModel().getSelectedItems().toString().equals("[]")){
            fileName = list.getSelectionModel().getSelectedItem();
            System.out.println(fileName);
            return true;
        } else {
            showError("Not Selected");
            System.out.println("Not Selected");
            return false;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(
                Alert.AlertType.WARNING,
                message,
                ButtonType.CLOSE
        );
        alert.showAndWait();
    }

    private void showAbout() {
        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                "This application created by Sc0utPsyX",
                ButtonType.CLOSE
        );
        alert.showAndWait();
    }

}
