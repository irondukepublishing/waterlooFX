/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package waterloo.fx.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import waterloo.fx.plot.Chart;


/**
 *
 * @author ML
 */
public class WaterlooFXDemo extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Demo.fxml"));
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.setTitle("waterlooFX Demo");
        stage.show();
        
        //Creates a bidirectional link on the x-axes for the two specified charts
        ((Chart) root.lookup("#linechart")).addAxisLinkXX((Chart) root.lookup("#barchart"));
        ((Chart) root.lookup("#plot0")).addAxisLinkXX((Chart) root.lookup("#plot1"));
        
        System.out.println(((Chart) root.lookup("#linechart")).getView().isPickOnBounds());

}

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
