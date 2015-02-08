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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.geometry.Point2D;
import waterloo.fx.plot.Chart;

/**
 *
 * @author malcolm
 */
public abstract class AbstractTransform extends ObjectBinding<ArrayList<Double>> {

    /**
     * @return the axis
     */
    public AXIS getAxis() {
        return axis;
    }

    public enum AXIS {

        HORIZONTAL,
        VERTICAL
    }

    private NumberFormat formatter = new DecimalFormat();
    final ArrayList<Double> majorTicks = new ArrayList<>();
    private final MinorTickLocatorClass minorTicks = new MinorTickLocatorClass();
    Chart layer;
    private AXIS axis;

    AbstractTransform() {
    }

    /**
     * Called to set layer property and associated bindings.
     *
     * @param p
     * @param or
     */
    public void updateBindings(Chart p, AXIS or) {
        if (layer != null) {
            unbind();
        }
        layer = p;
        if (or.equals(AXIS.HORIZONTAL)) {
            bind(layer.xLeftProperty());
            bind(layer.xRightProperty());
        } else {
            bind(layer.yTopProperty());
            bind(layer.yBottomProperty());
        }
        axis = or;
    }

    /**
     * Needs to be called when the layer property of is cleared/changed. Removes
     * the bindings created by {@code updateBindings()}.
     */
    public void unbind() {
        this.getDependencies().forEach((x) -> {
            if (x.equals(layer.xLeftProperty())
                    || x.equals(layer.xRightProperty())
                    || x.equals(layer.yTopProperty())
                    || x.equals(layer.yBottomProperty())) {
                unbind((Observable) x);
            }
        });
    }

    /**
     * @return the minorTicks
     */
    public MinorTickLocatorClass getMinorTicks() {
        return minorTicks;
    }

    abstract public Point2D getData(double x, double y);

    abstract public Point2D getInverse(double x, double y);

    abstract public String getTickLabel(double val);

    /**
     * @return the formatter
     */
    public NumberFormat getFormatter() {
        return formatter;
    }

    /**
     * @param formatter the formatter to set
     */
    public void setFormatter(NumberFormat formatter) {
        this.formatter = formatter;
    }

    /**
     * {@code ArrayList<Double>} extension for representing the locations of
     * minor ticks and grids.
     *
     */
    public class MinorTickLocatorClass extends ArrayList<Double> {

        /**
         * If autoUpdate is set false, the computeValue method will return
         * without altering the minor tick values. They may therefore be set
         * manually.
         */
        private boolean autoUpdate = true;

        /**
         * Private constructor.
         */
        private MinorTickLocatorClass() {

        }

        /**
         * This computeValue method is intended to be used from inside the
         * computeValue method of the major tick binding.
         *
         * @param start
         * @param stop
         * @param inc
         */
        protected void computeValue(double start, double stop, double inc) {
            if (isAutoUpdate()) {
                clear();
                start = Math.floor(start / inc) * inc;
                for (double s = start; s <= stop; s += inc) {
                    add(s);
                }
            }
        }

        /**
         * @return the autoUpdate setting
         */
        public boolean isAutoUpdate() {
            return autoUpdate;
        }

        /**
         * @param autoUpdate clears sets the autoUpdate flag
         */
        public void setAutoUpdate(boolean autoUpdate) {
            this.autoUpdate = autoUpdate;
        }
    }
}
