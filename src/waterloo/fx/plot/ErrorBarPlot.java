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
import javafx.beans.property.BooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Path;
import javafx.scene.transform.Rotate;
import waterloo.fx.markers.BottomErrorBar;
import waterloo.fx.markers.LeftErrorBar;
import waterloo.fx.markers.RightErrorBar;
import waterloo.fx.markers.TopErrorBar;
import waterloo.fx.plot.ErrorBarPlot.ErrorBarSet;

/**
 * Error bar class.
 *
 * Draws error bars <em>from</em> the locations specified in the data model's
 * {@code xData} and {@code yData} properties.
 *
 * When the {@code autoDirect} flag is false, the length of the error bars will
 * be set from the {@code extraData..} fields in the data model:
 * <ul>
 * <li> East - extraData0</li>
 * <li> North - extraData1</li>
 * <li> West - extraData2</li>
 * <li> South - extraData3</li>
 * </ul>
 * if an {@code extraData..} field is empty, no error bars will be plotted in
 * the matching direction.
 *
 * When the {@code autoDirect} flag is true, only the {@code extraData0} and
 * {@code extraData1} fields are used:
 * <ul>
 * <li>extraData0: bars will be drawn to the east when the xData value is
 * positive, west otherwise.</li>
 * <li>extraData1: bars will be drawn to the north when the xData value is
 * positive, west otherwise.</li>
 * </ul>
 * This can be useful, e.g. when superimposing error bars on histograms bars.
 *
 * <strong>Note:</strong>
 * When added to a chart (or strictly its view), a plot description of "plot-N"
 * will be added to the StyleClass of the top plot <em>and its children</em>,
 * where N is equal to {@code chart.getPlots().size() - 1}, i.e. to the number
 * of plots that are children of the view in that chart less one.
 *
 * A common css styling may therefore be applied to family of plots by declaring
 * styles for ".plot-0", ".plot-1" etc. in the style sheet.
 *
 * @author Malcolm Lidierth
 */
public class ErrorBarPlot extends AbstractPlot<ArrayList<Path>> implements MarkerInterface, BaseValueSensitiveInterface {

    /**
     * Temp list used internally
     */
    private final ArrayList<ErrorBarSet> bars = new ArrayList<>();
    /**
     * Additional styleable property for ErrorBarPlots. When the
     * {@code autoDirect} flag is true, only the {@code extraData0} and
     * {@code extraData1} fields are used:
     * <ul>
     * <li>extraData0: bars will be drawn to the east when the xData value is
     * positive, west otherwise.</li>
     * <li>extraData1: bars will be drawn to the north when the xData value is
     * positive, west otherwise.</li>
     * </ul>
     * This can be useful, e.g. when superimposing error bars on histograms
     * bars.
     *
     */
    BooleanProperty autoDirect = new StyleableBooleanProperty(false) {

        @Override
        public Object getBean() {
            return ErrorBarPlot.this;
        }

        @Override
        public String getName() {
            return "autoDirect";
        }

        @Override
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
            return StyleableProperties.AUTODIRECT;
        }

    };

    /**
     * Default constructor.
     *
     * N.B. Adds listeners on the extraData data model properties.
     *
     */
    public ErrorBarPlot() {
        visualElement = new ArrayList<>();
//        getStyleClass().add("errorbarplot");

        dataModel.getExtraData0().addListener(this);
        dataModel.getExtraData1().addListener(this);
        dataModel.getExtraData2().addListener(this);
        dataModel.getExtraData3().addListener(this);


    }

    /**
     * Constructs an instance parenting another plot.
     *
     * @param p1 the child plot to add to this instance.
     */
    public ErrorBarPlot(AbstractPlot p1) {
        this();
        super.add(p1);
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     * @since JavaFX 8.0
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return ErrorBarPlot.StyleableProperties.STYLEABLES;
    }

    /**
     * @return the present setting of the autoDirect flag
     */
    public boolean isAutoDirect() {
        return autoDirect.get();
    }

    /**
     * Sets the autoDirect flag as specified
     * @param flag true or false
     */
    public void setAutoDirect(boolean flag) {
        autoDirect.set(flag);
    }

    /**
     *
     * @return the autoDirect property
     */
    public BooleanProperty autoDirectProperty() {
        return autoDirect;
    }

    @Override
    protected void updateElements(Chart chart) {
        bars.clear();

        for (int k = 0; k < dataModel.size(); k++) {

            ErrorBarSet set = new ErrorBarSet();
            Point2D p0 = getData(chart,dataModel.getXData().get(k), dataModel.getYData().get(k));
            p0 = chart.toPixel(p0);

            if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY())) {

                if (k < dataModel.getExtraData1().size()) {
                    double y = dataModel.getYData().get(k) - dataModel.getExtraData1().get(k);
                    Point2D p1 = getData(chart,dataModel.getXData().get(k), y);
                    p1 = chart.toPixel(p1);
                    set.top = new TopErrorBar(visualModel.getEdgeWidth() * 5d, p1.getY() - p0.getY());
                }

                if (k < dataModel.getExtraData3().size()) {
                    Point2D p1 = getData(chart,dataModel.getXData().get(k),
                            dataModel.getYData().get(k) + dataModel.getExtraData3().get(k));
                    p1 = chart.toPixel(p1);
                    set.bottom = new BottomErrorBar(visualModel.getEdgeWidth() * 5d, p0.getY() - p1.getY());
                }

                if (k < dataModel.getExtraData0().size()) {
                    Point2D p1 = getData(chart,dataModel.getXData().get(k) + dataModel.getExtraData0().get(k),
                            dataModel.getYData().get(k));
                    p1 = chart.toPixel(p1);
                    set.right = new RightErrorBar(p1.getX() - p0.getX(), visualModel.getEdgeWidth() * 5d);
                }

                if (k < dataModel.getExtraData2().size()) {
                    Point2D p1 = getData(chart,dataModel.getXData().get(k) - dataModel.getExtraData2().get(k),
                            dataModel.getYData().get(k));
                    p1 = chart.toPixel(p1);
                    set.left = new LeftErrorBar(p0.getX() - p1.getX(), visualModel.getEdgeWidth() * 5d);
                }
            }
            bars.add(set);
        }
        bars.stream().forEach(x -> {
            if (x.top != null) {
                visualElement.add(x.top);
            }
            if (x.bottom != null) {
                visualElement.add(x.bottom);
            }
            if (x.right != null) {
                visualElement.add(x.right);
            }
            if (x.left != null) {
                visualElement.add(x.left);
            }
        });
        addElements();
    }

    @Override
    public final void arrangePlot(Chart chart) {
        super.arrangePlot(chart);
        
        double bv = dataModel.getBaseValue();
        
        for (int k = 0; k < dataModel.size(); k++) {

            Point2D p0 = getData(chart,dataModel.getXData().get(k), dataModel.getYData().get(k));
            p0 = chart.toPixel(p0);

            if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY())) {

                if (k < dataModel.getExtraData1().size()) {
                    Point2D p1 = getData(chart,dataModel.getXData().get(k),
                            dataModel.getYData().get(k) - dataModel.getExtraData1().get(k));
                    p1 = chart.toPixel(p1);
                    ((TopErrorBar) bars.get(k).top).setExtent(p1.getY() - p0.getY());
                    setProperties(bars.get(k).top, p0);
                    ((TopErrorBar) bars.get(k).top).getTransforms().clear();
                    if (isAutoDirect() && dataModel.getYData().get(k) < bv) {
                        ((TopErrorBar) bars.get(k).top).getTransforms().add(new Rotate(180));
                    }
                }

                if (k < dataModel.getExtraData3().size()) {
                    Point2D p1 = getData(chart,dataModel.getXData().get(k),
                            dataModel.getYData().get(k) + dataModel.getExtraData3().get(k));
                    p1 = chart.toPixel(p1);
                    ((BottomErrorBar) bars.get(k).bottom).setExtent(p0.getY() - p1.getY());
                    setProperties(bars.get(k).bottom, p0);
                }

                if (k < dataModel.getExtraData0().size()) {
                    Point2D p1 = getData(chart,dataModel.getXData().get(k) + dataModel.getExtraData0().get(k),
                            dataModel.getYData().get(k));
                    p1 = chart.toPixel(p1);
                    ((RightErrorBar) bars.get(k).right).setExtent(p1.getX() - p0.getX());
                    setProperties(bars.get(k).right, p0);
                    ((RightErrorBar) bars.get(k).right).getTransforms().clear();
                    if (isAutoDirect() && dataModel.getXData().get(k) < bv) {
                        ((RightErrorBar) bars.get(k).right).getTransforms().add(new Rotate(180));
                    }
                }

                if (k < dataModel.getExtraData2().size()) {
                    Point2D p1 = getData(chart,dataModel.getXData().get(k) - dataModel.getExtraData2().get(k),
                            dataModel.getYData().get(k));
                    p1 = chart.toPixel(p1);
                    ((LeftErrorBar) bars.get(k).left).setExtent(p0.getX() - p1.getX());
                    setProperties(bars.get(k).left, p0);
                }
            }
        }
    }

    @Override
    public boolean isValid() {
        return dataModel.getYData().size() > 0
                && (dataModel.getXData().size() == dataModel.getYData().size())
                && ((dataModel.getXData().size() == dataModel.getExtraData0().size() || dataModel.getExtraData0().isEmpty())
                && (dataModel.getXData().size() == dataModel.getExtraData1().size() || dataModel.getExtraData1().isEmpty())
                && (dataModel.getXData().size() == dataModel.getExtraData2().size() || dataModel.getExtraData2().isEmpty())
                && (dataModel.getXData().size() == dataModel.getExtraData3().size() || dataModel.getExtraData3().isEmpty()));
    }

    private void setProperties(Path marker, Point2D p0) {
        marker.setLayoutX(p0.getX());
        marker.setLayoutY(p0.getY());
        marker.setStroke(visualModel.getEdgeColor());
        marker.setStrokeWidth(visualModel.getEdgeWidth());
    }

    private static class StyleableProperties {

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private static final CssMetaData<ErrorBarPlot, Boolean> AUTODIRECT
                = new CssMetaData<ErrorBarPlot, Boolean>("-w-plot-errorbar-autodirect",
                        StyleConverter.getBooleanConverter(), false) {

                    @Override
                    public boolean isSettable(ErrorBarPlot node) {
                        return node instanceof MarkerInterface
                        && node.autoDirect != null
                        && !node.autoDirect.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(ErrorBarPlot node) {
                        return (StyleableProperty<Boolean>) node.autoDirect;
                    }
                };

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables
                    = new ArrayList<>(AbstractPlot.getClassCssMetaData());

            styleables.add(AUTODIRECT);

            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public class ErrorBarSet {

        Path right;
        Path top;
        Path left;
        Path bottom;
    }
}
