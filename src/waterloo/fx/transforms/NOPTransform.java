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
package waterloo.fx.transforms;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import waterloo.fx.plot.Chart;

/**
 *
 * @author malcolm
 */
public class NOPTransform extends AbstractTransform {

    public final Chart.TRANSFORMTYPE type = Chart.TRANSFORMTYPE.LINEAR;

    public NOPTransform() {
        super();
    }

    @Override
    public Point2D getData(double x, double y) {
        return new Point2D(x, y);
    }

    @Override
    public Point2D getInverse(double x, double y) {
        return new Point2D(x, y);
    }

    @Override
    public final String getTickLabel(double val) {
        if (val == -0) {
            val = 0;
        }
        return getFormatter().format(val);
    }

    @Override
    protected ArrayList<Double> computeValue() {
        majorTicks.clear();
        if (getAxis().equals(AXIS.HORIZONTAL)) {
            double start = layer.getXMin();
            double inc = layer.getMajorXInterval();
            start = Math.floor(start / inc) * inc;
            double stop = layer.getXMax() + layer.getMajorXInterval();
            if ((stop - start) / inc > 200) {
                return majorTicks;
            }
            for (double s = start; s <= stop; s += inc) {
                majorTicks.add(s);
            }
            majorTicks.replaceAll((Double t) -> (t < Math.ulp(stop) && t > -Math.ulp(stop)) ? 0d : t);
            getMinorTicks().computeValue(start, stop, layer.getMajorXInterval() / layer.getMinorCountXHint());
        } else {
            double start = layer.getYMin();
            double inc = layer.getMajorYInterval();
            start = Math.floor(start / inc) * inc;
            double stop = layer.getYMax() + layer.getMajorYInterval();
            if ((stop - start) / inc > 200) {
                return majorTicks;
            }
            for (double s = start; s <= stop; s += inc) {
                majorTicks.add(s);
            }
            getMinorTicks().computeValue(start, stop, layer.getMajorYInterval() / layer.getMinorCountYHint());
            majorTicks.replaceAll((Double t) -> (t < Math.ulp(stop) && t > -Math.ulp(stop)) ? 0d : t);
        }
        return majorTicks;
    }

}
