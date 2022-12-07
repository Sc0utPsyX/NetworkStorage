package client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Application extends javafx.application.Application {

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    private static Stage primaryStage;

    public static void run(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("/login.fxml"));
        Parent parent = loader.load();
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.setTitle("Cloud Storage");
        stage.show();
        stage.isResizable();
    }
    public static void  changeScene(String fxml) throws IOException {
        Parent pane = FXMLLoader.load(Objects.requireNonNull(Application.class.getResource(fxml)));
        primaryStage.getScene().setRoot(pane);
    }
}
