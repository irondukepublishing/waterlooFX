/* 
*
 * <http://waterloo.sourceforge.net/>
 *
 * Copyright King's College London 2014.
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

import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.transform.Rotate;

/**
 *
 * @author Malcolm Lidierth
 */
public class RightErrorBar extends Path  {

    Rotate rotor = null;

    public RightErrorBar(double w, double h) {
        getElements().add(new MoveTo(0, 0));
        getElements().add(new LineTo(w, 0));
        getElements().add(new MoveTo(w, -h / 2));
        getElements().add(new LineTo(w, h / 2));
    }

    public void setExtent(double w) {
        ((LineTo) getElements().get(1)).setX(w);
        ((MoveTo) getElements().get(2)).setX(w);
        ((LineTo) getElements().get(3)).setX(w);
    }

    public void setRotation(double angle) {
        if (rotor == null) {
            rotor = new Rotate(0d);
            getTransforms().add(rotor);
        }
        rotor.setAngle(angle);
    }
}
