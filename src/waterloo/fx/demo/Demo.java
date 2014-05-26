/* 
 * This code is part of Project Waterloo from King's College London
 * <http://waterloo.sourceforge.net/>
 *
 * Copyright King's College London  2014-
 * 
 * @author Malcolm Lidierth, King's College London <a href="http://sourceforge.net/p/waterloo/discussion/"> [Contact]</a>
 * 
 * Project Waterloo is free software:  you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project Waterloo is distributed in the hope that it will  be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package waterloo.fx.demo;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import waterloo.fx.css.StyleSheetManager;
import waterloo.fx.plot.Chart;
import waterloo.fx.plot.AbstractPlot;
import waterloo.fx.plot.BarExtra;
import waterloo.fx.plot.BarPlot;
import waterloo.fx.plot.FeatherPlot;
import waterloo.fx.plot.LinePlot;
import waterloo.fx.plot.PlotCollection;
import waterloo.fx.plot.ScatterPlot;


/**
 *
 * @author Malcolm Lidierth
 */
public class Demo extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        System.err.println(System.getProperty("java.version"));

        //GJChart g1 = new Chart();
        Chart g = new Chart();
        //g.getChildren().add(g1);

        //LinePlot lp = GJLinePlotLine();
        
        
        BarPlot bar0=new BarPlot();
        ((BarExtra)bar0.getDataModel().extraObject).setJustification(BarExtra.JUSTIFICATION.CENTERED);
        ((BarExtra)bar0.getDataModel().extraObject).setMode(BarExtra.MODE.STACKED);
        ((BarExtra)bar0.getDataModel().extraObject).setOrientation(BarExtra.ORIENTATION.HORIZONTAL);
        //collection.getChildren().add(bar0);
        BarPlot bar1=new BarPlot();
        ((BarExtra)bar1.getDataModel().extraObject).setJustification(BarExtra.JUSTIFICATION.CENTERED);
        ((BarExtra)bar1.getDataModel().extraObject).setMode(BarExtra.MODE.STACKED);
        ((BarExtra)bar1.getDataModel().extraObject).setOrientation(BarExtra.ORIENTATION.HORIZONTAL);
        //collection.getChildren().add(bar1);
        BarPlot bar2=new BarPlot();
        ((BarExtra)bar2.getDataModel().extraObject).setJustification(BarExtra.JUSTIFICATION.CENTERED);
        ((BarExtra)bar2.getDataModel().extraObject).setMode(BarExtra.MODE.STACKED);
        ((BarExtra)bar2.getDataModel().extraObject).setOrientation(BarExtra.ORIENTATION.HORIZONTAL);
        //collection.getChildren().add(bar2);
        
        PlotCollection<BarPlot> collection = new PlotCollection<>(bar0, bar1, bar2);
        

        //((BarExtra)s.getDataModel().extraObject).setBaseValue(1d);
        //AbstractPlot s1 = GJScatterPlottter(GJErrorBarPlotrGJLinePlotw GJLine()));
//        AbstractPlot sGJScatterPlotJScatter();
//        GJAbstractGJLinePlotp=new GJLine();
//        lp.setDataModel(s2.getDataModel());
//        s2.getChildren().add(lp);

//        s.getChildren().add(s1);
//        s1.getChildren().add(s2);
        //lp.setEffect(new DropShadow(2d, 5d, 5d, Color.BLUE));
        //g.getChildren().add(lp);

        //g.getChildren().add(s1);
       

        AbstractPlot line = new LinePlot();
        AbstractPlot scatter = new ScatterPlot();
        line.getChildren().add(scatter);
        g.getChildren().add(line);
        
        //System.out.println(line.getDataModel().xSummary());

        
       

//        g.getStylesheets().add(StyleSheetManager.getCss());
//        s.getStylesheets().add(StyleSheetManager.getCss());
        FlowPane root = new FlowPane(g);

        g.setViewAspectRatio(1d);

        Scene scene = new Scene(root, 500, 500);
        //scene.getStylesheets().add(StyleSheetManager.getCss());

        primaryStage.setTitle("WaterooFX Demo: Scientific charting for JavaFX8+");
        primaryStage.setScene(scene);

        primaryStage.show();
        
        bar0.getVisualModel().setFill(Color.RED);
        bar1.getVisualModel().setFill(Color.GREEN);
        bar2.getVisualModel().setFill(Color.BLUE);
        
        bar2.setLabels(new Text("Item 1"), new Text("Item 2"));
        
        g.setInnerAxisPainted(true);

        System.out.println(g.getAltFillHorizontal());
//        System.out.println(s.getVisualModel().getMarker(0).getEffect());
//
//
//        s.refresh();
//        s.getDataModel().xData.add(2d);
//        s.getDataModel().yData.add(2d);

        //((BarExtra)s.getDataModel().extraObject).setJustification(BarExtra.JUSTIFICATION.CENTERED);
        
        g.setXLeft(-20d);
        g.setXRight(20d);
        g.setYBottom(-20d);
        g.setYTop(20d);
        System.err.println(g.toPixel(0d,0d));
        System.err.println(g.getView().getPrefWidth()/2d);
        System.err.println(g.getView().getPrefHeight()/2d);
        System.err.println(g.toPositionX(g.toPixel(0d,0d).getX()));

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
