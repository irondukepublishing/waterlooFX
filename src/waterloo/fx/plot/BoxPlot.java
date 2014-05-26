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

import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;

/**
 * Box plot - draws a rectangle for each data point where:
 * <ul>
 * <li>The upper left corner is at [xData-extraData2, yData+extraData1]</li>
 * <li>The lower right corner is at [xData+extraData0, yData-extraData3]</li>
 * </ul>.
 *
 * The rectangle is not therefore necessarily centered on [xData, yData].
 *
 * @author Malcolm Lidierth
 */
public class BoxPlot extends AbstractBox<Rectangle> {

    /**
     * Default constructor.
     *
     */
    public BoxPlot() {
        super();
        getStyleClass().add("boxplot");
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
    public BoxPlot(AbstractPlot p1) {
        this();
        super.add(p1);
    }

    @Override
    protected void updateElements(Chart chart) {
        for (int k = 0; k < dataModel.size(); k++) {
            // Upper-left Limits
            Point2D p0 = getData(chart,
                    dataModel.xData.get(k) - dataModel.extraData2.get(k),
                    dataModel.yData.get(k) + dataModel.extraData1.get(k));
            p0 = chart.toPixel(p0);
            // Lower-right limits
            Point2D p1 = getData(chart,
                    dataModel.xData.get(k) + dataModel.extraData0.get(k),
                    dataModel.yData.get(k) - dataModel.extraData3.get(k));
            p1 = chart.toPixel(p1);
            if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY()) && Double.isFinite(p1.getX()) && Double.isFinite(p1.getY())) {
                visualElement.add(new Rectangle());
            } else {
                visualElement.add(null);
            }
        }
        addElements();
    }

    @Override
    public void arrangePlot(Chart chart) {
        super.arrangePlot(chart);
        for (int k = 0; k < dataModel.size(); k++) {
            // Upper-left Limits
            Point2D p0 = getData(chart,
                    dataModel.xData.get(k) - dataModel.extraData2.get(k),
                    dataModel.yData.get(k) + dataModel.extraData1.get(k));
            p0 = chart.toPixel(p0);
            // Lower-right limits
            Point2D p1 = getData(chart,
                    dataModel.xData.get(k) + dataModel.extraData0.get(k),
                    dataModel.yData.get(k) - dataModel.extraData3.get(k));
            p1 = chart.toPixel(p1);
            if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY()) && Double.isFinite(p1.getX()) && Double.isFinite(p1.getY())) {
                Rectangle marker = (Rectangle) visualElement.get(k);
                marker.setX(p0.getX());
                marker.setY(p0.getY());
                marker.setWidth(p1.getX() - p0.getX());
                marker.setHeight(p1.getY() - p0.getY());
                marker.setStroke(visualModel.getEdgeColor());
                marker.setStrokeWidth(visualModel.getEdgeWidth());
                marker.setFill(getFill());
            }
        }
    }
}
