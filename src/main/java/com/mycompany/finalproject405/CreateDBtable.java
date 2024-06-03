package com.mycompany.finalproject405;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDBtable {

    static final String DB_URL = "jdbc:mysql://localhost:3306/";
    static final String USER = "root";
    static final String PASS = "";

    public static void main(String[] args) {
        // Open a connection
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); Statement stmt = conn.createStatement();) {
            // create db
            String sqlDB = "CREATE DATABASE IF NOT EXISTS URLS";
            stmt.executeUpdate(sqlDB);
            System.out.println("Database created successfully...");

            // Switch to the created database
            stmt.executeUpdate("USE URLS");

            // create table
            String sql = "CREATE TABLE IF NOT EXISTS project_table "
                    + "(uid INTEGER not NULL AUTO_INCREMENT, "
                    + " date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + " page TEXT, "
                    + " extractedUrl TEXT, "
                    + " PRIMARY KEY ( uid ))";

            stmt.executeUpdate(sql);
            System.out.println("Created table in given database...");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
