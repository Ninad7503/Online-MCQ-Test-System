package db;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection(){
        try {
            return DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/exam_db",
                "postgres",
                "password"
            );

            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
