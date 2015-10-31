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

import javafx.scene.shape.Rectangle;

/**
 * Bubble plot - draws an ellipse for each data point where, <em>for the
 * bounding rectangle</em> of the ellipse:
 * <ul>
 * <li>The upper left corner is at [xData-extraData2, yData+extraData1]</li>
 * <li>The lower right corner is at [xData+extraData0, yData-extraData3]</li>
 * </ul>.
 *
 * The shape is not therefore necessarily centered on [xData, yData].
 *
 * @author Malcolm Lidierth
 */
public class BubblePlot extends BoxPlot {

    /**
     * Default constructor.
     *
     */
    public BubblePlot() {
        super();
//        getStyleClass().add("bubble");
    }

    /**
     * Constructs an instance parenting another plot.
     *
     * @param p1 the child plot to add to this instance.
     */
    public BubblePlot(AbstractPlot p1) {
        this();
        super.add(p1);
    }

    @Override
    public void arrangePlot(Chart chart) {
        super.arrangePlot(chart);
        for (int k = 0; k < dataModel.size(); k++) {
            Rectangle marker = (Rectangle) visualElement.get(k);
            marker.setArcHeight(marker.getHeight() * 0.9);
            marker.setArcWidth(marker.getWidth() * 0.9);
        }
    }

}
