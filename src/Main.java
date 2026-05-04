import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import db.DatabaseConnection;
import java.net.URL;
import java.io.File;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseConnection.testConnection();
        
        // Load login screen - Try multiple paths
        Parent root = loadFXML("login.fxml");
        
        if (root == null) {
            System.err.println("Cannot find login.fxml! Check file location.");
            return;
        }
        
        primaryStage.setTitle("Karimi Dental Clinic - Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    private Parent loadFXML(String fxmlFile) {
        // Try different paths
        String[] paths = {
            "/view/" + fxmlFile,           // For packaged structure
            "/src/view/" + fxmlFile,       // Alternative package structure
            "view/" + fxmlFile,            // Relative path
            "src/view/" + fxmlFile,        // Source folder
            fxmlFile                       // Same folder
        };
        
        for (String path : paths) {
            try {
                URL url = getClass().getResource(path);
                if (url == null) {
                    // Try as file
                    File file = new File(path);
                    if (file.exists()) {
                        url = file.toURI().toURL();
                    }
                }
                
                if (url != null) {
                    System.out.println("Loading FXML from: " + url);
                    FXMLLoader loader = new FXMLLoader(url);
                    return loader.load();
                }
            } catch (Exception e) {
                // Continue to next path
            }
        }
        
        // Last resort - try absolute path
        try {
            String userDir = System.getProperty("user.dir");
            String[] absPaths = {
                userDir + "/src/view/" + fxmlFile,
                userDir + "/view/" + fxmlFile,
                userDir + "/" + fxmlFile
            };
            
            for (String absPath : absPaths) {
                File file = new File(absPath);
                if (file.exists()) {
                    System.out.println("Loading FXML from absolute path: " + absPath);
                    FXMLLoader loader = new FXMLLoader(file.toURI().toURL());
                    return loader.load();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load FXML: " + e.getMessage());
        }
        
        System.err.println("Could not find " + fxmlFile);
        return null;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}