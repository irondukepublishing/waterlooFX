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
package waterloo.fx.plot.model;

import java.util.ArrayList;
import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Data model used by all plot classes.
 *
 * Security: Note that data within the model is exposed by reference via the
 * setter and getter methods.
 *
 * @author Malcolm Lidierth <a
 * href="http://sourceforge.net/p/waterloo/discussion/"> [Contact]</a>
 */
public final class DataModel {

    /**
     * Vector of values of X for display. Values are plotted in sequence /
     * together with the corresponding element from yData and/or Marker
     */
    private final ObservableList<Double> xData;
    /**
     * Vector of Y-values corresponding element-by-element to the values in
     * xData. yData must have the same length as xData. Missing values can be
     * set to NaN.
     */
    private final ObservableList<Double> yData;
    /**
     * A double[] array. Use is plot type specific.
     */
    private final ObservableList<Double> extraData0;//EAST, RIGHT
    /**
     * A double[] array. Use is plot type specific.
     */
    private final ObservableList<Double> extraData1;//NORTH, UPPER
    /**
     * A double[] array. Use is plot type specific.
     */
    private final ObservableList<Double> extraData2;//WEST, LEFT
    /**
     * A double[] array. Use is plot type specific.
     */
    private final ObservableList<Double> extraData3;//SOUTH, LOWER

    private double baseValue = 0d;

    private boolean dataPolar = false;

    /**
     * A java.lang.Object. Use is plot type specific.
     */
    private Object extraObject;

    /**
     * Constructor for XML de-serialization only. Do not use.
     */
    public DataModel() {
        xData = FXCollections.observableArrayList(new ArrayList<Double>());
        yData = FXCollections.observableArrayList(new ArrayList<Double>());
        extraData0 = FXCollections.observableArrayList(new ArrayList<Double>());
        extraData1 = FXCollections.observableArrayList(new ArrayList<Double>());
        extraData2 = FXCollections.observableArrayList(new ArrayList<Double>());
        extraData3 = FXCollections.observableArrayList(new ArrayList<Double>());
        for (double k = -5d; k <= 5d; k++) {
            xData.add(k);
            yData.add(k);
        }
    }

    public final void setExtraObject(Object o) {
        extraObject = o;
    }

    /**
     * @param data the extraData0 to set
     */
    public final void setExtraData0(double... data) {
        setData(getExtraData0(), data);
    }

    /**
     * @param data the extraData1 to set
     */
    public final void setExtraData1(double... data) {
        setData(getExtraData1(), data);
    }

    /**
     * @param data the extraData2 to set
     */
    public final void setExtraData2(double... data) {
        setData(getExtraData2(), data);
    }

    /**
     * @param data the extraData3 to set
     */
    public final void setExtraData3(double... data) {
        setData(getExtraData3(), data);
    }

    /**
     * @param data
     */
    public final void setXData(double... data) {
        setData(getXData(), data);
    }

    /**
     * @param data
     */
    public final void setYData(double... data) {
        setData(getYData(), data);
    }

    /**
     * Returns true if the xData and yData are if equal size; false otherwise.
     *
     * @return the flag
     */
    public boolean isValid() {
        return getXData().size() == getYData().size();
    }

    /**
     * Returns the length of the usable elements in the xData and yData
     * properties, i.e. the minimum of the two sizes.
     *
     * This should be used when plotting as updates to the data content in xData
     * and yData may transiently create arrays of unequal length.
     *
     * @return the usable size of the xData and yData arrays for this model.
     */
    public int size() {
        return Math.min(getXData().size(), getYData().size());
    }

    private void setData(ObservableList<Double> arr, double[] data) {
        arr.clear();
        Arrays.stream(data).forEach((y) -> arr.add(y));
    }

//    public DoubleSummaryStatistics xSummary(){
//        return xData.stream().mapToDouble(x->x).summaryStatistics();
//    }
//    
//    public DoubleSummaryStatistics ySummary(){
//        return yData.stream().mapToDouble(x->x).summaryStatistics();
//    }
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
     * @return the dataPolar
     */
    public boolean isDataPolar() {
        return dataPolar;
    }

    /**
     * @param dataPolar the dataPolar to set
     */
    public void setDataPolar(boolean dataPolar) {
        this.dataPolar = dataPolar;
    }

    /**
     * @return the xData
     */
    public ObservableList<Double> getXData() {
        return xData;
    }

    /**
     * @return the yData
     */
    public ObservableList<Double> getYData() {
        return yData;
    }

    /**
     * @return the extraData0
     */
    public ObservableList<Double> getExtraData0() {
        return extraData0;
    }

    /**
     * @return the extraData1
     */
    public ObservableList<Double> getExtraData1() {
        return extraData1;
    }

    /**
     * @return the extraData2
     */
    public ObservableList<Double> getExtraData2() {
        return extraData2;
    }

    /**
     * @return the extraData3
     */
    public ObservableList<Double> getExtraData3() {
        return extraData3;
    }

    /**
     * @return the extraObject
     */
    public Object getExtraObject() {
        return extraObject;
    }

}
