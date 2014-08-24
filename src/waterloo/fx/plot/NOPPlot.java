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
package waterloo.fx.plot;

import javafx.scene.shape.Shape;

/**
 * A {@code NOPPlot} can be used within a {@code PlotCollection} as a spacer.
 *
 * {@code NOPPlot}s do no rendering to the screen but their presence will be
 * detected by some calls e.g. {@code getChildren().size()} to influence the
 * layout of other plots. For example, a {@code NOPPlot} added to a
 * {@code PlotCollection} of {@code BarPlots} can be used to create a gap
 * between GROUPED bars.
 *
 * @author Malcolm Lidierth
 */
public class NOPPlot extends AbstractBox<Shape> {

    @Override
    protected void updateElements(Chart chart) {
    }

    @Override
    public void arrangePlot(Chart chart) {
    }

}
