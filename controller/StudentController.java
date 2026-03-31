package controller;

import db.DBConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class StudentController {

    private final ArrayList<Question> questions = new ArrayList<>();
    private int current = 0;
    private int score   = 0;

    public void showTestScreen(Stage stage, int studentId) {
        loadQuestions();
        if (questions.isEmpty()) {
            showAlert("No questions found.");
            return;
        }
        final int attemptId = createAttempt(studentId);
        if (attemptId == -1) {
            showAlert("Could not create test attempt.");
            return;
        }
        buildTestUI(stage, attemptId);
    }

    private void buildTestUI(Stage stage, int attemptId) {
        Label counterLabel = new Label("Question 1 / " + questions.size());
        counterLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888;");

        Label timerLabel = new Label("⏱  60s");
        timerLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #00ffe7; -fx-effect: dropshadow(gaussian,#00ffe7,10,0.5,0,0);");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(counterLabel, spacer, timerLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        ProgressBar progress = new ProgressBar(0);
        progress.setMaxWidth(Double.MAX_VALUE);
        progress.setStyle("-fx-accent: #00ffe7; -fx-background-color: #1e1e1e;");

        Label questionLabel = new Label();
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        ToggleGroup group = new ToggleGroup();
        RadioButton[] opts = new RadioButton[4];
        String[] letters = {"A", "B", "C", "D"};
        VBox optionBox = new VBox(10);
        for (int i = 0; i < 4; i++) {
            opts[i] = new RadioButton();
            opts[i].setToggleGroup(group);
            styleRadio(opts[i]);
            optionBox.getChildren().add(opts[i]);
        }

        Button nextBtn = new Button("Next  →");
        nextBtn.setStyle("-fx-background-color: #00ffe7; -fx-text-fill: #0a0a0a; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 28;");

        Label feedback = new Label("");
        feedback.setStyle("-fx-text-fill: #888888;");

        loadQuestionIntoUI(questionLabel, opts, letters, 0);

        final int[] timeLeft = {questions.size() * 30};
        final Timeline[] timelineWrapper = new Timeline[1];

        timelineWrapper[0] = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                timeLeft[0]--;
                timerLabel.setText("⏱  " + timeLeft[0] + "s");
                if (timeLeft[0] <= 10) {
                    timerLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ff4d4d; -fx-effect: dropshadow(gaussian,#ff4d4d,10,0.5,0,0);");
                }
                if (timeLeft[0] <= 0) {
                    saveCurrentAndFinish(group, opts, letters, attemptId, stage, timelineWrapper[0]);
                }
            })
        );
        timelineWrapper[0].setCycleCount(Timeline.INDEFINITE);
        timelineWrapper[0].play();

        nextBtn.setOnAction(e -> {
            RadioButton selected = (RadioButton) group.getSelectedToggle();
            if (selected == null) {
                feedback.setText("⚠ Please select an option.");
                return;
            }
            feedback.setText("");
            String selectedLetter = (String) selected.getUserData();
            saveResponse(attemptId, questions.get(current).id, selectedLetter);

            if (selectedLetter.equalsIgnoreCase(questions.get(current).correct)) {
                score++;
            }

            current++;
            if (current < questions.size()) {
                loadQuestionIntoUI(questionLabel, opts, letters, current);
                group.selectToggle(null);
                counterLabel.setText("Question " + (current + 1) + " / " + questions.size());
                progress.setProgress((double) current / questions.size());
                for (RadioButton rb : opts) styleRadio(rb);
            } else {
                if (timelineWrapper[0] != null) timelineWrapper[0].stop();
                showResult(stage, attemptId);
            }
        });

        VBox card = new VBox(16, header, progress, questionLabel, optionBox, feedback, nextBtn);
        card.setPadding(new Insets(28));
        card.setStyle("-fx-background-color: #111111; -fx-background-radius: 16; -fx-border-color: #222222; -fx-border-radius: 16;");
        card.setMaxWidth(520);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#00ffe7"));
        shadow.setRadius(20);
        card.setEffect(shadow);

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: #0a0a0a;");
        stage.setScene(new Scene(root, 580, 460));
        stage.show();
    }

    private void loadQuestionIntoUI(Label qLabel, RadioButton[] opts, String[] letters, int idx) {
        Question q = questions.get(idx);
        qLabel.setText("Q" + (idx + 1) + ".  " + q.text);
        String[] texts = {q.a, q.b, q.c, q.d};
        for (int i = 0; i < 4; i++) {
            opts[i].setText(letters[i] + ".  " + texts[i]);
            opts[i].setUserData(letters[i]);
        }
    }

    private void saveCurrentAndFinish(ToggleGroup group, RadioButton[] opts, String[] letters, int attemptId, Stage stage, Timeline timeline) {
        if (current < questions.size()) {
            RadioButton selected = (RadioButton) group.getSelectedToggle();
            String selectedLetter = selected != null ? (String) selected.getUserData() : "";
            saveResponse(attemptId, questions.get(current).id, selectedLetter);
            if (!selectedLetter.isEmpty() && selectedLetter.equalsIgnoreCase(questions.get(current).correct)) {
                score++;
            }
        }
        if (timeline != null) timeline.stop();
        showResult(stage, attemptId);
    }

    private void styleRadio(RadioButton rb) {
        String normal = "-fx-font-size: 14px; -fx-text-fill: #cccccc; -fx-background-color: transparent;";
        String selectedStyle = "-fx-font-size: 14px; -fx-text-fill: #00ffe7; -fx-font-weight: bold; -fx-background-color: transparent;";
        rb.setStyle(normal);
        rb.selectedProperty().addListener((obs, old, selected) -> rb.setStyle(selected ? selectedStyle : normal));
    }

    private void showResult(Stage stage, int attemptId) {
        updateScore(attemptId, score);
        int total = questions.size();
        double pct = total > 0 ? (double) score / total * 100 : 0;

        Label scoreLabel = new Label(score + " / " + total);
        scoreLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Button exitBtn = new Button("Exit");
        exitBtn.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 30;");
        exitBtn.setOnAction(e -> stage.close());

        VBox card = new VBox(14, new Label("Test Complete!"), scoreLabel, exitBtn);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #111111; -fx-padding: 40; -fx-background-radius: 16;");

        stage.setScene(new Scene(new StackPane(card), 400, 360));
    }

    private void loadQuestions() {
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT q.* FROM questions q JOIN test_questions tq ON q.question_id = tq.question_id WHERE tq.test_id = 1 ORDER BY RANDOM()");
            while (rs.next()) {
                questions.add(new Question(rs.getInt("question_id"), rs.getString("question_text"), rs.getString("option_a"), rs.getString("option_b"), rs.getString("option_c"), rs.getString("option_d"), rs.getString("correct_option")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private int createAttempt(int studentId) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("INSERT INTO test_attempts(student_id, test_id, attempted_at) VALUES (?,1,NOW()) RETURNING attempt_id");
            pst.setInt(1, studentId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt("attempt_id");
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    private void saveResponse(int attemptId, int questionId, String selectedOption) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("INSERT INTO responses(attempt_id, question_id, selected_option) VALUES (?,?,?) ON CONFLICT (attempt_id, question_id) DO UPDATE SET selected_option = EXCLUDED.selected_option");
            pst.setInt(1, attemptId); pst.setInt(2, questionId); pst.setString(3, selectedOption);
            pst.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateScore(int attemptId, int score) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("UPDATE test_attempts SET score=?, completed_at=NOW() WHERE attempt_id=?");
            pst.setInt(1, score); pst.setInt(2, attemptId);
            pst.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.showAndWait();
    }
}