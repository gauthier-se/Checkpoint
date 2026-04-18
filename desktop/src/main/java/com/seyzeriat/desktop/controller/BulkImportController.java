package com.seyzeriat.desktop.controller;

import java.util.List;

import com.seyzeriat.desktop.HelloApplication;
import com.seyzeriat.desktop.dto.BulkImportResult;
import com.seyzeriat.desktop.service.ApiService;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 * Controller for the Bulk Import view.
 * Lets an admin batch-import top-rated or recent games from IGDB.
 */
public class BulkImportController {

    @FXML private Spinner<Integer> topRatedLimitSpinner;
    @FXML private Spinner<Integer> minRatingCountSpinner;
    @FXML private Button topRatedStartButton;
    @FXML private Label topRatedStatusLabel;

    @FXML private Spinner<Integer> recentLimitSpinner;
    @FXML private Button recentStartButton;
    @FXML private Label recentStatusLabel;

    @FXML private ProgressIndicator globalProgress;
    @FXML private Label globalStatusLabel;

    private final ApiService apiService = new ApiService();
    private HelloApplication application;

    @FXML
    public void initialize() {
        topRatedLimitSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 100));
        minRatingCountSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 100));
        recentLimitSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 100));

        globalProgress.setVisible(false);
        globalStatusLabel.setText("");
        topRatedStatusLabel.setText("");
        recentStatusLabel.setText("");
    }

    @FXML
    private void onStartTopRated() {
        int limit = topRatedLimitSpinner.getValue();
        int minRatingCount = minRatingCountSpinner.getValue();

        startImport(
                topRatedStatusLabel,
                "Import des jeux les mieux notés en cours...",
                () -> apiService.bulkImportTopRated(limit, minRatingCount)
        );
    }

    @FXML
    private void onStartRecent() {
        int limit = recentLimitSpinner.getValue();

        startImport(
                recentStatusLabel,
                "Import des sorties récentes en cours...",
                () -> apiService.bulkImportRecent(limit)
        );
    }

    private void startImport(Label cardStatusLabel, String runningMessage, ImportCall call) {
        setRunning(true, runningMessage);
        cardStatusLabel.setText("");

        Task<BulkImportResult> task = new Task<>() {
            @Override
            protected BulkImportResult call() throws Exception {
                return call.invoke();
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            setRunning(false, "");
            BulkImportResult result = task.getValue();
            cardStatusLabel.setText(formatSummary(result));
            globalStatusLabel.setText("Import terminé.");
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            setRunning(false, "");
            if (ex instanceof ApiService.UnauthorizedException) {
                redirectToLogin();
                return;
            }
            String message = ex != null && ex.getMessage() != null ? ex.getMessage() : "erreur inconnue";
            cardStatusLabel.setText("Erreur : " + message);
            globalStatusLabel.setText("Échec de l'import.");
        }));

        new Thread(task, "bulk-import-thread").start();
    }

    private void setRunning(boolean running, String message) {
        topRatedStartButton.setDisable(running);
        recentStartButton.setDisable(running);
        globalProgress.setVisible(running);
        globalStatusLabel.setText(message);
    }

    private String formatSummary(BulkImportResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(result.getImported()).append(" importé(s), ");
        sb.append(result.getSkipped()).append(" ignoré(s) (déjà existants), ");
        sb.append(result.getFailed()).append(" échec(s)");
        sb.append(" sur ").append(result.getTotalFetched()).append(" récupéré(s).");

        List<String> errors = result.getErrors();
        if (errors != null && !errors.isEmpty()) {
            int shown = Math.min(errors.size(), 3);
            sb.append(" Échecs : ");
            for (int i = 0; i < shown; i++) {
                if (i > 0) sb.append(", ");
                sb.append(errors.get(i));
            }
            if (errors.size() > shown) {
                sb.append("…");
            }
        }
        return sb.toString();
    }

    public void setApplication(HelloApplication application) {
        this.application = application;
    }

    private void redirectToLogin() {
        if (application != null) {
            application.showLoginView();
        }
    }

    @FunctionalInterface
    private interface ImportCall {
        BulkImportResult invoke() throws Exception;
    }
}
