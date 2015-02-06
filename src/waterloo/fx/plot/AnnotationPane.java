/* 
 *
 * <http://waterloo.sourceforge.net/>
 *
 * Copyright King's College London  2014. Copyright Malcolm Lidierth 2014-.
 * 
 * @author Malcolm Lidierth <a href="http://sourceforge.net/p/waterloo/discussion/"> [Contact]</a>
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
package waterloo.fx.plot;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 *
 * @author Malcolm Lidierth
 */
public class AnnotationPane extends Pane {

    public AnnotationPane() {
        setBackground(Background.EMPTY);
        parentProperty().addListener((ObservableValue<? extends Parent> ov, Parent t, Parent t1) -> {
            if (t1 instanceof Region) {
                setLayoutX(0d);
                setLayoutY(0d);
                prefWidthProperty().bind(((Region) t1).widthProperty());
                prefHeightProperty().bind(((Region) t1).heightProperty());
            }
//            // Update the layout
//            // Note this will impose any aspect ratio restriction
//            requestParentLayout();
        });

    }

    @Override
    public void layoutChildren() {
        getChildren().forEach(x -> {
            if (x instanceof Annotation) {
                Annotation annotation = (Annotation) x;
                if (getGraph() != null) {
                    Point2D p0 = getGraph().toPixel(annotation.getX(), annotation.getY());
                    annotation.setLayoutX(p0.getX());
                    annotation.setLayoutY(p0.getY());
                }
            }
        });
        super.layoutChildren();
    }

    private Chart getGraph() {
        Node parent = getParent();
        while (!(parent instanceof Chart) && parent != null) {
            parent = parent.getParent();
        }
        return (Chart) parent;
    }

}
