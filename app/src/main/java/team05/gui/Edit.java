package team05.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import team05.db.Buyer;
import team05.db.BuyerDB;
import team05.db.BuyerRules;
import team05.db.BuyerRulesDB;
import team05.db.Categories;
import team05.db.CategoryDB;

// Author hypnotics-dev devhypnotics@proton.me
// Author Justin Babineau jbabine1
// Author Benjamin Hickey benjamin-hickey
/** Entry */
public class Edit {

  private Stage stage;
  private BorderPane border;

  public Edit(Stage stage) {
    this.stage = stage;
    border = new BorderPane();
  }

  public Tab getTab() {

    TableView<Buyer> table = buyerOutput();
    border.setTop(getModes());
    border.setCenter(table);
    border.setRight(buyerActions(table));

    Tab tab = new Tab("Edit", border);
    return tab;
  }

  private HBox getModes() {

    final Label label = new Label("Modes: ");
    final Button buyer = new Button("Buyer");
    final Button buyerRules = new Button("Buyer Rules");
    final Button category = new Button("Category");
    buyer.setOnAction(
        event -> {
          TableView<Buyer> table = buyerOutput();
          border.setCenter(table);
          border.setRight(buyerActions(table));
        });
    buyerRules.setOnAction(
        event -> {
          TableView<BuyerRules> table = buyerRulesOutput();
          border.setCenter(table);
          border.setRight(buyerRulesActions(table));
        });
    category.setOnAction(
        event -> {
          TableView<Categories> table = categoryOutput();
          border.setCenter(table);
          border.setRight(categoryActions(table));
        });

    HBox hbox = new HBox(label, buyer, buyerRules, category);
    hbox.setSpacing(10);
    hbox.setPadding(new Insets(10, 0, 0, 10));
    return hbox;
  }

  private VBox buyerActions(TableView<Buyer> table) {
    final Label mode = new Label("Buyer Mode");
    final Button add = new Button("Add");
    final TextField buyerName = new TextField();
    buyerName.setPromptText("Buyer Name");
    final Button edit = new Button("Edit");
    final TextField newName = new TextField();
    newName.setPromptText("New Name");
    final Button del = new Button("Delete");

    add.setOnAction(
        event -> {
          BuyerDB db = new BuyerDB();
          db.newRows(new Buyer[] {new Buyer(buyerName.getText())});
          table.setItems(FXCollections.observableList(db.getBuyers()));
          buyerName.clear();
          db.close();
        });
    edit.setOnAction(
        event -> {
          BuyerDB db = new BuyerDB();
          db.updateRow(
              new Buyer[] {
                new Buyer(table.getSelectionModel().getSelectedItem().getPk(), newName.getText())
              });
          newName.clear();
          table.setItems(FXCollections.observableList(db.getBuyers()));
          db.close();
        });
    del.setOnAction(
        event -> {
          BuyerDB db = new BuyerDB();
          db.removeRow(table.getSelectionModel().getSelectedItems().toArray(new Buyer[0]));
          table.setItems(FXCollections.observableList(db.getBuyers()));
          db.close();
        });

    final HBox adds = new HBox(add, buyerName);
    adds.setSpacing(5);
    adds.setPadding(new Insets(10, 0, 0, 0));
    final HBox edits = new HBox(edit, newName);
    edits.setSpacing(5);
    edits.setPadding(new Insets(10, 0, 10, 0));
    VBox vbox = new VBox(mode, adds, edits, del);
    vbox.setSpacing(5);
    vbox.setPadding(new Insets(10, 0, 0, 10));
    return vbox;
  }

  private VBox buyerRulesActions(TableView<BuyerRules> table) {
    BuyerDB db = new BuyerDB();
    final Label label = new Label("Buyer Rules Mode");
    final Button add = new Button("Add");
    final TextField buyerRule = new TextField();
    buyerRule.setPromptText("Rule");
    final ComboBox<String> buyers =
        new ComboBox<>(FXCollections.observableList(db.getBuyerNames()));
    buyers.setPromptText("Buyer");
    final Button editRule = new Button("Edit Rule");
    final TextField newRule = new TextField();
    newRule.setPromptText("Update Rule");
    final ComboBox<String> buyersEdit =
        new ComboBox<>(FXCollections.observableList(db.getBuyerNames()));
    buyersEdit.setPromptText("Update Buyer");
    final Button editBuyer = new Button("Edit Buyer");
    final Button del = new Button("Delete");
    db.close();

    add.setOnAction(
        event -> {
          String rule = buyerRule.getText();
          if (ruleCheck(rule, "Buyer Rules")) return;
          BuyerRulesDB ruleDB = new BuyerRulesDB();
          ruleDB.newRows(
              new BuyerRules[] {new BuyerRules(rule, ruleDB.getBuyerFromName(buyers.getValue()))});
          table.setItems(FXCollections.observableList(ruleDB.getRules()));
          buyerRule.clear();
          ruleDB.close();
        });

    editRule.setOnAction(
        event -> {
          String rule = newRule.getText();
          if (ruleCheck(rule, "Buyer Rules")) return;
          BuyerRulesDB rulesDB = new BuyerRulesDB();
          String old = table.getSelectionModel().getSelectedItem().getPk();
          rulesDB.updateRowsWithRule(
              new BuyerRules[] {new BuyerRules(newRule.getText(), rulesDB.getBuyerFromRegex(old))},
              new String[] {old});
          newRule.clear();
          table.setItems(FXCollections.observableList(rulesDB.getRules()));
          newRule.clear();
          rulesDB.close();
        });
    editBuyer.setOnAction(
        event -> {
          BuyerRulesDB rulesDB = new BuyerRulesDB();
          ObservableList<BuyerRules> rules = table.getSelectionModel().getSelectedItems();
          BuyerRules[] br = new BuyerRules[rules.size()];
          int i = 0;
          for (BuyerRules buyerRules : rules) {
            br[i++] =
                new BuyerRules(buyerRules.getPk(), rulesDB.getBuyerFromName(buyersEdit.getValue()));
          }
          rulesDB.updateRowsWithName(br);
          newRule.clear();
          table.setItems(FXCollections.observableList(rulesDB.getRules()));
          rulesDB.close();
        });
    del.setOnAction(
        event -> {
          BuyerRulesDB rulesDB = new BuyerRulesDB();
          rulesDB.removeRow(
              table.getSelectionModel().getSelectedItems().toArray(new BuyerRules[0]));
          table.setItems(FXCollections.observableList(rulesDB.getRules()));
          rulesDB.close();
        });
    final HBox adds = new HBox(add, buyerRule, buyers);
    adds.setSpacing(5);
    adds.setPadding(new Insets(10, 0, 0, 0));
    final HBox editsRules = new HBox(editRule, newRule);
    editsRules.setSpacing(5);
    editsRules.setPadding(new Insets(10, 0, 10, 0));
    final HBox editsNames = new HBox(editBuyer, buyersEdit);
    editsNames.setSpacing(5);
    editsNames.setPadding(new Insets(10, 0, 10, 0));

    final VBox vbox = new VBox(label, adds, editsRules, editsNames, del);
    vbox.setSpacing(5);
    vbox.setPadding(new Insets(10, 0, 0, 10));
    return vbox;
  }

  private VBox categoryActions(TableView<Categories> table) {
    CategoryDB db = new CategoryDB();
    table.setItems(FXCollections.observableList(db.getCategories()));
    final Label label = new Label("Category Mode");
    final Button add = new Button("Add");
    final TextField createRule = new TextField();
    createRule.setPromptText("Rule");
    createRule.setPrefWidth(100);
    final TextField createCat = new TextField();
    createCat.setPromptText("Name");
    createCat.setPrefWidth(100);
    final Button del = new Button("Delete");
    final Button editName = new Button("Edit Name");
    final TextField newName = new TextField();
    newName.setPromptText("Name");
    newName.setPrefWidth(100);
    final Button editRule = new Button("Edit Rule");
    final TextField newRule = new TextField();
    newRule.setPromptText("Rule");
    newRule.setPrefWidth(100);
    db.close();

    add.setOnAction(
        event -> {
          CategoryDB catDB = new CategoryDB();
          String rule = createRule.getText();
          if (ruleCheck(rule, "Categories")) return;
          Categories category = new Categories(createRule.getText(), createCat.getText());
          createRule.clear();
          createCat.clear();
          catDB.newRows(new Categories[] {category});
          table.setItems(FXCollections.observableList(catDB.getCategories()));
          catDB.close();
        });
    editName.setOnAction(
        event -> {
          CategoryDB catDB = new CategoryDB();
          catDB.updateRowWithName(
              new Categories[] {
                new Categories(
                    table.getSelectionModel().getSelectedItem().getPk(), newName.getText())
              });
          newName.clear();
          table.setItems(FXCollections.observableList(catDB.getCategories()));
          catDB.close();
        });
    editRule.setOnAction(
        event -> {
          String rule = newRule.getText();
          if (ruleCheck(rule, "Category Rules")) return;
          CategoryDB catDB = new CategoryDB();
          catDB.updateRowWithRule(
              new Categories[] {
                new Categories(rule, table.getSelectionModel().getSelectedItem().getName())
              },
              new String[] {table.getSelectionModel().getSelectedItem().getPk()});
          newName.clear();
          table.setItems(FXCollections.observableList(catDB.getCategories()));
          catDB.close();
        });
    del.setOnAction(
        event -> {
          CategoryDB catDB = new CategoryDB();
          catDB.removeRow(table.getSelectionModel().getSelectedItems().toArray(new Categories[0]));
          table.setItems(FXCollections.observableList(catDB.getCategories()));
          newRule.clear();
          catDB.close();
          ;
        });

    final HBox creates = new HBox(add, createRule, createCat);
    creates.setSpacing(5);
    creates.setPadding(new Insets(10, 0, 0, 0));
    final HBox removes = new HBox(del);
    removes.setSpacing(5);
    removes.setPadding(new Insets(10, 0, 0, 0));
    final HBox editsName = new HBox(editName, newName);
    editsName.setSpacing(5);
    editsName.setPadding(new Insets(10, 0, 0, 0));
    final HBox editsRule = new HBox(editRule, newRule);
    editsRule.setSpacing(5);
    editsRule.setPadding(new Insets(10, 0, 0, 0));

    final VBox vbox = new VBox(label, creates, editsName, editsRule, removes);
    vbox.setSpacing(5);
    vbox.setPadding(new Insets(10, 0, 0, 10));
    return vbox;
  }

  private TableView<Buyer> buyerOutput() {
    TableView<Buyer> table = new TableView<>();

    // TODO: Make buyer Col bigger by default
    table.setPlaceholder(new Label("No Buyers in System"));
    table.setEditable(false);
    table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    TableColumn<Buyer, String> buyer = new TableColumn<>("Buyers");
    buyer.setCellValueFactory(new PropertyValueFactory<Buyer, String>("name"));
    table.getColumns().add(buyer);
    BuyerDB db = new BuyerDB("fft.db");
    table.setItems(FXCollections.observableList(db.getBuyers()));
    db.close();

    return table;
  }

  private TableView<BuyerRules> buyerRulesOutput() {
    TableView<BuyerRules> table = new TableView<>();
    table.setPlaceholder(new Label("No Buyer Rules In System"));
    table.setEditable(false);
    table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    TableColumn<BuyerRules, SimpleStringProperty> name = new TableColumn<>("Buyers");
    name.setCellValueFactory(new PropertyValueFactory<BuyerRules, SimpleStringProperty>("name"));
    TableColumn<BuyerRules, SimpleStringProperty> rule = new TableColumn<>("Rules");
    rule.setCellValueFactory(new PropertyValueFactory<BuyerRules, SimpleStringProperty>("rule"));

    table.getColumns().addAll(rule, name);
    BuyerRulesDB db = new BuyerRulesDB();
    table.setItems(FXCollections.observableList(db.getRules()));
    db.close();
    return table;
  }

  private TableView<Categories> categoryOutput() {
    TableView<Categories> table = new TableView<>();
    table.setPlaceholder(new Label("No Categories In System"));
    table.setEditable(false);
    table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    TableColumn<Categories, SimpleStringProperty> rule = new TableColumn<>("Rules");
    rule.setCellValueFactory(new PropertyValueFactory<Categories, SimpleStringProperty>("rule"));
    TableColumn<Categories, SimpleStringProperty> name = new TableColumn<>("Categories");
    name.setCellValueFactory(new PropertyValueFactory<Categories, SimpleStringProperty>("name"));

    table.getColumns().addAll(rule, name);
    CategoryDB db = new CategoryDB();
    db.close();
    return table;
  }

  private boolean ruleCheck(String rule, String group) {
    if (rule.length() < 3) {
      Alert alert = new Alert(AlertType.WARNING);
      alert.setContentText(group + " must 3 or more characters long");
      alert.show();
      return true;
    }
    // Not sure if possible curently, but if it becomes possible just uncomment this line
    // if (rule.indexOf((int) '\n') > 0) {
    //  Alert alert = new Alert(AlertType.WARNING);
    //  alert.setContentText("Cannot have newline in a " + group);
    //  alert.show();
    //  return true;
    // }
    return false;
  }
}
