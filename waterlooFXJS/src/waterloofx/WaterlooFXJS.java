/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package waterloofx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import waterloo.fx.plot.AbstractPlot.VisualModel;
import waterloo.fx.plot.*;

/**
 *
 * @author ML
 */
public class WaterlooFXJS extends Application {

    Parent root = null;

    @Override
    public void start(Stage stage) throws Exception {
        String s = (String) getParameters().getNamed().get("fxml");
        if (s == null || s.isEmpty()) {
            Chart chart = new Chart();
            chart.setId("myChart");
            chart.setLeftAxisTitle("Y");
            chart.setBottomAxisTitle("X");
            root = new Pane(new Chart());
        } else if (!s.startsWith("fxml/")) {
            s = "fxml/".concat(s);
            if (!s.endsWith(".fxml")) {
                s = s.concat(".fxml");
            }
            root = FXMLLoader.load(WaterlooFXJS.class.getResource(s));
        }
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public boolean isRootVisible() {
        if (root == null) {
            return false;
        } else {
            return root.isVisible();
        }
    }

    public Chart getChart() {
        return (Chart) root.lookup("#myChart");
    }

    public void add(Node node) throws InterruptedException {
        int counter = 0;
        if (!isRootVisible()) {
            Thread.sleep(20L);
            if (counter++ > 250) {
                return;
            }
        }
        Platform.runLater(() -> {
            getChart().getChildren().add(node);
            getChart().requestLayout();
        });
    }

    public Object parseFXML(String s) {
        // HACKS FOR JAVASCRIPT SUPPORT OF JAVA ENUMS
        s = s.replace("markerType=", "markerTypeAsString=");
        FXMLLoader loader = new FXMLLoader();
              
        InputStream stream;
        try {
            //StringReader reader = new StringReader(s);
            stream = new ByteArrayInputStream(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
        Node node=null;
        try {
            node = loader.load(stream);
        } catch (IOException ex) {
            return ex.toString();
        }
        try {
            stream.close();
        } catch (IOException ex) {
            return ex.toString();
        }
        return node;
    }



    public static VisualModel newVisualModel() {
        return new ScatterPlot().getVisualModel();
    }
//
//    public void setMajorGridPainted() {
//        getChart().setMajorGridPainted(!getChart().isMajorGridPainted());
//        getChart().requestLayout();
//    }
//
//    public void setMinorGridPainted() {
//        getChart().setMinorGridPainted(!getChart().isMinorGridPainted());
//        getChart().requestLayout();
//    }
//
//    public void setInnerAxisPainted() {
//        getChart().setInnerAxisPainted(!getChart().isInnerAxisPainted());
//        getChart().requestLayout();
//    }
//

    public void verticalFill() {
        Paint color = getChart().getAltFillVertical();
        if (color == Color.TRANSPARENT) {
            getChart().setAltFillVertical(new Color(0f, 0f, 1f, 0.05f));
        } else {
            getChart().setAltFillVertical(Color.TRANSPARENT);
        }
        getChart().requestLayout();
    }

    public void horzFill() {
        Paint color = getChart().getAltFillHorizontal();
        if (color == Color.TRANSPARENT) {
            getChart().setAltFillHorizontal(new Color(0f, 0f, 1f, 0.05f));
        } else {
            getChart().setAltFillHorizontal(Color.TRANSPARENT);
        }
        getChart().requestLayout();
    }

//    public void print() {
//        Chart chart = getChart();
//        PrinterJob job = PrinterJob.createPrinterJob();
//        if (job != null && chart != null) {
//            boolean success = job.showPageSetupDialog(null);
//            if (success) {
//                success = job.printPage(chart);
//                if (success) {
//                    job.endJob();
//                }
//            }
//        }
//    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
