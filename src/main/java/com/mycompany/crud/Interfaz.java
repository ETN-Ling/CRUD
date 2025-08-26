package com.mycompany.crud;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.util.*;

public class Interfaz extends Application {

    private BorderPane root;
    private VBox menuBox;
    private StackPane contentPane;

    @Override
    public void start(Stage primaryStage) {
        root = new BorderPane();
        contentPane = new StackPane();
        root.setCenter(contentPane);

        crearMenu();

        Scene scene = new Scene(root, 900, 500);
        primaryStage.setTitle("Agenda");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void crearMenu() {
        menuBox = new VBox(15);
        menuBox.setPadding(new Insets(20));
        menuBox.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label("Elija una opción:");
        Button btnListar = new Button("Listar personas");
        Button btnInsertar = new Button("Insertar persona");
        Button btnEliminar = new Button("Eliminar persona");
        Button btnModificar = new Button("Modificar persona");
        Button btnSalir = new Button("Salir");

        btnListar.setOnAction(e -> mostrarListar());
        btnInsertar.setOnAction(e -> mostrarInsertar());
        btnEliminar.setOnAction(e -> mostrarEliminar());
        btnModificar.setOnAction(e -> mostrarModificar());
        btnSalir.setOnAction(e -> System.exit(0));

        menuBox.getChildren().addAll(lbl, btnListar, btnInsertar, btnEliminar, btnModificar, btnSalir);
        root.setLeft(menuBox);
    }

    // ------------------- FORMULARIOS -------------------

    private void mostrarListar() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);

        Label titulo = new Label("Listado de personas");

        TableView<Persona> table = new TableView<>();

        TableColumn<Persona, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());

        TableColumn<Persona, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));

        TableColumn<Persona, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDireccion()));

        TableColumn<Persona, String> colTelefonos = new TableColumn<>("Teléfonos");
        colTelefonos.setCellValueFactory(data -> {
            String telefonos = String.join(", ", data.getValue().getTelefonos());
            return new javafx.beans.property.SimpleStringProperty(telefonos);
        });

        table.getColumns().addAll(colId, colNombre, colDireccion, colTelefonos);

        // Cargar datos desde AgendaDB
        List<Persona> personas = AgendaDB.obtenerPersonas();
        table.getItems().addAll(personas);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        box.getChildren().addAll(titulo, table);
        contentPane.getChildren().setAll(box);
    }

    private void mostrarInsertar() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);

        Label titulo = new Label("Insertar persona");
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre");

        // varias direcciones
        VBox direccionesBox = new VBox(5);
        agregarCampoDireccion(direccionesBox);

        Button btnAgregarDir = new Button("Agregar dirección");
        btnAgregarDir.setOnAction(e -> agregarCampoDireccion(direccionesBox));

        // varios teléfonos
        VBox telefonosBox = new VBox(5);
        agregarCampoTelefono(telefonosBox);

        Button btnAgregarTel = new Button("Agregar teléfono");
        btnAgregarTel.setOnAction(e -> agregarCampoTelefono(telefonosBox));

        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> {
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                mostrarAlerta("Error", "Debe ingresar un nombre.");
                return;
            }

            List<String> direcciones = new ArrayList<>();
            for (javafx.scene.Node node : direccionesBox.getChildren()) {
                if (node instanceof TextField) {
                    String dir = ((TextField) node).getText().trim();
                    if (!dir.isEmpty()) direcciones.add(dir);
                }
            }

            List<String> telefonos = new ArrayList<>();
            for (javafx.scene.Node node : telefonosBox.getChildren()) {
                if (node instanceof TextField) {
                    String tel = ((TextField) node).getText().trim();
                    if (!tel.isEmpty()) telefonos.add(tel);
                }
            }

            if (direcciones.isEmpty()) {
                mostrarAlerta("Error", "Debe ingresar al menos una dirección.");
                return;
            }

            if (telefonos.isEmpty()) {
                mostrarAlerta("Error", "Debe ingresar al menos un teléfono.");
                return;
            }

            AgendaDB.insertarPersona(nombre, direcciones, telefonos);
            mostrarAlerta("Éxito", "Persona insertada correctamente.");
            contentPane.getChildren().clear();
        });
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> {
        contentPane.getChildren().clear();
        });
        
        box.getChildren().addAll(
                titulo,
                new Label("Nombre:"), txtNombre,
                new Label("Direcciones:"), direccionesBox, btnAgregarDir,
                new Label("Teléfonos:"), telefonosBox, btnAgregarTel,
                new HBox(10, btnGuardar, btnCancelar)
        );

        contentPane.getChildren().setAll(box);
    }

    private void mostrarEliminar() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);

        Label titulo = new Label("Eliminar persona");
        TextField txtId = new TextField();
        txtId.setPromptText("ID de la persona a eliminar");
    
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setStyle("-fx-background-color: red; -fx-text-fill: white;");

        btnEliminar.setOnAction(e -> {
            String input = txtId.getText().trim();
            if (input.isEmpty()) {
                mostrarAlerta("Error", "Debe ingresar un ID.");
                return;
            }

            try {
                int id = Integer.parseInt(input);

                // Confirmación (opcional)
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmar eliminación");
                confirm.setHeaderText(null);
                confirm.setContentText("¿Está seguro de eliminar la persona con ID " + id + "?");

                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    boolean eliminada = AgendaDB.eliminarPersona(id);
                    if (eliminada) {
                        mostrarAlerta("Éxito", "Persona eliminada correctamente.");
                        contentPane.getChildren().clear(); // Limpiar panel
                    } else {
                        mostrarAlerta("No encontrado", "No existe persona con ese ID.");
                    }
                }

            } catch (NumberFormatException ex) {
                mostrarAlerta("Error", "El ID debe ser un número válido.");
            }
        });

        box.getChildren().addAll(titulo, txtId, btnEliminar);
        contentPane.getChildren().setAll(box);
    }

    private void mostrarModificar() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);

        Label titulo = new Label("Modificar persona");
        titulo.setStyle("-fx-background-color: orange; -fx-padding: 5;");

        TextField txtId = new TextField();
        txtId.setPromptText("ID de la persona");

        Button btnVerificar = new Button("Verificar ID");

        HBox opciones = new HBox(10);
        opciones.setAlignment(Pos.CENTER);
        Button btnDatos = new Button("Datos personales");
        Button btnTelefonos = new Button("Teléfonos");
        opciones.getChildren().addAll(btnDatos, btnTelefonos);
        opciones.setDisable(true);

        btnVerificar.setOnAction(e -> {
    String input = txtId.getText().trim();
    if (input.isEmpty()) {
        mostrarAlerta("Error", "Debe ingresar un ID.");
        return;
    }

    int id;
    try {
        id = Integer.parseInt(input);
    } catch (NumberFormatException ex) {
        mostrarAlerta("Error", "El ID debe ser un número válido.");
        return;
    }

    Persona persona = AgendaDB.obtenerPersona(id);
    if (persona == null) {
        mostrarAlerta("No encontrado", "No existe persona con ID " + id);
        opciones.setDisable(true);
        return;
    }

    opciones.setDisable(false);

    btnDatos.setOnAction(ev -> {
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);

        Label currentName = new Label("Nombre actual: " + persona.getNombre());
        TextField newName = new TextField();
        newName.setPromptText("Nombre nuevo");

        Label currentDir = new Label("Dirección actual: " + persona.getDireccion());
        VBox newDirsBox = new VBox(5);
        agregarCampoDireccion(newDirsBox);
        Button btnAddDir = new Button("Agregar dirección");
        btnAddDir.setOnAction(e2 -> agregarCampoDireccion(newDirsBox));

        Button btnSave = new Button("Guardar cambios");
        btnSave.setOnAction(e2 -> {
            String nombreNuevo = newName.getText().trim();
            List<String> direccionesNuevas = new ArrayList<>();
            for (Node node : newDirsBox.getChildren()) {
                if (node instanceof TextField) {
                    String dir = ((TextField) node).getText().trim();
                    if (!dir.isEmpty()) direccionesNuevas.add(dir);
                }
            }

            if (nombreNuevo.isEmpty() && direccionesNuevas.isEmpty()) {
                mostrarAlerta("Error", "Debe ingresar nombre o dirección(s) nuevas.");
                return;
            }

            String nombreFinal = nombreNuevo.isEmpty() ? persona.getNombre() : nombreNuevo;
            String dirFinal = direccionesNuevas.isEmpty() ? persona.getDireccion() : String.join(", ", direccionesNuevas);

            AgendaDB.modificarDatosPersona(id, nombreFinal, dirFinal);
            mostrarAlerta("Éxito", "Datos de persona actualizados.");
            contentPane.getChildren().clear();
        });

        form.getChildren().addAll(
            new Label("Modificar datos personales"),
            currentName, newName,
            new Label("Dirección(es) nueva(s):"), newDirsBox, btnAddDir,
            btnSave
        );
        contentPane.getChildren().setAll(form);
    });

    btnTelefonos.setOnAction(ev -> {
        VBox formTel = new VBox(10);
        formTel.setPadding(new Insets(20));
        formTel.setAlignment(Pos.CENTER);

        Label currentTel = new Label("Teléfonos actuales: " + String.join(", ", persona.getTelefonos()));
        VBox newTelBox = new VBox(5);
        agregarCampoTelefono(newTelBox);
        Button btnAddTel = new Button("Agregar teléfono");
        btnAddTel.setOnAction(e2 -> agregarCampoTelefono(newTelBox));

        Button btnSaveTel = new Button("Guardar teléfonos");
        btnSaveTel.setOnAction(e2 -> {
            List<String> nuevosTel = new ArrayList<>();
            for (Node node : newTelBox.getChildren()) {
                if (node instanceof TextField) {
                    String tel = ((TextField) node).getText().trim();
                    if (!tel.isEmpty()) nuevosTel.add(tel);
                }
            }
            if (nuevosTel.isEmpty()) {
                mostrarAlerta("Error", "Debe ingresar al menos un teléfono nuevo.");
                return;
            }
            AgendaDB.modificarTelefonos(id, nuevosTel);
            mostrarAlerta("Éxito", "Teléfonos actualizados.");
            contentPane.getChildren().clear();
        });

        formTel.getChildren().addAll(
            new Label("Modificar teléfonos"),
            currentTel,
            newTelBox, btnAddTel,
            btnSaveTel
        );
        contentPane.getChildren().setAll(formTel);
    });
});

        box.getChildren().addAll(titulo, new Label("ID de la persona:"), txtId, btnVerificar,
                new Label("¿Qué desea modificar?"), opciones);
        contentPane.getChildren().setAll(box);
    }

    // ------------------- HELPERS -------------------

    private void agregarCampoDireccion(VBox direccionesBox) {
        TextField campo = new TextField();
        campo.setPromptText("Dirección");
        direccionesBox.getChildren().add(campo);
    }

    private void agregarCampoTelefono(VBox telefonosBox) {
        TextField campo = new TextField();
        campo.setPromptText("Teléfono");
        telefonosBox.getChildren().add(campo);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}