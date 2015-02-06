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

package waterloo.fx.markers;

import javafx.scene.shape.Polygon;


/**
 *
 * @author Malcolm Lidierth
 */
public class LeftTriangle  extends Polygon implements CenteredShapeInterface {
    
            
    public LeftTriangle(double a) {
        double h=Math.sqrt(3)*a;
        double[] x = {a/3-h, a/3, a/3};
        double[] y= {0,a,-a};
        getPoints().addAll(x[0], y[0], x[1], y[1], x[2], y[2]);
    }

}
