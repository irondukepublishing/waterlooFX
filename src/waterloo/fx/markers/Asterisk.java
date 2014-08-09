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
package waterloo.fx.markers;

import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 *
 * @author Malcolm Lidierth
 */
public class Asterisk extends Path implements CenteredShapeInterface {

    public Asterisk(double a) {
        double h = a / 1.414;
        double[] x = {-h, h, 1.414 * h, h, -h, -1.414 * h, 0, 0};
        double[] y = {-h, -h, 0, h, h, 0, 1.414 * h, -1.414 * h};
        getElements().add(new MoveTo(x[0], y[0]));
        getElements().add(new LineTo(x[3], y[3]));
        getElements().add(new MoveTo(x[1], y[1]));
        getElements().add(new LineTo(x[4], y[4]));
        getElements().add(new MoveTo(x[2], y[2]));
        getElements().add(new LineTo(x[5], y[5]));

        getElements().add(new MoveTo(x[6], y[6]));
        getElements().add(new LineTo(x[7], y[7]));
    }
}
