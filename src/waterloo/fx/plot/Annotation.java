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

import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 *
 * @author Malcolm Lidierth
 */
public class Annotation extends Pane {

    private double x;
    private double y;

    public Annotation() {
        super();
        getChildren().addListener((ListChangeListener.Change<? extends Node> c) -> {
            while (c.next()) {
                List<? extends Node> list = c.getAddedSubList();
                list.stream().forEach(node -> {
                    if (node instanceof Region) {
                        node.setLayoutX(0d);
                        node.setLayoutY(0d);
                        prefWidthProperty().bind(((Region) node).widthProperty());
                        prefHeightProperty().bind(((Region) node).heightProperty());
                    }
                });
            }
        });

    }

    public Annotation(Node item) {
        super(item);
    }

    public Annotation(Node item, double x, double y) {
        this(item);
        this.x = x;
        this.y = y;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * @return the wrappedItem
     */
    public Node getWrappedItem() {
        return getChildren().get(0);
    }

}
