import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import controller.LoginController;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        Label title = new Label("🚀 Online Test System");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField email = new TextField();
        email.setPromptText("Enter Email");
        email.setMaxWidth(200);

        PasswordField password = new PasswordField();
        password.setPromptText("Enter Password");
        password.setMaxWidth(200);

        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        LoginController controller = new LoginController();

        loginBtn.setOnAction(e -> {
            controller.handleLogin(email.getText(), password.getText(), stage);
        });

        VBox card = new VBox(15, title, email, password, loginBtn);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-padding: 25;" +
            "-fx-background-radius: 15;" +
            "-fx-border-radius: 15;" +
            "-fx-border-color: lightgray;"
        );

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);");

        Scene scene = new Scene(root, 500, 400);

        stage.setTitle("Online Test System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}