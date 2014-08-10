/* 
*
 * <http://waterloo.sourceforge.net/>
 *
 * Copyright King's College London  2011-
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

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Object to support bar charts. For implementation details of how individual
 * fields will be interpreted, see the help for the implementing chart class.
 *
 * @author Malcolm Lidierth
 */
public class BarExtra {

    ArrayList<Double> dYneg= new ArrayList<>();
    ArrayList<Double> dYpos= new ArrayList<>();
    /**
     * Orientation for this instance. Default is BarExtra.ORIENTATION.VERTICAL.
     */
    private BarExtra.ORIENTATION orientation = BarExtra.ORIENTATION.VERTICAL;
    private BarExtra.JUSTIFICATION justification = BarExtra.JUSTIFICATION.LEFT;
    /**
     * Mode for this instance. Default is BarExtra.MODE.GROUPED.
     */
    private BarExtra.MODE mode = BarExtra.MODE.GROUPED;
    /**
     * Position of the baseline for bar plots in axes units.
     */
    private double baseValue = 0d;
    /**
     * Width of the bars in axes units. This value may be ignored when MODE is
     * HIST or HISTC.
     */
    private double barWidth = 1d;
    private ArrayList<String> labels = new ArrayList<String>();
    private ORIENTATION labelOrientation = ORIENTATION.HORIZONTAL;

    /**
     * @return the justification
     */
    public BarExtra.JUSTIFICATION getJustification() {
        return justification;
    }

    /**
     * @param justification the justification to set
     */
    public void setJustification(BarExtra.JUSTIFICATION justification) {
        this.justification = justification;
    }

    /**
     * Get the bar width in axes units.
     *
     * @return the barWidth
     */
    public double getBarWidth() {
        return barWidth;
    }

    /**
     * Set the bar width in axes units.
     *
     * @param barWidth the barWidth to set
     */
    public void setBarWidth(double barWidth) {
        this.barWidth = barWidth;
    }

    /**
     * @return the mode
     */
    public BarExtra.MODE getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(BarExtra.MODE mode) {
        this.mode = mode;
    }

    /**
     * Returns the orientation: BarExtra.ORIENTATION.HORIZONTAL or VERTICAL.
     *
     * @return the orientation
     */
    public BarExtra.ORIENTATION getOrientation() {
        return orientation;
    }

    /**
     * Sets the orientation: BarExtra.ORIENTATION.HORIZONTAL or VERTICAL.
     *
     * @param orientation the orientation to set
     */
    public void setOrientation(BarExtra.ORIENTATION orientation) {
        this.orientation = orientation;
    }

    /**
     * @return the baseValue
     */
    public double getBaseValue() {
        return baseValue;
    }

    /**
     * @param baseValue the baseValue to set
     */
    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
    }

    /**
     * @return the labels
     */
    public ArrayList<String> getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        ArrayList<String> copy = new ArrayList<String>();
        copy.addAll(Arrays.asList(labels));
        this.labels = copy;
    }

    /**
     * @param labels the labels to set
     */
    public void setLabels(ArrayList<String> labels) {
        this.labels = labels;
    }

    /**
     * @return the labelOrientation
     */
    public ORIENTATION getLabelOrientation() {
        return labelOrientation;
    }

    public void setLabelOrientation(String s) {
        if (s.contains("VERT")) {
            labelOrientation = ORIENTATION.VERTICAL;
        } else {
            labelOrientation = ORIENTATION.HORIZONTAL;
        }
    }

    /**
     * @param labelOrientation the labelOrientation to set
     */
    public void setLabelOrientation(ORIENTATION labelOrientation) {
        this.labelOrientation = labelOrientation;
    }

    /**
     * Orientation for the boxes.
     */
    public enum ORIENTATION {

        HORIZONTAL, VERTICAL
    }

    /**
     * For bar plots, indicates the display mode for multiplexed data.
     */
    public enum MODE {

        GROUPED, STACKED
    }

    public enum JUSTIFICATION {
         LEFT, CENTERED
    }

}
