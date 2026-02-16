package com.seyzeriat.desktop.controller;

import java.util.List;

import com.seyzeriat.desktop.HelloApplication;
import com.seyzeriat.desktop.dto.UserResult;
import com.seyzeriat.desktop.service.ApiService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller for the User Management view.
 *
 * <p>Sends a {@code GET} request to {@code /api/admin/users} with the JWT Bearer token.
 * The backend verifies the user has {@code ROLE_ADMIN} authority before returning
 * the list. Displays ID, Username, and Email in a table.</p>
 */
public class UserManagementController {

    @FXML private TableView<UserResult> usersTable;
    @FXML private TableColumn<UserResult, String> idColumn;
    @FXML private TableColumn<UserResult, String> usernameColumn;
    @FXML private TableColumn<UserResult, String> emailColumn;
    @FXML private Label statusLabel;
    @FXML private Button refreshButton;
    @FXML private ProgressIndicator loadingIndicator;

    private final ApiService apiService = new ApiService();
    private HelloApplication application;

    @FXML
    public void initialize() {
        loadingIndicator.setVisible(false);

        // Configure table columns
        idColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getId()));
        usernameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUsername()));
        emailColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail()));

        // Load users on view init
        fetchUsers();
    }

    @FXML
    private void onRefresh() {
        fetchUsers();
    }

    public void setApplication(HelloApplication application) {
        this.application = application;
    }

    /**
     * Fetches all users from the admin API on a background thread.
     */
    private void fetchUsers() {
        setLoading(true);
        statusLabel.setText("Chargement des utilisateurs...");

        Task<List<UserResult>> fetchTask = new Task<>() {
            @Override
            protected List<UserResult> call() throws Exception {
                return apiService.getUsers();
            }
        };

        fetchTask.setOnSucceeded(event -> Platform.runLater(() -> {
            List<UserResult> users = fetchTask.getValue();
            setLoading(false);

            usersTable.getItems().setAll(users);

            if (users.isEmpty()) {
                statusLabel.setText("Aucun utilisateur trouvé.");
            } else {
                statusLabel.setText(users.size() + " utilisateur(s) chargé(s).");
            }
        }));

        fetchTask.setOnFailed(event -> Platform.runLater(() -> {
            setLoading(false);
            Throwable ex = fetchTask.getException();
            if (ex instanceof ApiService.UnauthorizedException) {
                redirectToLogin();
                return;
            }
            statusLabel.setText("Erreur : " + ex.getMessage());
        }));

        new Thread(fetchTask, "fetch-users-thread").start();
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        refreshButton.setDisable(loading);
    }

    private void redirectToLogin() {
        if (application != null) {
            application.showLoginView();
        }
    }
}
