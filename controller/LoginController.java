package controller;
import db.DBConnection;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    public void handleLogin(String email, String password,
        Stage stage){
            try {
                java.sql.Connection con = DBConnection.getConnection();

                String query = "SELECT * FROM users where email = ? AND password = ?";
                PreparedStatement pst = con.prepareStatement(query);

                pst.setString(1, email);
                pst.setString(2, password);

                ResultSet rs = pst.executeQuery();

                if(rs.next()){
                    String role = rs.getString("role");
                    int userId = rs.getInt("user_id");
                    System.out.println("Login Success");

                    if(role.equals("student")){
                        StudentController sc = new StudentController();
                        sc.showTestScreen(stage, userId);
                    }else{
                        System.out.println("Admin login not implemented yet");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
}
