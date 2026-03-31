package controller;

import db.DBConnection;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Optional;

public class AdminController {

    private Stage stage;
    private int   adminId;

    public void showAdminDashboard(Stage stage, int adminId) {
        this.stage   = stage;
        this.adminId = adminId;
        buildDashboard();
    }

    private void buildDashboard() {
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #0d0d0d;");

        Label logo = new Label("ADMIN");
        logo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #00ffe7;");
        sidebar.getChildren().add(logo);
        sidebar.getChildren().add(makeSpacer(16));

        StackPane content = new StackPane();
        content.setStyle("-fx-background-color: #0a0a0a;");
        content.setPadding(new Insets(24));

        String[] navItems = {"Question Bank", "Manage Tests", "Results"};
        for (String item : navItems) {
            Button btn = navButton(item);
            sidebar.getChildren().add(btn);
            btn.setOnAction(e -> {
                content.getChildren().clear();
                switch (item) {
                    case "Question Bank" -> content.getChildren().add(buildQuestionBank());
                    case "Manage Tests"  -> content.getChildren().add(buildTestManager());
                    case "Results"       -> content.getChildren().add(buildResults());
                }
            });
        }

        content.getChildren().add(buildQuestionBank());
        HBox root = new HBox(sidebar, content);
        HBox.setHgrow(content, Priority.ALWAYS);
        Scene scene = new Scene(root, 900, 580);
        stage.setScene(scene);
        stage.setTitle("Admin Dashboard");
        stage.setResizable(true);
        stage.show();
    }

    private VBox buildQuestionBank() {
        Label title = sectionTitle("Question Bank");

        TableView<QuestionRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<QuestionRow, Integer> idCol   = intCol("ID",       "id",      60);
        TableColumn<QuestionRow, String>  textCol = strCol("Question", "text",    340);
        TableColumn<QuestionRow, String>  ansCol  = strCol("Answer",   "correct", 70);
        table.getColumns().addAll(idCol, textCol, ansCol);
        styleTable(table);
        refreshQuestions(table);

        TextField qText = tf("Question text");
        TextField optA  = tf("Option A");
        TextField optB  = tf("Option B");
        TextField optC  = tf("Option C");
        TextField optD  = tf("Option D");

        ComboBox<String> answerBox = new ComboBox<>(
            FXCollections.observableArrayList("A", "B", "C", "D"));
        answerBox.setPromptText("Correct Answer");
        answerBox.setStyle(darkComboStyle());

        Button addBtn = actionButton("Add Question", "#00ffe7", "#0a0a0a");
        Button delBtn = actionButton("Delete Selected", "#ff4d4d", "#ffffff");
        Label  status = statusLabel();

        addBtn.setOnAction(e -> {
            if (qText.getText().isBlank() || optA.getText().isBlank() ||
                optB.getText().isBlank() || optC.getText().isBlank() ||
                optD.getText().isBlank() || answerBox.getValue() == null) {
                status.setText("Fill in all fields.");
                return;
            }
            addQuestion(qText.getText(), optA.getText(), optB.getText(),
                        optC.getText(), optD.getText(), answerBox.getValue(), status);
            refreshQuestions(table);
            qText.clear(); optA.clear(); optB.clear(); optC.clear(); optD.clear();
            answerBox.setValue(null);
        });

        delBtn.setOnAction(e -> {
            QuestionRow sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { status.setText("Select a question first."); return; }
            Optional<ButtonType> c = confirm("Delete question ID " + sel.getId() + "?");
            if (c.isPresent() && c.get() == ButtonType.OK) {
                deleteQuestion(sel.getId(), status);
                refreshQuestions(table);
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10);
        form.add(qText, 0, 0, 4, 1);
        form.add(optA,  0, 1); form.add(optB, 1, 1);
        form.add(optC,  2, 1); form.add(optD, 3, 1);
        form.add(answerBox, 0, 2);
        form.add(addBtn,    1, 2);
        form.add(delBtn,    2, 2);
        form.add(status,    3, 2);

        VBox layout = new VBox(14, title, table, form);
        layout.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(table, Priority.ALWAYS);
        return layout;
    }

    private VBox buildTestManager() {
        Label title = sectionTitle("Manage Tests");

        ListView<String> testList = new ListView<>();
        testList.setStyle("-fx-background-color: #111111; -fx-border-color: #222222;");
        testList.setPrefHeight(120);
        refreshTestList(testList);

        TextField testNameField = tf("New test name");
        Button createTestBtn = actionButton("Create Test", "#ffe000", "#0a0a0a");
        Label  createStatus  = statusLabel();

        createTestBtn.setOnAction(e -> {
            if (testNameField.getText().isBlank()) {
                createStatus.setText("Enter a test name."); return;
            }
            createTest(testNameField.getText(), createStatus);
            testNameField.clear();
            refreshTestList(testList);
        });

        Separator sep = new Separator();

        Label assignTitle = new Label("Assign questions to a test");
        assignTitle.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");

        TableView<QuestionRow> qTable = new TableView<>();
        qTable.setStyle("-fx-background-color: #111111; -fx-border-color: #222222;");
        qTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        qTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<QuestionRow, Integer> qIdCol   = intCol("ID",       "id",   50);
        TableColumn<QuestionRow, String>  qTextCol = strCol("Question", "text", 380);
        qTable.getColumns().addAll(qIdCol, qTextCol);
        styleTable(qTable);
        refreshQuestions(qTable);

        ComboBox<TestItem> testCombo = new ComboBox<>();
        testCombo.setPromptText("Select Test");
        testCombo.setStyle(darkComboStyle());
        refreshTestCombo(testCombo);

        Button assignBtn    = actionButton("Assign Selected", "#00ffe7", "#0a0a0a");
        Button removeBtn    = actionButton("Remove Selected", "#ff4d4d", "#ffffff");
        Label  assignStatus = statusLabel();

        assignBtn.setOnAction(e -> {
            TestItem test = testCombo.getValue();
            if (test == null) { assignStatus.setText("Select a test."); return; }
            ObservableList<QuestionRow> sel = qTable.getSelectionModel().getSelectedItems();
            if (sel.isEmpty()) { assignStatus.setText("Select at least one question."); return; }
            int added = 0;
            for (QuestionRow qr : sel) if (assignQuestionToTest(test.id, qr.getId())) added++;
            assignStatus.setText("Assigned " + added + " question(s).");
        });

        removeBtn.setOnAction(e -> {
            TestItem test = testCombo.getValue();
            if (test == null) { assignStatus.setText("Select a test."); return; }
            ObservableList<QuestionRow> sel = qTable.getSelectionModel().getSelectedItems();
            if (sel.isEmpty()) { assignStatus.setText("Select at least one question."); return; }
            int removed = 0;
            for (QuestionRow qr : sel) if (removeQuestionFromTest(test.id, qr.getId())) removed++;
            assignStatus.setText("Removed " + removed + " question(s).");
        });

        HBox testBar   = new HBox(10, testNameField, createTestBtn, createStatus);
        testBar.setAlignment(Pos.CENTER_LEFT);
        HBox assignBar = new HBox(10, testCombo, assignBtn, removeBtn, assignStatus);
        assignBar.setAlignment(Pos.CENTER_LEFT);

        VBox layout = new VBox(12, title, testList, testBar, sep, assignTitle, qTable, assignBar);
        VBox.setVgrow(qTable, Priority.ALWAYS);
        layout.setAlignment(Pos.TOP_LEFT);
        return layout;
    }

    private VBox buildResults() {
        Label title = sectionTitle("Student Results");

        TableView<ResultRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ResultRow, Integer> attemptCol = intCol("Attempt", "attemptId",   70);
        TableColumn<ResultRow, String>  studentCol = strCol("Student", "studentName", 200);
        TableColumn<ResultRow, Integer> scoreCol   = intCol("Score",   "score",       70);
        TableColumn<ResultRow, String>  dateCol    = strCol("Date",    "date",        200);

        table.getColumns().addAll(attemptCol, studentCol, scoreCol, dateCol);
        styleTable(table);
        refreshResults(table);

        Button refreshBtn = actionButton("Refresh", "#888888", "#ffffff");
        refreshBtn.setOnAction(e -> refreshResults(table));

        VBox layout = new VBox(14, title, table, refreshBtn);
        VBox.setVgrow(table, Priority.ALWAYS);
        layout.setAlignment(Pos.TOP_LEFT);
        return layout;
    }

    private void addQuestion(String text, String a, String b, String c, String d,
                              String correct, Label status) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO questions(question_text,option_a,option_b,option_c,option_d,correct_option) VALUES(?,?,?,?,?,?)");
            pst.setString(1, text); pst.setString(2, a); pst.setString(3, b);
            pst.setString(4, c);    pst.setString(5, d); pst.setString(6, correct);
            pst.executeUpdate();
            status.setText("Question added.");
        } catch (Exception e) { e.printStackTrace(); status.setText("Error: " + e.getMessage()); }
    }

    private void deleteQuestion(int id, Label status) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement p1 = con.prepareStatement("DELETE FROM test_questions WHERE question_id=?");
            p1.setInt(1, id); p1.executeUpdate();
            PreparedStatement p2 = con.prepareStatement("DELETE FROM questions WHERE question_id=?");
            p2.setInt(1, id); p2.executeUpdate();
            status.setText("Deleted.");
        } catch (Exception e) { e.printStackTrace(); status.setText("Error: " + e.getMessage()); }
    }

    private void createTest(String name, Label status) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement("INSERT INTO tests(test_name) VALUES(?)");
            pst.setString(1, name); pst.executeUpdate();
            status.setText("Test created.");
        } catch (Exception e) { e.printStackTrace(); status.setText("Error: " + e.getMessage()); }
    }

    private boolean assignQuestionToTest(int testId, int questionId) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO test_questions(test_id,question_id) VALUES(?,?) ON CONFLICT DO NOTHING");
            pst.setInt(1, testId); pst.setInt(2, questionId);
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private boolean removeQuestionFromTest(int testId, int questionId) {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement pst = con.prepareStatement(
                "DELETE FROM test_questions WHERE test_id=? AND question_id=?");
            pst.setInt(1, testId); pst.setInt(2, questionId);
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private void refreshQuestions(TableView<QuestionRow> table) {
        ObservableList<QuestionRow> data = FXCollections.observableArrayList();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT question_id, question_text, correct_option FROM questions ORDER BY question_id")) {
            while (rs.next())
                data.add(new QuestionRow(rs.getInt(1), rs.getString(2), rs.getString(3)));
        } catch (Exception e) { e.printStackTrace(); }
        table.setItems(data);
    }

    private void refreshTestList(ListView<String> list) {
        ObservableList<String> data = FXCollections.observableArrayList();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT t.test_id, t.test_name, COUNT(tq.question_id) " +
                 "FROM tests t LEFT JOIN test_questions tq USING(test_id) " +
                 "GROUP BY t.test_id, t.test_name ORDER BY t.test_id")) {
            while (rs.next())
                data.add("Test #" + rs.getInt(1) + " - " + rs.getString(2) +
                         "  [" + rs.getInt(3) + " questions]");
        } catch (Exception e) { e.printStackTrace(); }
        list.setItems(data);
    }

    private void refreshTestCombo(ComboBox<TestItem> combo) {
        ObservableList<TestItem> data = FXCollections.observableArrayList();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT test_id, test_name FROM tests ORDER BY test_id")) {
            while (rs.next()) data.add(new TestItem(rs.getInt(1), rs.getString(2)));
        } catch (Exception e) { e.printStackTrace(); }
        combo.setItems(data);
    }

    private void refreshResults(TableView<ResultRow> table) {
        ObservableList<ResultRow> data = FXCollections.observableArrayList();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT ta.attempt_id, u.full_name, ta.score, ta.attempted_at " +
                 "FROM test_attempts ta JOIN users u ON ta.student_id = u.user_id " +
                 "ORDER BY ta.attempted_at DESC")) {
            while (rs.next())
                data.add(new ResultRow(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4)));
        } catch (Exception e) { e.printStackTrace(); }
        table.setItems(data);
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#00ffe7;");
        return l;
    }

    private Button navButton(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        String normal = "-fx-background-color:transparent;-fx-text-fill:#aaaaaa;-fx-font-size:13px;-fx-alignment:CENTER_LEFT;-fx-cursor:hand;-fx-padding:10 12;";
        String hover  = "-fx-background-color:#1a1a1a;-fx-text-fill:#00ffe7;-fx-font-size:13px;-fx-alignment:CENTER_LEFT;-fx-cursor:hand;-fx-padding:10 12;";
        b.setStyle(normal);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e  -> b.setStyle(normal));
        return b;
    }

    private Button actionButton(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + fg + ";-fx-font-weight:bold;-fx-background-radius:7;-fx-cursor:hand;-fx-padding:7 16;");
        return b;
    }

    private TextField tf(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-color:#1e1e1e;-fx-text-fill:#e0e0e0;-fx-prompt-text-fill:#555555;-fx-border-color:#333333;-fx-border-width:1;-fx-border-radius:8;-fx-background-radius:8;-fx-padding:9 12;-fx-font-size:13px;");
        return f;
    }

    private Label statusLabel() {
        Label l = new Label("");
        l.setStyle("-fx-text-fill:#00ffe7;-fx-font-size:12px;");
        return l;
    }

    private String darkComboStyle() {
        return "-fx-background-color:#1e1e1e;-fx-text-fill:#e0e0e0;-fx-border-color:#333333;-fx-border-radius:7;-fx-background-radius:7;";
    }

    private <T> void styleTable(TableView<T> table) {
        table.setStyle("-fx-background-color:#111111;-fx-border-color:#222222;-fx-table-cell-border-color:#1a1a1a;");
    }

    private Optional<ButtonType> confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm"); a.setHeaderText(null); a.setContentText(msg);
        return a.showAndWait();
    }

    private Region makeSpacer(double height) {
        Region r = new Region(); r.setMinHeight(height); return r;
    }

    private <T> TableColumn<T, Integer> intCol(String title, String prop, int w) {
        TableColumn<T, Integer> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(w);
        return c;
    }

    private <T> TableColumn<T, String> strCol(String title, String prop, int w) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(w);
        return c;
    }

    public static class QuestionRow {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty  text;
        private final SimpleStringProperty  correct;
        public QuestionRow(int id, String text, String correct) {
            this.id      = new SimpleIntegerProperty(id);
            this.text    = new SimpleStringProperty(text);
            this.correct = new SimpleStringProperty(correct);
        }
        public int    getId()      { return id.get(); }
        public String getText()    { return text.get(); }
        public String getCorrect() { return correct.get(); }
    }

    public static class ResultRow {
        private final SimpleIntegerProperty attemptId;
        private final SimpleStringProperty  studentName;
        private final SimpleIntegerProperty score;
        private final SimpleStringProperty  date;
        public ResultRow(int aid, String name, int score, String date) {
            this.attemptId   = new SimpleIntegerProperty(aid);
            this.studentName = new SimpleStringProperty(name);
            this.score       = new SimpleIntegerProperty(score);
            this.date        = new SimpleStringProperty(date);
        }
        public int    getAttemptId()   { return attemptId.get(); }
        public String getStudentName() { return studentName.get(); }
        public int    getScore()       { return score.get(); }
        public String getDate()        { return date.get(); }
    }

    public static class TestItem {
        final int id; final String name;
        TestItem(int id, String name) { this.id = id; this.name = name; }
        public String toString() { return "#" + id + " - " + name; }
    }
}