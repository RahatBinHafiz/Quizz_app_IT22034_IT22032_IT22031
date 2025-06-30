# Bangladesh Quiz Application

A JavaFX-based quiz application that tests your knowledge about Bangladesh. The application connects to a MySQL database to fetch questions and store player scores.

## Features

- 🎯 **Random Quiz Generation**: Selects 5 random questions from a pool of 20+ questions
- ⏰ **Timer System**: 30-second timer for each question
- 💾 **Database Integration**: Stores player names and scores in MySQL database
- 🏆 **Score Tracking**: Saves and displays quiz results
- 🚪 **Exit Anytime**: Can terminate or restart the quiz at any point
- 📊 **Performance Feedback**: Shows percentage and performance level
- 🎨 **Modern UI**: Clean, responsive JavaFX interface

## Database Schema

The application uses two main tables:

### Questions Table
```sql
CREATE TABLE questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question TEXT,
    a VARCHAR(255),
    b VARCHAR(255),
    c VARCHAR(255),
    d VARCHAR(255),
    correct VARCHAR(255)
);
```

### Scores Table (Auto-created)
```sql
CREATE TABLE scores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_name VARCHAR(255) NOT NULL,
    score INT NOT NULL,
    total_questions INT NOT NULL,
    date_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Prerequisites

- Java 11 or higher
- JavaFX 19 or higher
- MySQL Server running
- Maven 3.6+ (for building)

## Setup Instructions

### 1. Database Setup
1. Make sure MySQL is running
2. Create the database:
   ```sql
   CREATE DATABASE mysql_rahat;
   USE mysql_rahat;
   ```
3. Run the questions table creation and insert statements (as provided in your original query)

### 2. Configure Database Connection
Edit the `DatabaseManager.java` file and update these constants:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/mysql_rahat";
private static final String DB_USERNAME = "your_username";  // Change this
private static final String DB_PASSWORD = "your_password";  // Change this
```

### 3. Project Structure
Create the following directory structure:
```
quiz-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── quiz/
│   │   │           └── app/
│   │   │               ├── QuizApplication.java
│   │   │               ├── Question.java
│   │   │               ├── DatabaseManager.java
│   │   │               └── QuizManager.java
│   │   └── resources/
│   └── module-info.java
├── pom.xml
└── README.md
```

### 4. Build and Run

#### Using Maven:
```bash
# Clone or create the project directory
cd quiz-app

# Install dependencies and compile
mvn clean compile

# Run the application
mvn javafx:run
```

#### Alternative - Direct Java execution:
```bash
# Compile (make sure JavaFX and MySQL connector are in classpath)
javac --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp mysql-connector-java.jar src/main/java/com/quiz/app/*.java

# Run
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp .:mysql-connector-java.jar com.quiz.app.QuizApplication
```

## Usage

1. **Start the Application**: Run the main class `QuizApplication`
2. **Enter Name**: Type your name in the welcome screen
3. **Take Quiz**: Answer 5 randomly selected questions
4. **Timer**: Each question has a 30-second timer
5. **Navigation**: Use "Next Question" button or let timer expire
6. **Exit**: Click "Exit" button anytime to quit
7. **Results**: View your score and performance at the end
8. **Play Again**: Option to restart with new random questions

## Application Flow

```
Welcome Screen → Enter Name → Start Quiz → Question 1-5 → Results → Play Again/Exit
```

## Key Features Explained

### Timer System
- Each question has a 30-second countdown
- Timer turns red when ≤10 seconds remain
- Auto-advances to next question when time expires

### Random Question Selection
- Selects 5 questions randomly from available pool
- Ensures different quiz experience each time

### Score Management
- Automatically saves player name and score to database
- Calculates percentage and provides performance feedback
- Maintains history of all quiz attempts

### Exit Handling
- Confirmation dialog before exiting
- Proper cleanup of database connections
- Can exit at any point during the quiz

## Troubleshooting

### Common Issues:

1. **Database Connection Failed**
   - Check MySQL server is running
   - Verify database name, username, and password
   - Ensure MySQL Connector/J is in classpath

2. **JavaFX Runtime Error**
   - Make sure JavaFX is properly installed
   - Check module path includes JavaFX libraries
   - Verify Java version compatibility

3. **Questions Not Loading**
   - Verify the questions table exists and has data
   - Check database connection settings
   - Look at console output for SQL errors

### Console Output
The application provides helpful console messages:
- Database connection status
- Number of questions loaded
- Score save confirmations
- Error messages for troubleshooting

## Customization

### Adding More Questions
Simply insert more questions into the database:
```sql
INSERT INTO questions (question, a, b, c, d, correct) VALUES
('Your question?', 'Option A', 'Option B', 'Option C', 'Option D', 'Correct Answer');
```

### Changing Quiz Length
Modify the `startQuiz()` method in `QuizApplication.java`:
```java
currentQuiz = quizManager.getRandomQuestions(10); // Change from 5 to 10
```

### Adjusting Timer
Change the `timeLeft` initial value in the `startTimer()` method:
```java
timeLeft = 60; // Change from 30 to 60 seconds
```

## Contributing

Feel free to enhance the application by:
- Adding more question types
- Implementing difficulty levels
- Adding sound effects
- Creating admin panel for question management
- Adding multiplayer support

## License

This project is created for educational purposes. Feel free to use and modify as needed.
