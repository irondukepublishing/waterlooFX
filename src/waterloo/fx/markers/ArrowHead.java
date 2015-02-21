/* 
*
 * <http://sigtool.github.io/waterlooFX/>
 *
 * Copyright King's College London  2014. Copyright Malcolm Lidierth 2014-.
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
package waterloo.fx.markers;

import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;

/**
 * ArrowHeads are used in some plots and are added to the lines drawn e.g. in
 * QuiverPlots and FeatherPlots. They are not general purpose markers with
 * centered points so should not be used in scatter plots etc.
 *
 * @author Malcolm Lidierth
 */
public class ArrowHead extends Polygon {

    /**
     * Rotate transform instance that rotates about the point 0,0 in the z-axis.
     */
    private final Rotate rotor = new Rotate();

    public ArrowHead(double a) {
        double[] x = {a, -a, 0};
        double h = Math.sqrt(3) * a;
        double[] y = {h, h, 0d};
        getPoints().addAll(x[0], y[0], x[1], y[1], x[2], y[2]);
        getTransforms().add(rotor);
    }

    public void setRotation(double angle) {
        rotor.setAngle(angle + 90d);
    }

}
