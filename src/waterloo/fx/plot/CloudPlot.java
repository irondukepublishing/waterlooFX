/* 
*
 * <http://waterloo.sourceforge.net/>
 *
 * Copyright King's College London 2014.
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

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

/**
 * A cloud plot can be used to display a scatter plot where there are,
 * typically, many tens of thousands of data points.
 *
 * Each point is represented by a single dash on screen. The weight of the dash
 * in pixels is set by the {@code markerRadius} property of the visual model.
 *
 * <strong>Note:</strong>
 * When added to a chart (or strictly its view), a plot description of "plot-N"
 * will be added to the StyleClass of the top plot <em>and its children</em>,
 * where N is equal to {@code chart.getPlots().size() - 1}, i.e. to the number
 * of plots that are children of the view in that chart less one.
 *
 * A common css styling may therefore be applied to family of plots by declaring
 * styles for ".plot-0", ".plot-1" etc. in the style sheet.
 *
 * @author Malcolm Lidierth
 */
public class CloudPlot extends AbstractPlot<ArrayList<Path>> {

    /**
     * Default constructor.
     *
     */
    public CloudPlot() {
//        getStyleClass().add("cloudplot");
        visualElement = new ArrayList<>();
    }

    /**
     * Constructs an instance parenting another plot.
     *
     * @param p1 the child plot to add to this instance.
     */
    public CloudPlot(AbstractPlot p1) {
        this();
        super.add(p1);
    }

    @Override
    protected final void updateElements(Chart chart) {
        if (visualElement.isEmpty()) {
            visualElement.add(new Path());
            addElements();
        }
        Path path = visualElement.get(0);
        ObservableList<PathElement> elements = path.getElements();

        if (elements.size() < dataModel.size() * 2) {
            // Adding new data points
            int start = elements.size();
            for (int k = start; k < dataModel.size(); k++) {
                Point2D p0 = getData(chart,dataModel.getXData().get(k), dataModel.getYData().get(k));
                p0 = chart.toPixel(p0);
                if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY())) {
                    elements.add(new MoveTo(0, 0));
                    elements.add(new LineTo(0, 0));
                }
            }
        } else {
            // Removing data points
            elements.removeAll(elements.subList(dataModel.size() * 2, elements.size()));
        }
    }

    @Override
    public final void arrangePlot(Chart chart) {
        super.arrangePlot(chart);
        Path path = visualElement.get(0);
        ObservableList<PathElement> elements = path.getElements();
        path.setStroke(getFill());
        //TODO: Debug check
//        if (elements.size()*2!=dataModel.size()){
//            System.err.println("Cloud plot out of synch");
//        }
        for (int k = 0; k < dataModel.size(); k++) {
            Point2D p0 = getData(chart,dataModel.getXData().get(k), dataModel.getYData().get(k));
            p0 = chart.toPixel(p0);
            if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY())) {
                ((MoveTo) elements.get(k * 2)).setX(p0.getX());
                ((MoveTo) elements.get(k * 2)).setY(p0.getY());
                ((LineTo) elements.get(k * 2 + 1)).setX(p0.getX());
                ((LineTo) elements.get(k * 2 + 1)).setY(p0.getY());
            }
        }
    }

}
