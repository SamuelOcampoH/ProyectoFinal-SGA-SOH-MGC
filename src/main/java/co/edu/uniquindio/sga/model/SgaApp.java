package co.edu.uniquindio.sga.model;

import co.edu.uniquindio.sga.model.*;
import co.edu.uniquindio.sga.service.AlojamientoSGA;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SgaApp extends Application {
    private final AlojamientoSGA sga = new AlojamientoSGA();
    private Stage primaryStage;
    private Autenticable usuarioActual;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("SGA - Aparta-Suites UQ (Salento, Quindío)");
        mostrarLogin();
        primaryStage.show();
    }

    private void mostrarLogin() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        Label titulo = new Label("Aparta-Suites UQ — Sistema de Gestión de Alojamiento");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label sub = new Label("Usuarios demo: Admin (1001 / admin123) | Recep (2001 / recep123) | Huésped (109491 / 1234)");
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");

        TextField txtUser = new TextField();
        txtUser.setPromptText("Documento / Usuario");
        txtUser.setMaxWidth(280);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Contraseña");
        txtPass.setMaxWidth(280);

        Button btnIngresar = new Button("Iniciar Sesión");
        btnIngresar.setMaxWidth(280);

        Button btnRegistrar = new Button("Registrarse como Huésped");
        btnRegistrar.setMaxWidth(280);

        btnIngresar.setOnAction(e -> {
            Autenticable auth = sga.iniciarSesion(txtUser.getText().trim(), txtPass.getText());
            if (auth == null) {
                alerta(Alert.AlertType.ERROR, "Acceso denegado", "Credenciales inválidas o empleado inactivo.");
            } else {
                usuarioActual = auth;
                mostrarPanelPrincipal();
            }
        });

        btnRegistrar.setOnAction(e -> mostrarDialogoRegistroHuesped());

        root.getChildren().addAll(titulo, sub, txtUser, txtPass, btnIngresar, btnRegistrar);
        primaryStage.setScene(new Scene(root, 860, 560));
    }

    private void mostrarDialogoRegistroHuesped() {
        Dialog<Huesped> dialog = new Dialog<>();
        dialog.setTitle("Registro de Nuevo Huésped");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField nombre = new TextField();
        TextField doc = new TextField();
        DatePicker fnac = new DatePicker(LocalDate.of(2000, 1, 1));
        TextField tel = new TextField();
        TextField correo = new TextField();
        PasswordField pass = new PasswordField();

        grid.addRow(0, new Label("Nombre:"), nombre);
        grid.addRow(1, new Label("Documento:"), doc);
        grid.addRow(2, new Label("Fecha Nacimiento:"), fnac);
        grid.addRow(3, new Label("Teléfono:"), tel);
        grid.addRow(4, new Label("Correo:"), correo);
        grid.addRow(5, new Label("Contraseña:"), pass);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new Huesped(nombre.getText(), doc.getText(), fnac.getValue(), tel.getText(), correo.getText(), pass.getText(), null);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(h -> {
            try {
                sga.registrarHuesped(h);
                alerta(Alert.AlertType.INFORMATION, "Registrado", "Huésped creado. Ya puedes iniciar sesión.");
            } catch (Exception ex) {
                alerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        });
    }

    private void mostrarPanelPrincipal() {
        BorderPane layout = new BorderPane();
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(10, 15, 10, 15));
        topBar.setStyle("-fx-background-color: #2c3e50;");
        Label lblInfo = new Label("Rol: " + usuarioActual.getRol() + " | Usuario: " + usuarioActual);
        lblInfo.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnSalir = new Button("Cerrar Sesión");
        btnSalir.setOnAction(e -> mostrarLogin());
        topBar.getChildren().addAll(lblInfo, spacer, btnSalir);
        layout.setTop(topBar);

        TabPane tabs = new TabPane();
        switch (usuarioActual.getRol()) {
            case "HUESPED" -> {
                tabs.getTabs().add(crearTabDisponibilidadYReserva((Huesped) usuarioActual));
                tabs.getTabs().add(crearTabMisReservas((Huesped) usuarioActual));
            }
            case "RECEPCIONISTA" -> {
                tabs.getTabs().add(crearTabOperacionRecepcion());
                tabs.getTabs().add(crearTabApartamentosYNovedades());
            }
            case "ADMINISTRADOR" -> {
                tabs.getTabs().add(crearTabAdminReportes());
                tabs.getTabs().add(crearTabOperacionRecepcion());
                tabs.getTabs().add(crearTabApartamentosYNovedades());
            }
        }
        layout.setCenter(tabs);
        primaryStage.setScene(new Scene(layout, 960, 620));
    }

    private Tab crearTabDisponibilidadYReserva(Huesped huesped) {
        Tab tab = new Tab("Cotizar y Reservar (Portal)");
        tab.setClosable(false);
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));

        HBox filtros = new HBox(10);
        DatePicker dpEntrada = new DatePicker(LocalDate.now().plusDays(1));
        DatePicker dpSalida = new DatePicker(LocalDate.now().plusDays(3));
        Spinner<Integer> spAdultos = new Spinner<>(1, 10, 2);
        Spinner<Integer> spMenores = new Spinner<>(0, 6, 0);
        Button btnBuscar = new Button("Buscar Disponibles");
        filtros.getChildren().addAll(new Label("Entrada:"), dpEntrada, new Label("Salida:"), dpSalida,
                new Label("Adultos (>=6):"), spAdultos, new Label("Menores (<6):"), spMenores, btnBuscar);

        ListView<Apartamento> lvAptos = new ListView<>();
        TextArea txtDesglose = new TextArea();
        txtDesglose.setEditable(false);
        txtDesglose.setPrefRowCount(6);

        Button btnReservar = new Button("Crear Reserva Portal");