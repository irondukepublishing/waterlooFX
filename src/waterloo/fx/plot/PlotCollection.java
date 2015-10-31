/* 
*
 * <http://sigtool.github.io/waterlooFX/>
 *
 * Copyright King's College London  2014. Copyright Ironduke Publishing Limited, UK 2014-.
 * 
 * @author Malcolm Lidierth <a href="https://github.com/sigtool/waterlooFX/issues"> [Contact]</a>
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
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * The plot collection class is intended for use when the appearance of a plot
 * is dependent upon others in the same collection. For example, with stacked
 * bars in a bar plot, the position of a bar depends on the positions of bars
 * painted earlier in the series.
 *
 * The PlotCollection class serves as a wrapper only. Rendering of plots needs
 * to be implemented in the plot class methods.
 *
 *
 * @author Malcolm Lidierth
 *
 * @param <T> the type of plot for this collection. Note that the getChildren
 * returns a list of Nodes, so any plot type can be added to the collection
 * regardless of this setting.
 */
public class PlotCollection<T extends AbstractPlot> extends StackPane {

    public PlotCollection() {
        setBackground(Background.EMPTY);
        parentProperty().addListener((ObservableValue<? extends Parent> ov, Parent t, Parent t1) -> {
            if (t1 instanceof Region) {
                setLayoutX(0d);
                setLayoutY(0d);
                prefWidthProperty().bind(((Region) t1).widthProperty());
                prefHeightProperty().bind(((Region) t1).heightProperty());
            }
            requestParentLayout();
        });

        getChildren().addListener((ListChangeListener.Change<? extends Node> c) -> {
//            while (c.next()) {
//                List<? extends Node> list = c.getAddedSubList();
//                list.forEach(x -> {
//                    if (x instanceof AbstractPlot) {
//                        addStyle((AbstractPlot) x);
//                    }
//                });
//            }
        });
    }

    @SuppressWarnings("unchecked")
    public PlotCollection(T... p) {
        super(p[0]);
        for (int k = 1; k < p.length; k++) {
            getChildren().add(p[k]);
        }
    }

//    @SuppressWarnings("unchecked")
//    private void addStyle(AbstractPlot child) {
//        // Process the added plot...
//        int index = getChildren().indexOf(child);
//        child.setPlotStyleIndex(index);
//        //... and any descendant plots
//        child.getAllPlots().forEach(x -> {
//            ((AbstractPlot) x).setPlotStyleIndex(index);
//        });
//    }

    public void arrangePlots(Chart chart) {
        getChildren().forEach(x -> {
            ((AbstractPlot) x).arrangePlot(chart);
        });
        requestLayout();
    }

}
