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

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import waterloo.fx.markers.ArrowHead;

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
public class FeatherPlot extends AbstractPlot<ArrayList<Shape>> implements LineInterface, BaseValueSensitiveInterface {

    /**
     * Default constructor.
     *
     */
    public FeatherPlot() {
//        getStyleClass().add("featherplot");
        visualElement = new ArrayList<>();
        setMarkerType(MARKERTYPE.ARROWHEAD);
        setMarkerRadius(7d);
    }

    /**
     * Constructs an instance parenting another plot.
     *
     * @param p1 the child plot to add to this instance.
     */
    public FeatherPlot(AbstractPlot p1) {
        this();
        super.add(p1);
    }

    @Override
    protected final void updateElements(Chart chart) {
        for (int k = 0; k < dataModel.size(); k++) {
            Point2D p0 = getData(chart,dataModel.getXData().get(k), dataModel.getYData().get(k));
            p0 = chart.toPixel(p0);
            if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY())) {
                // If this is the first entry for a new line, add that line to
                // the visualElement list
                visualElement.add(new Line());
                visualElement.add((Shape) visualModel.getMarker(k));
            } else {
                visualElement.add(null);
                visualElement.add(null);
            }
        }
        addElements();
    }

    @Override
    public final void arrangePlot(Chart chart) {

        // Call the super method to do house-keeping common to all plots
        super.arrangePlot(chart);

        for (int k = 0; k < dataModel.getXData().size(); k++) {
            Point2D p0 = getData(chart,k, dataModel.getBaseValue());
            Point2D p1 = getData(chart,k + dataModel.getXData().get(k), dataModel.getYData().get(k));
            p0 = chart.toPixel(p0);
            p1 = chart.toPixel(p1);
            if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY())) {
                Line line = (Line) visualElement.get(k * 2);
                if (line != null) {
                    line.setStartX(p0.getX());
                    line.setStartY(p0.getY());
                    line.setEndX(p1.getX());
                    line.setEndY(p1.getY());
                    line.setStrokeWidth(visualModel.getLineWidth());
                    line.setStroke(visualModel.getLineColor());
                    ArrowHead arrow = (ArrowHead) visualElement.get(k * 2 + 1);
                    arrow.setStroke(visualModel.getEdgeColor());
                    arrow.setStrokeWidth(visualModel.getEdgeWidth());
                    arrow.setFill(visualModel.getFill());
                    arrow.setLayoutX(p1.getX());
                    arrow.setLayoutY(p1.getY());
                    double rot = Math.atan2(p1.getY() - p0.getY(), p1.getX() - p0.getX());
                    arrow.setRotation(Math.toDegrees(rot));
                }
            }
        }

    }

}
