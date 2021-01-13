import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main extends Application {

  private Predict model = new Predict("./final_model.h5", 100, 100);
  private Stage stage;
  private final double AR = 800d / 600;
  private Image positive;
  private Image botPositive;
  private Image negative;
  private Image botNegative;

  @Override
  public void start(Stage stage) {

    this.stage = stage;
    positive = new Image("file:res/positive.png");
    botPositive = new Image("file:res/bot_positive.png");
    negative = new Image("file:res/negative.png");
    botNegative = new Image("file:res/bot_negative.png");

    stage.setScene(constructHome());
    stage.setTitle("Seefood");
    stage.show();

  }

  private Scene constructHome() {
    Pane contents = new Pane();
    Scene scene = new Scene(contents, 800, 800);
    ImageView logo = new ImageView("file:res/seefood.png");
    logo.setX(0);
    logo.setY(0);
    Rectangle bar = new Rectangle(800, 10);
    bar.setX(0);
    bar.setY(200);
    bar.setFill(Color.BLACK);
    ImageView desc = new ImageView("file:res/desc.png");
    desc.setX(0);
    desc.setY(210);
    Rectangle bar2 = new Rectangle(800, 10);
    bar2.setX(0);
    bar2.setY(320);
    bar2.setFill(Color.BLACK);
    ImageView top = new ImageView("file:res/main_top.png");
    top.setX(0);
    top.setY(330);
    ImageView bot = new ImageView("file:res/main_bot.png");
    bot.setX(0);
    bot.setY(565);
    Button start = new Button("Start");
    start.setLayoutX(310);
    start.setLayoutY(515);
    start.setMinWidth(200);
    start.setMinHeight(100);
    start.setStyle("-fx-background-radius: 60");
    start.setFont(generateFont(48));
    start.setOnMouseClicked((e) -> {

      FileChooser fileChooser = new FileChooser();
      fileChooser.setInitialDirectory(new File("."));
      fileChooser.setTitle("Select Picture to Analyze");
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
      File pic = fileChooser.showOpenDialog(stage);
      if (pic == null) {
        return;
      }
      double res = model.predict(pic.getPath());
      stage.setScene(constructResults(pic, res));
    });
    contents.getChildren().add(logo);
    contents.getChildren().add(bar);
    contents.getChildren().add(desc);
    contents.getChildren().add(bar2);
    contents.getChildren().add(top);
    contents.getChildren().add(bot);
    contents.getChildren().add(start);
    start.setOnMouseEntered(event -> scene.setCursor(Cursor.HAND));
    start.setOnMouseExited(event -> scene.setCursor(Cursor.DEFAULT));
    return scene;
  }

  private Scene constructResults(File pic, double odds) {
    boolean hotdog = odds < 0.5;
    Image overlay = hotdog ? positive : negative;
    Image botOverlay = hotdog ? botPositive : botNegative;
    Group group = new Group();
    Canvas canvas = new Canvas(800, 800);
    group.getChildren().add(canvas);
    Scene scene = new Scene(group);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    Image file = new Image("file:" + pic.getAbsolutePath());
    double w = file.getWidth();
    double h = file.getHeight();
    double ar = w / h;
    // Need to fill such that overflow is top/bot
    if (AR > ar) {
      double cutoff = (AR * h - w) / AR / 2;
      gc.drawImage(file, 0, cutoff, w, h - cutoff, 0, 100, 800, 600);
    }
    // Fill with overflow left/right
    else {
      double cutoff = (AR * w - h) / AR / 2;
      gc.drawImage(file, cutoff, 0, w - cutoff, h, 0, 100, 800, 600);
    }
    gc.drawImage(overlay, 0, 0);
    gc.drawImage(botOverlay, 0, 700);
    scene.setOnMouseClicked((ev) -> stage.setScene(constructHome()));
    return scene;
  }

  private Font generateFont(int size) {
    try {
      return Font.loadFont(new FileInputStream(new File("./res/solomon.otf")), size);
    }
    catch (IOException e) {
      e.printStackTrace();
      return new Font("Comic Sans MS", size);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
