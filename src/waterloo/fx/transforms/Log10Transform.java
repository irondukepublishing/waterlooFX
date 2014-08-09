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
package waterloo.fx.transforms;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import waterloo.fx.plot.Chart;
import waterloo.fx.util.GJUtilities;

/**
 *
 * @author malcolm
 */
public class Log10Transform extends AbstractTransform {
    
    public final Chart.TRANSFORMTYPE type = Chart.TRANSFORMTYPE.LOG10;

    public Log10Transform() {
    }

    @Override
    public Point2D getData(double x, double y) {
        if (getAxis().equals(AXIS.HORIZONTAL)) {
            x = Math.log10(x);
        } else {
            y = Math.log10(y);
        }
        return new Point2D(x, y);
    }

    @Override
    public Point2D getInverse(double x, double y) {
        if (getAxis().equals(AXIS.HORIZONTAL)) {
            x = Math.pow(10, x);
        } else {
            y = Math.pow(10, y);
        }
        return new Point2D(x, y);
    }

    @Override
    public final String getTickLabel(double val) {
        if (val == -0) {
            val = 0;
        }
        return ("10" + GJUtilities.getSuperscripts(getFormatter().format(val)));
    }

    @Override
    protected ArrayList<Double> computeValue() {
        majorTicks.clear();     
        for (double k = -15; k < 15; k++) {
            if (getAxis().equals(AXIS.HORIZONTAL)) {
                if (k >= layer.getXMin() && k <= layer.getXMax()) {
                    majorTicks.add(k);
                }
            } else {
                if (k >= layer.getXMin() && k <= layer.getYMax()) {
                    majorTicks.add(k);
                }
            }
        }
        return majorTicks;
    }

}
