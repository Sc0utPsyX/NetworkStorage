package client;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NotSelectedError implements Initializable {

    public Button notFoundOk;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        notFoundOk.setOnAction((event -> {
            try {
                Application.changeScene("/client.fxml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }
}
