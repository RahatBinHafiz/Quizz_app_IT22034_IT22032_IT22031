package com.quiz.app;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mysql_rahat";
    private static final String DB_USERNAME = "root";  // Change this to your MySQL username
    private static final String DB_PASSWORD = "052312";      // Change this to your MySQL password

    private Connection connection;

    public DatabaseManager() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Database connected successfully!");

            // Create scores table if it doesn't exist
            createScoresTable();

        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            System.err.println("Make sure to add MySQL Connector/J to your classpath");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            System.err.println("Make sure MySQL is running and credentials are correct");
        }
    }

    private void createScoresTable() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS scores (
                id INT AUTO_INCREMENT PRIMARY KEY,
                player_name VARCHAR(255) NOT NULL,
                score INT NOT NULL,
                total_questions INT NOT NULL,
                date_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("Scores table ready!");
        } catch (SQLException e) {
            System.err.println("Error creating scores table: " + e.getMessage());
        }
    }

    public List<com.quiz.app.Question> getAllQuestions() {
        List<com.quiz.app.Question> questions = new ArrayList<>();
        String query = "SELECT * FROM questions";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                com.quiz.app.Question question = new Question(
                        rs.getInt("id"),
                        rs.getString("question"),
                        rs.getString("a"),
                        rs.getString("b"),
                        rs.getString("c"),
                        rs.getString("d"),
                        rs.getString("correct")
                );
                questions.add(question);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching questions: " + e.getMessage());
        }

        return questions;
    }

    public List<com.quiz.app.Question> getRandomQuestions(int count) {
        List<com.quiz.app.Question> questions = new ArrayList<>();
        String query = "SELECT * FROM questions ORDER BY RAND() LIMIT ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, count);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                com.quiz.app.Question question = new Question(
                        rs.getInt("id"),
                        rs.getString("question"),
                        rs.getString("a"),
                        rs.getString("b"),
                        rs.getString("c"),
                        rs.getString("d"),
                        rs.getString("correct")
                );
                questions.add(question);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching random questions: " + e.getMessage());
        }

        return questions;
    }

    public boolean saveScore(String playerName, int score, int totalQuestions) {
        String insertSQL = "INSERT INTO scores (player_name, score, total_questions) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, playerName);
            pstmt.setInt(2, score);
            pstmt.setInt(3, totalQuestions);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Score saved successfully for " + playerName);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error saving score: " + e.getMessage());
        }

        return false;
    }

    public List<ScoreRecord> getTopScores(int limit) {
        List<ScoreRecord> scores = new ArrayList<>();
        String query = """
            SELECT player_name, score, total_questions, date_played 
            FROM scores 
            ORDER BY score DESC, date_played DESC 
            LIMIT ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ScoreRecord record = new ScoreRecord(
                        rs.getString("player_name"),
                        rs.getInt("score"),
                        rs.getInt("total_questions"),
                        rs.getTimestamp("date_played")
                );
                scores.add(record);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching top scores: " + e.getMessage());
        }

        return scores;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    // Inner class for score records
    public static class ScoreRecord {
        private String playerName;
        private int score;
        private int totalQuestions;
        private Timestamp datePlayed;

        public ScoreRecord(String playerName, int score, int totalQuestions, Timestamp datePlayed) {
            this.playerName = playerName;
            this.score = score;
            this.totalQuestions = totalQuestions;
            this.datePlayed = datePlayed;
        }

        // Getters
        public String getPlayerName() { return playerName; }
        public int getScore() { return score; }
        public int getTotalQuestions() { return totalQuestions; }
        public Timestamp getDatePlayed() { return datePlayed; }

        public double getPercentage() {
            return (double) score / totalQuestions * 100;
        }

        @Override
        public String toString() {
            return String.format("%s: %d/%d (%.1f%%) - %s",
                    playerName, score, totalQuestions, getPercentage(), datePlayed);
        }
    }
}