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
package waterloo.fx.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Line;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Shape;
import waterloo.fx.markers.ArrowHead;

/**
 * @author Malcolm Lidierth
 */
public class QuiverPlot extends AbstractPlot<ArrayList<Shape>> implements LineInterface {

    private final DoubleProperty scale = new StyleableDoubleProperty(0d) {

        @Override
        public Object getBean() {
            return QuiverPlot.this;
        }

        @Override
        public String getName() {
            return "scale";
        }

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.SCALE;
        }

    };
    private final BooleanProperty useQuad = new StyleableBooleanProperty(false) {

        @Override
        public Object getBean() {
            return QuiverPlot.this;
        }

        @Override
        public String getName() {
            return "useQuad";
        }

        @Override
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
            return StyleableProperties.USEQUAD;
        }

    };
    private double scaleFactor;

    /**
     * Default constructor.
     *
     */
    public QuiverPlot() {
//        getStyleClass().add("quiverplot");
        visualElement = new ArrayList<>();
        setMarkerType(MARKERTYPE.ARROWHEAD);
        setMarkerRadius(0.75);
    }

    /**
     * Constructs an instance parenting another plot.
     *
     * @param p1 the child plot to add to this instance.
     */
    public QuiverPlot(AbstractPlot p1) {
        this();
        super.add(p1);
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     * @since JavaFX 8.0
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return QuiverPlot.StyleableProperties.STYLEABLES;
    }

    public double getScale() {
        return scale.get();
    }

    public void setScale(double val) {
        scale.set(val);
    }

    public DoubleProperty scaleProperty() {
        return scale;
    }

    public boolean isUseQuad() {
        return useQuad.get();
    }

    public void setUseQuad(boolean flag) {
        useQuad.set(flag);
    }

    public BooleanProperty useQuadProperty() {
        return useQuad;
    }

    @Override
    protected final void updateElements(Chart chart) {
        for (int k = 0; k < dataModel.size(); k++) {
            Point2D p0 = getData(chart,dataModel.getXData().get(k), dataModel.getYData().get(k));
            p0 = chart.toPixel(p0);
            if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY())) {
                // If this is the first entry for a new line, add that line to
                // the visualElement list
                if (useQuad.get()) {
                    visualElement.add(new QuadCurve());
                } else {
                    visualElement.add(new Line());
                }
                visualElement.add((Shape) visualModel.getMarker(k));
            } else {
                visualElement.add(null);
                visualElement.add(null);
            }
        }
        addElements();
        scaleFactor = getFinalScaleFactor();
    }

    @Override
    public final void arrangePlot(Chart chart) {

        // Call the super method to do house-keeping common to all plots
        super.arrangePlot(chart);

        for (int k = 0; k < dataModel.size(); k++) {
            Point2D p0 = getData(chart,dataModel.getXData().get(k), dataModel.getYData().get(k));
            Point2D p1 = getData(chart,dataModel.getXData().get(k) + dataModel.getExtraData0().get(k) * scaleFactor,
                    dataModel.getYData().get(k) + dataModel.getExtraData1().get(k) * scaleFactor);
            p0 = chart.toPixel(p0);
            p1 = chart.toPixel(p1);
            if (Double.isFinite(p0.getX()) && Double.isFinite(p0.getY())) {

                if (visualElement.get(k * 2) != null) {
                    double rot;
                    if (useQuad.get()) {
                        Point2D anchor = new Point2D(p0.getX() + (p1.getX() - p0.getX()) / 2d,
                                p0.getY() + (p1.getY() - p0.getY()) * 0.667d);
                        QuadCurve line = (QuadCurve) visualElement.get(k * 2);
                        line.setStartX(p0.getX());
                        line.setStartY(p0.getY());
                        line.setControlX(anchor.getX());
                        line.setControlY(anchor.getY());
                        line.setEndX(p1.getX());
                        line.setEndY(p1.getY());
                        line.setStrokeWidth(visualModel.getLineWidth());
                        line.setStroke(visualModel.getLineColor());
                        rot = Math.atan2(p1.getY() - anchor.getY(), p1.getX() - anchor.getX());
                    } else {
                        Line line = (Line) visualElement.get(k * 2);
                        line.setStartX(p0.getX());
                        line.setStartY(p0.getY());
                        line.setEndX(p1.getX());
                        line.setEndY(p1.getY());
                        line.setStrokeWidth(visualModel.getLineWidth());
                        line.setStroke(visualModel.getLineColor());
                        rot = Math.atan2(p1.getY() - p0.getY(), p1.getX() - p0.getX());
                    }

                    Shape arrow = visualElement.get(k * 2 + 1);
                    arrow.setStroke(visualModel.getEdgeColor());
                    arrow.setStrokeWidth(visualModel.getEdgeWidth());
                    arrow.setFill(visualModel.getFill());
                    arrow.setLayoutX(p1.getX());
                    arrow.setLayoutY(p1.getY());

                    if (arrow instanceof ArrowHead) {
                        ((ArrowHead) arrow).setRotation(Math.toDegrees(rot));
                    }
                }
            }
        }
    }

    private double getFinalScaleFactor() {

        double spacingX = Double.NEGATIVE_INFINITY;
        for (int k = 0; k < dataModel.getXData().size() - 1; k++) {
            spacingX = Math.max(spacingX, Math.abs(dataModel.getXData().get(k + 1) - dataModel.getXData().get(k)));
        }

        double spacingY = Double.NEGATIVE_INFINITY;
        for (int k = 0; k < dataModel.getYData().size() - 1; k++) {
            spacingY = Math.max(spacingY, Math.abs(dataModel.getYData().get(k + 1) - dataModel.getYData().get(k)));
        }

        // Maximum distance for vectors along X and Y axes
        double dU = dataModel.getExtraData0().stream().max(Comparator.naturalOrder()).get();
        double dV = dataModel.getExtraData1().stream().max(Comparator.naturalOrder()).get();

        // Scale factor required for vectors to just fill the available x,y
        // spacing
        double scx = spacingX / dU;
        double scy = spacingY / dV;
        double scale2 = Math.min(scx, scy);

        // Calculate the final scaling based on user-setting
        double factor;
        if (scale.get() == 0) {
            // If zero, do not scale - just use the values supplied in dX and dY
            factor = 1d;
        } else {
            // Stretch or shrink the factors according to the user-specified scale
            factor = scale.get() * scale2;
        }
        return factor;
    }

    private static class StyleableProperties {

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private static final CssMetaData<QuiverPlot, Number> SCALE
                = new CssMetaData<QuiverPlot, Number>("-w-plot-quiverplot-scale",
                        StyleConverter.getSizeConverter(), 0d) {

                    @Override
                    public boolean isSettable(QuiverPlot node) {
                        return node.scale != null && !node.scale.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(QuiverPlot node) {
                        return (StyleableProperty<Number>) node.scale;
                    }
                };
        private static final CssMetaData<QuiverPlot, Boolean> USEQUAD
                = new CssMetaData<QuiverPlot, Boolean>("-w-plot-quiverplot-usequad",
                        StyleConverter.getBooleanConverter(), false) {

                    @Override
                    public boolean isSettable(QuiverPlot node) {
                        return node.useQuad != null && !node.useQuad.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(QuiverPlot node) {
                        return (StyleableProperty<Boolean>) node.useQuad;
                    }
                };

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables
                    = new ArrayList<>(AbstractPlot.getClassCssMetaData());

            styleables.add(SCALE);
            styleables.add(USEQUAD);

            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

}
