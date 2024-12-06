package team05.gui;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.stage.Stage;

// Author hypnotics-dev devhypnotics@proton.me
public class App extends Application {

  @Override
  public void start(Stage stage) throws Exception {
    stage.setTitle("JavaFX test");
    stage.setHeight(720);
    stage.setWidth(1280);

    Scene scene = new Scene(new Group());

    TabPane tabs = new TabPane();
    tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
    View view = new View(stage);
    Edit edit = new Edit(stage);

    tabs.getTabs().addAll(view.getTab(), edit.getTab());

    ((Group) scene.getRoot()).getChildren().addAll(tabs);
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
