package controller;

import db.DBConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class StudentController {

    ArrayList<Question> questions = new ArrayList<>();
    int current = 0;
    int score = 0;

    public void showTestScreen(Stage mystage, int studentId) {

        final int attemptId = createAttempt(studentId); 

        loadQuestions();

        Label questionLabel = new Label();
        RadioButton a = new RadioButton();
        RadioButton b = new RadioButton();
        RadioButton c = new RadioButton();
        RadioButton d = new RadioButton();

        ToggleGroup group = new ToggleGroup();
        a.setToggleGroup(group);
        b.setToggleGroup(group);
        c.setToggleGroup(group);
        d.setToggleGroup(group);

        Button nextBtn = new Button("Next");

        Label timerLabel = new Label("Time: 60");
        timerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");

        final int[] timeLeft = {60};

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {   
                timeLeft[0]--;
                timerLabel.setText("Time: " + timeLeft[0]);
                if (timeLeft[0] <= 0) {
                    showResult(mystage, attemptId);   
                }
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        questionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        a.setStyle("-fx-font-size: 14px;");
        b.setStyle("-fx-font-size: 14px;");
        c.setStyle("-fx-font-size: 14px;");
        d.setStyle("-fx-font-size: 14px;");
        nextBtn.setStyle(
            "-fx-background-color: #667eea;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;"
        );

        showQuestion(questionLabel, a, b, c, d);

        nextBtn.setOnAction(e -> {
            RadioButton selected = (RadioButton) group.getSelectedToggle();  
            saveResponse(attemptId, questions.get(current).id,
                selected != null ? selected.getText() : "");          

            if (selected != null) {
                if (selected.getText().equals(questions.get(current).correct)) {
                    score++;
                }
            }

            current++;

            if (current < questions.size()) {
                showQuestion(questionLabel, a, b, c, d);
                group.selectToggle(null);
            } else {
                timeline.stop();                       
                showResult(mystage, attemptId);        
            }
        });

      
        VBox root = new VBox(15, timerLabel, questionLabel, a, b, c, d, nextBtn);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
            "-fx-padding: 20;" +
            "-fx-background-color: #f5f7fa;"
        );

        Scene scene = new Scene(root, 400, 300);
        mystage.setScene(scene);
        mystage.show();
    }

    private void loadQuestions() {
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT q.* FROM questions q " +
                "JOIN test_questions tq ON q.question_id = tq.question_id " +
                "WHERE tq.test_id = 1"
            );
            while (rs.next()) {
                questions.add(new Question(
                    rs.getInt("question_id"),
                    rs.getString("question_text"),
                    rs.getString("option_a"),
                    rs.getString("option_b"),
                    rs.getString("option_c"),
                    rs.getString("option_d"),
                    rs.getString("correct_option")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showQuestion(Label q, RadioButton a, RadioButton b,
                               RadioButton c, RadioButton d) {
        Question ques = questions.get(current);
        q.setText("Q" + (current + 1) + ": " + ques.text);
        a.setText(ques.a);
        b.setText(ques.b);
        c.setText(ques.c);
        d.setText(ques.d);
    }

    private void showResult(Stage stage, int attemptId) { 
        Label result = new Label("Score: " + score + "/" + questions.size());
        result.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-text-fill: green;" +
            "-fx-font-weight: bold;"
        );

        Button exitBtn = new Button("Exit");
        exitBtn.setStyle(
            "-fx-background-color: #ff4d4d;" +
            "-fx-text-fill: white;"
        );
        exitBtn.setOnAction(e -> stage.close());

        VBox root = new VBox(20, result, exitBtn);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f7fa;");

        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);

        updateScore(attemptId, score);  
    }

    private int createAttempt(int studentId) {
        try {
            Connection con = DBConnection.getConnection();
            String query = "INSERT INTO test_attempts(student_id, test_id) VALUES (?,1) RETURNING attempt_id";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, studentId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("attempt_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void saveResponse(int attemptId, int questionId, String selectedOption) {
        try {
            Connection con = DBConnection.getConnection();
            String query = "INSERT INTO responses(attempt_id, question_id, selected_option) VALUES (?,?,?)";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, attemptId);
            pst.setInt(2, questionId);
            pst.setString(3, selectedOption);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateScore(int attemptId, int score) {
        try {
            Connection con = DBConnection.getConnection();
            String query = "UPDATE test_attempts SET score=? WHERE attempt_id=?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, score);
            pst.setInt(2, attemptId);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}