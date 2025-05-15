package utils;

import enums.StatutMatch;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.Match;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class LivescoreStyleUtil {
    
    /**
     * Applies styling to a TableView of matches based on their status
     * - Live matches: Red background
     * - Upcoming matches: Blue background
     * - Finished matches: Green background
     */
    public static void applyLivescoreTableStyles(TableView<Match> tableView) {
        // Set table style with modern look
        tableView.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-width: 1px; "
                + "-fx-border-radius: 8px; -fx-background-radius: 8px; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); "
                + "-fx-text-fill: #333333;");

        for (Object col : tableView.getColumns()) {
            @SuppressWarnings("unchecked")
            TableColumn<Match, Object> column = (TableColumn<Match, Object>) col;

            column.setCellFactory(c -> new TableCell<>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.toString());
                        setStyle("-fx-text-fill: #333333;");
                    }
                }
            });
        }

        // Style the column headers
        tableView.getStyleClass().add("livescore-table");

        // Set row factory with hover effect and status-based styling
        tableView.setRowFactory(tv -> {
            TableRow<Match> row = new TableRow<>();

            // Add hover effect with smooth transition
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.setStyle(row.getStyle() + "; -fx-background-color: rgba(0, 0, 0, 0.08); -fx-cursor: hand;");
                }
            });

            row.setOnMouseExited(event -> {
                if (!row.isEmpty()) {
                    updateRowStyle(row, row.getItem());
                }
            });

            // Update row style based on match status
            row.itemProperty().addListener((obs, oldMatch, newMatch) -> {
                if (newMatch != null) {
                    updateRowStyle(row, newMatch);
                } else {
                    row.setStyle("");
                }
            });

            return row;
        });

        // Apply padding to the table cells
        tableView.setStyle(tableView.getStyle() + "; -fx-padding: 5px;");

        // Style the column headers
        tableView.getColumns().forEach(column -> {
            column.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold; -fx-padding: 10px 5px;");
        });
    }

    private static void updateRowStyle(TableRow<Match> row, Match match) {
        if (match == null) {
            row.setStyle("");
            return;
        }

        StatutMatch status = match.getStatutMatch();
        if (status == null) {
            status = StatutMatch.EN_ATTENTE;
        }

        String baseStyle = "-fx-background-color: ";
        switch (status) {
            case EN_COURS:
                baseStyle += "rgba(255, 0, 0, 0.1)";
                break;
            case EN_ATTENTE:
                baseStyle += "rgba(0, 0, 255, 0.05)";
                break;
            case TERMINE:
                baseStyle += "rgba(0, 128, 0, 0.05)";
                break;
            default:
                baseStyle = "";
                break;
        }

        row.setStyle(baseStyle + " -fx-border-color transparent transparent #e0e0e0 transparent; -fx-border-width: 0 0 1 0");
    }

    /**
     * Returns the appropriate style for score display based on match status
     * - Live matches: Bold red
     * - Finished matches: Bold
     * - Upcoming matches: Gray
     */
    public static String getScoreStyle(Match match) {
        if (match == null) return "";

        StatutMatch status = match.getStatutMatch();
        if (status == null) {
            status = StatutMatch.EN_ATTENTE; // Default to upcoming if null
        }

        switch (status) {
            case EN_COURS:
                return "-fx-font-weight: bold; -fx-text-fill: red;";
            case TERMINE:
                return "-fx-font-weight: bold;";
            case EN_ATTENTE:
                return "-fx-text-fill: #999;";
            default:
                return "";
        }
    }
    
    /**
     * Creates a TableCell that applies appropriate styling to scores based on match status
     */
    public static <T> TableCell<T, String> createStyledScoreCell() {
        TableCell<T, String> cell = new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (getTableRow() != null && getTableRow().getItem() instanceof Match) {
                        Match match = (Match) getTableRow().getItem();
                        setStyle(getScoreStyle(match));
                    }
                }
            }
        };
        return cell;
    }
    
    /**
     * Returns the appropriate text color for status display
     */
    public static Color getStatusColor(StatutMatch status) {
        if (status == null) return Color.BLACK;
        
        switch (status) {
            case EN_COURS:
                return Color.RED;
            case TERMINE:
                return Color.GREEN;
            case EN_ATTENTE:
                return Color.BLUE;
            default:
                return Color.BLACK;
        }
    }
    
    /**
     * Returns the appropriate font for status display
     */
    public static Font getStatusFont(StatutMatch status) {
        if (status == null) return Font.font("System", 12);
        
        switch (status) {
            case EN_COURS:
                return Font.font("System", FontWeight.BOLD, 12);
            default:
                return Font.font("System", 12);
        }
    }
}