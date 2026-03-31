import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import controller.LoginController;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // ^ Title 
        Label title = new Label("⚡ MCQ TEST SYSTEM");
        title.setStyle(
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #00ffe7;" +          
            "-fx-effect: dropshadow(gaussian, #00ffe7, 18, 0.6, 0, 0);"
        );

        Label subtitle = new Label("Enter credentials to continue");
        subtitle.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #888888;"
        );

       
        TextField email = new TextField();
        email.setPromptText("📧  Email");
        email.setMaxWidth(280);
        styleField(email);

        PasswordField password = new PasswordField();
        password.setPromptText("🔒  Password");
        password.setMaxWidth(280);
        styleField(password);

       
        Button loginBtn = new Button("LOGIN →");
        loginBtn.setMaxWidth(280);
        loginBtn.setStyle(
            "-fx-background-color: #00ffe7;" +
            "-fx-text-fill: #0a0a0a;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 20;"
        );
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-text-fill: #0a0a0a;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 20;"
        ));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(
            "-fx-background-color: #00ffe7;" +
            "-fx-text-fill: #0a0a0a;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 20;"
        ));

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #ff4d4d; -fx-font-size: 12px;");

        LoginController controller = new LoginController();
        loginBtn.setOnAction(e -> {
            errorLabel.setText("");
            if (email.getText().isBlank() || password.getText().isBlank()) {
                errorLabel.setText("⚠ Please fill in all fields.");
                return;
            }
            controller.handleLogin(email.getText().trim(),
                                   password.getText().trim(),
                                   stage, errorLabel);
        });

        
        password.setOnAction(e -> loginBtn.fire());

       
        VBox card = new VBox(14, title, subtitle, email, password, loginBtn, errorLabel);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: #111111;" +
            "-fx-padding: 35;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: #00ffe7;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 16;"
        );
        card.setMaxWidth(360);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00ffe7"));
        glow.setRadius(30);
        glow.setSpread(0.05);
        card.setEffect(glow);

        
        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: #0a0a0a;");

        Scene scene = new Scene(root, 520, 420);
        stage.setTitle("⚡ Online MCQ Test System");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    
    public static void styleField(TextInputControl field) {
        field.setStyle(
            "-fx-background-color: #1e1e1e;" +
            "-fx-text-fill: #e0e0e0;" +
            "-fx-prompt-text-fill: #555555;" +
            "-fx-border-color: #333333;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 9 12;" +
            "-fx-font-size: 13px;"
        );
        field.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                field.setStyle(
                    "-fx-background-color: #1e1e1e;" +
                    "-fx-text-fill: #e0e0e0;" +
                    "-fx-prompt-text-fill: #555555;" +
                    "-fx-border-color: #00ffe7;" +
                    "-fx-border-width: 1.5;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 9 12;" +
                    "-fx-font-size: 13px;"
                );
            } else {
                field.setStyle(
                    "-fx-background-color: #1e1e1e;" +
                    "-fx-text-fill: #e0e0e0;" +
                    "-fx-prompt-text-fill: #555555;" +
                    "-fx-border-color: #333333;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 9 12;" +
                    "-fx-font-size: 13px;"
                );
            }
        });
    }

    public static void main(String[] args) {
         launch(); 
    }
}