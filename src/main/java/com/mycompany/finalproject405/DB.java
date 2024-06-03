package com.mycompany.finalproject405;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DB {
    // Create a Map with values as ArrayLists
    private static HashMap<String, ArrayList<String>> map = new HashMap<>();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/URLS";
    private static final String USER = "root";
    private static final String PASS = "";

    public static void main(String[] args) {
        // For testing purposes
        DB db = new DB();
        db.insertData();
    }

    public void insertData() {
        try {
            // Load JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish a connection
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);

            // Prepare a statement for insertion
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO project_table (page, extractedUrl) VALUES (?, ?)");

            // Iterate over the map entries
            for (HashMap.Entry<String, ArrayList<String>> entry : map.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                
                // Set parameters in the prepared statement
                for (String value : values) {
                    preparedStatement.setString(1, key);
                    preparedStatement.setString(2, value);
                    
                    // Execute the statement
                    preparedStatement.executeUpdate();
                }
            }

            // Close the connection and resources
            preparedStatement.close();
            connection.close();

            System.out.println("Data inserted successfully!");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, ArrayList<String>> getMap() {
        return map;
    }

    public void setMap(HashMap<String, ArrayList<String>> map) {
        DB.map = map;
    }
}
