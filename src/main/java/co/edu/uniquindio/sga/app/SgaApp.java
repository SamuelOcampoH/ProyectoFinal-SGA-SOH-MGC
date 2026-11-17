package co.edu.uniquindio.sga.app;

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

        btnBuscar.setOnAction(e -> {
            try {
                int total = spAdultos.getValue() + spMenores.getValue();
                lvAptos.getItems().setAll(sga.buscarDisponibles(dpEntrada.getValue(), dpSalida.getValue(), total));
                txtDesglose.clear();
            } catch (Exception ex) {
                alerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        });

        lvAptos.getSelectionModel().selectedItemProperty().addListener((obs, oldV, apt) -> {
            if (apt != null) {
                List<Ocupante> oc = generarOcupantesDemo(huesped, spAdultos.getValue(), spMenores.getValue());
                List<String> lineas = new ArrayList<>();
                long total = sga.cotizarEstancia(apt, dpEntrada.getValue(), dpSalida.getValue(), oc, lineas);
                txtDesglose.setText(String.join("\n", lineas) + String.format("\nTOTAL ESTANCIA: $%,d COP (Anticipo 30%%: $%,d)",
                        total, Math.round(total * 0.30)));
            }
        });

        btnReservar.setOnAction(e -> {
            Apartamento apt = lvAptos.getSelectionModel().getSelectedItem();
            if (apt == null) return;
            try {
                List<Ocupante> oc = generarOcupantesDemo(huesped, spAdultos.getValue(), spMenores.getValue());
                String cod = "RES-" + (sga.getReservas().size() + 101);
                ReservaPortal r = sga.crearReservaPortal(cod, huesped, apt, dpEntrada.getValue(), dpSalida.getValue(), oc);
                alerta(Alert.AlertType.INFORMATION, "Reserva Creada", "Código: " + r.getCodigo() + " en estado PENDIENTE.");
                btnBuscar.fire();
            } catch (Exception ex) {
                alerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        });

        box.getChildren().addAll(filtros, new Label("Apartamentos Disponibles:"), lvAptos,
                new Label("Cotización Noche por Noche:"), txtDesglose, btnReservar);
        tab.setContent(box);
        return tab;
    }

    private Tab crearTabMisReservas(Huesped huesped) {
        Tab tab = new Tab("Mis Reservas y Pagos");
        tab.setClosable(false);
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));

        ListView<String> lv = new ListView<>();
        Button btnRefrescar = new Button("Actualizar Lista");
        Button btnPagarAnticipo = new Button("Pagar 30% y Confirmar");
        Button btnCancelar = new Button("Cancelar Reserva Seleccionada");

        Runnable cargar = () -> {
            lv.getItems().clear();
            for (Reserva r : sga.getReservas()) {
                if (r.getTitular().getDocumento().equals(huesped.getDocumento())) {
                    lv.getItems().add(r.getCodigo() + " | " + r.getApartamento().getCodigo() + " | " +
                            r.getFechaEntrada() + " a " + r.getFechaSalida() + " | " + r.getEstado() +
                            " | Saldo: $" + r.getFolio().getSaldoPendiente());
                }
            }
        };
        cargar.run();
        btnRefrescar.setOnAction(e -> cargar.run());

        btnPagarAnticipo.setOnAction(e -> {
            Reserva r = obtenerReservaSeleccionada(lv);
            if (r == null) return;
            try {
                long anticipo = Math.round(r.getValorEstanciaCongelado() * 0.30);
                r.getFolio().registrarPago(anticipo, MedioPago.TRANSFERENCIA);
                r.confirmar();
                alerta(Alert.AlertType.INFORMATION, "Confirmada", "Pago registrado y reserva CONFIRMADA.");
                cargar.run();
            } catch (Exception ex) {
                alerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        });

        btnCancelar.setOnAction(e -> {
            Reserva r = obtenerReservaSeleccionada(lv);
            if (r == null) return;
            try {
                long dev = r.cancelar(LocalDateTime.now());
                alerta(Alert.AlertType.INFORMATION, "Cancelada", String.format("Reserva cancelada. Devolución calculada: $%,d COP", dev));
                cargar.run();
            } catch (Exception ex) {
                alerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        });

        box.getChildren().addAll(new HBox(10, btnRefrescar, btnPagarAnticipo, btnCancelar), lv);
        tab.setContent(box);
        return tab;
    }

    private Tab crearTabOperacionRecepcion() {
        Tab tab = new Tab("Recepción: Reservas, Check-In / Out y Folios");
        tab.setClosable(false);
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));

        ListView<String> lv = new ListView<>();
        Button btnRefrescar = new Button("Refrescar Reservas");
        Button btnConfirmar = new Button("Confirmar");
        Button btnCheckIn = new Button("Check-In");
        Button btnCheckOut = new Button("Check-Out");
        Button btnNoShow = new Button("Declarar No-Show");
        Button btnAbonoTotal = new Button("Registrar Pago Total Saldo");
        Button btnCargoExtra = new Button("Agregar Cargo Desayuno ($25.000)");
        Button btnSimularExterna = new Button("Simular Reserva Externa (Choque/Éxito)");

        Runnable cargar = () -> {
            lv.getItems().clear();
            for (Reserva r : sga.getReservas()) {
                lv.getItems().add(r.getCodigo() + " | [" + r.getCanal() + "] | " + r.getApartamento().getCodigo() +
                        " | " + r.getTitular().getNombre() + " | " + r.getFechaEntrada() + "->" + r.getFechaSalida() +
                        " | Estado: " + r.getEstado() + " | Saldo: $" + r.getFolio().getSaldoPendiente());
            }
        };
        cargar.run();
        btnRefrescar.setOnAction(e -> cargar.run());

        btnConfirmar.setOnAction(e -> ejecutarSobreReserva(lv, r -> r.confirmar(), cargar));
        btnCheckIn.setOnAction(e -> ejecutarSobreReserva(lv, r -> r.hacerCheckIn(r.getFechaEntrada(), LocalDateTime.now()), cargar));
        btnCheckOut.setOnAction(e -> ejecutarSobreReserva(lv, r -> r.hacerCheckOut(LocalDateTime.now()), cargar));
        btnNoShow.setOnAction(e -> ejecutarSobreReserva(lv, Reserva::declararNoShow, cargar));
        btnCargoExtra.setOnAction(e -> ejecutarSobreReserva(lv, r -> r.getFolio().registrarCargo("Servicio Desayuno", 25000L), cargar));
        btnAbonoTotal.setOnAction(e -> ejecutarSobreReserva(lv, r -> {
            long saldo = r.getFolio().getSaldoPendiente();
            if (saldo > 0) r.getFolio().registrarPago(saldo, MedioPago.TARJETA);
        }, cargar));

        btnSimularExterna.setOnAction(e -> {
            try {
                Huesped h = sga.getHuespedes().get(0);
                Apartamento apt = sga.getApartamentos().get(0);
                sga.recibirReservaExterna("EXT-" + (sga.getReservas().size() + 1), "BOOKING-" + System.currentTimeMillis() % 10000,
                        h, apt, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                        List.of(new Ocupante(h.getNombre(), h.getFechaNacimiento())));
                alerta(Alert.AlertType.INFORMATION, "Externa Recibida", "Reserva externa ingresada sin conflictos.");
                cargar.run();
            } catch (Exception ex) {
                alerta(Alert.AlertType.WARNING, "Conflicto de Canal", ex.getMessage());
            }
        });

        box.getChildren().addAll(
                new HBox(8, btnRefrescar, btnConfirmar, btnCheckIn, btnCheckOut, btnNoShow),
                new HBox(8, btnCargoExtra, btnAbonoTotal, btnSimularExterna),
                lv
        );
        tab.setContent(box);
        return tab;
    }

    private Tab crearTabApartamentosYNovedades() {
        Tab tab = new Tab("Apartamentos y Novedades");
        tab.setClosable(false);
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));

        ListView<Apartamento> lv = new ListView<>();
        lv.getItems().setAll(sga.getApartamentos());

        Button btnPreparado = new Button("Marcar PREPARADO");
        Button btnEnPrep = new Button("Marcar EN_PREPARACION");
        Button btnFuera = new Button("Marcar FUERA_DE_SERVICIO");
        Button btnNovedad = new Button("Registrar Novedad");

        btnPreparado.setOnAction(e -> cambiarEstadoApto(lv, EstadoApartamento.PREPARADO, null));
        btnEnPrep.setOnAction(e -> cambiarEstadoApto(lv, EstadoApartamento.EN_PREPARACION, null));
        btnFuera.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog("Mantenimiento preventivo");
            d.setHeaderText("Motivo obligatorio para Fuera de Servicio:");
            d.showAndWait().ifPresent(m -> cambiarEstadoApto(lv, EstadoApartamento.FUERA_DE_SERVICIO, m));
        });
        btnNovedad.setOnAction(e -> {
            Apartamento a = lv.getSelectionModel().getSelectedItem();
            if (a == null) return;
            TextInputDialog d = new TextInputDialog();
            d.setHeaderText("Descripción de novedad en " + a.getCodigo() + ":");
            d.showAndWait().ifPresent(desc -> {
                sga.registrarNovedad(new Novedad(a.getCodigo(), LocalDateTime.now(), desc, usuarioActual.toString()));
                alerta(Alert.AlertType.INFORMATION, "Novedad Guardada", "Registrada correctamente.");
            });
        });

        box.getChildren().addAll(new HBox(10, btnPreparado, btnEnPrep, btnFuera, btnNovedad), lv);
        tab.setContent(box);
        return tab;
    }

    private Tab crearTabAdminReportes() {
        Tab tab = new Tab("Administración y Reportes");
        tab.setClosable(false);
        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        TextArea txtReporte = new TextArea();
        txtReporte.setEditable(false);
        txtReporte.setPrefRowCount(15);

        Button btnGenerar = new Button("Generar Reporte Consolidado SGA");
        btnGenerar.setOnAction(e -> {
            StringBuilder sb = new StringBuilder("=== REPORTE GERENCIAL APARTA-SUITES UQ ===\n");
            sb.append(String.format("Ingresos Recaudados Netos: $%,d COP\n", sga.calcularIngresosTotales()));
            sb.append(String.format("Saldos Pendientes Activos: $%,d COP\n", sga.calcularSaldosPendientesTotales()));
            sb.append("Total Reservas Registradas: ").append(sga.getReservas().size()).append("\n");
            sb.append("Conflictos de Canal Externa Rechazados: ").append(sga.getConflictosCanal().size()).append("\n");
            sb.append("Novedades Reportadas: ").append(sga.getNovedades().size()).append("\n\n--- DETALLE RESERVAS ---\n");
            sga.getReservas().forEach(r -> sb.append(r.obtenerDetalleFacturacion()).append(" | Estado: ").append(r.getEstado()).append("\n"));
            txtReporte.setText(sb.toString());
        });

        box.getChildren().addAll(btnGenerar, txtReporte);
        tab.setContent(box);
        return tab;
    }

    private void cambiarEstadoApto(ListView<Apartamento> lv, EstadoApartamento est, String motivo) {
        Apartamento a = lv.getSelectionModel().getSelectedItem();
        if (a == null) return;
        try {
            a.cambiarEstado(est, motivo);
            lv.refresh();
        } catch (Exception ex) {
            alerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
        }
    }

    private interface AccionReserva { void ejecutar(Reserva r) throws Exception; }

    private void ejecutarSobreReserva(ListView<String> lv, AccionReserva accion, Runnable refrescar) {
        Reserva r = obtenerReservaSeleccionada(lv);
        if (r == null) return;
        try {
            accion.ejecutar(r);
            refrescar.run();
        } catch (Exception ex) {
            alerta(Alert.AlertType.ERROR, "Regla de Negocio", ex.getMessage());
        }
    }

    private Reserva obtenerReservaSeleccionada(ListView<String> lv) {
        String sel = lv.getSelectionModel().getSelectedItem();
        if (sel == null) return null;
        String cod = sel.split("\\|")[0].trim();
        return sga.getReservas().stream().filter(r -> r.getCodigo().equals(cod)).findFirst().orElse(null);
    }

    private List<Ocupante> generarOcupantesDemo(Huesped titular, int adultos, int menores) {
        List<Ocupante> lista = new ArrayList<>();
        lista.add(new Ocupante(titular.getNombre(), titular.getFechaNacimiento()));
        for (int i = 1; i < adultos; i++) {
            lista.add(new Ocupante("Acompañante Adulto " + i, LocalDate.now().minusYears(25)));
        }
        for (int i = 1; i <= menores; i++) {
            lista.add(new Ocupante("Menor " + i, LocalDate.now().minusYears(3)));
        }
        return lista;
    }

    private void alerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}