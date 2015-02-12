/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package waterloofx;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import waterloo.fx.plot.Chart;

/**
 *
 * @author ML
 */
public class FXMain extends Application {

    Chart chart;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Demo.fxml"));
        Scene scene = new Scene(root);

        stage.setScene(scene);
        
        stage.show();
        chart = ((Chart) root.lookup("#myChart"));

    }
    
    public Chart getChart(){
        return chart;
    }

    public void setMajorGridPainted(){
        chart.setMajorGridPainted(!chart.isMajorGridPainted());
        chart.requestLayout();
    }
    
    public void setMinorGridPainted(){
        chart.setMinorGridPainted(!chart.isMinorGridPainted());
        chart.requestLayout();
    }
    
    public void setInnerAxisPainted(){
        chart.setInnerAxisPainted(!chart.isInnerAxisPainted());
        chart.requestLayout();
    }

    public void verticalFill(){
        Paint color = chart.getAltFillVertical();
        if (color==Color.TRANSPARENT)
                chart.setAltFillVertical(new Color(0f,0f,1f,0.05f));
        else 
            chart.setAltFillVertical(Color.TRANSPARENT);
        chart.requestLayout();
    }
    
    public void horzFill(){
        Paint color = chart.getAltFillHorizontal();
        if (color==Color.TRANSPARENT)
                chart.setAltFillHorizontal(new Color(0f,0f,1f,0.05f));
        else 
            chart.setAltFillHorizontal(Color.TRANSPARENT);
        chart.requestLayout();
    }


//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
//        launch(args);
//    }
}
