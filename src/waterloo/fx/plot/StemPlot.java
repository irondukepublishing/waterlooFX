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
package waterloo.fx.plot;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

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
public class StemPlot extends AbstractPlot<ArrayList<Shape>> implements LineInterface, BaseValueSensitiveInterface {

    /**
     * Default constructor.
     *
     */
    public StemPlot() {
        getStyleClass().add("stemplot");
        visualElement = new ArrayList<>();
        setMarkerType(MARKERTYPE.CIRCLE);
        setMarkerRadius(5d);
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
    public StemPlot(AbstractPlot p1) {
        this();
        super.add(p1);
    }

    @Override
    protected final void updateElements(Chart chart) {
        for (int k = 0; k < dataModel.size(); k++) {
            Point2D p0 = getData(chart,dataModel.xData.get(k), dataModel.yData.get(k));
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

        for (int k = 0; k < dataModel.xData.size(); k++) {
            Point2D p0 = getData(chart,dataModel.xData.get(k), dataModel.getBaseValue());
            Point2D p1 = getData(chart,dataModel.xData.get(k), dataModel.yData.get(k));
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
                    Shape marker =  visualElement.get(k * 2 + 1);
                    marker.setStroke(visualModel.getEdgeColor());
                    marker.setStrokeWidth(visualModel.getEdgeWidth());
                    marker.setFill(visualModel.getFill());
                    marker.setLayoutX(p1.getX());
                    marker.setLayoutY(p1.getY());
                }
            }
        }

    }

}
