package waterloo.fx.plot.axis;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import waterloo.fx.plot.Chart;

/**
 *
 * @author ML
 */
public class CrossHair extends Path {

    private Chart layer;
    private Text xPosText = new Text("X");
    private Text yPosText = new Text("Y");
    

    public CrossHair(Chart chart) {
        layer = chart;
        parentProperty().addListener((ObservableValue<? extends Parent> ov, Parent t, Parent t1) -> {
            Pane pane = (Pane) getParent();
            getElements().add(new MoveTo(0, layer.mouseY().get()));
            getElements().add(new LineTo(pane.getWidth(), layer.mouseY().get() / 2d));
            getElements().add(new MoveTo(layer.mouseX().get(), 0));
            getElements().add(new LineTo(pane.getWidth() / 2d, pane.getHeight()));
            bind();
        });
        chart.getView().getChildren().add(this);
        yPosText.setTextOrigin(VPos.CENTER);
        chart.getChildren().add(xPosText);
        chart.getChildren().add(yPosText);
        this.setStroke(Color.GREEN);
    }

    private void bind() {
        Pane pane = (Pane) getParent();
        ((MoveTo) getElements().get(0)).yProperty().bind(layer.mouseY());

        ((LineTo) getElements().get(1)).xProperty().bind(pane.widthProperty());
        ((LineTo) getElements().get(1)).yProperty().bind(layer.mouseY());

        ((MoveTo) getElements().get(2)).xProperty().bind(layer.mouseX());

        ((LineTo) getElements().get(3)).xProperty().bind(layer.mouseX());
        ((LineTo) getElements().get(3)).yProperty().bind(pane.heightProperty());
        
        xPosText.textProperty().bind(layer.mouseX().asString("%g"));
    
        xPosText.xProperty().bind(layer.mouseX().add(xPosText.prefWidth(-1)/2d));
        xPosText.yProperty().bind(layer.getView().layoutYProperty());
        
        yPosText.textProperty().bind(layer.mouseY().asString("%g"));
        yPosText.yProperty().bind(layer.mouseY().add(layer.getView().layoutYProperty()));
        yPosText.xProperty().bind(layer.getView().widthProperty().add(layer.getView().layoutXProperty()));
    }

    public void unbind() {
        ((MoveTo) getElements().get(0)).yProperty().unbind();
        ((LineTo) getElements().get(1)).xProperty().unbind();
        ((LineTo) getElements().get(1)).yProperty().unbind();
        ((MoveTo) getElements().get(2)).xProperty().unbind();
        ((LineTo) getElements().get(3)).xProperty().unbind();
        ((LineTo) getElements().get(3)).yProperty().unbind();
        layer.getView().getChildren().remove(this);
        layer.getChildren().remove(xPosText);
        layer.getChildren().remove(yPosText);
        xPosText=null;
        yPosText=null;
    }

}
