package team05.gui;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import team05.db.Buyer;
import team05.db.BuyerDB;
import team05.db.Categories;
import team05.db.CategoryDB;
import team05.db.FileDB;
import team05.db.Outlier;
import team05.db.OutlierDB;
import team05.db.TransactionDB;
import team05.db.Where;
import team05.excel.XLSXReader;
import team05.excel.LedgerWriter;
import team05.fft.Os;
import team05.fft.Transaction;

// Author hypnotics-dev devhypnotics@proton.me
// Author Justin Babineau jbabine1
// Author Eric Smith EWillCliff

/** MainScene */
public class View {

  private Stage stage;
  TableView<Transaction> table = new TableView<>();
  Where where = new Where();

  public View(Stage stage) {
    this.stage = stage;
  }

  /*
   * Layout ==>
   * Top: filter options :: HBox :: Give Pref Height
   * Left and Center TableView :: Vbox :: Auto assigned
   * Right: Action buttons :: VBox :: Give Pref Width
   * Top: Tabs :: Tab :: Give Pref Width
   */
  public Tab getTab() {

    BorderPane border = new BorderPane();
    border.setCenter(getTable());
    border.setRight(getActions());
    border.setTop(getFilter());
    // scene.getStylesheets().add("../../../resources/stylesheet.css");
    Tab tab = new Tab("View", border);
    return tab;
  }

  /*
   * The Center of the screen (Takes up the most real estate)
   * Issue #31
   */
  private VBox getTable() {

    // final ObservableList<Transaction> data;

    final Label label = new Label("Transactions");

    table.setEditable(false);

    final double datew = 90;
    final double descw = 250;
    final double valw = 90;
    final double balw = 95;
    final double buyw = 120;
    final double catw = 120;

    TableColumn<Transaction, SimpleStringProperty> date =
        new TableColumn<Transaction, SimpleStringProperty>("Date");
    date.setCellValueFactory(new PropertyValueFactory<Transaction, SimpleStringProperty>("date"));
    date.setPrefWidth(datew);

    TableColumn<Transaction, SimpleStringProperty> desc =
        new TableColumn<Transaction, SimpleStringProperty>("Description");
    desc.setCellValueFactory(new PropertyValueFactory<Transaction, SimpleStringProperty>("desc"));
    desc.setPrefWidth(descw);

    TableColumn<Transaction, SimpleDoubleProperty> val =
        new TableColumn<Transaction, SimpleDoubleProperty>("Value");
    val.setCellValueFactory(new PropertyValueFactory<Transaction, SimpleDoubleProperty>("val"));
    val.setPrefWidth(valw);

    TableColumn<Transaction, SimpleDoubleProperty> bal =
        new TableColumn<Transaction, SimpleDoubleProperty>("Balance");
    bal.setCellValueFactory(new PropertyValueFactory<Transaction, SimpleDoubleProperty>("bal"));
    bal.setPrefWidth(balw);

    TableColumn<Transaction, SimpleStringProperty> buy =
        new TableColumn<Transaction, SimpleStringProperty>("Buyer");
    buy.setCellValueFactory(new PropertyValueFactory<Transaction, SimpleStringProperty>("buy"));
    buy.setPrefWidth(buyw);

    TableColumn<Transaction, SimpleStringProperty> cat =
        new TableColumn<Transaction, SimpleStringProperty>("Category");
    cat.setCellValueFactory(new PropertyValueFactory<Transaction, SimpleStringProperty>("cat"));
    cat.setPrefWidth(catw);

    // Warings are from the fact that table is of type TableView<Transaction> and it's taking an
    // array of TableColumn<Transaction,?> as the type from each collum can differ
    table.getColumns().addAll(date, desc, buy, val, bal, cat);
    table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    refresh();

    VBox vbox = new VBox(label, table);
    vbox.setSpacing(5);
    vbox.setPadding(new Insets(10, 0, 0, 10));

    return vbox;
  }

  private VBox getActions() {
    BuyerDB db = new BuyerDB();
    final Label label = new Label("Actions:");
    final Button beOutilier = new Button("Assign Transaction");
    final ComboBox<String> buyers =
        new ComboBox<>(FXCollections.observableList(db.getBuyerNames()));
    buyers.setPromptText("Buyers");
    final Button importButton = new Button("Import");
    final Button exportButton = new Button("Export");
    final Button refresh = new Button("Refresh");
    final Button removeOutlier = new Button("Remove Buyer");
    db.close();
    final Button revertTransactions = new Button("Revert");
    final ComboBox<String> fileList =
        new ComboBox<>(FXCollections.observableList(FileDB.getFiles()));
    fileList.setPromptText("File");

    beOutilier.setOnAction(
        event -> {
          BuyerDB bdb = new BuyerDB();
          Buyer bid = bdb.getBuyerFromName(buyers.getValue());
          bdb.close();
          OutlierDB odb = new OutlierDB();
          ArrayList<Outlier> arr = new ArrayList<>();
          for (Transaction i : table.getSelectionModel().getSelectedItems()) {
            if (odb.exists(i)) odb.removeRow(new Transaction[] {i});
            arr.add(new Outlier(i, bid));
          }
          odb.newRows(arr.toArray(new Outlier[arr.size()]));
          odb.close();
          refresh();
        });

    importButton.setOnAction(
        event -> {
          FileChooser file = new FileChooser();
          file.setTitle("Import Bank Statment");
          File exFile = file.showOpenDialog(stage);
          try {
            XLSXReader xlsxReader = new XLSXReader();
            xlsxReader.read(exFile.toString());
          } catch (IOException e) {
            Os.loggerErr(e.getMessage());
          }
          fileList.setItems(FXCollections.observableList(FileDB.getFiles()));
          refresh();
        });
    exportButton.setOnAction(
        event -> {
          FileChooser fileChooser = new FileChooser();
          fileChooser.setTitle("Save Excel File");
          fileChooser.setInitialFileName("Ledger-2024.xlsx");
          File selectedFile = fileChooser.showSaveDialog(stage);
          if(selectedFile != null){
        	  try {
        		  LedgerWriter writer = new LedgerWriter();
        		  writer.write(selectedFile.toString());
        	  } catch (IOException e) {
        		  Os.loggerErr(e.getMessage());
        	  }
          }
        });
    refresh.setOnAction(
        event -> {
          refresh();
          BuyerDB bdb = new BuyerDB();
          buyers.setItems(FXCollections.observableList(bdb.getBuyerNames()));
          bdb.close();
        });
    removeOutlier.setOnAction(
        event -> {
          OutlierDB odb = new OutlierDB();
          odb.removeRow(table.getSelectionModel().getSelectedItems().toArray(new Transaction[0]));
          odb.close();
          refresh();
        });

    revertTransactions.setOnAction(
        event -> {
          FileDB.revertTransactions(
              FileDB.getFileId(fileList.getSelectionModel().getSelectedItem()),
              fileList.getSelectionModel().getSelectedItem());
          refresh();
          fileList.setItems(FXCollections.observableList(FileDB.getFiles()));
        });

    HBox outliers = new HBox(beOutilier, buyers);
    outliers.setSpacing(5);
    outliers.setPadding(new Insets(10, 0, 0, 0));
    HBox btns = new HBox(importButton, exportButton, refresh);
    btns.setSpacing(5);
    btns.setPadding(new Insets(10, 0, 0, 0));
    HBox reverts = new HBox(fileList, revertTransactions);
    reverts.setSpacing(5);
    reverts.setPadding(new Insets(10, 0, 0, 0));
    VBox vbox = new VBox(label, btns, outliers, reverts, removeOutlier);
    vbox.setSpacing(5);
    vbox.setPadding(new Insets(10, 0, 0, 10));

    return vbox;
  }

  private VBox getFilter() {
    /*
     * Start Date, End Date, Buyer, Category, (dropdown menu)
     * Rule (Text Field)
     * Show Outliers,Show Unasigned, Ignore filters (checkboxes)
     * Apply Filters (Button)
     */

    TransactionDB tdb = new TransactionDB();
    final Label label = new Label("Transaction Filters");
    final DatePicker start = new DatePicker(LocalDate.ofEpochDay(tdb.getFirstDay()));
    final Tooltip startTip = new Tooltip("Only show transactions before this date");
    final DatePicker end = new DatePicker(LocalDate.ofEpochDay(tdb.getLastDay()));
    final Tooltip endTip = new Tooltip("Only show transactions after this date");
    tdb.close();
    BuyerDB bdb = new BuyerDB();
    final ComboBox<String> buyers = new ComboBox<>();
    buyers.setPromptText("Buyers");
    buyers.getItems().add(null);
    buyers.getItems().addAll(FXCollections.observableList(bdb.getBuyerNames()));
    final Tooltip buyersTip = new Tooltip("Only show transactions belonging to this buyer");
    bdb.close();
    CategoryDB cdb = new CategoryDB();
    final ComboBox<String> cats = new ComboBox<>();
    cats.setPromptText("Categories");
    cats.getItems().add(null);
    cats.getItems().addAll(FXCollections.observableList(cdb.getCategoryNames()));
    final Tooltip catTip = new Tooltip("Only show transactions belonging to this category");
    cdb.close();

    final TextField rules = new TextField();
    rules.setPromptText("Search");
    final Tooltip rulesTip =
        new Tooltip("Only show transactions with description LIKE the one specified");

    final ComboBox<String> files = new ComboBox<>();
    files.setPromptText("Files");
    files.getItems().add(null);
    files.getItems().addAll(FXCollections.observableList(FileDB.getFiles()));
    final Tooltip filesTip = new Tooltip("Filters by file");

    final CheckBox outliers = new CheckBox("Buyers Assigned");
    final Tooltip outliersTip = new Tooltip("Only shows transactions that you assigned to a buyer");

    final CheckBox noBuyer = new CheckBox("Buyerless");
    final Tooltip noBuyerTip = new Tooltip("Only shows transactions with no buyer");

    final CheckBox noFilter = new CheckBox("Disable");
    final Tooltip noFilterTip = new Tooltip("Diable all other filters");

    final Button btn = new Button("Apply");
    final Tooltip btnTip = new Tooltip("Applies specified filters to the Transaction View");

    final Button reset = new Button("Reset");
    final Tooltip resetTip =
        new Tooltip("Refreshes contents of filters, and resets them to their default states");

    showDelay(
        startTip,
        endTip,
        buyersTip,
        catTip,
        rulesTip,
        outliersTip,
        filesTip,
        noBuyerTip,
        noFilterTip,
        btnTip,
        resetTip);

    Tooltip.install(start, startTip);
    Tooltip.install(end, endTip);
    Tooltip.install(buyers, buyersTip);
    Tooltip.install(cats, catTip);
    Tooltip.install(rules, rulesTip);
    Tooltip.install(outliers, outliersTip);
    Tooltip.install(files, filesTip);
    Tooltip.install(noBuyer, noBuyerTip);
    Tooltip.install(noFilter, noFilterTip);
    Tooltip.install(btn, btnTip);
    Tooltip.install(reset, resetTip);

    outliers.setOnAction(
        event -> {
          noBuyer.setSelected(false);
        });

    reset.setOnAction(
        event -> {
          outliers.setSelected(false);
          noBuyer.setSelected(false);
          noFilter.setSelected(false);
          rules.clear();
          BuyerDB lbdb = new BuyerDB();
          cats.setPromptText("Buyers");
          cats.setItems(FXCollections.observableList(lbdb.getBuyerNames()));
          lbdb.close();
          CategoryDB lcdb = new CategoryDB();
          cats.setPromptText("Categories");
          cats.setItems(FXCollections.observableList(lcdb.getCategoryNames()));
          lcdb.close();
          files.setPromptText("Files");
          files.setItems(FXCollections.observableList(FileDB.getFiles()));
          TransactionDB ltdb = new TransactionDB();
          start.setValue(LocalDate.ofEpochDay(ltdb.getFirstDay()));
          end.setValue(LocalDate.ofEpochDay(ltdb.getLastDay()));
          ltdb.close();
          where = new Where();
          refresh();
        });

    noBuyer.setOnAction(
        event -> {
          outliers.setSelected(false);
        });

    btn.setOnAction(
        event -> {
          if (noFilter.isSelected()) {
            where = new Where();
            refresh();
            return;
          }
          where = new Where(start.getValue(), end.getValue());

          if (!rules.getText().equals("")) where.setRule(rules.getText());

          if (buyers.getValue() != null)
            where.setBuyerFilter(new Buyer[] {new Buyer(buyers.getValue())});

          if (cats.getValue() != null)
            where.setCatsFilter(new Categories[] {new Categories("Placeholder", cats.getValue())});

          if (files.getValue() != null)
            where.setFileIds(new Integer[] {FileDB.getFileId(files.getValue())});

          if (outliers.isSelected()) where.showOutliers();
          else if (noBuyer.isSelected()) where.showUnassigned();

          refresh();
        });

    HBox filters = new HBox(start, end, buyers, cats, rules, files);
    filters.setPadding(new Insets(10, 0, 0, 10));
    filters.setSpacing(5);
    HBox checks = new HBox(outliers, noBuyer, noFilter);
    checks.setPadding(new Insets(10, 0, 0, 10));
    checks.setSpacing(5);
    HBox buttons = new HBox(btn, reset);
    buttons.setPadding(new Insets(10, 0, 0, 10));
    buttons.setSpacing(5);
    VBox vbox = new VBox(label, filters, checks, buttons);

    return vbox;
  }

  private void refresh() {
    TransactionDB db = new TransactionDB();
    table.setItems(FXCollections.observableList(db.getTransactions(where)));
    db.close();
  }

  private void showDelay(Tooltip... tool) {
    for (Tooltip tooltip : tool) {
      tooltip.setShowDelay(new Duration(500));
    }
  }
}
