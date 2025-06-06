package org.jabref.gui.entryeditor;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javafx.geometry.HPos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import org.jabref.gui.DialogService;
import org.jabref.gui.desktop.os.NativeDesktop;
import org.jabref.gui.preferences.GuiPreferences;
import org.jabref.logic.l10n.Localization;
import org.jabref.logic.util.TaskExecutor;
import org.jabref.model.entry.BibEntry;

import com.tobiasdiez.easybind.EasyBind;
import org.controlsfx.control.HyperlinkLabel;

/**
 * @implNote This tab is called <code>SciteTab</code>, because it uses the service <code>**scite** aI</code>.
 */
public class SciteTab extends EntryEditorTab {

    public static final String NAME = "Citation information";
    public static final String SCITE_REPORTS_URL_BASE = "https://scite.ai/reports/";

    private final GridPane sciteResultsPane;
    private final ProgressIndicator progressIndicator;
    private final SciteTabViewModel viewModel;
    private final GuiPreferences preferences;
    private final DialogService dialogService;

    public SciteTab(GuiPreferences preferences, TaskExecutor taskExecutor, DialogService dialogService) {
        this.preferences = preferences;
        this.viewModel = new SciteTabViewModel(preferences, taskExecutor);
        this.dialogService = dialogService;
        this.sciteResultsPane = new GridPane();
        this.progressIndicator = new ProgressIndicator();
        setText(Localization.lang("Citation information"));
        setTooltip(new Tooltip(Localization.lang("Search scite.ai for Smart Citations")));
        setSciteResultsPane();
    }

    private void setSciteResultsPane() {
        progressIndicator.setMaxSize(100, 100);
        sciteResultsPane.add(progressIndicator, 0, 0);

        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(100);
        column.setHalignment(HPos.CENTER);

        sciteResultsPane.getColumnConstraints().setAll(column);
        sciteResultsPane.setId("scitePane");
        setContent(sciteResultsPane);

        EasyBind.subscribe(viewModel.statusProperty(), status -> {
            sciteResultsPane.getChildren().clear();
            switch (status) {
                case IN_PROGRESS ->
                        sciteResultsPane.add(progressIndicator, 0, 0);
                case FOUND ->
                        viewModel.getCurrentResult().ifPresent(result -> sciteResultsPane.add(getTalliesPane(result), 0, 0));
                case ERROR ->
                        sciteResultsPane.add(getErrorPane(), 0, 0);
            }
        });
    }

    @Override
    public boolean shouldShow(BibEntry entry) {
        return viewModel.shouldShow();
    }

    @Override
    protected void bindToEntry(BibEntry entry) {
        viewModel.bindToEntry(entry);
    }

    private VBox getErrorPane() {
        Label titleLabel = new Label(Localization.lang("Error"));
        titleLabel.setId("scite-error-label");
        Text errorMessageText = new Text(viewModel.searchErrorProperty().get());
        VBox errorMessageBox = new VBox(30, titleLabel, errorMessageText);
        errorMessageBox.getStyleClass().add("scite-error-box");
        return errorMessageBox;
    }

    private VBox getTalliesPane(SciteTallyModel tallModel) {
        Label titleLabel = new Label(Localization.lang("Tallies for %0", tallModel.doi()));
        titleLabel.getStyleClass().add("scite-tallies-label");
        Text message = new Text(Localization.lang(
                "Total Citations: %0\nSupporting: %1\nContradicting: %2\nMentioning: %3\nUnclassified: %4\nCiting Publications: %5",
                tallModel.total(),
                tallModel.supporting(),
                tallModel.contradicting(),
                tallModel.mentioning(),
                tallModel.unclassified(),
                tallModel.citingPublications()
        ));
        String url = SCITE_REPORTS_URL_BASE + URLEncoder.encode(tallModel.doi(), StandardCharsets.UTF_8);
        VBox messageBox = getMessageBox(url, titleLabel, message);
        messageBox.getStyleClass().add("scite-message-box");
        return messageBox;
    }

    private VBox getMessageBox(String url, Label titleLabel, Text message) {
        HyperlinkLabel link = new HyperlinkLabel(Localization.lang("See full report at [%0]", url));
        link.setOnAction(event -> {
            if (event.getSource() instanceof Hyperlink) {
                try {
                    NativeDesktop.openBrowser(url, preferences.getExternalApplicationsPreferences());
                } catch (IOException ioex) {
                    // Can't throw a checked exception from here, so display a message to the user instead.
                    dialogService.showErrorDialogAndWait(
                    "An error occurred opening web browser",
                "JabRef was unable to open a web browser for link:\n\n" + url + "\n\nError Message:\n\n" + ioex.getMessage(),
                        ioex
                    );
                }
            }
        });

        return new VBox(30, titleLabel, message, link);
    }
}
