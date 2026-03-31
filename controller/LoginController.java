package controller;

import db.DBConnection;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

   
    public void handleLogin(String email, String password,
                            Stage stage, Label errorLabel) {
        try {
            Connection con = DBConnection.getConnection();
            if (con == null) {
                errorLabel.setText(" Database connection failed. Contact admin.");
                return;
            }

           
            String query = "SELECT * FROM users WHERE email = ? AND password = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, email);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String role   = rs.getString("role");
                int    userId = rs.getInt("user_id");

                if ("student".equals(role)) {
                    StudentController sc = new StudentController();
                    sc.showTestScreen(stage, userId);
                } else if ("admin".equals(role)) {
                    AdminController ac = new AdminController();
                    ac.showAdminDashboard(stage, userId);
                } else {
                    errorLabel.setText(" Unknown role: " + role);
                }
            } else {
                errorLabel.setText(" Invalid email or password.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText(" Unexpected error: " + e.getMessage());
        }
    }
}