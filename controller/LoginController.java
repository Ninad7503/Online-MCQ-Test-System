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

                System.out.println("Login button clicked!");
                java.sql.Connection con = DBConnection.getConnection();
                if (con == null) {
                    System.out.println("DB CONNECTION FAILED ");
                    return;
                }
                System.out.println("Email: " + email);
                System.out.println("Password: " + password);
                

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
                    }else if(role.equals("admin")) {
                        System.out.println("Admin login success");
                    }else{
                        System.out.println("Admin login not implemented yet");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
}
