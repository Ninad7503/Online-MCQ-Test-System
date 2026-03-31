package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

   
    private static final String URL  = "jdbc:postgresql://localhost:5432/Online_MCQ_Test_Management";
    private static final String USER = "ninadshinde";
    private static final String PASS = "";          //  ^password set up 
    

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] PostgreSQL JDBC driver not found. " +
                               "Add postgresql-xx.jar to your classpath.");
            e.printStackTrace();
        }
    }

  
    public static Connection getConnection() {
        try {
            Connection con = DriverManager.getConnection(URL, USER, PASS);
            return con;
        } catch (SQLException e) {
            System.err.println("[DBConnection] Failed to connect: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    
    public static void testConnection() {
        try (Connection con = getConnection()) {
            if (con != null && !con.isClosed()) {
                System.out.println(" Connection OK");
            } else {
                System.out.println(" Connection FAILED");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}