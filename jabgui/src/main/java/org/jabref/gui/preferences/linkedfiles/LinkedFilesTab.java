package org.jabref.gui.preferences.linkedfiles;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

import org.jabref.gui.actions.ActionFactory;
import org.jabref.gui.actions.StandardActions;
import org.jabref.gui.desktop.os.NativeDesktop;
import org.jabref.gui.help.HelpAction;
import org.jabref.gui.preferences.AbstractPreferenceTabView;
import org.jabref.gui.preferences.PreferencesTab;
import org.jabref.gui.util.IconValidationDecorator;
import org.jabref.logic.help.HelpFile;
import org.jabref.logic.l10n.Localization;

import com.airhacks.afterburner.views.ViewLoader;
import de.saxsys.mvvmfx.utils.validation.visualization.ControlsFxVisualizer;

public class LinkedFilesTab extends AbstractPreferenceTabView<LinkedFilesTabViewModel> implements PreferencesTab {

    @FXML private TextField mainFileDirectory;
    @FXML private RadioButton useMainFileDirectory;
    @FXML private RadioButton useBibLocationAsPrimary;
    @FXML private Button browseDirectory;
    @FXML private Button autolinkRegexHelp;
    @FXML private RadioButton autolinkFileStartsBibtex;
    @FXML private RadioButton autolinkFileExactBibtex;
    @FXML private RadioButton autolinkUseRegex;
    @FXML private RadioButton openFileExplorerInFilesDirectory;
    @FXML private RadioButton openFileExplorerInLastDirectory;
    @FXML private TextField autolinkRegexKey;

    @FXML private CheckBox fulltextIndex;

    @FXML private ComboBox<String> fileNamePattern;
    @FXML private TextField fileDirectoryPattern;
    @FXML private CheckBox confirmLinkedFileDelete;
    @FXML private CheckBox moveToTrash;

    private final ControlsFxVisualizer validationVisualizer = new ControlsFxVisualizer();

    public LinkedFilesTab() {
        ViewLoader.view(this)
                  .root(this)
                  .load();
    }

    @Override
    public String getTabName() {
        return Localization.lang("Linked files");
    }

    public void initialize() {
        this.viewModel = new LinkedFilesTabViewModel(dialogService, preferences);

        mainFileDirectory.textProperty().bindBidirectional(viewModel.mainFileDirectoryProperty());
        mainFileDirectory.disableProperty().bind(viewModel.useBibLocationAsPrimaryProperty());
        browseDirectory.disableProperty().bind(viewModel.useBibLocationAsPrimaryProperty());
        useBibLocationAsPrimary.selectedProperty().bindBidirectional(viewModel.useBibLocationAsPrimaryProperty());
        useMainFileDirectory.selectedProperty().bindBidirectional(viewModel.useMainFileDirectoryProperty());

        moveToTrash.selectedProperty().bindBidirectional(viewModel.moveToTrashProperty());
        moveToTrash.setDisable(!NativeDesktop.get().moveToTrashSupported());

        autolinkFileStartsBibtex.selectedProperty().bindBidirectional(viewModel.autolinkFileStartsBibtexProperty());
        autolinkFileExactBibtex.selectedProperty().bindBidirectional(viewModel.autolinkFileExactBibtexProperty());
        autolinkUseRegex.selectedProperty().bindBidirectional(viewModel.autolinkUseRegexProperty());
        autolinkRegexKey.textProperty().bindBidirectional(viewModel.autolinkRegexKeyProperty());
        autolinkRegexKey.disableProperty().bind(autolinkUseRegex.selectedProperty().not());
        fulltextIndex.selectedProperty().bindBidirectional(viewModel.fulltextIndexProperty());
        fileNamePattern.valueProperty().bindBidirectional(viewModel.fileNamePatternProperty());
        fileNamePattern.itemsProperty().bind(viewModel.defaultFileNamePatternsProperty());
        fileDirectoryPattern.textProperty().bindBidirectional(viewModel.fileDirectoryPatternProperty());
        confirmLinkedFileDelete.selectedProperty().bindBidirectional(viewModel.confirmLinkedFileDeleteProperty());
        openFileExplorerInFilesDirectory.selectedProperty().bindBidirectional(viewModel.openFileExplorerInFilesDirectoryProperty());
        openFileExplorerInLastDirectory.selectedProperty().bindBidirectional(viewModel.openFileExplorerInLastDirectoryProperty());

        ActionFactory actionFactory = new ActionFactory();
        actionFactory.configureIconButton(StandardActions.HELP_REGEX_SEARCH, new HelpAction(HelpFile.REGEX_SEARCH, dialogService, preferences.getExternalApplicationsPreferences()), autolinkRegexHelp);

        validationVisualizer.setDecoration(new IconValidationDecorator());
        Platform.runLater(() -> validationVisualizer.initVisualization(viewModel.mainFileDirValidationStatus(), mainFileDirectory));
    }

    public void mainFileDirBrowse() {
        viewModel.mainFileDirBrowse();
    }
}
