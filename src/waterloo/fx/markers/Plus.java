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

import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 *
 * @author Malcolm Lidierth
 */
public class Plus extends Path implements CenteredShapeInterface{
    
    public Plus(double a){
        getElements().add(new MoveTo(-a,0));
        getElements().add(new LineTo(a,0));
        getElements().add(new MoveTo(0,-a));
        getElements().add(new LineTo(0,a));
    }

}
