/* 
*
 * <http://waterloo.sourceforge.net/>
 *
 * Copyright King's College London  2014-
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
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.shape.Polyline;

/**
 * Line plot class.
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
public class LinePlot extends AbstractPlot<ArrayList<Polyline>> implements LineInterface {

    /**
     * Default constructor.
     *
     */
    public LinePlot() {
        getStyleClass().add("lineplot");
        visualElement = new ArrayList<>();
    }

    /**
     * Constructs an instance parenting another plot.
     *
     * <em>The data model of the child will be copied by reference to the new
     * parent instance.</em>
     *
     * Compound plots that share a data model may therefore be constructed by
     * chaining constructor calls, e.g.:
     * <p>
     * {@code GJScatter = new GJScatter(new GJLine(new GJErrorBar));}
     * </p>
     *
     * Further plots may be added by calling the {@code add(AbstractPlot p)}
     * method and will also share the data model. Note that data model are
     * <strong>not</strong> shared when using the standard
     * <p>
     * {@code getChildren().add(...)}
     * </p>
     * method.
     *
     * @param p1 the child plot to add to this instance.
     */
    public LinePlot(AbstractPlot p1) {
        this();
        super.add(p1);
    }

    @Override
    protected final void updateElements(Chart chart) {

        Polyline line = new Polyline();
        for (int k = 0; k < dataModel.size(); k++) {
            Point2D p0 = getData(chart,dataModel.getXData().get(k), dataModel.getYData().get(k));
            p0 = chart.toPixel(p0);
            if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY())) {
                // If this is the first entry for a new line, add that line to
                // the visualElement list
                if (!visualElement.contains(line)) {
                    visualElement.add(line);
                }
            } else {
                // Non - finite value: start a new line unless the exisiting one
                // is empty - which it will be if we a skipping multiple non-finite
                // values
                if (line.getPoints().size() > 0) {
                    line = new Polyline();
                }
            }
        }
        addElements();
    }

    @Override
    public final void arrangePlot(Chart chart) {

        // Call the super method to do house-keeping common to all plots
        super.arrangePlot(chart);

        // Create the first line
        Polyline line = visualElement.get(0);
        line.getPoints().clear();
        line.setCursor(Cursor.CROSSHAIR);
        line.setStrokeWidth(visualModel.getLineWidth());
        line.setStroke(visualModel.getLineColor());

        for (int k = 0; k < dataModel.size(); k++) {
            Point2D p0 = getData(chart,dataModel.getXData().get(k), dataModel.getYData().get(k));
            p0 = chart.toPixel(p0);
            if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY())) {
                line.getPoints().addAll(p0.getX(), p0.getY());
            } else {
                if (line.getPoints().size() > 0) {
                    line = visualElement.get(k);
                    line.getPoints().clear();
                }
            }
        }
    }

}
