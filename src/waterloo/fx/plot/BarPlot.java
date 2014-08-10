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
package waterloo.fx.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author malcolm
 */
public class BarPlot extends AbstractBox<Rectangle> implements BaseValueSensitiveInterface {

    private final ObjectProperty<BarExtra.MODE> mode = new StyleableObjectProperty<BarExtra.MODE>(BarExtra.MODE.GROUPED) {

        @Override
        public void set(BarExtra.MODE val) {
            ((BarExtra) dataModel.getExtraObject()).setMode(val);
            update();
        }

        @Override
        public BarExtra.MODE get() {
            return ((BarExtra) dataModel.getExtraObject()).getMode();
        }

        @Override
        public Object getBean() {
            return BarPlot.this;
        }

        @Override
        public String getName() {
            return "mode";
        }

        @Override
        public CssMetaData<? extends Styleable, BarExtra.MODE> getCssMetaData() {
            return StyleableProperties.MODE;
        }

    };
    private final ObjectProperty<BarExtra.JUSTIFICATION> justification = new StyleableObjectProperty<BarExtra.JUSTIFICATION>(BarExtra.JUSTIFICATION.LEFT) {
        @Override
        public void set(BarExtra.JUSTIFICATION val) {
            ((BarExtra) dataModel.getExtraObject()).setJustification(val);
            update();
        }

        @Override
        public BarExtra.JUSTIFICATION get() {
            return ((BarExtra) dataModel.getExtraObject()).getJustification();
        }

        @Override
        public Object getBean() {
            return BarPlot.this;
        }

        @Override
        public String getName() {
            return "justification";
        }

        @Override
        public CssMetaData<? extends Styleable, BarExtra.JUSTIFICATION> getCssMetaData() {
            return StyleableProperties.JUSTIFICATION;
        }
    };
    DoubleProperty barWidth = new StyleableDoubleProperty(1d){

        @Override
        public void set(double val){
            ((BarExtra) dataModel.getExtraObject()).setBarWidth(val);
            update();
        }

        @Override
        public double get(){
            return ((BarExtra) dataModel.getExtraObject()).getBarWidth();
        }

        @Override
        public Object getBean() {
            return BarPlot.this;
        }

        @Override
        public String getName() {
            return "barWidth";
        }

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.BARWIDTH;
        }

    };
    private final ObjectProperty<BarExtra.ORIENTATION> orientation = new StyleableObjectProperty<BarExtra.ORIENTATION>(BarExtra.ORIENTATION.VERTICAL) {

        @Override
        public void set(BarExtra.ORIENTATION val) {
            ((BarExtra) dataModel.getExtraObject()).setOrientation(val);
            update();
        }

        @Override
        public BarExtra.ORIENTATION get() {
            return ((BarExtra) dataModel.getExtraObject()).getOrientation();
        }

        @Override
        public Object getBean() {
            return BarPlot.this;
        }

        @Override
        public String getName() {
            return "orientation";
        }

        @Override
        public CssMetaData<? extends Styleable, BarExtra.ORIENTATION> getCssMetaData() {
            return StyleableProperties.ORIENTATAION;
        }

    };

    /**
     * Default constructor.
     *
     */
    public BarPlot() {
        super();
        getStyleClass().add("barplot");
        dataModel.setExtraObject(new BarExtra());
    }

    /**
     * Constructs an instance parenting another plot.
     *
     * <em>The data model of the child will be copied by reference to the new
     * parent instance.</em>
     *
     * Compound plots that share a data model may therefore be constructed by
     * chaining constructor calls, e.g.:
     * <p>
     * {@code GJScatter = new GJScatter(new GJLine(new GJErrorBar));}
     * </p>
     *
     * Further plots may be added by calling the {@code add(AbstractPlot p)}
     * method and will also share the data model. Note that data model are
     * <strong>not</strong> shared when using the standard
     * <p>
     * {@code getChildren().add(...)}
     * </p>
     * method.
     *
     * @param p1 the child plot to add to this instance.
     */
    public BarPlot(AbstractPlot p1) {
        this();
        super.add(p1);
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     * @since JavaFX 8.0
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return BarPlot.StyleableProperties.STYLEABLES;
    }

    @Override
    protected void updateElements(Chart chart) {
        BarExtra barExtra = (BarExtra) dataModel.getExtraObject();
        barExtra.dYneg.clear();
        barExtra.dYpos.clear();
        for (int k = 0; k < dataModel.size(); k++) {
            // Upper-left Limits
            Point2D p0 = getData(chart,
                    dataModel.getXData().get(k),
                    dataModel.getYData().get(k));
            p0 = chart.toPixel(p0);
            // Lower-right limits
            Point2D p1 = getData(chart,
                    dataModel.getXData().get(k),
                    barExtra.getBaseValue());
            p1 = chart.toPixel(p1);
            if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY())
                    && Double.isFinite(p1.getX()) && Double.isFinite(p1.getY())) {
                // Valid data so add rectangle
                visualElement.add(new Rectangle());
            } else {
                // Invalid - so add null
                visualElement.add(null);
            }
            barExtra.dYneg.add(0d);
            barExtra.dYpos.add(0d);
        }
        addElements();
    }

    @Override
    public void arrangePlot(Chart chart) {

        super.arrangePlot(chart);

        BarExtra barExtra = (BarExtra) dataModel.getExtraObject();

        // Retrieve the baseline value.
        // Note that this is not required to be the same for all plots in a GROUPED
        // or STACKED collection but it makes no obvious sense if it is not.
        double bv = dataModel.getBaseValue();

        for (int k = 0; k < dataModel.size(); k++) {

            Point2D p0, p1;

            // x-data value
            double x0 = dataModel.getXData().get(k);
            // x-data value for next bin - need to extrapolate at end of data series
            double x1 = k < (dataModel.size() - 1)
                    ? dataModel.getXData().get(k + 1)
                    : dataModel.getXData().get(k) + (dataModel.getXData().get(k) - dataModel.getXData().get(k - 1));

            if (barExtra.getOrientation() == BarExtra.ORIENTATION.VERTICAL) {

                // Coordinates of the corners for the rectangle to represent these data.
                // Plotting relative to the baseline, not zero, so need to accommodate that
                if (bv <= dataModel.getYData().get(k)) {
                    //Upper-left
                    p0 = getData(chart,x0, dataModel.getYData().get(k));
                    p0 = chart.toPixel(p0);
                    //Lower right
                    p1 = getData(chart,x1, bv);
                    p1 = chart.toPixel(p1);
                } else {
                    //Upper-left
                    p0 = getData(chart,x0, bv);
                    p0 = chart.toPixel(p0);
                    //Lower right
                    p1 = getData(chart,x1, dataModel.getYData().get(k));
                    p1 = chart.toPixel(p1);
                }

                // Relative width of the bar. This will be reduced below if using a GROUPED
                // bar display.
                double bw = (p1.getX() - p0.getX()) * barExtra.getBarWidth();

                // x-offset to apply when GROUPED
                double xoffset = 0d;
                // y-offset to apply when STACKED
                double yoffset = 0d;

                // GROUPED and STACKED only available when using a PlotCollection.
                // The {@code BarExtra} object of the first bar plot is used to
                // carry over the required data to other bar plots
                if (getParent() instanceof PlotCollection) {
                    PlotCollection parent = (PlotCollection) getParent();
                    if (barExtra.getMode() == BarExtra.MODE.GROUPED) {
                        // Reduce the bar width by the number of plots.
                        // All plots are counted so a NOPPlot can be added as a spacer
                        // between sets.
                        bw /= parent.getChildren().size();
                        // Reduce the barWidthY by the dividing by the number of bar plots
                        xoffset = bw * parent.getChildren().indexOf(this)
                                - (bw * parent.getChildren().filtered(x -> x instanceof BarPlot).size() / 2d);
                        // Also adjust the x-offset to keep the bars centered when requested
                        if (barExtra.getJustification() == BarExtra.JUSTIFICATION.CENTERED) {
                            xoffset += bw / 2d;
                        }
                    } else if (barExtra.getMode() == BarExtra.MODE.STACKED) {
                        // STACKED
                        int index = parent.getChildren().indexOf(this);
                        if (index == 0) {
                            // Initialise the extent of the used y-axis range on
                            // the first plot...
                            if (bv <= dataModel.getYData().get(k)) {
                                barExtra.dYneg.set(k, p0.getY() - p1.getY());
                                barExtra.dYpos.set(k, 0d);
                            } else {
                                barExtra.dYneg.set(k, 0d);
                                barExtra.dYpos.set(k, p1.getY() - p0.getY());
                            }
                            yoffset = 0d;
                        } else {
                            //... and update the values for subsequent plots
                            if (bv <= dataModel.getYData().get(k)) {
                                yoffset = ((BarExtra) ((BarPlot) parent.getChildren().get(0)).dataModel.getExtraObject()).dYneg.get(k);
                                ((BarExtra) ((BarPlot) parent.getChildren().get(0)).dataModel.getExtraObject()).dYneg.set(k, p0.getY() - p1.getY() + yoffset);
                            } else {
                                yoffset = ((BarExtra) ((BarPlot) parent.getChildren().get(0)).dataModel.getExtraObject()).dYpos.get(k);
                                ((BarExtra) ((BarPlot) parent.getChildren().get(0)).dataModel.getExtraObject()).dYpos.set(k, p1.getY() - p0.getY() + yoffset);
                            }
                        }
                    }
                }
                if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY()) && Double.isFinite(p1.getX()) && Double.isFinite(p1.getY())) {
                    // Retrieve the bar
                    Rectangle marker = (Rectangle) visualElement.get(k);
                    // Set the x-offset
                    if (barExtra.getJustification() == BarExtra.JUSTIFICATION.CENTERED) {
                        marker.setX(xoffset + p0.getX() - bw / 2d);
                    } else {
                        marker.setX(xoffset + p0.getX());
                    }
                    // If stacking bars add the required y-offset to place this bar
                    // above/below those drawn for previous plots
                    if (barExtra.getMode() == BarExtra.MODE.STACKED) {
                        marker.setY(p0.getY() + yoffset);
                    } else {
                        marker.setY(p0.getY());
                    }
                    // Set this bar's properties
                    marker.setWidth(bw);
                    marker.setHeight(p1.getY() - p0.getY());
                    marker.setStroke(visualModel.getEdgeColor());
                    marker.setStrokeWidth(visualModel.getEdgeWidth());
                    marker.setFill(getFill());
                }
            } else if (barExtra.getOrientation() == BarExtra.ORIENTATION.HORIZONTAL) {
                // Coordinates of the corners for the rectangle to represent these data.
                // Plotting releative to the baseline, not zero, so need to accommodate that
                if (bv <= dataModel.getYData().get(k)) {
                    //Upper-left
                    p0 = getData(chart,bv, x0);
                    p0 = chart.toPixel(p0);
                    //Lower right
                    p1 = getData(chart,dataModel.getYData().get(k), x1);;
                    p1 = chart.toPixel(p1);
                } else {
                    //Upper-left
                    p0 = getData(chart,dataModel.getYData().get(k), x0);
                    p0 = chart.toPixel(p0);
                    //Lower right
                    p1 = getData(chart,bv, x1);
                    p1 = chart.toPixel(p1);
                }
                // Relative width of the bar. This will be reduced below if using a GROUPED
                // bar display.
                double barWidthY = (p0.getY() - p1.getY()) * barExtra.getBarWidth();

                // x-offset to apply when GROUPED
                double yoffset = 0d;
                // y-offset to apply when STACKED
                double xoffset = 0d;

                // GROUPED and STACKED only available when using a PlotCollection.
                // The {@code BarExtra} object of the first bar plot is used to
                // carry over the required data to other bar plots
                if (getParent() instanceof PlotCollection) {
                    PlotCollection parent = (PlotCollection) getParent();
                    if (barExtra.getMode() == BarExtra.MODE.GROUPED) {
                        // Reduce the bar width by the number of plots.
                        // All plots are counted so a NOPPlot can be added as a spacer
                        // between sets.
                        barWidthY /= parent.getChildren().size();
                        // Reduce the barWidthY by the dividing by the number of bar plots
                        yoffset = barWidthY * parent.getChildren().indexOf(this)
                                - (barWidthY * parent.getChildren().filtered(x -> x instanceof BarPlot).size() / 2d);
                        // Also adjust the x-offset to keep the bars centered when requested
                        if (barExtra.getJustification() == BarExtra.JUSTIFICATION.CENTERED) {
                            yoffset += barWidthY / 2d;
                        }
                    } else if (barExtra.getMode() == BarExtra.MODE.STACKED) {
                        // STACKED
                        int index = parent.getChildren().indexOf(this);
                        if (index == 0) {
                            // Initialise the extent of the used y-axis range on
                            // the first plot...
                            if (bv <= dataModel.getYData().get(k)) {
                                barExtra.dYneg.set(k, p1.getX() - p0.getX());
                                barExtra.dYpos.set(k, 0d);
                            } else {
                                barExtra.dYneg.set(k, 0d);
                                barExtra.dYpos.set(k, p0.getX() - p1.getX());
                            }
                            xoffset = 0d;
                        } else {
                            //... and update the values for subsequent plots
                            if (bv <= dataModel.getYData().get(k)) {
                                xoffset = ((BarExtra) ((BarPlot) parent.getChildren().get(0)).dataModel.getExtraObject()).dYneg.get(k);
                                ((BarExtra) ((BarPlot) parent.getChildren().get(0)).dataModel.getExtraObject()).dYneg.set(k, p1.getX() - p0.getX() + xoffset);
                            } else {
                                xoffset = ((BarExtra) ((BarPlot) parent.getChildren().get(0)).dataModel.getExtraObject()).dYpos.get(k);
                                ((BarExtra) ((BarPlot) parent.getChildren().get(0)).dataModel.getExtraObject()).dYpos.set(k, p0.getX() - p1.getX() + xoffset);
                            }
                        }
                    }
                }
                if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY()) && Double.isFinite(p1.getX()) && Double.isFinite(p1.getY())) {
                    // Retrieve the bar
                    Rectangle marker = (Rectangle) visualElement.get(k);
                    // Set the y-offset
                    if (barExtra.getJustification() == BarExtra.JUSTIFICATION.CENTERED) {
                        marker.setY(yoffset + p0.getY() - barWidthY / 2d);
                    } else {
                        marker.setY(yoffset + p0.getY());
                    }
                    // If stacking bars add the required offset to place this bar
                    // to the left/right of those drawn for previous plots
                    if (barExtra.getMode() == BarExtra.MODE.STACKED) {
                        marker.setX(p0.getX() + xoffset);
                    } else {
                        marker.setX(p0.getX());
                    }
                    // Set this bar's properties
                    marker.setHeight(barWidthY);
                    marker.setWidth(p1.getX() - p0.getX());
                    marker.setStroke(visualModel.getEdgeColor());
                    marker.setStrokeWidth(visualModel.getEdgeWidth());
                    marker.setFill(getFill());
                }
            }
        }
    }

    public BarExtra.ORIENTATION getOrientation() {
        return orientation.get();
    }

    public void setOrientation(BarExtra.JUSTIFICATION val) {
        justification.set(val);
    }

    public void setOrientation(BarExtra.ORIENTATION val) {
        orientation.set(val);
    }

    public ObjectProperty<BarExtra.ORIENTATION> orientationProperty() {
        return orientation;
    }

    public BarExtra.MODE getMode() {
        return mode.get();
    }

    public void setMode(BarExtra.MODE val) {
        mode.set(val);
    }

    public ObjectProperty<BarExtra.MODE> modeProperty() {
        return mode;
    }
    
    public BarExtra.JUSTIFICATION getJustification() {
        return justification.get();
    }

    public ObjectProperty<BarExtra.JUSTIFICATION> justificationProperty() {
        return justification;
    }
    
    public double getBarWidth(){
        return barWidth.get();
    }
    
        public void setBarWidth(double val){
        barWidth.set(val);
    };

public DoubleProperty barWidthProperty() {return barWidth;}

    @SuppressWarnings("unchecked")
    private static class StyleableProperties {

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private static final Class<? extends Enum> clzz0 = BarExtra.ORIENTATION.class;
        private static final Class<? extends Enum> clzz1 = BarExtra.MODE.class;
        private static final Class<? extends Enum> clzz2 = BarExtra.JUSTIFICATION.class;

        private static final CssMetaData<BarPlot, BarExtra.ORIENTATION> ORIENTATAION
                = new CssMetaData<BarPlot, BarExtra.ORIENTATION>("-w-plot-bar-orientation",
                        StyleConverter.getEnumConverter(clzz0), BarExtra.ORIENTATION.VERTICAL) {

                    @Override
                    public boolean isSettable(BarPlot node) {
                        return node.orientation != null
                        && !node.orientation.isBound();
                    }

                    @Override
                    public StyleableProperty<BarExtra.ORIENTATION> getStyleableProperty(BarPlot node) {
                        return (StyleableProperty<BarExtra.ORIENTATION>) node.orientation;
                    }
                };

        private static final CssMetaData<BarPlot, BarExtra.MODE> MODE
                = new CssMetaData<BarPlot, BarExtra.MODE>("-w-plot-bar-orientation",
                        StyleConverter.getEnumConverter(clzz1), BarExtra.MODE.GROUPED) {

                    @Override
                    public boolean isSettable(BarPlot node) {
                        return node.mode != null
                        && !node.mode.isBound();
                    }

                    @Override
                    public StyleableProperty<BarExtra.MODE> getStyleableProperty(BarPlot node) {
                        return (StyleableProperty<BarExtra.MODE>) node.mode;
                    }
                };
        private static final CssMetaData<BarPlot, BarExtra.JUSTIFICATION> JUSTIFICATION
                = new CssMetaData<BarPlot, BarExtra.JUSTIFICATION>("-w-plot-bar-justification",
                        StyleConverter.getEnumConverter(clzz2), BarExtra.JUSTIFICATION.LEFT) {

                    @Override
                    public boolean isSettable(BarPlot node) {
                        return node.justification != null
                        && !node.justification.isBound();
                    }

                    @Override
                    public StyleableProperty<BarExtra.JUSTIFICATION> getStyleableProperty(BarPlot node) {
                        return (StyleableProperty<BarExtra.JUSTIFICATION>) node.justification;
                    }
                };
        private static final CssMetaData<BarPlot, Number> BARWIDTH
                = new CssMetaData<BarPlot, Number>("-w-plot-bar-width",
                        StyleConverter.getSizeConverter(), 1d) {

                    @Override
                    public boolean isSettable(BarPlot node) {
                        return node.barWidth != null
                        && !node.barWidth.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(BarPlot node) {
                        return (StyleableProperty<Number>) node.barWidth;
                    }
                };

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables
                    = new ArrayList<>(AbstractPlot.getClassCssMetaData());

            styleables.add(ORIENTATAION);
            styleables.add(MODE);
            styleables.add(JUSTIFICATION);
            styleables.add(BARWIDTH);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
