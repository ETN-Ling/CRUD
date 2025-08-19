package com.mycompany.crud;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Interfaz extends Application {
    @Override
    public void start(Stage stage) {
        Label label = new Label("¡Hola desde JavaFX!");
        Scene scene = new Scene(label, 400, 200);
        stage.setTitle("Mi Agenda");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
