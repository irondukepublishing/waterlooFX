package waterloo.fx.plot.axis;

import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 *
 * @author ML
 */
public class CrossHair extends Path {

    public CrossHair() {
        parentProperty().addListener((ObservableValue<? extends Parent> ov, Parent t, Parent t1) -> {
            Pane pane = (Pane) getParent();
            getElements().add(new MoveTo(0, pane.getHeight() / 2d));
            getElements().add(new LineTo(pane.getWidth(), pane.getHeight() / 2d));
            getElements().add(new MoveTo(pane.getWidth() / 2d, 0));
            getElements().add(new LineTo(pane.getWidth() /2d, pane.getHeight()));
            
            ((MoveTo) getElements().get(0)).yProperty().bind(pane.heightProperty().divide(2d));
            
            ((LineTo) getElements().get(1)).xProperty().bind(pane.widthProperty());
            ((LineTo) getElements().get(1)).yProperty().bind(pane.heightProperty().divide(2d));
            
            ((MoveTo) getElements().get(2)).xProperty().bind(pane.widthProperty().divide(2d));
            
            ((LineTo) getElements().get(3)).xProperty().bind(pane.widthProperty().divide(2d));
            ((LineTo) getElements().get(3)).yProperty().bind(pane.heightProperty());
        });
        this.setStroke(Color.GREEN);
    }

}
