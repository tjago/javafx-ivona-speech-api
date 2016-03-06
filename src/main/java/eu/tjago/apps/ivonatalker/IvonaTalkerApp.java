package eu.tjago.apps.ivonatalker;

import eu.tjago.apps.ivonatalker.api.IvonaCredentials;
import eu.tjago.apps.ivonatalker.api.SpeechCloudSingleton;
import eu.tjago.apps.ivonatalker.controller.AboutModalController;
import eu.tjago.apps.ivonatalker.controller.AwsCredentialsController;
import eu.tjago.apps.ivonatalker.controller.ContentAreaController;
import eu.tjago.apps.ivonatalker.controller.MainWindowController;
import eu.tjago.apps.ivonatalker.util.Constants;
import eu.tjago.apps.ivonatalker.util.CredentialsXmlWrapper;
import eu.tjago.apps.ivonatalker.util.FileHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.prefs.Preferences;

/**
 * Created by Tomasz on 2015-12-24.
 */
public class IvonaTalkerApp extends Application {

    private Stage primaryStage;
    private BorderPane mainWindowLayout;
    private IvonaCredentials credentials;

    public static void main(String[] args) {
        System.out.println("working dir: "  + System.getProperty("user.dir") );
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Ivona Talker Application");
        this.primaryStage.setResizable(false);

        initMainWindow();
        loadCredentials();
        SpeechCloudSingleton.initSpeechCloudClient(credentials);

        //after credentials are set init Center Content
        setCenterContent();
    }

    /**
     * Exit program
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {

        // remove temporary .mp3 file
        Files.delete(SpeechCloudSingleton.getTmpSpeechFile().toPath());

        Platform.exit();
    }

    /**
     * Initializes the root layout.
     */
    public void initMainWindow() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(IvonaTalkerApp.class.getResource("/view/MainWindow.fxml"));
            mainWindowLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(mainWindowLayout);
            primaryStage.setScene(scene);

            // Give the controller access to the main app.
            MainWindowController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
        //try to
//       loadCredentials();
    }

    public void setCredentials(IvonaCredentials credentials) {
        this.credentials = credentials;
        saveCredentialsToFile(credentials);
    }

    public IvonaCredentials getCredentials() {
        return credentials;
    }

    public void setCenterContent() {
        try {
            // Load Content
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(IvonaTalkerApp.class.getResource("/view/ContentArea.fxml"));
            AnchorPane centerContent = (AnchorPane) loader.load();

            mainWindowLayout.setCenter(centerContent);

            // Give the controller access to the main app.
            ContentAreaController controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Credentials dialog init
     */
    public void showCredentialsDialog() {

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(IvonaTalkerApp.class.getResource("/view/CredentialsModal.fxml"));
            AnchorPane credentialsDialog = (AnchorPane) loader.load();

            // Create Credentials dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Credentials");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);

            Scene scene = new Scene(credentialsDialog);
            dialogStage.setScene(scene);
            dialogStage.show();


            AwsCredentialsController controller = loader.getController();
            controller.setMainApp(this);
            controller.setStage(dialogStage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * About dialog init
     */
    public void showAboutDialog() {

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(IvonaTalkerApp.class.getResource("/view/AboutModal.fxml"));
            AnchorPane aboutDialog = (AnchorPane) loader.load();

            // Create Credentials dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("About Ivona Talker");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);

            AboutModalController controller = loader.getController();
            controller.setMainApp(this);
            controller.setStage(dialogStage);

            Scene scene = new Scene(aboutDialog);
            dialogStage.setScene(scene);
            dialogStage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load Ivona Cloud Api Credentials
     */
    public void loadCredentials() {
        this.credentials = FileHelper.loadCredentialsFromFile(Constants.CREDENTIALS_XML_FILENAME);
    }

    /**
     * Saves user and pass to xml file
     * @param credentials
     */
    public void saveCredentialsToFile(IvonaCredentials credentials) {

        File file = new File(Constants.CREDENTIALS_XML_FILENAME);
        try {

            JAXBContext context = JAXBContext.newInstance(CredentialsXmlWrapper.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Marshalling and saving XML to the file.
            m.marshal(new CredentialsXmlWrapper(credentials), file);

        } catch (Exception e) { // catches ANY exception
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save data");
            alert.setContentText("Could not save data to file:\n" + file.getPath());
            e.printStackTrace();

            alert.showAndWait();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}