package com.quiz.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizManager {
    private com.quiz.app.DatabaseManager dbManager;
    private List<com.quiz.app.Question> allQuestions;

    public QuizManager(com.quiz.app.DatabaseManager dbManager) {
        this.dbManager = dbManager;
        loadQuestions();
    }

    private void loadQuestions() {
        if (dbManager.isConnected()) {
            allQuestions = dbManager.getAllQuestions();
            System.out.println("Loaded " + allQuestions.size() + " questions from database");
        } else {
            System.err.println("Database not connected, cannot load questions");
            allQuestions = new ArrayList<>();
        }
    }

    public List<com.quiz.app.Question> getRandomQuestions(int count) {
        if (allQuestions.isEmpty()) {
            System.err.println("No questions available");
            return new ArrayList<>();
        }

        // If we have fewer questions than requested, return all available
        if (allQuestions.size() <= count) {
            List<com.quiz.app.Question> shuffled = new ArrayList<>(allQuestions);
            Collections.shuffle(shuffled);
            return shuffled;
        }

        // Create a copy and shuffle to get random questions
        List<com.quiz.app.Question> shuffled = new ArrayList<>(allQuestions);
        Collections.shuffle(shuffled);

        // Return the first 'count' questions
        return shuffled.subList(0, count);
    }

    public <Question> List<Question> getAllQuestions() {
        return (List<Question>) new ArrayList<>(allQuestions);
    }

    public com.quiz.app.Question getQuestionById(int id) {
        return allQuestions.stream()
                .filter(q -> q.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public boolean saveScore(String playerName, int score) {
        return dbManager.saveScore(playerName, score, 5); // 5 is the total questions per quiz
    }

    public List<com.quiz.app.DatabaseManager.ScoreRecord> getTopScores(int limit) {
        return dbManager.getTopScores(limit);
    }

    public int getTotalQuestionsCount() {
        return allQuestions.size();
    }

    public boolean isQuizAvailable() {
        return !allQuestions.isEmpty();
    }

    // Method to refresh questions from database
    public void refreshQuestions() {
        loadQuestions();
    }

    // Method to validate answer
    public boolean isAnswerCorrect(com.quiz.app.Question question, String selectedAnswer) {
        if (question == null || selectedAnswer == null) {
            return false;
        }
        return question.getCorrect().trim().equalsIgnoreCase(selectedAnswer.trim());
    }

    // Method to get question difficulty level (based on some criteria)
    public String getQuestionDifficulty(com.quiz.app.Question question) {
        // Simple difficulty assessment based on question length
        if (question.getQuestion().length() < 50) {
            return "Easy";
        } else if (question.getQuestion().length() < 100) {
            return "Medium";
        } else {
            return "Hard";
        }
    }

    // Method to get statistics
    public QuizStatistics getQuizStatistics() {
        return new QuizStatistics(
                allQuestions.size(),
                (int) allQuestions.stream().filter(q -> getQuestionDifficulty(q).equals("Easy")).count(),
                (int) allQuestions.stream().filter(q -> getQuestionDifficulty(q).equals("Medium")).count(),
                (int) allQuestions.stream().filter(q -> getQuestionDifficulty(q).equals("Hard")).count()
        );
    }

    // Inner class for quiz statistics
    public static class QuizStatistics {
        private int totalQuestions;
        private int easyQuestions;
        private int mediumQuestions;
        private int hardQuestions;

        public QuizStatistics(int totalQuestions, int easyQuestions, int mediumQuestions, int hardQuestions) {
            this.totalQuestions = totalQuestions;
            this.easyQuestions = easyQuestions;
            this.mediumQuestions = mediumQuestions;
            this.hardQuestions = hardQuestions;
        }

        // Getters
        public int getTotalQuestions() { return totalQuestions; }
        public int getEasyQuestions() { return easyQuestions; }
        public int getMediumQuestions() { return mediumQuestions; }
        public int getHardQuestions() { return hardQuestions; }

        @Override
        public String toString() {
            return String.format("Total: %d, Easy: %d, Medium: %d, Hard: %d",
                    totalQuestions, easyQuestions, mediumQuestions, hardQuestions);
        }
    }
}