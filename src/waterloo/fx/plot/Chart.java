/* 
 * This code is part of Project Waterloo from King's College London
 * <http://waterloo.sourceforge.net/>
 *
 * Copyright King's College London  2013-
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
package waterloo.fx.plot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.css.CssMetaData;
import javafx.css.FontCssMetaData;
import javafx.css.StyleConverter;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import waterloo.fx.transforms.AbstractTransform;
import waterloo.fx.transforms.Log10Transform;
import waterloo.fx.transforms.LogTransform;
import waterloo.fx.transforms.NOPTransform;

/**
 * The {@code Chart} is the basic container for representing charts in
 * waterlooFX.
 * <p>
 * The {@code Chart} is a {@code Pane} subclass with an embedded view. The
 * <strong>view</strong> is a {@code StackPane} that contains a {@code Canvas}
 * for plotting.
 * </p>
 * <p>
 * The {@code Chart} also hosts sub-{@code Region}s for representing a chart's
 * axes.
 * </p>
 * A {@code Chart} instance can host other {@code Chart} instances to create
 * layered graphs where each {@code Chart} has independent axes. The axis
 * diplays will be automatically positioned within the individual graphs to
 * prevent overlap.
 *
 * @author Malcolm Lidierth
 */
public class Chart extends Pane {

    /**
     * TODO: make these styleable
     */
    public static final boolean toolTipFlag = false;
    public static final boolean editable = true;
    public static final boolean interactive = true;

    /**
     * Singleton instance used as the standard Insets before the padding can be
     * calculated.
     */
    private static final Insets defaultInsets = new Insets(50, 50, 50, 50);
    private final AxisTop axisTop;
    private final AxisBottom axisBottom;
    private final AxisLeft axisLeft;
    private final AxisRight axisRight;
    /**
     * Defines the viewAspectRatio for this graph.
     * <p/>
     * If zero (or NaN), the view viewAspectRatio will change in relation to the
     * size of the graph or any parent.
     * <p/>
     * Otherwise, the view viewAspectRatio will be fixed at the specified value.
     * The width and height will alter:
     * <ol>
     * <li>to maintain this ratio</li>
     * <li>to maximize the space used by the view</li>
     * </ol>
     * When the viewAspectRatio is a valid non-zero number, the view for the
     * graph may be centered (the dafault) or have be positioned to the top/left
     * or bottom/right.
     */
    private final DoubleProperty viewAspectRatio = new StyleableDoubleProperty(Double.NaN) {

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "aspectRatio";
        }

        @Override
        public CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.ASPECTRATIO;
        }
    };
    private final ObjectProperty<TRANSFORMTYPE> xTransformType = new StyleableObjectProperty<TRANSFORMTYPE>(TRANSFORMTYPE.LINEAR) {

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "xTransformType";
        }

        @Override
        public CssMetaData<? extends Styleable, TRANSFORMTYPE> getCssMetaData() {
            return StyleableProperties.XTRANSFORMTYPE;
        }

        @Override
        public void applyStyle(StyleOrigin so, TRANSFORMTYPE v) {
            super.applyStyle(so, v);
            setXTransformType(v);
        }

    };
    private final ObjectProperty<TRANSFORMTYPE> yTransformType = new StyleableObjectProperty<TRANSFORMTYPE>(TRANSFORMTYPE.LINEAR) {

        //private StyleOrigin origin;
        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "yTransformType";
        }

        @Override
        public CssMetaData<? extends Styleable, TRANSFORMTYPE> getCssMetaData() {
            return StyleableProperties.YTRANSFORMTYPE;
        }

        @Override
        public void applyStyle(StyleOrigin so, TRANSFORMTYPE v) {
            super.applyStyle(so, v);
            setYTransformType(v);
        }

    };
    /**
     * The axisSet
     */
    private final AxisSet axisSet;
    //private final ObservableList<GJAnnotation> annotations = FXCollections.observableArrayList(new ArrayList<>());
    private final Canvas canvas;
    private final StackPane view;
    /**
     * Value for the xpos-axis at the left
     */
    private final DoubleProperty xLeft = new StyleableDoubleProperty(-1d) {

        @Override
        public CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.XLEFT;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "xLeft";
        }

    };
    /**
     * Value for the x-axis at the right-most position
     */
    private final DoubleProperty xRight = new StyleableDoubleProperty(1d) {

        @Override
        public CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.XRIGHT;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "xRight";
        }

    };
    /**
     * Value for the ypos-axis at the bottom
     */
    private final DoubleProperty yBottom = new StyleableDoubleProperty(-1d) {

        @Override
        public CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.YBOTTOM;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "yBottom";
        }

    };
    /**
     * Value for the ypos-axis at the top
     */
    private final DoubleProperty yTop = new StyleableDoubleProperty(1d) {

        @Override
        public CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.YTOP;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "yTop";
        }

    };
    /**
     * Value for the origin on the xpos-axis. Internal axisSet are painted here.
     */
    private final DoubleProperty xOrigin = new StyleableDoubleProperty(0d) {

        @Override
        public CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.XORIGIN;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "xOrigin";
        }

    };
    /**
     * Value for the origin on the xpos-axis. Internal axisSet are painted here.
     */
    private final DoubleProperty yOrigin = new StyleableDoubleProperty(0d) {

        @Override
        public CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.YORIGIN;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "xOrigin";
        }

    };
    /**
     * stroke width used to draw an internal axis
     */
    private final DoubleProperty axisStrokeWidth = new StyleableDoubleProperty(1d) {

        @Override
        public CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.AXISSTROKEWIDTH;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "axisStrokeWeight";
        }

    };
    private final DoubleProperty innerAxisStrokeWidth = new StyleableDoubleProperty(1.1d) {

        @Override
        public CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.INNERAXISSTROKEWIDTH;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "innerAxisStrokeWeight";
        }

    };
    /**
     * stroke width used to draw the minor grid
     */
    private final DoubleProperty minorGridStrokeWidth = new StyleableDoubleProperty(1.1d) {

        @Override
        public CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.MINORGRIDSTROKEWIDTH;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "minorGridStrokeWidth";
        }

    };
    private final DoubleProperty majorGridStrokeWidth = new StyleableDoubleProperty(1.3d) {

        @Override
        public CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.MAJORGRIDSTROKEWIDTH;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "majorGridStrokeWidth";
        }

    };
    /**
     * Set true to paint the minor grid
     */

    private final BooleanProperty minorGridPainted = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.MINORGRIDPAINTED;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "minorGridPainted";
        }

    };
    /**
     * Set true to paint the major grid
     */
    private final BooleanProperty majorGridPainted = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.MAJORGRIDPAINTED;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "majorGridPainted";
        }

    };
    /**
     * getParent true to paint the axisSet within the view
     */
    private final BooleanProperty innerAxisPainted = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.INNERAXISPAINTED;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "innerAxisPainted";
        }

    };
    /**
     * getParent true to paint the labels for axisSet within the view
     */
    private final BooleanProperty innerAxisLabelled = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.INNERAXISLABELLED;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "innerAxisLabelled";
        }

    };
    /**
     * Set true to paint left axis
     */
    private final BooleanProperty leftAxisPainted = new StyleableBooleanProperty(Boolean.TRUE) {

        @Override
        public CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.LEFTAXISPAINTED;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "leftAxisPainted";
        }

    };
    /**
     * Set true to paint right axis
     */
    private final BooleanProperty rightAxisPainted = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.RIGHTAXISPAINTED;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "rightAxisPainted";
        }

    };
    /**
     * Set true to paint top axis
     */
    private final BooleanProperty topAxisPainted = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.TOPAXISPAINTED;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "topAxisPainted";
        }

    };
    /**
     * Set true to paint bottom axis
     */
    private final BooleanProperty bottomAxisPainted = new StyleableBooleanProperty(Boolean.TRUE) {

        @Override
        public CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.BOTTOMAXISPAINTED;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "bottomAxisPainted";
        }

    };
    /**
     * Set true to label the left axis
     */
    private final BooleanProperty leftAxisLabelled = new StyleableBooleanProperty(Boolean.TRUE) {

        @Override
        public CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.LEFTAXISLABELLED;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "leftAxisLabelled";
        }

    };
    /**
     * Set true to label the right axis
     */
    private final BooleanProperty rightAxisLabelled = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.RIGHTAXISLABELLED;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "rightAxisLabelled";
        }

    };
    /**
     * Set true to label the top axis
     */
    private final BooleanProperty topAxisLabeled = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.TOPAXISLABELLED;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "topAxisLabelled";
        }

    };
    /**
     * Set true to label the bottom axis
     */
    private final BooleanProperty bottomAxisLabelled = new StyleableBooleanProperty(Boolean.TRUE) {

        @Override
        public CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.BOTTOMAXISLABELLED;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "bottomAxisLabelled";
        }

    };
    /**
     * Draw as polar plot
     */
    private final BooleanProperty polar = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.POLAR;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "polar";
        }

    };
    /**
     * Major grid color
     */
    private final ObjectProperty<Paint> majorGridColor = new StyleableObjectProperty<Paint>(Color.SLATEBLUE) {

        @Override
        public CssMetaData<Chart, Paint> getCssMetaData() {
            return StyleableProperties.MAJORGRIDCOLOR;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "majorGridColor";
        }

    };
    /**
     * Minor grid color
     */
    private final ObjectProperty<Paint> minorGridColor = new StyleableObjectProperty<Paint>(Color.SLATEBLUE) {

        @Override
        public CssMetaData<Chart, Paint> getCssMetaData() {
            return StyleableProperties.MINORGRIDCOLOR;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "minorGridColor";
        }

    };
    /**
     * Inner axis color
     */
    private final ObjectProperty<Paint> innerAxisColor = new StyleableObjectProperty<Paint>(Color.BLACK) {

        @Override
        public CssMetaData<Chart, Paint> getCssMetaData() {
            return StyleableProperties.INNERAXISCOLOR;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "innerAxisColor";
        }

    };
    private final ObjectProperty<Paint> axisColor = new StyleableObjectProperty<Paint>(Color.BLACK) {

        @Override
        public CssMetaData<Chart, Paint> getCssMetaData() {
            return StyleableProperties.AXISCOLOR;
        }

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "axisColor";
        }

    };
    private final ObjectProperty<Paint> altFillVertical = new StyleableObjectProperty<Paint>(Color.TRANSPARENT) {

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "altFillVertical";
        }

        @Override
        public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
            return StyleableProperties.ALTFILLVERTICAL;
        }

    };
    private final ObjectProperty<Paint> altFillHorizontal = new StyleableObjectProperty<Paint>(Color.TRANSPARENT) {

        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "altFillHorizontal";
        }

        @Override
        public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
            return StyleableProperties.ALTFILLHORIZONTAL;
        }

    };
    public String yTopLabel = "Axis Label";
    public Paint yTopColor = Color.BLACK;
    public double yTopTickLength = 5;
    public String yBottomLabel = "Axis Label";
    public Paint yBottomColor = Color.BLACK;
    public double yBottomTickLength = 5;
    public String xLeftLabel = "Axis Label";
    public Paint xLeftColor = Color.BLACK;
    public double xLeftTickLength = 5;
    public String xRightLabel = "Axis Label";
    public Paint xRightColor = Color.BLACK;
    public double xRightTickLength = 5;
    /**
     * Base font to use. This is styleable via the "-w-font-" settings.
     */
    private Font font = Font.getDefault();
    /**
     * The inner axes are those drawn within the plotting area of the chart.
     * Inner axis colors, font characteristics etc are editable separately from
     * those drawn outside of the plotting area.
     *
     * innerAxisFontSize represents the size of the font to use.
     */
    private DoubleProperty innerAxisFontSize = new StyleableDoubleProperty(
            font == null ? Font.getDefault().getSize() : font.getSize()) {

                @Override
                public Object getBean() {
                    return Chart.this;
                }

                @Override
                public String getName() {
                    return "innerAxisFontSize";
                }

                @Override
                public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return (CssMetaData<? extends Styleable, Number>) StyleableProperties.INNERAXISFONTSIZE;
                }

            };
    /**
     * {@code axisFontSize} holds the size of the font to be used for rendering
     * tick marks, labels etc in th outer axes (those drawn to the left, right,
     * top etc of the plotting area).
     */
    private DoubleProperty axisFontSize = new StyleableDoubleProperty(
            font == null ? Font.getDefault().getSize() : font.getSize()) {

                @Override
                public Object getBean() {
                    return Chart.this;
                }

                @Override
                public String getName() {
                    return "axisFontSize";
                }

                @Override
                public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return (CssMetaData<? extends Styleable, Number>) StyleableProperties.INNERAXISFONTSIZE;
                }

            };
    public final ObjectProperty<Font> fontProperty = new StyleableObjectProperty<Font>(font) {

        @Override
        public void set(Font value) {
            font = value;
        }

        @Override
        public Font get() {
            return font;
        }

        @Override
        public FontCssMetaData<Chart> getCssMetaData() {
            return (FontCssMetaData<Chart>) StyleableProperties.FONT;
        }

        @Override
        public Chart getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "font";
        }

    };
    private double yTopOffset = 0;
    private double yBottomOffset = 0;
    private double xLeftOffset = 0;
    private double xRightOffset = 0;
    private ObjectProperty<VIEWALIGNMENT> viewAlignment = new StyleableObjectProperty<VIEWALIGNMENT>(VIEWALIGNMENT.CENTER) {
        @Override
        public Object getBean() {
            return Chart.this;
        }

        @Override
        public String getName() {
            return "viewAlignment";
        }

        @Override
        public CssMetaData<Chart, VIEWALIGNMENT> getCssMetaData() {
            return StyleableProperties.VIEWALIGN;
        }
    };
    private boolean clipping = true;
    private ObjectBinding<Rectangle2D> axesBounds = new AxesBounds();
    /**
     * Sets the interval between major xpos-axis ticks and grids.
     */
    private MajorXInterval majorXInterval = new MajorXInterval();
    /**
     * Number of minor ticks/grids in the majorTickInterval.
     * <strong>This is a hint, not all AxesSets support its use.</strong>
     */
    private int minorCountXHint = 4;
    private MajorYInterval majorYInterval = new MajorYInterval();
    private int minorCountYHint = 4;
    private double dragXStart = Double.NaN;
    private double dragYStart = Double.NaN;
    private double deltaX, deltaY;
    private Tolerance xTol = new Tolerance("X");
    private Tolerance yTol = new Tolerance("Y");

    // MAIN CODE
    public Chart(Chart layer) {
        this();
        adjustAxes(layer);
        getChildren().add(layer);
        setPadding(computeRequiredInsets());
        layer.setPadding(getPadding());
    }

    public Chart() {

        //setManaged(false);
        super();
        setPrefWidth(500d);
        setPrefHeight(500d);

        getStyleClass().add("gjchart");

        canvas = new Canvas(500d, 500d);
        view = new StackPane(canvas);
        //view.getChildren().add(new Pane());

        getChildren().add(view);

        setPadding(computeRequiredInsets());

        view.setLayoutX(50d);
        view.setLayoutY(50d);
        view.setPrefHeight(getPrefHeight() - getPadding().getTop() - getPadding().getBottom());
        view.setPrefWidth(getPrefWidth() - getPadding().getLeft() - getPadding().getRight());
        view.setCenterShape(true);
        view.setCursor(Cursor.DEFAULT);
        view.getStyleClass().add("view");

        canvas.setLayoutX(0d);
        canvas.setLayoutY(0d);
        canvas.setHeight(view.getPrefHeight());
        canvas.setWidth(view.getPrefWidth());

        axisRight = new AxisRight(this);
        axisTop = new AxisTop(this);
        axisLeft = new AxisLeft(this);
        axisBottom = new AxisBottom(this);
        getChildren().add(axisTop);
        getChildren().add(axisRight);
        getChildren().add(axisLeft);
        getChildren().add(axisBottom);
        axisSet = new AxisSet(axisRight, axisTop, axisLeft, axisBottom);

        // Add the scene dimension listener
        ChangeListener<Scene> addedToScene = (ObservableValue<? extends Scene> ov, Scene t, Scene t1) -> {
            // Scrolling via mouse wheel or touch-enabled device

            view.setOnScroll((ScrollEvent t0) -> {

                if (t0.isInertia() || t0.getDeltaY() == 0) {
                    return;
                } else {
                    double xdiff = Math.signum(t0.getDeltaY()) * (getXMax() - getXMin());
                    double ydiff = Math.signum(t0.getDeltaY()) * (getYMax() - getYMin());
                    setXLeft(getXLeft() - xdiff / 50.0);
                    setXRight(getXRight() + xdiff / 50.0);
                    setYBottom(getYBottom() - ydiff / 50.0);
                    setYTop(getYTop() + ydiff / 50.0);
                }
                requestLayout();
            });

            view.setOnMousePressed((MouseEvent m0) -> {
                dragXStart = m0.getX();
                dragYStart = m0.getY();
            });

            view.setOnMouseReleased((MouseEvent m1) -> {
                dragXStart = Double.NaN;
                dragYStart = Double.NaN;
                view.setCursor(Cursor.DEFAULT);
            });

            view.setOnMouseMoved((MouseEvent m2) -> {
                if (m2.isStillSincePress()) {
                    return;
                }
                if (isInnerAxisPainted()) {
                    double x = toPositionX(m2.getX());
                    double y = toPositionY(m2.getY());
                    if (atOrigin(x, y)) {
                        view.setCursor(Cursor.HAND);
                    } else if (onXAxis(y)) {
                        view.setCursor(Cursor.E_RESIZE);
                    } else if (onYAxis(x)) {
                        view.setCursor(Cursor.N_RESIZE);
                    } else {
                        view.setCursor(Cursor.DEFAULT);
                    }
                }
            });

            view.setOnMouseDragged((MouseEvent m4) -> {

                if (m4.isStillSincePress()) {
                    return;
                }
                double x = toPositionX(m4.getX());
                double y = toPositionY(m4.getY());
                deltaX = toPositionX(dragXStart) - toPositionX(m4.getX());
                deltaY = toPositionY(dragYStart) - toPositionY(m4.getY());

                if (view.getCursor().equals(Cursor.HAND)) {
                    setXOrigin(getXOrigin() - deltaX);
                    setYOrigin(getYOrigin() - deltaY);
                    dragXStart = m4.getX();
                    dragYStart = m4.getY();
                    requestPaint();
                } else if (!onAxis(x, y)) {
                    double xl = getXLeft() + deltaX;
                    double xr = getXRight() + deltaX;
                    setXLeft(xl);
                    setXRight(xr);
                    dragXStart = m4.getX();

                    double yt = getYTop() + deltaY;
                    double yb = getYBottom() + deltaY;
                    setYTop(yt);
                    setYBottom(yb);
                    dragYStart = m4.getY();
                    requestLayout();

                } else if (onXAxis(y)) {
                    if (x >= getXOrigin()) {
                        setXRight(getXRight() + deltaX);
                    } else {
                        setXLeft(getXLeft() + deltaX);
                    }
                    dragXStart = m4.getX();
                } else if (onYAxis(x)) {
                    if (y >= getYOrigin()) {
                        setYTop(getYTop() + deltaY);
                    } else {
                        setYBottom(getYBottom() + deltaY);
                    }
                    dragYStart = m4.getY();
                }

            });

            // Do this only of a GJGraph has been set as the root element.
            // This sets the GJGraph to resize with the scene.
            if (t1 != null && this.equals(t1.getRoot())) {
                setLayoutX(0d);
                setLayoutY(0d);
                prefWidthProperty().bind(t1.widthProperty());
                prefHeightProperty().bind(t1.heightProperty());
            }
        };
        view.sceneProperty().addListener(addedToScene);

        /**
         * Install a change listener that responds to changes in the axis limits
         * or origin.
         *
         * The listener simple posts a layout request which will cause a repaint
         * of the canvas and a layout for tick marks, labels etc in the axes.
         */
        ChangeListener<Number> axisLimitListener = (ObservableValue<? extends Number> val, Number n0, Number n1) -> {
            requestLayout();
        };
        xLeftProperty().addListener(axisLimitListener);
        xRightProperty().addListener(axisLimitListener);
        yTopProperty().addListener(axisLimitListener);
        yBottomProperty().addListener(axisLimitListener);
        xOrigin().addListener(axisLimitListener);
        yOrigin().addListener(axisLimitListener);
        // Also, bind to changes in the width/height as we need to recalculate
        // grid, tick  etc. pixel positions.
        widthProperty().addListener(axisLimitListener);
        heightProperty().addListener(axisLimitListener);
        prefWidthProperty().addListener(axisLimitListener);
        prefHeightProperty().addListener(axisLimitListener);

        /**
         * If the parent extends Region, which will normally be the case, bind
         * the preferred width and height properties of the graph to the width
         * and height of the parent.
         */
        parentProperty().addListener((ObservableValue<? extends Parent> ov, Parent t, Parent t1) -> {
            if (t1 instanceof Region) {
                try {
                    if (((Region) t1).getPrefWidth() == Pane.USE_COMPUTED_SIZE) {
                        ((Region) t1).resize(getPrefWidth(), getPrefHeight());
                    } else {
                        setPrefWidth(((Region) t1).getWidth());
                        setPrefHeight(((Region) t1).getHeight());
                    }
                } finally {
                    setLayoutX(0d);
                    setLayoutY(0d);
                    prefWidthProperty().bind(((Region) t1).widthProperty());
                    prefHeightProperty().bind(((Region) t1).heightProperty());
                    //throw (new IllegalStateException("Error: Chart added as child of " + t1.getClass() + ": data may be displaced during layout"));
                }
            }
            if (t1 instanceof Chart) {
                viewAspectRatioProperty().bind(((Chart) t1).viewAspectRatioProperty());
            }
            // Update the layout
            // Note this will impose any aspect ratio restriction
            requestParentLayout();
        });

        getChildren().addListener((ListChangeListener.Change<? extends Node> c) -> {
            while (c.next()) {
                List<? extends Node> list = c.getAddedSubList();
                list.stream().filter((node) -> (node instanceof Chart)).forEach((node) -> {
                    // Make sure a child Chart shares insets, view position etc.
                    // with the parent.
                    //node.setManaged(true);
                    adjustAxes((Chart) node);
                    setPadding(computeRequiredInsets());
                    ((Chart) node).setPadding(getPadding());
                    ((Chart) node).requestLayout();
                    // It makes no sense to have a background on the child as it will
                    // obscure the parent's contents.
                    ((Chart) node).setStyle("-fx-background-color: transparent");
                });
                // If a plot, plot collection or annotation pane has been added
                // to the graph, transfer it to the view
                list.stream().filter(node -> (node instanceof AbstractPlot
                        || node instanceof PlotCollection)
                        || node instanceof AnnotationPane).forEach(node -> {
                            // Plots need to be added to the view, but generic software like
                            // the Oracle Scene Builder does not know this. It can be useful in
                            // these cases to allow drag-and-drop of plots to chart, then reparent them
                            // to the view to which such software will not otherwise provide
                            // access.
                            getChildren().remove(node);
                            view.getChildren().add(node);
                        });
            }
        });

        view.getChildren().addListener((ListChangeListener.Change<? extends Node> c) -> {
            while (c.next()) {
                List<? extends Node> list = c.getAddedSubList();
                list.stream().filter((node) -> (node instanceof AbstractPlot)).forEach((node) -> {
                    AbstractPlot plot = (AbstractPlot) node;
//                    plot.setLayoutX(0d);
//                    plot.setLayoutY(0d);
                    int index = getPlots().indexOf(node);
                    plot.setPlotStyleIndex(index);
                    plot.getAllPlots().forEach((p) -> {
                        ((AbstractPlot) p).setPlotStyleIndex(index);
                    });
                });
                requestLayout();
            }
        });

        requestLayout();

    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     * @since JavaFX 8.0
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * NB. To use other panes, e.g. a GridPane or TitledPane, add the chart to
     * one of the supported pane's then add that to the target.
     *
     * @param node the node the chart is to be added to
     * @return true if the node is supported as a parent of a chart
     */
    private boolean isSupportedAsParent(Node node) {
        Class clzz = node.getClass();
        return clzz.equals(Pane.class)
                || clzz.equals(Chart.class)
                || clzz.equals(AnchorPane.class)
                || clzz.equals(FlowPane.class)
                || clzz.equals(StackPane.class);
    }

    public ObjectProperty<Paint> innerAxisColorProperty() {
        return innerAxisColor;
    }

    public Paint getInnerAxisColor() {
        return innerAxisColor.get();
    }

    public void setInnerAxisColor(Paint color) {
        innerAxisColor.set(color);
    }

    /**
     * The viewAlignment positions the display of the view on the longest axis
     * of the graph when the viewAspectRatio is a non-zero, non-NaN value.
     *
     * Available settings are:
     * <ol>
     * <li>VIEWALIGN.CENTER - centers the view in the graph</li>
     * <li>VIEWALIGN.LEFT_TOP - positions the view to the left or top in the
     * graph</li>
     * <li>VIEWALIGN.RIGHT_BOTTOM - positions the view to the right or bottom in
     * the graph</li>
     * </ol>
     *
     *
     * @return the viewAlignment
     */
    public VIEWALIGNMENT getViewAlignment() {
        return viewAlignment.get();
    }

    /**
     * The viewAlignment positions the display of the view on the longest axis
     * of the graph when the viewAspectRatio is a non-zero, non-NaN value.
     *
     * Available settings are:
     * <ol>
     * <li>VIEWALIGN.CENTER - centers the view in the graph</li>
     * <li>VIEWALIGN.LEFT_TOP - positions the view to the left or top in the
     * graph</li>
     * <li>VIEWALIGN.RIGHT_BOTTOM - positions the view to the right or bottom in
     * the graph</li>
     * </ol>
     *
     * @param val the viewAlignment to set
     */
    public void setViewAlignment(VIEWALIGNMENT val) {
        viewAlignment.set(val);
    }

    /**
     * The viewAlignment positions the display of the view on the longest axis
     * of the graph when the viewAspectRatio is a non-zero, non-NaN value.
     *
     * Available settings are:
     * <ol>
     * <li>VIEWALIGN.CENTER - centers the view in the graph</li>
     * <li>VIEWALIGN.LEFT_TOP - positions the view to the left or top in the
     * graph</li>
     * <li>VIEWALIGN.RIGHT_BOTTOM - positions the view to the right or bottom in
     * the graph</li>
     * </ol>
     *
     * @return the viewAlignment property
     */
    public ObjectProperty<VIEWALIGNMENT> viewAlignmentProperty() {
        return viewAlignment;
    }

    /**
     * Called when a graph is added as a child of another graph to adjust axis
     * offsets
     *
     * @param layer
     */
    private void adjustAxes(Chart layer) {
        if (layer.isLeftAxisPainted()) {
            layer.xLeftOffset += xLeftOffset + axisLeft.computePrefWidth(-1d);
        }
        if (layer.isRightAxisPainted()) {
            layer.xRightOffset += xRightOffset + axisRight.computePrefWidth(-1d);
        }
        if (layer.isTopAxisPainted()) {
            layer.yTopOffset += yTopOffset + axisTop.computePrefHeight(-1d);
        }
        if (layer.isBottomAxisPainted()) {
            layer.yBottomOffset += yBottomOffset + axisBottom.computePrefHeight(-1d);
            ;
        }
        layer.setMouseTransparent(true);
    }

    /**
     * Calculate the offsets for each axis to accommodate their contents
     */
    private void enforceMinimumOffsets() {
        if (getLayers().size() > 1) {
            double sumxl = getFirstLayer().axisLeft.computePrefWidth(-1d);
            for (int k = 1; k < getLayers().size(); k++) {
                if (getLayers().get(k).isLeftAxisPainted()) {
                    getLayers().get(k).xLeftOffset = sumxl;
                    sumxl += getLayers().get(k).axisLeft.computePrefWidth(-1d);
                }
            }
            sumxl = getFirstLayer().xRightOffset;
            for (int k = 1; k < getLayers().size(); k++) {
                if (getLayers().get(k).isRightAxisPainted()) {
                    getLayers().get(k).xRightOffset = sumxl;
                    sumxl += getLayers().get(k).axisRight.computePrefWidth(-1d);
                }
            }
            sumxl = getFirstLayer().yBottomOffset + getFirstLayer().axisBottom.computePrefHeight(-1d);
            for (int k = 1; k < getLayers().size(); k++) {
                if (getLayers().get(k).isBottomAxisPainted()) {
                    getLayers().get(k).yBottomOffset = sumxl;
                    sumxl += getLayers().get(k).axisBottom.computePrefHeight(-1d);
                }
            }
            sumxl = getFirstLayer().yTopOffset + getFirstLayer().axisTop.computePrefHeight(-1d);
            for (int k = 1; k < getLayers().size(); k++) {
                if (getLayers().get(k).isTopAxisPainted()) {
                    getLayers().get(k).yTopOffset = sumxl;
                    sumxl += getLayers().get(k).axisTop.computePrefHeight(-1d);
                }
            }
        }
    }

    /**
     * Returns the sum of the widths of the horizontally aligned components:
     * left and right axes and the view.
     *
     * This method takes no account of other nodes that may have been added and
     * ignores the height parameter.
     *
     * @param height presently ignored (use -1d);
     *
     * @return the minimum width to accommodate the components.
     */
    @Override
    public double computePrefWidth(double height) {
        return getPadding().getLeft() + getPadding().getRight() + view.getWidth();
    }

    /**
     * Returns the sum of the widths of the vertically aligned components: top
     * and bottom axes and the view.
     *
     * This method takes no account of other nodes that may have been added and
     * ignores the width parameter.
     *
     * @param width presently ignored (use -1d);
     *
     * @return the minimum height to accommodate the components.
     */
    @Override
    public double computePrefHeight(double width) {
        return getPadding().getTop() + getPadding().getBottom() + view.getHeight();
    }

    /**
     * Returns a set of insets with dimensions just adequate to accomodate the
     * contents of all display axis elements.
     *
     * @return the Insets
     */
    private Insets computeRequiredInsets() {
        enforceMinimumOffsets();
        if (!getFirstLayer().equals(this)) {
            return getFirstLayer().computeRequiredInsets();
        } else {
            if (axisLeft == null) {
                return defaultInsets;
            } else {
                double xl = axisLeft.computePrefWidth(-1d) + xLeftOffset;
                double xr = axisRight.computePrefWidth(-1d) + xRightOffset;
                double yb = axisTop.computePrefHeight(-1d) + yBottomOffset;
                double yt = axisBottom.computePrefHeight(-1d) + yTopOffset;
                for (Chart g : getLayers()) {
                    if (g.isLeftAxisPainted()) {
                        xl = Math.max(xl, g.axisLeft.computePrefWidth(-1d) + g.xLeftOffset);
                    }
                    if (g.isRightAxisPainted()) {
                        xr = Math.max(xr, g.axisRight.computePrefWidth(-1d) + g.xRightOffset);
                    }
                    if (g.isBottomAxisPainted()) {
                        yb = Math.max(yb, g.axisBottom.computePrefHeight(-1d) + g.yBottomOffset);
                    }
                    if (g.isTopAxisPainted()) {
                        yt = Math.max(yt, g.axisTop.computePrefHeight(-1d) + g.yTopOffset);
                    }
                }
                return new Insets(yt + 5, xr + 5, yb + 5, xl + 5);
            }
        }
    }

    /**
     *
     * @return the DoubleProperty wrapping the size of Font used to label the
     * inner axes
     */
    public DoubleProperty innerAxisFontSizeProperty() {
        return innerAxisFontSize;
    }

    /**
     *
     * @return size of Font used to label the inner axes
     */
    public double getInnerAxisFontSize() {
        return innerAxisFontSize.get();
    }

    /**
     * Sets the font size used to label the inner axes.
     *
     * @param val size of Font used to label the inner axes
     */
    public void setInnerAxisFontSize(double val) {
        innerAxisFontSize.set(val);
    }

    /**
     * Sets the base font for use in this Chart.
     *
     * Font size, posture and weight can be set through CSS.
     *
     * Fonts used for labeling axes will be based on this font but their size
     * and color may be set independently.
     *
     * @return the font property
     */
    public ObjectProperty<Font> fontProperty() {
        return fontProperty;
    }

    /**
     * Returns the TRANSFORMTYPE for the x-axis.
     *
     * @return the TRANSFORMTYPE
     */
    public TRANSFORMTYPE getXTransformType() {
        return xTransformType.get();
    }

    /**
     * Sets the TRANSFORMTYPE for the x-axis.
     *
     * @param type
     */
    public void setXTransformType(TRANSFORMTYPE type) {
        axisSet.setXTransform(getTransformForType(type));
        xTransformType.set(type);
    }

    /**
     * ObjectProperty for the TRANSFORMTYPE of the x-axis.
     *
     * @return the property
     */
    public ObjectProperty<TRANSFORMTYPE> xTransformTypeProperty() {
        return xTransformType;
    }

    /**
     * Returns the TRANSFORMTYPE for the y-axis.
     *
     * @return the TRANSFORMTYPE
     */
    public TRANSFORMTYPE getYTransformType() {
        return yTransformType.get();
    }

    /**
     * Sets the TRANSFORMTYPE for the y-axis.
     *
     * @param type
     */
    public void setYTransformType(TRANSFORMTYPE type) {
        axisSet.setYTransform(getTransformForType(type));
        yTransformType.set(type);
    }

    /**
     * ObjectProperty for the TRANSFORMTYPE of the y-axis.
     *
     * @return the property
     */
    public ObjectProperty<TRANSFORMTYPE> yTransformTypeProperty() {
        return yTransformType;
    }

    /**
     * Returns a transform instance for the specified TRANSFORMTYPE
     *
     * @param type - the TRANSFORMTYPE
     * @return an AbstractTransform subclass
     */
    private AbstractTransform getTransformForType(TRANSFORMTYPE type) {
        switch (type) {
            case LINEAR:
                return new NOPTransform();
            case LOG10:
                return new Log10Transform();
            case LOG:
                return new LogTransform();
            default:
                return null;
        }
    }

    /**
     * Returns the viewAspectRatio for the view within the graph.
     * <p>
     * A value of zero or {@code }Double.NaN} value allows the view to be sized
     * to fill the graph. A non-zero, non-NaN value causes the view to be sized
     * to fill the smallest dimension of the graph and constrains the size in
     * the remaining dimension to match the required aspect ratio.
     * </p>
     * <p>
     * When a non-zero, non-NaN value is used the view can be positioned to the
     * centre, top/left or right/bottom of the longest dimension by setting the
     * viewAlignment property.
     * </p>
     *
     * @return the aspect ratio
     */
    public double getViewAspectRatio() {
        return viewAspectRatio.get();
    }

    /**
     * Sets the viewAspectRatio for the view within the graph.
     * <p>
     * A value of zero or {@code }Double.NaN} value allows the view to be sized
     * to fill the graph. A non-zero, non-NaN value causes the view to be sized
     * to fill the smallest dimension of the graph and constrains the size in
     * the remaining dimension to match the required aspect ratio.
     * </p>
     * <p>
     * When a non-zero, non-NaN value is used the view can be positioned to the
     * centre, top/left or right/bottom of the longest dimension by setting the
     * viewAlignment property.
     * </p>
     * <em>
     * Setting the aspect ratio can also be used to help fix the size of the
     * view as the width of left and right axes is altered to accommodate
     * changing tick labels. The axes will expand into the padding area of the
     * chart without altering the size of the view.
     * </em>
     * <strong>
     * The aspect ratio may not be set independently on a Chart that is a child
     * of another Chart. The views must overlap so the aspect ratio of the first
     * layer in the view will always be used</strong>. This does not prevent
     * GJCharts being added, for example as insets to a chart, as long as the
     * inset is housed in a standard container such as a {@code Pane}.
     *
     * @param val the aspect ratio
     */
    public void setViewAspectRatio(double val) {
        if (!viewAspectRatio.isBound()) {
            viewAspectRatio.set(val);
        }
    }

    /**
     * Returns the viewAspectRatio property.
     *
     * @return the property
     */
    public final DoubleProperty viewAspectRatioProperty() {
        return viewAspectRatio;
    }

    /**
     * Internal method used to find the required height and width to satisfy the
     * aspect ratio constraint given a specified height and width.
     *
     * @param w the present width
     * @param h the present height
     *
     * @return a double[2] with width and height in the two elements.
     */
    private double[] computePrefSize(double w, double h) {
        double ratio = getViewAspectRatio();
        if (ratio == 0d || Double.isNaN(ratio)) {
            return new double[]{w, h};
        } else {
            double requiredHeight = w / ratio;
            double requiredWidth = requiredHeight * ratio;

            if (requiredHeight > h || requiredWidth > w) {
                requiredWidth = h * ratio;
                requiredHeight = requiredWidth / ratio;
            }
            return new double[]{requiredWidth, requiredHeight};
        }
    }

    /**
     * Returns the the Chart instance that is first in painting order. With a
     * single layered chart, this will be the instance that the method is called
     * on. With multiple-layers, this will be the instance that parents the
     * others.
     *
     * @return the the Chart instance that is first in painting order
     */
    public Chart getFirstLayer() {
        if (getParent() != null && getParent() instanceof Chart) {
            return ((Chart) getParent()).getFirstLayer();
        } else {
            return this;
        }
    }

    /**
     * Returns an {@literal ArrayList<Chart>} with references to the layers of
     * this chart. For a single-layered chart, this will contain only one
     * element: i.e. a reference to the Chart instance the method was called on.
     *
     * @return an {@literal ArrayList<Chart>} with references to the layers
     */
    public ArrayList<Chart> getLayers() {
        ArrayList<Chart> layers = new ArrayList<>();
        layers.add(this);
        getChildren().filtered(x -> x instanceof Chart)
                .forEach(node -> {
                    layers.add((Chart) node);
                });
        return layers;
    }

    /**
     * Returns a list of plots added to this Chart
     *
     * @return the plot list.
     */
    public final ArrayList<AbstractPlot> getPlots() {
        ArrayList<AbstractPlot> arr = new ArrayList<>();
        view.getChildren().stream().filter((node) -> (node instanceof AbstractPlot)).forEach((node) -> {
            arr.add((AbstractPlot) node);
        });
        return arr;
    }

    /**
     * Called by the parent's layout mechanism.
     *
     * This should not normally be called directly by the user - use
     * {@code requestLayout()} instead.
     */
    @Override

    public void layoutChildren() {
        updateLayout();
        super.layoutChildren();
        // Layout has been called on the view so render its contents.
        // This is a convenient place to set the layout constraints on the nodes
        // for each plot: they will then have their layoutChildren() method called in
        // the normal scene layout pass.
        getLayers().stream().forEach((Chart g) -> {
            g.paintCanvas();
            g.arrangePlots();
        });
    }

    private void arrangePlots() {
        view.getChildren().stream().forEach(x -> {
            if (x instanceof AbstractPlot) {
                ((AbstractPlot) x).arrangePlot(this);
            } else if (x instanceof PlotCollection) {
                ((PlotCollection) x).arrangePlots(this);
            }
        });
    }

    /**
     * Does much of the work required in the {@code layoutChildren()} method.
     */
    private void updateLayout() {

        Insets padding = computeRequiredInsets();

        setPadding(padding);

        //Size the view
        double w = getPrefWidth() - getPadding().getLeft() - getPadding().getRight();
        double h = getPrefHeight() - getPadding().getTop() - getPadding().getBottom();

        // If the aspect ratio setting is use specified, apply it
        if (viewAspectRatio.get() != 0d && !Double.isNaN(viewAspectRatio.get())) {
            double[] dim = computePrefSize(w, h);
            w = dim[0];
            h = dim[1];
        }
        view.setPrefWidth(w);
        view.setMinWidth(w);
        view.setMaxWidth(w);

        view.setPrefHeight(h);
        view.setMaxHeight(h);
        view.setMinHeight(h);

        if (Double.isNaN(viewAspectRatio.get()) || viewAspectRatio.get() == 0d) {
            // No aspect ratio set so fill the graph with the view
            view.setLayoutX(getPadding().getLeft());
            view.setLayoutY(getPadding().getTop());
        } else {
            // Aspect ratio set so at least one dimension will generally have white space
            double extraX = getPrefWidth() - w - getPadding().getLeft() - getPadding().getRight();
            double extraY = getPrefHeight() - h - getPadding().getTop() - getPadding().getBottom();
            switch (viewAlignment.get()) {
                case CENTER:
                default:
                    // Place view at center for both x and y
                    view.setLayoutX(getPadding().getLeft() + extraX / 2d);
                    view.setLayoutY(getPadding().getTop() + extraY / 2d);
                    break;
                case RIGHT_BOTTOM:
                    if (getPrefWidth() > getPrefHeight()) {
                        // Fill vertically, shift to right
                        view.setLayoutX(getPadding().getLeft() + extraX);
                        view.setLayoutY(getPadding().getTop());
                    } else {
                        // Fill horiizontally, shift to bottom 
                        view.setLayoutX(getPadding().getLeft());
                        view.setLayoutY(getPadding().getTop() + extraY);
                    }
                    break;
                case LEFT_TOP:
                    // Do nothing special here as this is standard behaviour 
                    view.setLayoutX(getPadding().getLeft());
                    view.setLayoutY(getPadding().getTop());
            }
        }

        // Set the canvas to fill the view
        canvas.setLayoutX(0);
        canvas.setLayoutY(0);
        canvas.setWidth(w);
        canvas.setHeight(h);
        // Resize any other nodes added to the view
        for (Node node : view.getChildrenUnmodifiable()) {
            node.resize(w, h);
            if (node instanceof Parent) {
                ((Parent) node).requestLayout();
            }
        }

        // If clipping is true, this limits painting of added nodes to the limits
        // of the view bounds
        if (clipping) {
            view.setClip(new Rectangle(0, 0, w, h));
        }

//        // For each annotation, recalculate the nodes pixel position
//        annotations.stream().forEach((GJAnnotation node) -> {
//            // Get the required location in the view
//            Point2D p = toPixel(node.getX(), node.getY());
//            // Convert from view space to this pane's space (as the node
//            // is in the view' parent's layout.
//            p = view.localToParent(p);
//            // Set the xpos,ypos location
//            node.getNode().relocate(p.getX(), p.getY());
//        });
        // Layout the axes
        axisTop.setLayoutX(view.getLayoutX());
        axisTop.setLayoutY(view.getLayoutY() - yTopOffset - axisTop.computePrefHeight(-1d));
        axisTop.setPrefHeight(Region.USE_COMPUTED_SIZE);

        axisBottom.setLayoutX(view.getLayoutX());
        axisBottom.setLayoutY(view.getLayoutY() + view.getPrefHeight() + yBottomOffset);
        axisBottom.setPrefHeight(Region.USE_COMPUTED_SIZE);

        axisLeft.setLayoutX(view.getLayoutX() - axisLeft.computePrefWidth(-1d) - xLeftOffset);
        axisLeft.setLayoutY(view.getLayoutY());
        axisLeft.setPrefWidth(Region.USE_COMPUTED_SIZE);

        axisRight.setLayoutX(view.getLayoutX() + view.getPrefWidth()
                + xRightOffset);
        axisRight.setLayoutY(view.getLayoutY());
        axisRight.setPrefWidth(Region.USE_COMPUTED_SIZE);

    }

    /**
     * Paints the canvas that forms the background of the view. This contains
     * the inner axes, grids and fills.
     */
    private void paintCanvas() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        axisSet.paintGrid(g);
    }

    /**
     * Returns the x-axis value to the extreme left of the view.
     *
     * @return the value
     */
    public final double getXLeft() {
        return xLeft.doubleValue();
    }

    /**
     * Sets the x-axis value to the extreme left of the view.
     *
     * @param value the value to use
     */
    public final void setXLeft(double value) {
        xLeft.set(value);
    }

    /**
     *
     * @return the property
     */
    public final DoubleProperty xLeftProperty() {
        return xLeft;
    }

    /**
     * Returns the x-axis value to the extreme right of the view.
     *
     * @return the value
     */
    public final double getXRight() {
        return xRight.doubleValue();
    }

    /**
     * Sets the x-axis value to the extreme right of the view.
     *
     * @param value the value to use
     */
    public final void setXRight(double value) {
        xRight.set(value);
    }

    /**
     *
     * @return the property
     */
    public final DoubleProperty xRightProperty() {
        return xRight;
    }

    public final double getYBottom() {
        return yBottom.doubleValue();
    }

    public final void setYBottom(double val) {
        yBottom.set(val);
    }

    public final DoubleProperty yBottomProperty() {
        return yBottom;
    }

    public final double getYTop() {
        return yTop.doubleValue();
    }

    public final void setYTop(double val) {
        yTop.set(val);
    }

    public final DoubleProperty yTopProperty() {
        return yTop;
    }

    /**
     * @return the view
     */
    public StackPane getView() {
        return view;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public final ObjectBinding<Rectangle2D> axesBounds() {
        return axesBounds;
    }

    public final Rectangle2D getAxesBounds() {
        return axesBounds.get();
    }

    /**
     *
     * @return true if the left-most x-axis value is larger than the right-most.
     */
    public final boolean isReverseX() {
        return getXLeft() > getXRight();
    }

    public final void setReverseX(boolean flag) {
        double temp = getXLeft();
        if (flag) {
            if (getXLeft() < getXRight()) {
                setXLeft(getXRight());
                setXRight(temp);
            }
        } else {
            if (getXLeft() > getXRight()) {
                setXLeft(getXRight());
                setXRight(temp);
            }
        }
    }

    /**
     *
     * @return true if the bottom-most x-axis value is larger than the top-most.
     */
    public final boolean isReverseY() {
        return getYBottom() > getYTop();
    }

    public final void setReverseY(boolean flag) {
        double temp = getYBottom();
        if (flag) {
            if (getYBottom() < getYTop()) {
                setYBottom(getYTop());
                setYTop(temp);
            }
        } else {
            if (getYBottom() > getYTop()) {
                setYBottom(getYTop());
                setYTop(temp);
            }
        }
    }

    public final MajorXInterval majorXInterval() {
        return majorXInterval;
    }

    /**
     * The interval between major tick marks and grid lines in chart coordinate
     * space on the x-axis. This is calculated automatically unless the user
     * enforces a specified value.
     *
     * @return the interval.
     */
    public double getMajorXInterval() {
        return majorXInterval.get();
    }

    public final void setMajorXInterval(double majorX) {
        if (majorX <= 0.0) {
            majorX = 1e-15;
        }
        majorXInterval.userSpecifiedValue = majorX;
    }

    public MajorYInterval majorYInterval() {
        return majorYInterval;
    }

    /**
     * The interval between major tick marks and grid lines in chart coordinate
     * space on the y-axis. This is calculated automatically unless the user
     * enforces a specified value.
     *
     * @return the interval.
     */
    public final double getMajorYInterval() {
        return majorYInterval.get();
    }

    public final void setMajorYInterval(double majorY) {
        if (majorY <= 0.0) {
            majorY = 1e-15;
        }
        majorYInterval.userSpecifiedValue = majorY;
    }

    /**
     * The interval between minor tick marks and grid lines in chart coordinate
     * space on the x-axis.
     *
     * This is calculated automatically unless the user enforces a specified
     * value.
     *
     * Note: this is a hint and may be ignored depending on the transform
     * applied to the axis data.
     *
     * @return the interval.
     */
    public final int getMinorCountXHint() {
        return minorCountXHint;
    }

    public final void setMinorCountXHint(int minorCountX) {
        if (minorCountX < 0) {
            return;
        }
        this.minorCountXHint = minorCountX;
    }

    /**
     * The interval between minor tick marks and grid lines in chart coordinate
     * space on the x-axis.
     *
     * This is calculated automatically unless the user enforces a specified
     * value.
     *
     * Note: this is a hint and may be ignored depending on the transform
     * applied to the axis data.
     *
     * @return the interval.
     */
    public final int getMinorCountYHint() {
        return minorCountYHint;
    }

    public final void setMinorCountYHint(int minorCountY) {
        if (minorCountY < 0) {
            return;
        }
        this.minorCountYHint = minorCountY;
    }

    /**
     * The minimum limit of the present x-axis range.
     *
     * @return the limit
     */
    public final double getXMin() {
        return Math.min(getXLeft(), getXRight());
    }

    /**
     * The maximum limit of the present x-axis range.
     *
     * @return the limit
     */
    public final double getXMax() {
        return Math.max(getXLeft(), getXRight());
    }

    /**
     * The minimum limit of the present y-axis range.
     *
     * @return the limit
     */
    public final double getYMin() {
        return Math.min(getYTop(), getYBottom());
    }

    /**
     * The maximum limit of the present y-axis range.
     *
     * @return the limit
     */
    public final double getYMax() {
        return Math.max(getYTop(), getYBottom());
    }

    public final DoubleProperty xOrigin() {
        return xOrigin;
    }

    public final double getXOrigin() {
        return xOrigin.doubleValue();
    }

    public final void setXOrigin(double val) {
        xOrigin.set(val);
    }

    public final DoubleProperty yOrigin() {
        return yOrigin;
    }

    public final double getYOrigin() {
        return yOrigin.doubleValue();
    }

    public final void setYOrigin(double val) {
        yOrigin.set(val);
    }

    public final DoubleProperty innerAxisStrokeWidthProperty() {
        return innerAxisStrokeWidth;
    }

    public final double getInnerAxisStrokeWidth() {
        return innerAxisStrokeWidth.doubleValue();
    }

    public final void setInnerAxisStrokeWidth(double val) {
        innerAxisStrokeWidth.set(val);
    }

    public final DoubleProperty axisStrokeWidthProperty() {
        return axisStrokeWidth;
    }

    public final double getAxisStrokeWidth() {
        return axisStrokeWidth.doubleValue();
    }

    public final void setAxisStrokeWidth(double val) {
        axisStrokeWidth.set(val);
    }

    public final double minorGridStrokeWidth() {
        return minorGridStrokeWidth.doubleValue();
    }

    public final double getMinorGridStrokeWidth() {
        return minorGridStrokeWidth.doubleValue();
    }

    public final void setMinorGridStrokeWidth(double val) {
        minorGridStrokeWidth.set(val);
    }

    public final DoubleProperty majorGridStrokeWidth() {
        return majorGridStrokeWidth;
    }

    public final double getMajorGridStrokeWidth() {
        return majorGridStrokeWidth.doubleValue();
    }

    public final void setMajorGridStrokeWidth(double val) {
        majorGridStrokeWidth.set(val);
    }

    public final BooleanProperty minorGridPainted() {
        return minorGridPainted;
    }

    public final boolean isMinorGridPainted() {
        return minorGridPainted.get();
    }

    /**
     * Set true to paint the minor grid
     *
     * @param val
     */
    public final void setMinorGridPainted(boolean val) {
        minorGridPainted.set(val);
    }

    public final BooleanProperty majorGridPainted() {
        return majorGridPainted;
    }

    public final boolean isMajorGridPainted() {
        return majorGridPainted.get();
    }

    public final void setMajorGridPainted(boolean val) {
        majorGridPainted.set(val);
    }

    public final BooleanProperty innerAxisPainted() {
        return innerAxisPainted;
    }

    public final boolean isInnerAxisPainted() {
        return innerAxisPainted.get();
    }

    public final void setInnerAxisPainted(boolean val) {
        innerAxisPainted.set(val);
    }

    public final BooleanProperty innerAxisLabelled() {
        return innerAxisLabelled;
    }

    public final boolean isInnerAxisLabelled() {
        return innerAxisLabelled.get();
    }

    public final void setInnerAxisLabelled(boolean val) {
        innerAxisLabelled.set(val);
    }

    public final boolean leftAxisPainted() {
        return leftAxisPainted.get();
    }

    public final boolean isLeftAxisPainted() {
        return leftAxisPainted.get();
    }

    public final void setLeftAxisPainted(boolean val) {
        leftAxisPainted.set(val);
    }

    public final BooleanProperty rightAxisPainted() {
        return rightAxisPainted;
    }

    public final boolean isRightAxisPainted() {
        return rightAxisPainted.get();
    }

    public final void setRightAxisPainted(boolean val) {
        rightAxisPainted.set(val);
    }

    public final BooleanProperty topAxisPainted() {
        return topAxisPainted;
    }

    public final boolean isTopAxisPainted() {
        return topAxisPainted.get();
    }

    public final void setTopAxisPainted(boolean val) {
        topAxisPainted.set(val);
    }

    public final BooleanProperty bottomAxisPainted() {
        return bottomAxisPainted;
    }

    public final boolean isBottomAxisPainted() {
        return bottomAxisPainted.get();
    }

    public final void setBottomAxisPainted(boolean val) {
        bottomAxisPainted.set(val);
    }

    public final BooleanProperty leftAxisLabelled() {
        return leftAxisLabelled;
    }

    public final boolean isLeftAxisLabelled() {
        return leftAxisLabelled.get();
    }

    public final void setLeftAxisLabelled(boolean val) {
        leftAxisLabelled.set(val);
    }

    public final boolean rightAxisLabelled() {
        return rightAxisLabelled.get();
    }

    public final boolean isRightAxisLabelled() {
        return rightAxisLabelled.get();
    }

    public final void setRightAxisLabelled(boolean val) {
        rightAxisLabelled.set(val);
    }

    public final BooleanProperty topAxisLabelled() {
        return topAxisLabeled;
    }

    public final boolean isTopAxisLabelled() {
        return topAxisLabeled.get();
    }

    public final void setTopAxisLabelled(boolean val) {
        topAxisLabeled.set(val);
    }

    public final BooleanProperty bottomAxisLabelled() {
        return bottomAxisLabelled;
    }

    public final boolean isBottomAxisLabelled() {
        return bottomAxisLabelled.get();
    }

    public final void setBottomAxisLabelled(boolean val) {
        bottomAxisLabelled.set(val);
    }

    public String getLeftAxisTitle() {
        return axisLeft.axisLabel.getText();
    }

    public void setLeftAxisTitle(String s) {
        axisLeft.axisLabel.setText(s);
    }

    public String getBottomAxisTitle() {
        return axisBottom.axisLabel.getText();
    }

    public void setBottomAxisTitle(String s) {
        axisBottom.axisLabel.setText(s);
    }

    public String getRightAxisTitle() {
        return axisRight.axisLabel.getText();
    }

    public void setRightAxisTitle(String s) {
        axisRight.axisLabel.setText(s);
    }

    public String getTopAxisTitle() {
        return axisTop.axisLabel.getText();
    }

    public void setTopAxisTitle(String s) {
        axisTop.axisLabel.setText(s);
    }

    /**
     * Establishes a bi-directional binding between the x-axis limits of this
     * chart and x-axis limits of the specified chart.
     *
     * @param chart
     */
    public void setAxisLinkXX(Chart chart) {
        xLeft.bindBidirectional(chart.xLeft);
        xRight.bindBidirectional(chart.xRight);
    }

    /**
     * Establishes a bi-directional binding between the x-axis limits of this
     * chart and y-axis limits of the specified chart.
     *
     * @param chart
     */
    public void setAxisLinkXY(Chart chart) {
        xLeft.bindBidirectional(chart.yBottom);
        xRight.bindBidirectional(chart.yTop);
    }

    /**
     * Establishes a bi-directional binding between the y-axis limits of this
     * chart and y-axis limits of the specified chart.
     *
     * @param chart
     */
    public void setAxisLinkYY(Chart chart) {
        yBottom.bindBidirectional(chart.yBottom);
        yTop.bindBidirectional(chart.yTop);
    }

    /**
     * Establishes a bi-directional binding between the y-axis limits of this
     * chart and x-axis limits of the specified chart.
     *
     * @param chart
     */
    public void setAxisLinkYX(Chart chart) {
        yBottom.bindBidirectional(chart.xLeft);
        yTop.bindBidirectional(chart.xRight);
    }

    public void removeAxisLinkXX(Chart chart) {
        xLeft.unbindBidirectional(chart.xLeft);
        xRight.unbindBidirectional(chart.xRight);
    }

    public void removeAxisLinkXY(Chart chart) {
        xLeft.unbindBidirectional(chart.yBottom);
        xRight.unbindBidirectional(chart.yTop);
    }

    public void removeAxisLinkYY(Chart chart) {
        yBottom.unbindBidirectional(chart.yBottom);
        yTop.unbindBidirectional(chart.yTop);
    }

    public void removeAxisLinkYX(Chart chart) {
        yBottom.unbindBidirectional(chart.xLeft);
        yTop.unbindBidirectional(chart.xRight);
    }

    public final BooleanProperty polar() {
        return polar;
    }

    public final boolean isPolar() {
        return polar.get();
    }

    public final void setPolar(boolean val) {
        polar.set(val);
    }

    public final ObjectProperty<Paint> majorGridColor() {
        return majorGridColor;
    }

    public final Paint getMajorGridColor() {
        return majorGridColor.get();
    }

    public final void setMajorGridColor(Paint val) {
        majorGridColor.set(val);
    }

    public final ObjectProperty<Paint> minorGridColor() {
        return minorGridColor;
    }

    public final Paint getMinorGridColor() {
        return minorGridColor.get();
    }

    public final void setMinorGridColor(Paint val) {
        minorGridColor.set(val);
    }

    public final ObjectProperty<Paint> axisColor() {
        return axisColor;
    }

    public final Paint getAxisColor() {
        return axisColor.get();
    }

    public final void setAxisColor(Paint val) {
        axisColor.set(val);
    }

    public ObjectProperty<Paint> altFillVerticalProperty() {
        return altFillVertical;
    }

    /**
     * @return the alternateBackground
     */
    public Paint getAltFillVertical() {
        return altFillVertical.get();
    }

    /**
     * @param alternateBackground the alternateBackground to getParent
     */
    public void setAltFillVertical(Paint alternateBackground) {
        altFillVertical.set(alternateBackground);
    }

    /**
     * Property wrapping the Paint instance to use when filling alternate
     * horizontal major grid divisions.
     *
     * @return the property.
     */
    public ObjectProperty<Paint> altFillHorizontalProperty() {
        return altFillHorizontal;
    }

    /**
     * Returns the Paint instance to used filling alternate horizontal major
     * grid divisions.
     *
     * @return the Paint instance.
     */
    public Paint getAltFillHorizontal() {
        return altFillHorizontal.get();
    }

    /**
     * Sets the Paint instance to used filling alternate horizontal major grid
     * divisions.
     *
     * @param alternateBackground the Paint instance
     */
    public void setAltFillHorizontal(Paint alternateBackground) {
        altFillHorizontal.set(alternateBackground);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     * @since JavaFX 8.0
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    /**
     * @return the axisSet
     */
    public final AxisSet getAxisSet() {
        return axisSet;
    }

    //    /**
    private boolean atOrigin(double x, double y) {
        return onXAxis(y) & onYAxis(x);
    }

    private boolean onXAxis(double y) {
        return (Math.abs(yOrigin.doubleValue() - y) < xTol.get());
    }

    private boolean onYAxis(double x) {
        return (Math.abs(xOrigin.doubleValue() - x) < yTol.get());
    }

    private boolean onAxis(double x, double y) {
        return onXAxis(y) || onYAxis(x);
    }

    public final void requestPaint() {
        requestLayout();
        axisSet.paintGrid(canvas.getGraphicsContext2D());
    }

    public final double toPositionX(double pixel) {
        return getXLeft() + pixel * (getXRight() - getXLeft()) / view.getWidth();
    }

    public final double toPositionY(double pixel) {
        return getYBottom() + (view.getHeight() - pixel) * (getYTop() - getYBottom()) / view.getHeight();
    }

    public double toPixelX(double x) {
        return (x - getXLeft()) * getView().getWidth() / (getXRight() - getXLeft());
    }

    public double toPixelY(double y) {
        return view.getHeight() - ((y - getYBottom()) * view.getHeight() / (getYTop() - getYBottom()));
    }

    /**
     * Returns the pixel location local to the layer that corresponds to a
     * location in the graph's coordinate space.
     *
     * @param x x value in graph coordinates
     * @param y y value in graph coordinates
     * @return a Point2D instance with the pixel values for the point
     */
    public Point2D toPixel(double x, double y) {
        return new Point2D(toPixelX(x), toPixelY(y));
    }

    /**
     * Returns the pixel location local to the layer that corresponds to a
     * location in the graph's coordinate space.
     *
     * @param p a Point2D instance with the graph x and y coordinates in graph
     * space
     * @return a Point2D instance with the pixel values for the point
     */
    public Point2D toPixel(Point2D p) {
        return new Point2D(toPixelX(p.getX()), toPixelY(p.getY()));
    }

    public double getPixelWidth() {
        return (getXMax() - getXMin()) / view.getWidth();
    }

    public double getPixelHeight() {
        return (getYMax() - getYMin()) / view.getHeight();
    }

    public ArrayList<Point2D> toPixel(Collection<Point2D> p) {
        ArrayList<Point2D> val = new ArrayList<>();
        p.stream().forEach((Point2D x) -> {
            val.add(toPixel(x));
        });
        return val;
    }

    public Point2D pixelToPos(Point2D p) {
        return toPixel(axisSet.getInverse(p.getX(), p.getY()));

    }

    /**
     * @return the axisFontSize
     */
    public double getAxisFontSize() {
        return axisFontSize.get();
    }

    ;

    /**
     * @param value the axisFontSize to set
     */
    public void setAxisFontSize(double value) {
        this.axisFontSize.set(value);
    }

    public DoubleProperty axisFontSizeProperty() {
        return axisFontSize;

    }

    public static enum TRANSFORMTYPE {

        LINEAR,
        LOG,
        LOG10, LOG2
    }

    /**
     * Sets the alignment of the view in the graph when the viewAspectRatio is
     * set to a non-zero value.
     * <ul>
     * <li>{@code CENTER} position the view in the center of the graph- </li>
     * <li>{@code LEFT_TOP} position the view in the left or top of the graph's
     * longest axis- </li>
     * <li>{@code RIGHT_BOTTOM} position the view in the right or bottom of the
     * graph's longest axis- </li>
     * </ul>
     */
    public enum VIEWALIGNMENT {

        CENTER, LEFT_TOP, RIGHT_BOTTOM
    }

// -------------- STYLESHEET HANDLING ------------------------------------------------------------------------------
    private enum AXISPOSITION {

        LEFT, TOP, RIGHT, BOTTOM
    }

//    private static final class Tooltips {
//
//        private final static String axisTooltip = "Double click to edit axis settings";
//        private final static String axisLineTooltip = "Click and drag to change axis limits";
//    }
    /**
     * @treatAsPrivate implementation detail
     */
    private static class StyleableProperties {

        static final Class<? extends Enum> clzz = TRANSFORMTYPE.class;
        static final Class<? extends Enum> clzz0 = VIEWALIGNMENT.class;
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private static final CssMetaData<Chart, Number> XLEFT
                = new CssMetaData<Chart, Number>("-w-xleft",
                        StyleConverter.getSizeConverter(), -10) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.xLeft != null && !n.xLeft.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.xLeft;
                    }
                };
        private static final CssMetaData<Chart, Number> XRIGHT
                = new CssMetaData<Chart, Number>("-w-xright",
                        StyleConverter.getSizeConverter(), 1) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.xRight != null && !n.xRight.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.xRight;
                    }
                };
        private static final CssMetaData<Chart, Number> YBOTTOM
                = new CssMetaData<Chart, Number>("-w-ybottom",
                        StyleConverter.getSizeConverter(), 1) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.yBottom != null && !n.yBottom.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.yBottom;
                    }
                };
        private static final CssMetaData<Chart, Number> YTOP
                = new CssMetaData<Chart, Number>("-w-ytop",
                        StyleConverter.getSizeConverter(), 1) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.yTop != null && !n.yTop.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.yTop;
                    }
                };
        private static final CssMetaData<Chart, Number> XORIGIN
                = new CssMetaData<Chart, Number>("-w-xorigin",
                        StyleConverter.getSizeConverter(), 0d) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.xOrigin != null && !n.xOrigin.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.xOrigin;
                    }
                };
        private static final CssMetaData<Chart, Number> YORIGIN
                = new CssMetaData<Chart, Number>("-w-yorigin",
                        StyleConverter.getSizeConverter(), 0d) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.yOrigin != null && !n.yOrigin.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.yOrigin;
                    }
                };
        private static final CssMetaData<Chart, Number> AXISSTROKEWIDTH
                = new CssMetaData<Chart, Number>("-w-axis-stroke-width",
                        StyleConverter.getSizeConverter(), 1d) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.axisStrokeWidth != null && !n.axisStrokeWidth.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.axisStrokeWidth;
                    }
                };
        private static final CssMetaData<Chart, Number> INNERAXISSTROKEWIDTH
                = new CssMetaData<Chart, Number>("-w-inner-axis-stroke-width",
                        StyleConverter.getSizeConverter(), 1.5d) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.innerAxisStrokeWidth != null && !n.innerAxisStrokeWidth.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.innerAxisStrokeWidth;
                    }
                };
        private static final CssMetaData<Chart, Number> MINORGRIDSTROKEWIDTH
                = new CssMetaData<Chart, Number>("-w-minor-grid-stroke-width",
                        StyleConverter.getSizeConverter(), 1.1) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.yTop != null && !n.yTop.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.yTop;
                    }
                };
        private static final CssMetaData<Chart, Number> MAJORGRIDSTROKEWIDTH
                = new CssMetaData<Chart, Number>("-w-major-grid-stroke-width",
                        StyleConverter.getSizeConverter(), 1.3) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.yTop != null && !n.yTop.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.yTop;
                    }
                };
        private static final CssMetaData<Chart, Boolean> MINORGRIDPAINTED
                = new CssMetaData<Chart, Boolean>("-w-minor-grid-painted",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.minorGridPainted != null && !n.minorGridPainted.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.minorGridPainted;
                    }
                };
        private static final CssMetaData<Chart, Boolean> MAJORGRIDPAINTED
                = new CssMetaData<Chart, Boolean>("-w-major-grid-painted",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.majorGridPainted != null && !n.majorGridPainted.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.majorGridPainted;
                    }
                };
        private static final CssMetaData<Chart, Boolean> INNERAXISPAINTED
                = new CssMetaData<Chart, Boolean>("-w-inner-axis-painted",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.innerAxisPainted != null && !n.innerAxisPainted.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.innerAxisPainted;
                    }
                };
        private static final CssMetaData<Chart, Number> INNERAXISFONTSIZE
                = new CssMetaData<Chart, Number>("-w-inner-axis-font-size",
                        StyleConverter.getSizeConverter(), Font.getDefault().getSize()) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.innerAxisFontSize != null && !n.innerAxisFontSize.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.innerAxisFontSize;
                    }
                };
        private static final CssMetaData<Chart, Boolean> INNERAXISLABELLED
                = new CssMetaData<Chart, Boolean>("-w-inner-axis-labelled",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.innerAxisLabelled != null && !n.innerAxisLabelled.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.innerAxisLabelled;
                    }
                };
        private static final CssMetaData<Chart, Boolean> LEFTAXISPAINTED
                = new CssMetaData<Chart, Boolean>("-w-left-axis-painted",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.leftAxisPainted != null && !n.leftAxisPainted.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.leftAxisPainted;
                    }
                };
        private static final CssMetaData<Chart, Boolean> RIGHTAXISPAINTED
                = new CssMetaData<Chart, Boolean>("-w-right-axis-painted",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.rightAxisPainted != null && !n.rightAxisPainted.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.rightAxisPainted;
                    }
                };
        private static final CssMetaData<Chart, Boolean> TOPAXISPAINTED
                = new CssMetaData<Chart, Boolean>("-w-top-axis-painted",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.topAxisPainted != null && !n.topAxisPainted.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.topAxisPainted;
                    }
                };
        private static final CssMetaData<Chart, Boolean> BOTTOMAXISPAINTED
                = new CssMetaData<Chart, Boolean>("-w-bottom-axis-painted",
                        StyleConverter.getBooleanConverter(), Boolean.TRUE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.bottomAxisPainted != null && !n.bottomAxisPainted.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.bottomAxisPainted;
                    }
                };
        private static final CssMetaData<Chart, Boolean> LEFTAXISLABELLED
                = new CssMetaData<Chart, Boolean>("-w-left-axis-labelled",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.leftAxisLabelled != null && !n.leftAxisLabelled.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.leftAxisLabelled;
                    }
                };
        private static final CssMetaData<Chart, Boolean> BOTTOMAXISLABELLED
                = new CssMetaData<Chart, Boolean>("-w-bottom-axis-labelled",
                        StyleConverter.getBooleanConverter(), Boolean.TRUE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.bottomAxisLabelled != null && !n.bottomAxisLabelled.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.bottomAxisLabelled;
                    }
                };
        private static final CssMetaData<Chart, Boolean> TOPAXISLABELLED
                = new CssMetaData<Chart, Boolean>("-w-top-axis-labelled",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.topAxisLabeled != null && !n.topAxisLabeled.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.topAxisLabeled;
                    }
                };
        private static final CssMetaData<Chart, Boolean> RIGHTAXISLABELLED
                = new CssMetaData<Chart, Boolean>("-w-right-axis-labelled",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.rightAxisLabelled != null && !n.rightAxisLabelled.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.rightAxisLabelled;
                    }
                };
        private static final CssMetaData<Chart, Boolean> POLAR
                = new CssMetaData<Chart, Boolean>("-w-polar",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.polar != null && !n.polar.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.polar;
                    }
                };
        private static final CssMetaData<Chart, Paint> MAJORGRIDCOLOR
                = new CssMetaData<Chart, Paint>("-w-major-grid-color",
                        StyleConverter.getPaintConverter(), Color.SLATEBLUE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.majorGridColor != null && !n.majorGridColor.isBound();
                    }

                    @Override
                    public StyleableProperty<Paint> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Paint>) n.majorGridColor;
                    }
                };
        private static final CssMetaData<Chart, Paint> MINORGRIDCOLOR
                = new CssMetaData<Chart, Paint>("-w-minor-grid-color",
                        StyleConverter.getPaintConverter(), Color.SLATEBLUE) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.minorGridColor != null && !n.minorGridColor.isBound();
                    }

                    @Override
                    public StyleableProperty<Paint> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Paint>) n.minorGridColor;
                    }
                };
        private static final CssMetaData<Chart, Font> FONT
                = new FontCssMetaData<Chart>("-w-font", Font.getDefault()) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.fontProperty != null && !n.fontProperty.isBound();
                    }

                    @Override
                    public StyleableProperty<Font> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Font>) n.fontProperty;
                    }
                };
        private static final CssMetaData<Chart, Paint> AXISCOLOR
                = new CssMetaData<Chart, Paint>("-w-axis-color",
                        StyleConverter.getPaintConverter(), Color.BLACK) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.axisColor != null && !n.axisColor.isBound();
                    }

                    @Override
                    public StyleableProperty<Paint> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Paint>) n.axisColor;
                    }
                };
        private static final CssMetaData<Chart, Paint> INNERAXISCOLOR
                = new CssMetaData<Chart, Paint>("-w-inner-axis-color",
                        StyleConverter.getPaintConverter(), Color.BLACK) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.innerAxisColor != null && !n.innerAxisColor.isBound();
                    }

                    @Override
                    public StyleableProperty<Paint> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Paint>) n.innerAxisColor;
                    }
                };
        private static final CssMetaData<Chart, Paint> ALTFILLVERTICAL
                = new CssMetaData<Chart, Paint>("-w-alt-fill-vertical",
                        StyleConverter.getPaintConverter(), Color.TRANSPARENT) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.altFillVertical != null && !n.altFillVertical.isBound();
                    }

                    @Override
                    public StyleableProperty<Paint> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Paint>) n.altFillVertical;
                    }
                };
        private static final CssMetaData<Chart, Paint> ALTFILLHORIZONTAL
                = new CssMetaData<Chart, Paint>("-w-alt-fill-horizontal",
                        StyleConverter.getPaintConverter(), Color.TRANSPARENT) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.altFillHorizontal != null && !n.altFillHorizontal.isBound();
                    }

                    @Override
                    public StyleableProperty<Paint> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Paint>) n.altFillHorizontal;
                    }
                };
        private static final CssMetaData<Chart, Number> ASPECTRATIO
                = new CssMetaData<Chart, Number>("-w-view-aspectratio",
                        StyleConverter.getSizeConverter(), Double.NaN) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return n.viewAspectRatio != null && !n.viewAspectRatio.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.viewAspectRatio;
                    }
                };
        @SuppressWarnings("unchecked")
        private static final CssMetaData<Chart, TRANSFORMTYPE> XTRANSFORMTYPE
                = new CssMetaData<Chart, TRANSFORMTYPE>("-w-xtransformtype",
                        StyleConverter.getEnumConverter(clzz), TRANSFORMTYPE.LINEAR) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<TRANSFORMTYPE> getStyleableProperty(Chart n) {
                        return (StyleableProperty<TRANSFORMTYPE>) n.xTransformType;
                    }
                };
        @SuppressWarnings("unchecked")
        private static final CssMetaData<Chart, TRANSFORMTYPE> YTRANSFORMTYPE
                = new CssMetaData<Chart, TRANSFORMTYPE>("-w-ytransformtype",
                        StyleConverter.getEnumConverter(clzz), TRANSFORMTYPE.LINEAR) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<TRANSFORMTYPE> getStyleableProperty(Chart n) {
                        return (StyleableProperty<TRANSFORMTYPE>) n.yTransformType;
                    }
                };
        @SuppressWarnings("unchecked")
        private static final CssMetaData<Chart, VIEWALIGNMENT> VIEWALIGN
                = new CssMetaData<Chart, VIEWALIGNMENT>("-w-view-alignment",
                        StyleConverter.getEnumConverter(clzz0), VIEWALIGNMENT.CENTER) {

                    @Override
                    public boolean isSettable(Chart n) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<VIEWALIGNMENT> getStyleableProperty(Chart n) {
                        return (StyleableProperty<VIEWALIGNMENT>) n.viewAlignment;
                    }
                };

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables
                    = new ArrayList<>(Pane.getClassCssMetaData());

            styleables.add(FONT);
            styleables.add(ASPECTRATIO);
            styleables.add(POLAR);
            styleables.add(XTRANSFORMTYPE);
            styleables.add(YTRANSFORMTYPE);

            styleables.add(XLEFT);
            styleables.add(XRIGHT);
            styleables.add(YBOTTOM);
            styleables.add(YTOP);
            styleables.add(XORIGIN);
            styleables.add(YORIGIN);

            styleables.add(AXISCOLOR);
            styleables.add(AXISSTROKEWIDTH);

            styleables.add(MINORGRIDSTROKEWIDTH);
            styleables.add(MAJORGRIDSTROKEWIDTH);
            styleables.add(MINORGRIDPAINTED);
            styleables.add(MAJORGRIDPAINTED);
            styleables.add(MAJORGRIDCOLOR);
            styleables.add(MINORGRIDCOLOR);

            styleables.add(INNERAXISCOLOR);
            styleables.add(INNERAXISPAINTED);
            styleables.add(INNERAXISLABELLED);
            styleables.add(INNERAXISFONTSIZE);
            styleables.add(INNERAXISSTROKEWIDTH);

            styleables.add(LEFTAXISPAINTED);
            styleables.add(RIGHTAXISPAINTED);
            styleables.add(TOPAXISPAINTED);
            styleables.add(BOTTOMAXISPAINTED);

            styleables.add(LEFTAXISLABELLED);
            styleables.add(BOTTOMAXISLABELLED);
            styleables.add(RIGHTAXISLABELLED);
            styleables.add(TOPAXISLABELLED);

            styleables.add(ALTFILLVERTICAL);
            styleables.add(ALTFILLHORIZONTAL);

            //styleables.add(ALTFILLHORIZONTALFLAG);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public class TickLabel extends Text {

        public double xpos;
        public double ypos;

        public TickLabel(String string) {
            super(string);
            this.setTextOrigin(VPos.CENTER);
        }
    }

    /**
     * {@code AbstractAxisRegion} provides the base class for axes drawn outside
     * of the view area.
     * <p/>
     * These extend {@code javafx.scene.layout.Region} and use standard JavaFX
     * components rather than painting to the view.
     * <p/>
     * Separate subclasses are used for the axes to the left and right and at
     * the top and bottom of the view area.
     *
     * @author Malcolm Lidierth
     */
    private class AbstractAxisRegion extends Region {

        public final LineClass line;
        protected final Chart layer;
        /**
         * Text instance for the axis label
         */
        protected final Text axisLabel = new Text("Axis Label");
        /**
         * A {@code LinkedHashMap} containing Integer keys and String values.
         * When an axis tick position coincides with the Integer key, the
         * corresponding String value will be used for the tick label.
         */
        protected final LinkedHashMap<Integer, String> categories = new LinkedHashMap<>();
        /**
         * Boolean indicating whether to use categorical labels for the tick
         * marks
         */
        protected BooleanProperty categorical = new SimpleBooleanProperty(Boolean.FALSE);
        /**
         * Internally generated list of labels corresponding to the numerical
         * position of each tick.
         */
        //private final ArrayList<TickLabel> tickLabels = new ArrayList<>();

        private double dragXStart = Double.NaN;
        private double dragYStart = Double.NaN;
        private double deltaX;
        private double deltaY;

        public AbstractAxisRegion(Chart layer) {
            this.layer = layer;
            getStyleClass().add("axis");
            //setBackground(Background.EMPTY);
            //layer.getChildren().add(this);
            line = new LineClass(layer);

            axisLabel.fillProperty().bind(layer.axisColor());

//            if (Chart.toolTipFlag && Platform.isFxApplicationThread()) {
//                Tooltip.install(this, new Tooltip(Tooltips.axisTooltip));
//            }
            ChangeListener<Scene> addedToScene = (ObservableValue<? extends Scene> ov, Scene t, Scene t1) -> {
                setOnMousePressed((MouseEvent m0) -> {
                    switch (getAxisPosition()) {
                        case TOP:
                        case BOTTOM:
                            setCursor(Cursor.E_RESIZE);
                            break;
                        default:
                            setCursor(Cursor.N_RESIZE);
                    }
                    dragXStart = m0.getX();
                    dragYStart = m0.getY();

                });
                setOnMouseReleased((MouseEvent m1) -> {
                    setCursor(Cursor.DEFAULT);
                });
                setOnMouseDragged((MouseEvent m2) -> {
                    switch (getAxisPosition()) {
                        case TOP:
                        case BOTTOM:
                            deltaX = dragXStart - m2.getX();
                            if (deltaX != 0) {
                                deltaX = deltaX * this.layer.getPixelWidth();
                                if (layer.toPositionX(m2.getX()) >= layer.getXOrigin()) {
                                    this.layer.setXRight(this.layer.getXRight() + deltaX);
                                } else {
                                    this.layer.setXLeft(this.layer.getXLeft() + deltaX);
                                }
                            }
                            break;
                        case LEFT:
                        case RIGHT:
                            deltaY = dragYStart - m2.getY();
                            if (deltaY != 0) {
                                deltaY = deltaY * this.layer.getPixelHeight();
                                if (layer.toPositionY(m2.getY()) >= layer.getYOrigin()) {
                                    this.layer.setYTop(this.layer.getYTop() - deltaY);
                                } else {
                                    this.layer.setYBottom(this.layer.getYBottom() - deltaY);
                                }
                            }
                    }
                    dragXStart = m2.getX();
                    dragYStart = m2.getY();
                    this.layer.requestLayout();
                });
            };
            sceneProperty().addListener(addedToScene);
        }

        public final Chart getLayer() {
            return layer;
        }

        /**
         * @return the categorical
         */
        public boolean isCategorical() {
            return categorical.get();
        }

        public void setCategorial(boolean categorical) {
            this.categorical.set(categorical);
        }

        public BooleanProperty categoricalProperty() {
            return categorical;
        }

        public Font getFont() {
            return Font.font(layer.fontProperty().get().getFamily(),
                    FontPosture.valueOf(layer.fontProperty().get().getStyle().toUpperCase()),
                    layer.getAxisFontSize());
        }

        /**
         * @return the categories
         */
        public LinkedHashMap<Integer, String> getCategories() {
            return categories;
        }

        /**
         * @return the tickLabels
         */
        public ArrayList<TickLabel> getTickLabels() {
            ArrayList<TickLabel> arr = new ArrayList<>();
            getChildren().filtered(x -> x instanceof TickLabel).forEach((x) -> {
                arr.add((TickLabel) x);
            });
            return arr;
        }

        protected double calcTickLabelWidth() {
            double w = 0d;
            for (TickLabel t : getTickLabels()) {
                w = Math.max(w, t.getText().length());
            }
            return w;
        }

        /**
         * @return the axisPosition
         */
        public final AXISPOSITION getAxisPosition() {
            if (this instanceof AxisRight) {
                return AXISPOSITION.RIGHT;
            } else if (this instanceof AxisTop) {
                return AXISPOSITION.TOP;
            } else if (this instanceof AxisLeft) {
                return AXISPOSITION.LEFT;
            } else {
                return AXISPOSITION.BOTTOM;
            }
        }

        void removeAxisLabel() {
            if (getChildren().contains(axisLabel)) {
                getChildren().remove(axisLabel);
            }
        }

        void addAxisLabel() {
            if (!getChildren().contains(axisLabel)) {
                getChildren().add(axisLabel);
            }
        }

        /**
         * Line used to represent the axis and tickmarks
         */
        public final class LineClass extends ObjectBinding<Polyline> {

            private final Chart layer;

            private final Polyline value = new Polyline();

            public LineClass(Chart layer) {
                super();
                this.layer = layer;

                value.strokeProperty().bind(layer.axisColor);
                value.strokeWidthProperty().bind(layer.axisStrokeWidth);

                bind();
//                if (Chart.toolTipFlag && Platform.isFxApplicationThread()) {
//                    Tooltip.install(value, new Tooltip(Tooltips.axisLineTooltip));
//                }

                ChangeListener<Scene> addedToScene = (ObservableValue<? extends Scene> ov, Scene t, Scene t1) -> {
                    value.setOnMouseEntered((MouseEvent m0) -> {
                        setCursor(Cursor.E_RESIZE);
                    });
                    value.setOnMouseExited((MouseEvent m1) -> {
                        setCursor(Cursor.DEFAULT);
                    });
                };
                sceneProperty().addListener(addedToScene);
            }

            public void bind() {
                bind(layer.widthProperty());
                bind(layer.heightProperty());
                switch (getAxisPosition()) {
                    case TOP:
                    case BOTTOM:
                        bind(layer.xLeftProperty());
                        bind(layer.xRightProperty());
                        bind(prefWidthProperty());
                        break;
                    case LEFT:
                    case RIGHT:
                        bind(layer.yTopProperty());
                        bind(layer.yBottomProperty());
                        bind(prefHeightProperty());
                }
            }

            @Override
            public Polyline computeValue() {
                if (getAxisSet() != null) {
                    value.getPoints().clear();

                    switch (getAxisPosition()) {
                        case TOP:
                            if (layer.isTopAxisPainted()) {
                                double h = layer.axisTop.computePrefHeight(-1d);
                                value.getPoints().addAll(new Double[]{0d, h});
                                value.getPoints().addAll(new Double[]{getWidth(), h});
                                layer.getAxisSet().getXTransform().get().stream().filter((Double x) -> x >= layer.getXMin() && x <= layer.getXMax()).forEach((Double x) -> {
                                    Point2D p1 = layer.toPixel(x, 0);
                                    value.getPoints().addAll(new Double[]{p1.getX(), h});
                                    value.getPoints().addAll(new Double[]{p1.getX(), h - layer.yTopTickLength});
                                    value.getPoints().addAll(new Double[]{p1.getX(), h});
                                });
                                layer.getAxisSet().getXTransform().getMinorTicks().stream().filter((Double x) -> x >= layer.getXMin() && x <= layer.getXMax()).forEach((Double x) -> {
                                    Point2D p1 = layer.toPixel(x, 0);
                                    value.getPoints().addAll(new Double[]{p1.getX(), h});
                                    value.getPoints().addAll(new Double[]{p1.getX(), h - layer.yTopTickLength * 0.7});
                                    value.getPoints().addAll(new Double[]{p1.getX(), h});
                                });
                            }
                            break;
                        case BOTTOM:
                            if (layer.getLayers().size() == 1) {

                            }
                            if (layer.isBottomAxisPainted()) {
                                value.getPoints().addAll(new Double[]{0d, 0d});
                                value.getPoints().addAll(new Double[]{getWidth(), 0d});
                                layer.getAxisSet().getXTransform().get().stream().filter((Double x) -> x >= layer.getXMin() && x <= layer.getXMax()).forEach((Double x) -> {
                                    Point2D p1 = layer.toPixel(x, 0d);
                                    value.getPoints().addAll(new Double[]{p1.getX(), 0d});
                                    value.getPoints().addAll(new Double[]{p1.getX(), layer.yBottomTickLength});
                                    value.getPoints().addAll(new Double[]{p1.getX(), 0d});
                                });
                                layer.getAxisSet().getXTransform().getMinorTicks().stream().filter((Double x) -> x >= layer.getXMin() && x <= layer.getXMax()).forEach((Double x) -> {
                                    Point2D p1 = layer.toPixel(x, 0d);
                                    value.getPoints().addAll(new Double[]{p1.getX(), 0d});
                                    value.getPoints().addAll(new Double[]{p1.getX(), layer.yBottomTickLength * 0.7});
                                    value.getPoints().addAll(new Double[]{p1.getX(), 0d});
                                });
                            }
                            break;
                        case LEFT:
                            if (layer.getLayers().size() == 1) {

                            }
                            if (layer.isLeftAxisPainted()) {
                                Point2D p0 = layer.toPixel(0, layer.getYBottom());
                                double w = computePrefWidth(-1d);
                                value.getPoints().addAll(new Double[]{w, p0.getY()});
                                p0 = layer.toPixel(0, layer.getYTop());
                                value.getPoints().addAll(new Double[]{w, p0.getY()});
                                layer.getAxisSet().getYTransform().get().stream().filter((Double y) -> y >= layer.getYMin() && y <= layer.getYMax()).forEach((Double y) -> {
                                    Point2D p1 = layer.toPixel(0, y);
                                    value.getPoints().addAll(new Double[]{w, p1.getY()});
                                    value.getPoints().addAll(new Double[]{w - layer.xLeftTickLength, p1.getY()});
                                    value.getPoints().addAll(new Double[]{w, p1.getY()});
                                });
                                layer.getAxisSet().getYTransform().getMinorTicks().stream().filter((Double y) -> y >= layer.getYMin() && y <= layer.getYMax()).forEach((Double y) -> {
                                    Point2D p1 = layer.toPixel(0, y);
                                    value.getPoints().addAll(new Double[]{w, p1.getY()});
                                    value.getPoints().addAll(new Double[]{w - layer.xLeftTickLength * 0.7, p1.getY()});
                                    value.getPoints().addAll(new Double[]{w, p1.getY()});
                                });
                            }
                            break;
                        case RIGHT:
                            if (layer.isRightAxisPainted()) {
                                Point2D p0 = layer.toPixel(layer.getXLeft(), layer.getYBottom());
                                value.getPoints().addAll(new Double[]{p0.getX() + layer.xRightOffset + layer.xRightOffset, p0.getY()});
                                p0 = layer.toPixel(layer.getXLeft(), layer.getYTop());
                                value.getPoints().addAll(new Double[]{p0.getX() - layer.xRightOffset + layer.xRightOffset, p0.getY()});
                                layer.getAxisSet().getYTransform().get().stream().filter((Double y) -> y >= layer.getYMin() && y <= layer.getYMax()).forEach((Double y) -> {
                                    Point2D p1 = layer.toPixel(layer.getXLeft(), y);
                                    value.getPoints().addAll(new Double[]{p1.getX() + layer.xRightOffset, p1.getY()});
                                    value.getPoints().addAll(new Double[]{p1.getX() + layer.xRightOffset + layer.xRightTickLength, p1.getY()});
                                    value.getPoints().addAll(new Double[]{p1.getX() + layer.xRightOffset, p1.getY()});
                                });
                                layer.getAxisSet().getYTransform().getMinorTicks().stream().filter((Double y) -> y >= layer.getYMin() && y <= layer.getYMax()).forEach((Double y) -> {
                                    Point2D p1 = layer.toPixel(layer.getXLeft(), y);
                                    value.getPoints().addAll(new Double[]{p1.getX() - layer.xRightOffset, p1.getY()});
                                    value.getPoints().addAll(new Double[]{p1.getX() - layer.xRightOffset + layer.xRightTickLength * 0.7, p1.getY()});
                                    value.getPoints().addAll(new Double[]{p1.getX() - layer.xRightOffset, p1.getY()});
                                });
                            }
                            break;
                    }
                }
                return value;
            }
        }

    }

    /**
     * Represents a Rectangle2D describing the present limits of the coordinate
     * system for this graph: i.e. [xmin, ymin, width, height].
     */
    private class AxesBounds extends ObjectBinding<Rectangle2D> {

        private AxesBounds() {
            bind(xLeftProperty());
            bind(xRightProperty());
            bind(yTopProperty());
            bind(yBottomProperty());
        }

        @Override
        protected Rectangle2D computeValue() {
            return new Rectangle2D(getXMin(), getYMin(), getXMax() - getXMin(), getYMax() - getYMin());
        }
    }

    /**
     * Binding to support lazy evaluation of the major ticks on the xpos-axis
     */
    public class MajorXInterval extends ObjectBinding<Double> {

        /**
         * A user-specified constant value that will be used instead of the
         * automatically calculated value. Set this to Double.NaN to enable
         * auto-calculation.
         */
        private double userSpecifiedValue = Double.NaN;

        private MajorXInterval() {
            bind(xLeftProperty());
            bind(xRightProperty());
            bind(yTopProperty());
            bind(yBottomProperty());
        }

        @Override
        protected Double computeValue() {
            if (Double.isNaN(userSpecifiedValue)) {
                double width = Math.abs(getAxesBounds().getWidth());
                double lg = Math.log10(width);
                double rem = lg - Math.floor(lg);
                double scope = Math.pow(10, rem);
                double scale = Math.pow(10, Math.floor(lg));
                double inc;
                if (scope > 5) {
                    inc = scale;
                } else if (scope > 2) {
                    inc = scale / 2d;
                } else {
                    inc = scale / 5d;
                }
                return inc;
            } else {
                return userSpecifiedValue;
            }
        }

        public void reset() {
            userSpecifiedValue = Double.NaN;
        }

    }

    public class MajorYInterval extends ObjectBinding<Double> {

        private double userSpecifiedValue = Double.NaN;

        private MajorYInterval() {
            bind(xLeftProperty());
            bind(xRightProperty());
            bind(yTopProperty());
            bind(yBottomProperty());
        }

        @Override
        protected Double computeValue() {
            if (Double.isNaN(userSpecifiedValue)) {
                double height = Math.abs(getAxesBounds().getHeight());
                double lg = Math.log10(height);
                double rem = lg - Math.floor(lg);
                double scope = Math.pow(10, rem);
                double scale = Math.pow(10, Math.floor(lg));
                double inc;
                if (scope > 5) {
                    inc = scale;
                } else if (scope > 2) {
                    inc = scale / 2d;
                } else {
                    inc = scale / 5d;
                }
                return inc;
            } else {
                return userSpecifiedValue;
            }
        }

        public void reset() {
            userSpecifiedValue = Double.NaN;
        }
    }

    /**
     * Used to calculate the tolerance used to determine whether the mouse is
     * "over" a location using graph coordinate space.
     */
    private class Tolerance extends ObjectBinding<Double> {

        private final String axis;

        public Tolerance(String axis) {
            this.axis = axis;
            if (axis.matches("X")) {
                bind(xLeftProperty());
                bind(xRightProperty());
            } else {
                bind(yTopProperty());
                bind(yBottomProperty());
            }
        }

        @Override
        protected Double computeValue() {
            if (axis.matches("X")) {
                return (getYMax() - getYMin()) / 30d;
            } else {
                return (getXMax() - getXMin()) / 30d;
            }
        }

    }

    public class AxisTop extends AbstractAxisRegion {

        public AxisTop(Chart layer) {
            super(layer);
            getChildren().add(line.get());
            axisLabel.setTextAlignment(TextAlignment.CENTER);
            axisLabel.setTextOrigin(VPos.BOTTOM);
            setCursor(Cursor.DEFAULT);
            getChildren().add(axisLabel);
            prefWidthProperty().bind(layer.getFirstLayer().view.prefWidthProperty());
            requestLayout();
        }

        @Override
        public double computePrefHeight(double w) {
            if (getTickLabels().size() > 0) {
                return axisLabel.prefHeight(-1d) * 3d;
            } else {
                return 50d;
            }
        }

        @Override
        public void layoutChildren() {
            line.get();
            computeValue();
            if (getLayer().isTopAxisLabelled()) {
                double p = getLayer().isTopAxisPainted()
                        ? line.get().getBoundsInParent().getMinY()
                        : getHeight() - 2d;
                if (getTickLabels().size() > 0) {
                    getChildren().stream().filter(x -> x instanceof TickLabel).forEach((Node x) -> {
                        TickLabel text = (TickLabel) x;
                        //text.setFont(getLayer().fontProperty.get());
                        text.setLayoutX(text.xpos);
                        text.setLayoutY(p);
                    });
                }
                addAxisLabel();
                axisLabel.setFont(getFont());
                axisLabel.setLayoutY(getTickLabels().get(0).getBoundsInParent().getMinY());
                axisLabel.setLayoutX(getLayer().getView().prefWidth(0d) / 2d
                        - axisLabel.prefWidth(0d) / 2d);
            } else {
                getTickLabels().stream().forEach(x -> getChildren().remove(x));
                removeAxisLabel();
            }
            super.layoutChildren();
        }

        private void computeValue() {
            /**
             * Add or remove text as per the present settings
             */
            getTickLabels().stream().forEach((TickLabel x) -> {
                getChildren().remove(x);
            });

            if (getLayer().isTopAxisLabelled()) {
                getLayer().axisSet.xTransform.get().stream()
                        .filter(x -> x >= getLayer().getXMin() && x <= getLayer().getXMax())
                        .forEach((Double x) -> {
                            TickLabel text = null;
                            if (isCategorical()) {
                                if (getCategories().containsKey(x.intValue())) {
                                    text = new TickLabel(getCategories().get(x.intValue()));
                                }
                            } else {
                                text = new TickLabel(getLayer().axisSet.xTransform.getTickLabel(x));
                            }
                            if (text != null) {
                                Point2D p1 = getLayer().toPixel(x, getLayer().getYTop());
                                p1 = getLayer().getView().localToParent(p1);
                                p1 = parentToLocal(p1);
                                text.setFont(getFont());
                                text.setFill(layer.getAxisColor());
                                text.xpos = p1.getX() - text.prefWidth(0) / 2d;
                                text.setTextOrigin(VPos.BOTTOM);
                                getChildren().add(text);
                            }
                        });
            }
        }
    }

    public class AxisBottom extends AbstractAxisRegion {

        public AxisBottom(Chart layer) {
            super(layer);
            getChildren().add(line.get());
            axisLabel.setTextAlignment(TextAlignment.CENTER);
            setCursor(Cursor.DEFAULT);
            axisLabel.setFont(getFont());
            axisLabel.setTextOrigin(VPos.TOP);
            getChildren().add(axisLabel);
            prefWidthProperty().bind(layer.getFirstLayer().view.prefWidthProperty());
            requestLayout();
        }

        @Override
        public double computePrefHeight(double w) {
            if (getTickLabels().size() > 0) {
                return getTickLabels().get(0).getBoundsInParent().getMaxY() + axisLabel.prefHeight(-1);
            } else {
                return 50d;
            }
        }

        @Override
        public void layoutChildren() {
            line.get();
            computeValue();
            double p = line.get().getBoundsInParent().getMaxY();
            if (getLayer().isBottomAxisLabelled()) {
                if (getTickLabels().size() > 0) {
                    getChildren().stream().filter(x -> x instanceof TickLabel).forEach((Node x) -> {
                        TickLabel text = (TickLabel) x;
                        //text.setFont(getFont());
                        text.setLayoutX(text.xpos);
                        text.setLayoutY(p);
                    });
                }
                addAxisLabel();
                axisLabel.setFont(getFont());
                axisLabel.setLayoutY(getTickLabels().get(0).getBoundsInParent().getMaxY());
                axisLabel.setLayoutX(getLayer().getView().getWidth() / 2d
                        - axisLabel.getBoundsInParent().getWidth() / 2d);
            } else {
                getTickLabels().stream().forEach(x -> getChildren().remove(x));
                removeAxisLabel();
            }
            super.layoutChildren();
        }

        private void computeValue() {
            getTickLabels().stream().forEach((TickLabel x) -> {
                getChildren().remove(x);
            });

            if (getLayer().isBottomAxisLabelled()) {
                final double w = this.calcTickLabelWidth();
                getLayer().axisSet.xTransform.get().stream()
                        .filter(x -> x >= getLayer().getXMin() && x <= getLayer().getXMax())
                        .forEach((Double x) -> {
                            Point2D p1;
                            TickLabel text = null;
                            if (isCategorical()) {
                                if (getCategories().containsKey(x.intValue())) {
                                    text = new TickLabel(getCategories().get(x.intValue()));
                                }
                            } else {
                                text = new TickLabel(getLayer().axisSet.xTransform.getTickLabel(x));
                            }
                            if (text != null) {
                                p1 = getLayer().toPixel(x, getLayer().getYBottom());
                                p1 = getLayer().getView().localToParent(p1);
                                p1 = parentToLocal(p1);
                                text.setFont(getFont());
                                text.setFill(layer.getAxisColor());
                                text.xpos = p1.getX() - text.prefWidth(0) / 2d;
                                text.setTextOrigin(VPos.TOP);
                                getChildren().add(text);

                            }
                        });
            }
        }
    }

    public class AxisLeft extends AbstractAxisRegion {

        public AxisLeft(Chart layer) {
            super(layer);
            getChildren().add(line.get());
            axisLabel.setRotate(-90d);
            axisLabel.setTextOrigin(VPos.CENTER);
            getChildren().add(axisLabel);
            setCursor(Cursor.DEFAULT);
            prefHeightProperty().bind(layer.getFirstLayer().view.prefHeightProperty());
            requestLayout();
        }

        @Override
        public final double computePrefWidth(double h) {
            double w = Double.NEGATIVE_INFINITY;
            if (getTickLabels().size() > 0) {
                for (TickLabel text : getTickLabels()) {
                    w = Math.max(w, text.getBoundsInParent().getWidth());
                }
            } else {
                Text t = new Text("000");
                t.setFont(getFont());
                w = t.prefWidth(-1d);
            }
            w += axisLabel.prefHeight(-1d) + 5d;
            return Math.max(w, 20);
        }

        @Override
        public void layoutChildren() {
            line.get();
            computeValue();
            if (getLayer().isLeftAxisLabelled()) {
                if (getTickLabels().size() > 0) {
                    double p = line.get().getBoundsInParent().getMinX();
                    getChildren().stream().filter(x -> x instanceof TickLabel).forEach((Node x) -> {
                        TickLabel text = (TickLabel) x;
                        //text.setFont(getFont());
                        text.setLayoutX(p - text.getBoundsInParent().getWidth());
                        text.setLayoutY(text.ypos);
                    });
                }
                addAxisLabel();
                axisLabel.setFont(getFont());
                axisLabel.setLayoutY(getHeight() / 2d);
                axisLabel.setLayoutX(findMinX() - (axisLabel.prefWidth(0) / 2d) - 5d);
            } else {
                getTickLabels().stream().forEach(x -> getChildren().remove(x));
                removeAxisLabel();
            }
            super.layoutChildren();
        }

        private double findMinX() {
            double minx = Double.POSITIVE_INFINITY;
            for (Text text : getTickLabels()) {
                minx = Math.min(minx, text.prefWidth(-1d));
            }
            return minx;
        }

        private void computeValue() {
            getTickLabels().stream().forEach((TickLabel x) -> {
                getChildren().remove(x);
            });

            if (getLayer().isBottomAxisLabelled()) {
                getLayer().axisSet.yTransform.get().stream()
                        .filter(y -> y >= getLayer().getYMin() && y <= getLayer().getYMax())
                        .forEach((Double y) -> {
                            TickLabel text = null;
                            if (isCategorical()) {
                                if (getCategories().containsKey(y.intValue())) {
                                    text = new TickLabel(getCategories().get(y.intValue()));
                                }
                            } else {
                                text = new TickLabel(getLayer().axisSet.yTransform.getTickLabel(y));
                            }
                            if (text != null) {
                                Point2D p1;
                                p1 = getLayer().toPixel(getLayer().getXLeft(), y);
                                p1 = getLayer().getView().localToParent(p1);
                                p1 = parentToLocal(p1);
                                text.setFont(getFont());
                                text.setFill(layer.getAxisColor());
                                text.ypos = p1.getY();
                                getChildren().add(text);
                            }
                        });
            }
        }

    }

    public class AxisRight extends AbstractAxisRegion {

        public AxisRight(Chart layer) {
            super(layer);
            getChildren().add(line.get());
            axisLabel.setRotate(90d);
            setCursor(Cursor.DEFAULT);
            axisLabel.setFont(getFont());
            getChildren().add(axisLabel);
            prefHeightProperty().bind(layer.getFirstLayer().view.prefHeightProperty());
            requestLayout();
        }

        @Override
        public final double computePrefWidth(double h) {
            double p = findMaxX();
            p += axisLabel.prefHeight(-1d) + 5d;
            return Math.max(p, 20);
        }

        @Override
        public void layoutChildren() {
            line.get();
            computeValue();
            if (getLayer().isRightAxisLabelled()) {
                double p = line.get().getBoundsInParent().getMaxX();
                if (getTickLabels().size() > 0) {
                    getChildren().stream().filter(x -> x instanceof TickLabel).forEach((Node x) -> {
                        TickLabel text = (TickLabel) x;
                        //text.setFont(getFont());
                        text.setLayoutX(p);
                        text.setLayoutY(text.ypos);
                    });
                    addAxisLabel();
                    axisLabel.setFont(getFont());
                    axisLabel.setLayoutX(5d + findMaxX() - axisLabel.prefWidth(-1d) / 2d + axisLabel.prefHeight(-1d));
                    axisLabel.setLayoutY(getHeight() / 2d);
                }
            } else {
                getTickLabels().stream().forEach(x -> getChildren().remove(x));
                removeAxisLabel();
            }
            super.layoutChildren();
        }

        private double findMaxX() {
            double maxx = Double.NEGATIVE_INFINITY;
            for (Text text : getTickLabels()) {
                maxx = Math.max(maxx, text.prefWidth(-1d));
            }
            if (Double.isFinite(maxx)) {
                return maxx;
            } else {
                Text t = new Text("000");
                t.setFont(getFont());
                return t.prefWidth(-1d);
            }
        }

        private void computeValue() {
            getTickLabels().stream().forEach((TickLabel x) -> {
                getChildren().remove(x);
            });
            if (getLayer().isRightAxisLabelled()) {
                getLayer().axisSet.yTransform.get().stream()
                        .filter(y -> y >= getLayer().getYMin() && y <= getLayer().getYMax())
                        .forEach((Double y) -> {
                            TickLabel text = null;
                            if (isCategorical()) {
                                if (getCategories().containsKey(y.intValue())) {
                                    text = new TickLabel(getCategories().get(y.intValue()));
                                }
                            } else {
                                text = new TickLabel(getLayer().axisSet.yTransform.getTickLabel(y));
                            }
                            if (text != null) {
                                Point2D p1;
                                p1 = getLayer().toPixel(getLayer().getXLeft(), y);
                                p1 = getLayer().getView().localToParent(p1);
                                p1 = parentToLocal(p1);
                                getChildren().add(text);
                                text.setFont(getFont());
                                text.setFill(layer.getAxisColor());
                                text.setX(0d);
                                text.ypos = p1.getY();
                            }
                        });

            }
        }
    }

    /**
     * AxisSet class.
     *
     */
    public class AxisSet {

        private final AxisTop axisTop;
        private final AxisBottom axisBottom;
        private final AxisLeft axisLeft;
        private final AxisRight axisRight;
        protected Chart layer;
        private AbstractTransform xTransform = new NOPTransform();
        private AbstractTransform yTransform = new NOPTransform();

        /**
         * Constructs the axis set.
         *
         * @param axisRight - right axis instance.
         * @param axisTop - top axis instance.
         * @param axisLeft - left axis instance.
         * @param axisBottom - bottom axis instance.
         */
        private AxisSet(AxisRight axisRight, AxisTop axisTop, AxisLeft axisLeft, AxisBottom axisBottom) {
            this.axisRight = axisRight;
            this.axisTop = axisTop;
            this.axisLeft = axisLeft;
            this.axisBottom = axisBottom;
            layer = (Chart) axisRight.getParent();
            xTransform.bind(layer, AbstractTransform.AXIS.HORIZONTAL);
            yTransform.bind(layer, AbstractTransform.AXIS.VERTICAL);
        }

        /**
         * @return the left axis instance
         */
        public AxisLeft getLeftAxis() {
            return axisLeft;
        }

        /**
         *
         * @return the top axis instance
         */
        public AxisTop getTopAxis() {
            return axisTop;
        }

        /**
         *
         * @return the bottom axis instance
         */
        public AxisBottom getBottomAxis() {
            return axisBottom;
        }

        /**
         *
         * @return the right axis instance
         */
        public AxisRight getRightAxis() {
            return axisRight;
        }

        /**
         * Returns a Point2D instance containing the inverse transform for the
         * data point represented by the supplied [x,y] pair (in axis
         * co-oordinate space).
         *
         * @param x the x coordinate
         * @param y the y coordinate
         * @return the inverse transformed values as a Point2D instance.
         */
        Point2D getInverse(double x, double y) {
            return new Point2D(xTransform.getInverse(x, Double.NaN).getX(), yTransform.getInverse(Double.NaN, y).getY());
        }

        /**
         * Returns a Point2D instance containing the transform for the data
         * point represented by the supplied [x,y] pair (in axis co-oordinate
         * space).
         *
         * @param x the x coordinate
         * @param y the y coordinate
         * @return the transformed values as a Point2D instance.
         */
        Point2D getData(double x, double y) {
            return new Point2D(xTransform.getData(x, Double.NaN).getX(), yTransform.getData(Double.NaN, y).getY());
        }

        /**
         * @return the xTransform
         */
        public AbstractTransform getXTransform() {
            return xTransform;
        }

        /**
         * @param xTransform the xTransform to set
         */
        private void setXTransform(AbstractTransform xTransform) {
            xTransform.bind(layer, AbstractTransform.AXIS.HORIZONTAL);
            this.xTransform = xTransform;
        }

        /**
         * @return the yTransform
         */
        public AbstractTransform getYTransform() {
            return yTransform;
        }

        /**
         * @param yTransform the yTransform to set
         */
        private void setYTransform(AbstractTransform yTransform) {
            yTransform.bind(layer, AbstractTransform.AXIS.VERTICAL);
            this.yTransform = yTransform;
        }

        /**
         * Renders the canvas for this chart view. This includes the grid,
         * internal axes etc.
         *
         * @param g the GraphicsContext retrieved from the canvas instance.
         */
        private void paintGrid(GraphicsContext g) {

            axisTop.requestLayout();
            axisBottom.requestLayout();
            axisLeft.requestLayout();
            axisRight.requestLayout();

            if (layer.getParent() instanceof Chart) {
                g.clearRect(0, 0, layer.getView().getWidth(), layer.getView().getHeight());
            } else {
                g.setFill(Color.WHITE);
                g.fillRect(0, 0, layer.getView().getWidth(), layer.getView().getHeight());
            }

            if (layer.isPolar()) {
                final Point2D p0 = layer.toPixel(0, 0);
                if (layer.isMajorGridPainted()) {
                    g.setStroke(layer.getMajorGridColor());
                    g.setLineWidth(layer.getMajorGridStrokeWidth());

                    //Set the clipping axisRegion
                    g.save();
                    g.beginPath();
                    g.arc(layer.getView().getWidth() / 2d, layer.getView().getHeight() / 2d,
                            layer.getView().getWidth() / 2d + 2d, layer.getView().getHeight() / 2d + 2d, 0, 360);
                    g.clip();

                    /**
                     * Paints the rays of the grid.
                     */
                    g.beginPath();
                    g.arc(layer.getView().getWidth() / 2d, layer.getView().getHeight() / 2d,
                            layer.getView().getWidth() / 2d, layer.getView().getHeight() / 2d, 0, 360);

                    for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / 6) {
                        Point2D p1 = layer.toPixel(Math.cos(theta) * layer.getView().getWidth() / 2d,
                                Math.sin(theta) * layer.getView().getHeight() / 2d);
                        g.moveTo(p0.getX(), p0.getY());
                        g.lineTo(p1.getX(), p1.getY());
                    }
                    g.stroke();

                    /**
                     * This paints the concentric rings of the grid. Note that
                     * this is done for both x and y (which may have different
                     * ranges). It will generally make sense for the interval on
                     * the two axes to be the same.
                     */
                    g.beginPath();
                    for (int k = 0; k < xTransform.get().size(); k++) {
                        double x = xTransform.get().get(k);
                        g.arc(p0.getX(), p0.getY(), x / layer.getPixelWidth(), x / layer.getPixelHeight(), 0d, 360d);
                    }
                    for (int k = 0; k < yTransform.get().size(); k++) {
                        double y = yTransform.get().get(k);
                        g.arc(p0.getX(), p0.getY(), y / layer.getPixelWidth(), y / layer.getPixelHeight(), 0d, 360d);
                    }
                    g.stroke();
                }

                if (layer.isInnerAxisLabelled()) {

                    g.setFont(Font.font(layer.fontProperty().get().getFamily(), layer.getInnerAxisFontSize()));
                    g.setTextAlign(TextAlignment.CENTER);
                    g.setTextBaseline(VPos.CENTER);
                    g.setFill(layer.getInnerAxisColor());

                    /**
                     * Labels for the rays
                     */
                    double rx = layer.getView().getWidth() / 2d;
                    rx -= layer.fontProperty().get().getSize();
                    double ry = layer.getView().getHeight() / 2d;
                    ry -= layer.fontProperty().get().getSize();
                    // Unit circle
                    Ellipse e0 = new Ellipse(0, 0, rx, ry);
                    for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / 6) {
                        Point2D p2 = layer.toPixel(Math.cos(theta) * rx, Math.sin(theta) * ry);
                        Point2D p = getIntersection(e0, p2.getX(), p2.getY());
                        if (p != null) {
                            g.fillText(String.format("%3.0f\u00b0", theta * 180 / Math.PI),
                                    p0.getX() + p.getX(),
                                    p0.getY() + p.getY());
                        }
                    }

                    g.restore();
                }

            } else {

                if (layer.getAltFillVertical() != Color.TRANSPARENT) {
                    g.setFill(layer.getAltFillVertical());
                    Point2D p2;
                    Point2D p3;
                    for (int k = 0; k < xTransform.get().size() - 1; k += 2) {
                        if (layer.isReverseX()) {
                            p2 = layer.toPixel(xTransform.get().get(k + 1), layer.getYTop());
                            p3 = layer.toPixel(xTransform.get().get(k), layer.getYBottom());
                        } else {
                            p2 = layer.toPixel(xTransform.get().get(k), layer.getYTop());
                            p3 = layer.toPixel(xTransform.get().get(k + 1), layer.getYBottom());
                        }
                        g.fillRect(p2.getX(), p2.getY(), p3.getX() - p2.getX(), p3.getY() - p2.getY());
                    }
                }

                if (layer.getAltFillHorizontal() != Color.TRANSPARENT) {
                    g.setFill(layer.getAltFillHorizontal());
                    Point2D p2;
                    Point2D p3;
                    for (int k = yTransform.get().size() - 1; k > 0; k -= 2) {
                        if (layer.isReverseY()) {
                            p2 = layer.toPixel(layer.getXLeft(), yTransform.get().get(k - 1));
                            p3 = layer.toPixel(layer.getXRight(), yTransform.get().get(k));
                        } else {
                            p2 = layer.toPixel(layer.getXLeft(), yTransform.get().get(k));
                            p3 = layer.toPixel(layer.getXRight(), yTransform.get().get(k - 1));
                        }
                        g.fillRect(p2.getX(), p2.getY(), p3.getX() - p2.getX(), p3.getY() - p2.getY());
                    }
                }
                // MAJOR GRID 
                if (layer.isMajorGridPainted()) {
                    g.setStroke(layer.getMajorGridColor());
                    g.setLineWidth(layer.getMajorGridStrokeWidth());
                    xTransform.get().stream().forEach((Double x) -> {
                        Point2D p0 = layer.toPixel(x, layer.getYBottom());
                        Point2D p1 = layer.toPixel(x, layer.getYTop());
                        g.strokeLine(p0.getX(), p0.getY(), p1.getX(), p1.getY());
                    });

                    yTransform.get().stream().forEach((Double y) -> {
                        Point2D p0 = layer.toPixel(layer.getXMin(), y);
                        Point2D p1 = layer.toPixel(layer.getXMax(), y);
                        g.strokeLine(p0.getX(), p0.getY(), p1.getX(), p1.getY());
                    });

                }

                //MINOR GRID
                if (layer.isMinorGridPainted()) {
                    g.setStroke(layer.getMinorGridColor());
                    g.setLineWidth(layer.minorGridStrokeWidth());
                    xTransform.getMinorTicks().stream().forEach((Double x) -> {
                        Point2D p0 = layer.toPixel(x, layer.getYBottom());
                        Point2D p1 = layer.toPixel(x, layer.getYTop());
                        g.strokeLine(p0.getX(), p0.getY(), p1.getX(), p1.getY());
                    });

                    yTransform.getMinorTicks().stream().forEach((Double y) -> {
                        Point2D p0 = layer.toPixel(layer.getXMin(), y);
                        Point2D p1 = layer.toPixel(layer.getXMax(), y);
                        g.strokeLine(p0.getX(), p0.getY(), p1.getX(), p1.getY());
                    });

                }

                if (layer.isInnerAxisPainted()) {
                    g.setStroke(layer.getInnerAxisColor());
                    g.setLineWidth(layer.getInnerAxisStrokeWidth());
                    Point2D p0 = layer.toPixel(layer.getXLeft(), layer.getYOrigin());
                    Point2D p1 = layer.toPixel(layer.getXRight(), layer.getYOrigin());
                    g.strokeLine(p0.getX(), p0.getY(), p1.getX(), p1.getY());
                    p0 = layer.toPixel(layer.getXOrigin(), layer.getYTop());
                    p1 = layer.toPixel(layer.getXOrigin(), layer.getYBottom());
                    g.strokeLine(p0.getX(), p0.getY(), p1.getX(), p1.getY());

                    xTransform.get().stream().forEach((Double x) -> {
                        Point2D p2 = layer.toPixel(x, layer.getYOrigin());
                        Point2D p3 = layer.toPixel(x, layer.getYOrigin());
                        g.strokeLine(p2.getX(), p2.getY() - 4, p3.getX(), p3.getY() + 4);
                    });

                    yTransform.get().stream().forEach((Double y) -> {
                        Point2D p4 = layer.toPixel(layer.getXOrigin(), y);
                        Point2D p5 = layer.toPixel(layer.getXOrigin(), y);
                        g.strokeLine(p4.getX() - 4, p4.getY(), p5.getX() + 4, p5.getY());
                    });
                }

                if (layer.isInnerAxisLabelled()) {
                    g.setTextAlign(TextAlignment.CENTER);
                    g.setFill(layer.getInnerAxisColor());
                    g.setTextBaseline(VPos.TOP);
                    g.setFont(Font.font(layer.fontProperty().get().getFamily(), layer.getInnerAxisFontSize()));
                    xTransform.get().stream().forEach((Double x) -> {
                        Point2D p2 = layer.toPixel(x, layer.getYOrigin());
                        Point2D p3 = layer.toPixel(x, layer.getYOrigin());
                        if (Math.abs(x - layer.getXOrigin()) > layer.getMajorXInterval() / 4d) {
                            g.fillText(xTransform.getTickLabel(x), p2.getX(), p3.getY() + 5);
                        }
                    });
                    g.setTextAlign(TextAlignment.RIGHT);
                    g.setTextBaseline(VPos.CENTER);
                    yTransform.get().stream().forEach((Double y) -> {
                        Point2D p4 = layer.toPixel(layer.getXOrigin(), y);
                        Point2D p5 = layer.toPixel(layer.getXOrigin(), y);
                        if (Math.abs(y - layer.getYOrigin()) > layer.getMajorYInterval() / 4d) {
                            g.fillText(yTransform.getTickLabel(y), p4.getX() - 7, p5.getY());
                        }
                    });
                }
            }

        }
    }

    private static Point2D getIntersection(Ellipse e0, double x0, double y0) {
        //See: http://mathworld.wolfram.com/Ellipse-LineIntersection.html
        double a = e0.getRadiusX();
        double b = e0.getRadiusY();
        double x = a * b * x0 / (Math.sqrt(a * a * y0 * y0 + b * b * x0 * x0));
        double y = a * b * y0 / (Math.sqrt(a * a * y0 * y0 + b * b * x0 * x0));
        return new Point2D(x, y);
    }

}
