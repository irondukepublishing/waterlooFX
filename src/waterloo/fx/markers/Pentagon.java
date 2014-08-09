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
package waterloo.fx.markers;

import javafx.scene.shape.Polygon;

/**
 *
 * @author Malcolm Lidierth
 */
public class Pentagon extends Polygon implements CenteredShapeInterface {

    public Pentagon(double a) {
        //see http://mathworld.wolfram.com/Pentagon.html
        double c1 = a / 4 * (Math.sqrt(5) - 1);
        double c2 = a / 4 * (Math.sqrt(5) + 1);
        double s1 = a / 4 * Math.sqrt(10 + 2 * Math.sqrt(5));
        double s2 = a / 4 * Math.sqrt(10 - 2 * Math.sqrt(5));
        double[] x = {0, s1, s2, -s2, -s1};
        double[] y = {-a, -c1, c2, c2, -c1};
        getPoints().addAll(x[0], y[0], x[1], y[1], x[2], y[2], x[3], y[3], x[4], y[4]);
    }

}
