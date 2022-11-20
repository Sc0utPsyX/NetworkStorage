package client;

import files.LoginMessage;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LogonController implements Initializable {
    public PasswordField csnPswd;
    public TextField csnLogin;
    public TextField serverPort;
    public TextField serverIp;
    public Button btnLogin;

    private static LoginMessage loginMessage;

    public static LoginMessage getLoginMessage() {
        return loginMessage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
                try {
                    Application.changeScene("/client.fxml");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        });
    }
}
