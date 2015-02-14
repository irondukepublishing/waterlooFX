/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package waterloofx;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javax.swing.JButton;
import javax.swing.JPanel;
import waterloo.fx.plot.Chart;

/**
 *
 * @author ML
 */
public class FXMain extends Application {

    Parent root = null;

    @Override
    public void start(Stage stage) throws Exception {
        String s = (String) getParameters().getNamed().get("fxml");
        if (s == null || s.isEmpty()) {
            s = "fxml/Demo.fxml";
        } else if (!s.startsWith("fxml/")) {
            s = "fxml/".concat(s);
        }
        if (!s.endsWith(".fxml")) {
            s = s.concat(".fxml");
        }
        root = FXMLLoader.load(FXMain.class.getResource(s));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public boolean isRootReady() {
        return root != null;
    }

    public Chart getChart() {
        return (Chart) root.lookup("#myChart");
    }

    public void setMajorGridPainted() {
        getChart().setMajorGridPainted(!getChart().isMajorGridPainted());
        getChart().requestLayout();
    }

    public void setMinorGridPainted() {
        getChart().setMinorGridPainted(!getChart().isMinorGridPainted());
        getChart().requestLayout();
    }

    public void setInnerAxisPainted() {
        getChart().setInnerAxisPainted(!getChart().isInnerAxisPainted());
        getChart().requestLayout();
    }

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

    public void print() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean success = job.printPage(getChart());
            if (success) {
                job.endJob();
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
