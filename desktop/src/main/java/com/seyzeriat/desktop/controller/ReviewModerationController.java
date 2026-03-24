package com.seyzeriat.desktop.controller;

import java.util.Optional;

import com.seyzeriat.desktop.HelloApplication;
import com.seyzeriat.desktop.dto.PagedResponse;
import com.seyzeriat.desktop.dto.ReviewResult;
import com.seyzeriat.desktop.service.ApiService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ReviewModerationController {

    @FXML private TableView<ReviewResult> reviewsTable;
    @FXML private TableColumn<ReviewResult, String> reportCountColumn;
    @FXML private TableColumn<ReviewResult, String> gameColumn;
    @FXML private TableColumn<ReviewResult, String> authorColumn;
    @FXML private TableColumn<ReviewResult, String> contentColumn;
    @FXML private TableColumn<ReviewResult, Void> actionColumn;

    @FXML private Label statusLabel;
    @FXML private Button prevButton;
    @FXML private Label pageLabel;
    @FXML private Button nextButton;
    @FXML private Button refreshButton;
    @FXML private ProgressIndicator loadingIndicator;

    private final ApiService apiService = new ApiService();
    private HelloApplication application;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;

    @FXML
    public void initialize() {
        loadingIndicator.setVisible(false);

        // Configure table columns
        reportCountColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getReportCount())));
        gameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getGameTitle()));
        authorColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAuthorUsername()));
        contentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getContent()));

        setupActionColumn();

        fetchReviews(currentPage);
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");

            {
                deleteBtn.getStyleClass().add("logout-button"); // reuse destructive style
                deleteBtn.setOnAction(event -> {
                    ReviewResult review = getTableView().getItems().get(getIndex());
                    confirmAndDelete(review);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item != null || getIndex() >= getTableView().getItems().size() || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
    }

    @FXML
    private void onRefresh() {
        fetchReviews(currentPage);
    }

    @FXML
    private void onPrevPage() {
        if (currentPage > 0) {
            fetchReviews(currentPage - 1);
        }
    }

    @FXML
    private void onNextPage() {
        fetchReviews(currentPage + 1);
    }

    public void setApplication(HelloApplication application) {
        this.application = application;
    }

    private void fetchReviews(int page) {
        setLoading(true);
        statusLabel.setText("Chargement des avis signalés...");

        Task<PagedResponse<ReviewResult>> fetchTask = new Task<>() {
            @Override
            protected PagedResponse<ReviewResult> call() throws Exception {
                return apiService.getReportedReviews(page, PAGE_SIZE);
            }
        };

        fetchTask.setOnSucceeded(event -> Platform.runLater(() -> {
            PagedResponse<ReviewResult> response = fetchTask.getValue();
            setLoading(false);

            reviewsTable.getItems().setAll(response.getContent());
            currentPage = response.getMetadata().getPage();

            // Update pagination UI
            prevButton.setDisable(currentPage == 0);
            nextButton.setDisable(currentPage >= response.getMetadata().getTotalPages() - 1);
            pageLabel.setText("Page " + (currentPage + 1) + " / " + Math.max(1, response.getMetadata().getTotalPages()));

            statusLabel.setText(response.getMetadata().getTotalElements() + " avis signalé(s).");
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

        new Thread(fetchTask, "fetch-reported-reviews-thread").start();
    }

    private void confirmAndDelete(ReviewResult review) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'avis de " + review.getAuthorUsername() + " sur " + review.getGameTitle() + " ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteReview(review.getId());
        }
    }

    private void deleteReview(String id) {
        setLoading(true);
        statusLabel.setText("Suppression en cours...");

        Task<Void> deleteTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiService.deleteReview(id);
                return null;
            }
        };

        deleteTask.setOnSucceeded(event -> Platform.runLater(() -> {
            statusLabel.setText("Avis supprimé avec succès.");
            fetchReviews(currentPage); // reload current page (auto-refresh)
        }));

        deleteTask.setOnFailed(event -> Platform.runLater(() -> {
            setLoading(false);
            Throwable ex = deleteTask.getException();
            if (ex instanceof ApiService.UnauthorizedException) {
                redirectToLogin();
                return;
            }
            statusLabel.setText("Erreur lors de la suppression : " + ex.getMessage());
        }));

        new Thread(deleteTask, "delete-review-thread").start();
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
