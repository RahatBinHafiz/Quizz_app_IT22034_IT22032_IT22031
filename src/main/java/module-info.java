module com.quiz.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens com.quiz.app to javafx.fxml;
    exports com.quiz.app;
}
