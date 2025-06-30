package com.quiz.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;

public class QuizApplication extends Application {

    private Stage primaryStage;
    private DatabaseManager dbManager;
    private QuizManager quizManager;
    private String playerName;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private List<Question> currentQuiz;
    private Timeline timer;
    private int timeLeft = 30; // 30 seconds per question
    private Label timerLabel;
    private VBox questionContainer;
    private ToggleGroup optionsGroup;
    private Button nextButton;
    private Button exitButton;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.dbManager = new DatabaseManager();
        this.quizManager = new QuizManager(dbManager);

        primaryStage.setTitle("Bangladesh Quiz Application");
        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            handleExit();
        });

        showWelcomeScreen();
        primaryStage.show();
    }

    private void showWelcomeScreen() {
        VBox welcomeBox = new VBox(20);
        welcomeBox.setAlignment(Pos.CENTER);
        welcomeBox.setPadding(new Insets(50));
        welcomeBox.setStyle("-fx-background-color: linear-gradient(to bottom, #4CAF50, #45a049);");

        Label titleLabel = new Label("🇧🇩 Bangladesh Quiz");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);

        Label subLabel = new Label("Test your knowledge about Bangladesh!");
        subLabel.setFont(Font.font("Arial", 16));
        subLabel.setTextFill(Color.WHITE);

        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");
        nameField.setMaxWidth(300);
        nameField.setFont(Font.font(14));

        Button startButton = new Button("Start Quiz");
        startButton.setStyle("-fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20;");
        startButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showAlert("Error", "Please enter your name to start the quiz!");
                return;
            }
            playerName = name;
            startQuiz();
        });

        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-background-color: #666; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16;");
        exitButton.setOnAction(e -> handleExit());

        welcomeBox.getChildren().addAll(titleLabel, subLabel, nameField, startButton, exitButton);

        Scene welcomeScene = new Scene(welcomeBox, 600, 400);
        primaryStage.setScene(welcomeScene);
    }

    private void startQuiz() {
        currentQuiz = quizManager.getRandomQuestions(5);
        if (currentQuiz.isEmpty()) {
            showAlert("Error", "Could not load questions from database!");
            return;
        }

        currentQuestionIndex = 0;
        score = 0;
        showQuestion();
    }

    private void showQuestion() {
        if (currentQuestionIndex >= currentQuiz.size()) {
            showResults();
            return;
        }

        Question question = currentQuiz.get(currentQuestionIndex);

        // Main container
        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(30));
        mainBox.setStyle("-fx-background-color: #f5f5f5;");

        // Header with timer and progress
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(20);

        Label progressLabel = new Label("Question " + (currentQuestionIndex + 1) + " of " + currentQuiz.size());
        progressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        timerLabel = new Label("Time: 30s");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        timerLabel.setTextFill(Color.DARKGREEN);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        exitButton = new Button("Exit Quiz");
        exitButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px;");
        exitButton.setOnAction(e -> handleExit());

        headerBox.getChildren().addAll(progressLabel, spacer, timerLabel, exitButton);

        // Question container
        questionContainer = new VBox(15);
        questionContainer.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label questionLabel = new Label(question.getQuestion());
        questionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        questionLabel.setWrapText(true);

        // Options
        optionsGroup = new ToggleGroup();
        VBox optionsBox = new VBox(10);

        String[] options = {question.getA(), question.getB(), question.getC(), question.getD()};
        String[] optionLabels = {"A", "B", "C", "D"};

        for (int i = 0; i < options.length; i++) {
            RadioButton option = new RadioButton(optionLabels[i] + ". " + options[i]);
            option.setToggleGroup(optionsGroup);
            option.setFont(Font.font("Arial", 14));
            option.setUserData(options[i]);
            optionsBox.getChildren().add(option);
        }

        questionContainer.getChildren().addAll(questionLabel, optionsBox);

        // Navigation buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        nextButton = new Button("Next Question");
        nextButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        nextButton.setOnAction(e -> handleNextQuestion());

        buttonBox.getChildren().add(nextButton);

        mainBox.getChildren().addAll(headerBox, questionContainer, buttonBox);

        Scene questionScene = new Scene(new ScrollPane(mainBox), 700, 500);
        primaryStage.setScene(questionScene);

        startTimer();
    }

    private void startTimer() {
        timeLeft = 30;
        updateTimerLabel();

        if (timer != null) {
            timer.stop();
        }

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            updateTimerLabel();

            if (timeLeft <= 0) {
                timer.stop();
                handleTimeUp();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void updateTimerLabel() {
        timerLabel.setText("Time: " + timeLeft + "s");
        if (timeLeft <= 10) {
            timerLabel.setTextFill(Color.RED);
        } else {
            timerLabel.setTextFill(Color.DARKGREEN);
        }
    }

    private void handleTimeUp() {
        showAlert("Time Up!", "Time's up! Moving to next question.");
        currentQuestionIndex++;
        showQuestion();
    }

    private void handleNextQuestion() {
        if (timer != null) {
            timer.stop();
        }

        Toggle selectedToggle = optionsGroup.getSelectedToggle();
        if (selectedToggle != null) {
            String selectedAnswer = (String) selectedToggle.getUserData();
            String correctAnswer = currentQuiz.get(currentQuestionIndex).getCorrect();

            if (selectedAnswer.equals(correctAnswer)) {
                score++;
            }
        }

        currentQuestionIndex++;
        showQuestion();
    }

    private void showResults() {
        if (timer != null) {
            timer.stop();
        }

        // Save score to database
        quizManager.saveScore(playerName, score);

        VBox resultsBox = new VBox(20);
        resultsBox.setAlignment(Pos.CENTER);
        resultsBox.setPadding(new Insets(50));
        resultsBox.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2);");

        Label resultsTitle = new Label("Quiz Complete!");
        resultsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        resultsTitle.setTextFill(Color.WHITE);

        Label nameLabel = new Label("Player: " + playerName);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.WHITE);

        Label scoreLabel = new Label("Score: " + score + " out of " + currentQuiz.size());
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        scoreLabel.setTextFill(Color.WHITE);

        double percentage = (double) score / currentQuiz.size() * 100;
        Label percentageLabel = new Label(String.format("Percentage: %.1f%%", percentage));
        percentageLabel.setFont(Font.font("Arial", 16));
        percentageLabel.setTextFill(Color.WHITE);

        String performance;
        if (percentage >= 80) performance = "Excellent! 🎉";
        else if (percentage >= 60) performance = "Good job! 👍";
        else if (percentage >= 40) performance = "Not bad! 👌";
        else performance = "Keep trying! 💪";

        Label performanceLabel = new Label(performance);
        performanceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        performanceLabel.setTextFill(Color.YELLOW);

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button playAgainButton = new Button("Play Again");
        playAgainButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20;");
        playAgainButton.setOnAction(e -> showWelcomeScreen());

        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20;");
        exitButton.setOnAction(e -> handleExit());

        buttonBox.getChildren().addAll(playAgainButton, exitButton);

        resultsBox.getChildren().addAll(resultsTitle, nameLabel, scoreLabel, percentageLabel, performanceLabel, buttonBox);

        Scene resultsScene = new Scene(resultsBox, 600, 400);
        primaryStage.setScene(resultsScene);
    }

    private void handleExit() {
        if (timer != null) {
            timer.stop();
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Quiz");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Your progress will be lost if you haven't completed the quiz.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dbManager != null) {
                dbManager.closeConnection();
            }
            Platform.exit();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}