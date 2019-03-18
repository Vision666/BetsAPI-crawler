package by.vision.betsapiparser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import sun.misc.Unsafe;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;


public class App extends Application {
    String artifactId;
    String version;
    static FileWriter fileWriter;
    static {
        try {
            fileWriter = new FileWriter("c:/temp/samplefile1.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init(){
        //disable warning "An illegal reflective access operation has occurred"
        disableWarning();
        initTelegBotsAPI();
        try {
            new FileWriter("c:/temp/proxy_list.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("scene.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        setTitle(stage);

        stage.setScene(scene);
        stage.setMinHeight(480);
        stage.setMinWidth(800);
        stage.getIcons().add(new Image("file:..\\BetsAPI-parser\\src\\main\\resources\\images\\icon.png"));
        stage.show();
    }

    public void setTitle(Stage stage) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        try {
            model = reader.read(new FileReader("pom.xml"));
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        assert model != null;
        stage.setTitle(model.getArtifactId() + " " + model.getVersion());
    }

    @Override
    public void stop(){
        System.exit(0);
    }

    public static void initTelegBotsAPI() {
        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new TelegramBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void disableWarning() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe u = (Unsafe) theUnsafe.get(null);
            Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
        } catch (Exception e) {
            // ignore
        }
    }

}