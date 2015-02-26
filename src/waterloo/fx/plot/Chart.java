/* 
 *
 * <http://sigtool.github.io/waterlooFX/>
 *
 * Copyright King's College London  2013-2014. 
 * Copyright Malcolm Lidierth 2014-.
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
 */
package waterloo.fx.plot;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.FontCssMetaData;
import javafx.css.StyleConverter;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableIntegerProperty;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import waterloo.fx.plot.axis.AbstractAxisRegion;
import waterloo.fx.plot.axis.AxisBottom;
import waterloo.fx.plot.axis.AxisLeft;
import waterloo.fx.plot.axis.AxisRight;
import waterloo.fx.plot.axis.AxisSet;
import waterloo.fx.plot.axis.AxisTop;
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
 * The {@code Chart} also hosts an axisPane that is used for the chart's axes.
 * </p>
 * A {@code Chart} instance can host other {@code Chart} instances to create
 * layered graphs where each {@code Chart} has independent axes. The axis
 * displays will be automatically positioned within the axisPane <strong>of the
 * first chart</strong> to prevent overlap.
 *
 * @author Malcolm Lidierth
 */
public final class Chart extends Pane {

    /**
     * Singleton instance used as the standard Insets before the padding can be
     * calculated and to set a minimum spacing.
     */
    private static Insets defaultInsets = new Insets(30, 30, 30, 30);

    private static final Paint altFillColor = new Color(0f, 0f, 1f, 0.1f);

    /**
     * @return the defaultInsets
     */
    public static Insets getDefaultInsets() {
        return defaultInsets;
    }

    public static void setDefaultInsets(Insets insets) {
        defaultInsets = insets;
    }

    /**
     * Offset (in pixels) between the top axis and the view area.
     */
    private double yTopOffset = 0;
    /**
     * Offset (in pixels) between the bottom axis and the view area.
     */
    private double yBottomOffset = 0;
    /**
     * Offset (in pixels) between the left axis and the view area.
     */
    private double xLeftOffset = 0;
    /**
     * Offset (in pixels) between the right axis and the view area.
     */
    private double xRightOffset = 0;
    /**
     * Tick length (in pixels) for major ticks on the top axis
     */
    public double yTopTickLength = 5;
    /**
     * Tick length (in pixels) for major ticks on the bottom axis
     */
    public double yBottomTickLength = 5;
    /**
     * Tick length (in pixels) for major ticks on the left axis
     */
    public double xLeftTickLength = 5;
    /**
     * Tick length (in pixels) for major ticks on the right axis
     */
    public double xRightTickLength = 5;

    //public final String yTopLabel = "Axis Label";
    //private Paint yTopColor = Color.BLACK;
    //public final String yBottomLabel = "Axis Label";
    //private Paint yBottomColor = Color.BLACK;
    //public final String xLeftLabel = "Axis Label";
    //private Paint xLeftColor = Color.BLACK;
    //public final String xRightLabel = "Axis Label";
    //private Paint xRightColor = Color.BLACK;
    /**
     * TODO: make these styleable
     */
    //public final static final boolean toolTipFlag = false;
    //public final static final boolean editable = true;
    //public final static final boolean interactive = true;
    private final AxisTop axisTop;
    private final AxisBottom axisBottom;
    private final AxisLeft axisLeft;
    private final AxisRight axisRight;
    /**
     * The axisSet
     */
    private final AxisSet axisSet;
    //private final ObservableList<GJAnnotation> annotations = FXCollections.observableArrayList(new ArrayList<>());
    private final Canvas canvas;
    private final StackPane view;
    /**
     * This Pane is used to parent the axes painted outside the view area. Note
     * that with layered {@code Charts}, the axisPane of the first {@code Chart}
     * contains the axes for all {@code Charts}.
     */
    private final Pane axisPane;
    /**
     * Base font to use. This is styleable via the "-w-font-" settings.
     */
    private Font font = Font.getDefault();
    /**
     * If clipping is true, this limits painting of added nodes to the limits of
     * the view bounds
     */
    private boolean clipping = true;

    /**
     * Used internally to store the axes limits
     */
    private final ObjectBinding<Rectangle2D> axesBounds;
    /**
     * Interval between major ticks/grids in axis coordinates for the x-axis
     */
    private final MajorTickIntervalBinding majorXInterval;
    /**
     * Interval between major ticks/grids in axis coordinates for the y-axis
     */
    private final MajorTickIntervalBinding majorYInterval;
    /**
     * Number of minor ticks/grids in the majorXInterval.
     * <strong>This is a hint, not all AxesSets support its use.</strong>
     */

    private static NumberFormat formatter = new DecimalFormat();
    private Text xPosText = null;
    private Text yPosText = null;
    private CursorTextBox xCursorText;
    private CursorTextBox yCursorText;

    private StyleableIntegerProperty minorCountXHint = new StyleableIntegerProperty(4) {
        @Override
        public final CssMetaData<Chart, Number> getCssMetaData() {
            return Chart.StyleableProperties.MINORCOUNTXHINT;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "minorCountX";
        }
    };
    /**
     * Number of minor ticks/grids in the majorYInterval.
     * <strong>This is a hint, not all AxesSets support its use.</strong>
     */
    private StyleableIntegerProperty minorCountYHint = new StyleableIntegerProperty(4) {

        @Override
        public final CssMetaData<Chart, Number> getCssMetaData() {
            return Chart.StyleableProperties.MINORCOUNTYHINT;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "minorCountY";
        }
    };

    private double dragXStart = Double.NaN;
    private double dragYStart = Double.NaN;
    private double deltaX, deltaY;
    private final Tolerance xTol;
    private final Tolerance yTol;
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
    private final StyleableDoubleProperty viewAspectRatio = new StyleableDoubleProperty(Double.NaN) {

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "aspectRatio";
        }

        @Override
        public final CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.ASPECTRATIO;
        }
    };
    private final StyleableObjectProperty<TRANSFORMTYPE> xTransformType = new StyleableObjectProperty<TRANSFORMTYPE>(TRANSFORMTYPE.LINEAR) {

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "xTransformType";
        }

        @Override
        public final CssMetaData<? extends Styleable, TRANSFORMTYPE> getCssMetaData() {
            return StyleableProperties.XTRANSFORMTYPE;
        }

        @Override
        public final void applyStyle(StyleOrigin so, TRANSFORMTYPE v) {
            super.applyStyle(so, v);
            setXTransformType(v);
        }

    };
    private final StyleableObjectProperty<TRANSFORMTYPE> yTransformType = new StyleableObjectProperty<TRANSFORMTYPE>(TRANSFORMTYPE.LINEAR) {

        //private StyleOrigin origin;
        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "yTransformType";
        }

        @Override
        public final CssMetaData<? extends Styleable, TRANSFORMTYPE> getCssMetaData() {
            return StyleableProperties.YTRANSFORMTYPE;
        }

        @Override
        public final void applyStyle(StyleOrigin so, TRANSFORMTYPE v) {
            super.applyStyle(so, v);
            setYTransformType(v);
        }
    };
    /**
     * Value for the xpos-axis at the left
     */
    private final StyleableDoubleProperty xLeft = new StyleableDoubleProperty(-1d) {

        @Override
        public final CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.XLEFT;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "xLeft";
        }

    };
    /**
     * Value for the x-axis at the right-most position
     */
    private final StyleableDoubleProperty xRight = new StyleableDoubleProperty(1d) {

        @Override
        public final CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.XRIGHT;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "xRight";
        }

    };
    /**
     * Value for the y-axis at the bottom of the view
     */
    private final StyleableDoubleProperty yBottom = new StyleableDoubleProperty(-1d) {

        @Override
        public final CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.YBOTTOM;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "yBottom";
        }

    };
    /**
     * Value for the y-axis at the top of the view
     */
    private final StyleableDoubleProperty yTop = new StyleableDoubleProperty(1d) {

        @Override
        public final CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.YTOP;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "yTop";
        }

    };
    /**
     * Value for the origin of the x-axis (i.e where it intersects the y-axis).
     */
    private final StyleableDoubleProperty xOrigin = new StyleableDoubleProperty(0d) {

        @Override
        public final CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.XORIGIN;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "xOrigin";
        }

    };
    /**
     * Value for the origin of the y-axis (i.e where it intersects the x-axis).
     */
    private final StyleableDoubleProperty yOrigin = new StyleableDoubleProperty(0d) {

        @Override
        public final CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.YORIGIN;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "xOrigin";
        }

    };
    /**
     * Stroke width used to draw an axis
     */
    private final StyleableDoubleProperty axisStrokeWidth = new StyleableDoubleProperty(1d) {

        @Override
        public final CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.AXISSTROKEWIDTH;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "axisStrokeWeight";
        }

    };

    /**
     * Stroke width used to draw an internal axis
     */
    private final StyleableDoubleProperty innerAxisStrokeWidth = new StyleableDoubleProperty(1.1d) {

        @Override
        public final CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.INNERAXISSTROKEWIDTH;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "innerAxisStrokeWeight";
        }

    };
    /**
     * Stroke width used to draw the minor grid
     */
    private final StyleableDoubleProperty minorGridStrokeWidth = new StyleableDoubleProperty(1.1d) {

        @Override
        public final CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.MINORGRIDSTROKEWIDTH;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "minorGridStrokeWidth";
        }

    };
    /**
     * Stroke width used to draw the major grid
     */
    private final StyleableDoubleProperty majorGridStrokeWidth = new StyleableDoubleProperty(1.3d) {

        @Override
        public final CssMetaData<Chart, Number> getCssMetaData() {
            return StyleableProperties.MAJORGRIDSTROKEWIDTH;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "majorGridStrokeWidth";
        }

    };
    /**
     * Set true to paint the minor grid
     */
    private final StyleableBooleanProperty minorGridPainted = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public final CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.MINORGRIDPAINTED;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "minorGridPainted";
        }

    };
    /**
     * Set true to paint the major grid
     */
    private final StyleableBooleanProperty majorGridPainted = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public final CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.MAJORGRIDPAINTED;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "majorGridPainted";
        }

    };
    /**
     * Set true to paint the inner axis.
     */
    private final StyleableBooleanProperty innerAxisPainted = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public final CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.INNERAXISPAINTED;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "innerAxisPainted";
        }

    };
    /**
     * getParent true to paint the labels for axisSet within the view
     */
    private final StyleableBooleanProperty innerAxisLabelled = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public final CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.INNERAXISLABELLED;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "innerAxisLabelled";
        }

    };
    /**
     * Set true to paint left axis
     */
    private final StyleableBooleanProperty leftAxisPainted = new StyleableBooleanProperty(Boolean.TRUE) {

        @Override
        public final CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.LEFTAXISPAINTED;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "leftAxisPainted";
        }

    };
    /**
     * Set true to paint right axis
     */
    private final StyleableBooleanProperty rightAxisPainted = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public final CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.RIGHTAXISPAINTED;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "rightAxisPainted";
        }

    };
    /**
     * Set true to paint top axis
     */
    private final StyleableBooleanProperty topAxisPainted = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public final CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.TOPAXISPAINTED;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "topAxisPainted";
        }

    };
    /**
     * Set true to paint bottom axis
     */
    private final StyleableBooleanProperty bottomAxisPainted = new StyleableBooleanProperty(Boolean.TRUE) {

        @Override
        public final CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.BOTTOMAXISPAINTED;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "bottomAxisPainted";
        }

    };
    /**
     * Set true to label the left axis
     */
    private final StyleableBooleanProperty leftAxisLabelled = new StyleableBooleanProperty(Boolean.TRUE) {

        @Override
        public final CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.LEFTAXISLABELLED;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "leftAxisLabelled";
        }

    };
    /**
     * Set true to label the right axis
     */
    private final StyleableBooleanProperty rightAxisLabelled = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public final CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.RIGHTAXISLABELLED;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "rightAxisLabelled";
        }

    };
    /**
     * Set true to label the top axis
     */
    private final StyleableBooleanProperty topAxisLabeled = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public final CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.TOPAXISLABELLED;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "topAxisLabelled";
        }

    };
    /**
     * Set true to label the bottom axis
     */
    private final StyleableBooleanProperty bottomAxisLabelled = new StyleableBooleanProperty(Boolean.TRUE) {

        @Override
        public final CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.BOTTOMAXISLABELLED;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "bottomAxisLabelled";
        }

    };
    /**
     * Draw as polar property. Set true for polar plot.
     */
    private final StyleableBooleanProperty polar = new StyleableBooleanProperty(Boolean.FALSE) {

        @Override
        public final CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.POLAR;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "polar";
        }

    };
    /**
     * Major grid color as a {@code Paint} instance
     */
    private final StyleableObjectProperty<Paint> majorGridColor = new StyleableObjectProperty<Paint>(Color.SLATEBLUE) {

        @Override
        public final CssMetaData<Chart, Paint> getCssMetaData() {
            return StyleableProperties.MAJORGRIDCOLOR;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "majorGridColor";
        }

    };
    /**
     * Minor grid color as a {@code Paint} instance
     */
    private final StyleableObjectProperty<Paint> minorGridColor = new StyleableObjectProperty<Paint>(Color.SLATEBLUE) {

        @Override
        public final CssMetaData<Chart, Paint> getCssMetaData() {
            return StyleableProperties.MINORGRIDCOLOR;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "minorGridColor";
        }

    };
    /**
     * Inner axis color as a {@code Paint} instance
     */
    private final StyleableObjectProperty<Paint> innerAxisColor = new StyleableObjectProperty<Paint>(Color.BLACK) {

        @Override
        public final CssMetaData<Chart, Paint> getCssMetaData() {
            return StyleableProperties.INNERAXISCOLOR;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "innerAxisColor";
        }

    };
    /**
     * Axis color as a {@code Paint} instance
     */
    private final StyleableObjectProperty<Paint> axisColor = new StyleableObjectProperty<Paint>(Color.BLACK) {

        @Override
        public final CssMetaData<Chart, Paint> getCssMetaData() {
            return StyleableProperties.AXISCOLOR;
        }

        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "axisColor";
        }

    };
    /**
     * Grid fill color as a {@code Paint} instance: alternate vertical grid
     * elements will be filled.
     */
    private final StyleableObjectProperty<Paint> altFillVertical
            = new StyleableObjectProperty<Paint>(altFillColor) {

                @Override
                public final Object getBean() {
                    return Chart.this;
                }

                @Override
                public final String getName() {
                    return "altFillVertical";
                }

                @Override
                public final CssMetaData<? extends Styleable, Paint> getCssMetaData() {
                    return StyleableProperties.ALTFILLVERTICAL;
                }

            };

    private StyleableBooleanProperty altFillVerticalPainted
            = new StyleableBooleanProperty(Boolean.FALSE) {
                @Override
                public final CssMetaData<Chart, Boolean> getCssMetaData() {
                    return StyleableProperties.ALTFILLVERTICALPAINTED;
                }

                @Override
                public final Object getBean() {
                    return Chart.this;
                }

                @Override
                public final String getName() {
                    return "altFillVerticalPainted";
                }
            };

    private StyleableBooleanProperty altFillHorizontalPainted
            = new StyleableBooleanProperty(Boolean.FALSE) {
                @Override
                public final CssMetaData<Chart, Boolean> getCssMetaData() {
                    return StyleableProperties.ALTFILLHORIZONTALPAINTED;
                }

                @Override
                public final Object getBean() {
                    return Chart.this;
                }

                @Override
                public final String getName() {
                    return "altFillHorizontalPainted";
                }
            };

    /**
     * Grid fill color as a {@code Paint} instance: alternate horizontal grid
     * elements will be filled.
     */
    private final StyleableObjectProperty<Paint> altFillHorizontal
            = new StyleableObjectProperty<Paint>(altFillColor) {

                @Override
                public final Object getBean() {
                    return Chart.this;
                }

                @Override
                public final String getName() {
                    return "altFillHorizontal";
                }

                @Override
                public final CssMetaData<? extends Styleable, Paint> getCssMetaData() {
                    return StyleableProperties.ALTFILLHORIZONTAL;
                }

            };

    /**
     * The inner axes are those drawn within the plotting area of the chart.
     * Inner axis colors, font characteristics etc are editable separately from
     * those drawn outside of the plotting area.
     *
     * innerAxisFontSize represents the size of the font to use.
     */
    private StyleableDoubleProperty innerAxisFontSize = new StyleableDoubleProperty(
            font == null ? Font.getDefault().getSize() : font.getSize()) {

                @Override
                public final Object getBean() {
                    return Chart.this;
                }

                @Override
                public final String getName() {
                    return "innerAxisFontSize";
                }

                @Override
                public final CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return (CssMetaData<? extends Styleable, Number>) StyleableProperties.INNERAXISFONTSIZE;
                }

            };
    /**
     * {@code axisFontSize} holds the size of the font to be used for rendering
     * tick marks, labels etc in th outer axes (those drawn to the left, right,
     * top etc of the plotting area).
     */
    private StyleableDoubleProperty axisFontSize = new StyleableDoubleProperty(
            font == null ? Font.getDefault().getSize() : font.getSize()) {

                @Override
                public final Object getBean() {
                    return Chart.this;
                }

                @Override
                public final String getName() {
                    return "axisFontSize";
                }

                @Override
                public final CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return (CssMetaData<? extends Styleable, Number>) StyleableProperties.INNERAXISFONTSIZE;
                }

            };
    private final StyleableObjectProperty<Font> fontProperty = new StyleableObjectProperty<Font>(font) {

        @Override
        public final void set(Font value) {
            font = value;
        }

        @Override
        public final Font get() {
            return font;
        }

        @Override
        public final FontCssMetaData<Chart> getCssMetaData() {
            return (FontCssMetaData<Chart>) StyleableProperties.FONT;
        }

        @Override
        public final Chart getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "font";
        }

    };
    /**
     * For alignment of the view within the {@code Chart} area.
     */
    private final StyleableObjectProperty<VIEWALIGNMENT> viewAlignment = new StyleableObjectProperty<VIEWALIGNMENT>(VIEWALIGNMENT.CENTER) {
        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "viewAlignment";
        }

        @Override
        public final CssMetaData<Chart, VIEWALIGNMENT> getCssMetaData() {
            return StyleableProperties.VIEWALIGN;
        }
    };

    StyleableBooleanProperty mousePositionDisplayed = new StyleableBooleanProperty(Boolean.FALSE) {
        @Override
        public final Object getBean() {
            return Chart.this;
        }

        @Override
        public final String getName() {
            return "mousePositionD=";
        }

        @Override
        public final CssMetaData<Chart, Boolean> getCssMetaData() {
            return StyleableProperties.MOUSEPOSITION;
        }
    };

    private CrossHair crossHair = null;
    SimpleDoubleProperty mouseX = new SimpleDoubleProperty(Double.NaN);
    SimpleDoubleProperty mouseY = new SimpleDoubleProperty(Double.NaN);

    /**
     * Default constructor.
     */
    public Chart() {
        super();

        getStyleClass().add("chart");

        yTol = new Tolerance("Y");
        xTol = new Tolerance("X");
        majorYInterval = MajorTickIntervalBinding.setupMajorYInterval(this, new StyleableDoubleProperty(Double.NaN) {

            @Override
            public final CssMetaData<Chart, Number> getCssMetaData() {
                return Chart.StyleableProperties.MAJORYINTERVAL;
            }

            @Override
            public final Object getBean() {
                return this;
            }

            @Override
            public final String getName() {
                return "majorY";
            }
        });

        majorXInterval = MajorTickIntervalBinding.setupMajorXInterval(this, new StyleableDoubleProperty(Double.NaN) {

            @Override
            public final CssMetaData<Chart, Number> getCssMetaData() {
                return StyleableProperties.MAJORXINTERVAL;
            }

            @Override
            public final Object getBean() {
                return this;
            }

            @Override
            public final String getName() {
                return "majorX";
            }
        });

        setPrefWidth(500d);
        setPrefHeight(500d);

        axesBounds = new AxesBounds();

        // Create a canvas that can be used to draw grids, axes etc behind added plots
        canvas = new Canvas(500d, 500d);
        // Put the canvas into a StackPane - this forms the "view" where charts will be drawn
        view = new StackPane(canvas);

        // Now creae a Pane that can be used to show axes alongside the charts
        axisPane = new Pane();

        //Add the view and axisPane to the chart....
        getChildren().add(view);
        getChildren().add(axisPane);
        //...and bind the axisPane to fill the chart Pane
        axisPane.setLayoutX(0d);
        axisPane.setLayoutY(0d);
        axisPane.prefHeightProperty().bind(prefHeightProperty());
        axisPane.prefWidthProperty().bind(prefWidthProperty());
        // Pick on bounds should be false
        axisPane.setPickOnBounds(false);

        // Constrain the size of the view and canvas within it.
        // This will be done again on each layout pass to support resizing.
        setPadding(computeRequiredInsets());
        view.setLayoutX(50d);
        view.setLayoutY(50d);
        view.setPrefHeight(getPrefHeight() - getPadding().getTop() - getPadding().getBottom());
        view.setPrefWidth(getPrefWidth() - getPadding().getLeft() - getPadding().getRight());
        view.setCenterShape(true);
        view.setCursor(Cursor.DEFAULT);
        canvas.setLayoutX(0d);
        canvas.setLayoutY(0d);
        canvas.setHeight(view.getPrefHeight());
        canvas.setWidth(view.getPrefWidth());

        // Populate the axisPane with the axes, and add them to the axisSet
        axisRight = new AxisRight(this);
        axisTop = new AxisTop(this);
        axisLeft = new AxisLeft(this);
        axisBottom = new AxisBottom(this);
        axisPane.getChildren().add(axisTop);
        axisPane.getChildren().add(axisRight);
        axisPane.getChildren().add(axisLeft);
        axisPane.getChildren().add(axisBottom);
        axisSet = new AxisSet(axisRight, axisTop, axisLeft, axisBottom);

        installMouseListeners();

        // Add the scene dimension listener
        ChangeListener<Scene> addedToScene = (ObservableValue<? extends Scene> ov, Scene t, Scene t1) -> {
            // Do this only of a Chart has been set as the root element.
            // This sets the Chart to resize with the scene.
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
        xOriginProperty().addListener(axisLimitListener);
        yOriginProperty().addListener(axisLimitListener);
        // Also, bind to changes in the width/height as we need to recalculate
        // grid, tick  etc. pixel positions.
        widthProperty().addListener(axisLimitListener);
        heightProperty().addListener(axisLimitListener);
        prefWidthProperty().addListener(axisLimitListener);
        prefHeightProperty().addListener(axisLimitListener);

        /**
         * Create and add a listener to redo the layout on resizing
         */
        ChangeListener<Number> sizeListener = (ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            Platform.runLater(() -> {
                requestLayout();
            });
        };
        widthProperty().addListener(sizeListener);
        heightProperty().addListener(sizeListener);
        prefWidthProperty().addListener(sizeListener);
        prefHeightProperty().addListener(sizeListener);

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

                    // It makes no sense to have a background on the child as it will
                    // obscure the parent's contents.
                    ((Chart) node).setStyle("-fx-background-color: transparent");

                    AbstractAxisRegion axis;
                    axis = ((Chart) node).getAxisSet().getLeftAxis();
                    ((Chart) node).axisPane.getChildren().remove(axis);
                    axisPane.getChildren().add(axis);

                    axis = ((Chart) node).getAxisSet().getRightAxis();
                    ((Chart) node).axisPane.getChildren().remove(axis);
                    axisPane.getChildren().add(axis);

                    axis = ((Chart) node).getAxisSet().getBottomAxis();
                    ((Chart) node).axisPane.getChildren().remove(axis);
                    axisPane.getChildren().add(axis);

                    axis = ((Chart) node).getAxisSet().getTopAxis();
                    ((Chart) node).axisPane.getChildren().remove(axis);
                    axisPane.getChildren().add(axis);

                    // Make child charts mouse transparent by default
                    ((Chart) node).setMouseTransparent(true);

                    // Make sure a child Chart shares insets, view position etc.
                    // with the parent.
                    setPadding(computeRequiredInsets());
                    ((Chart) node).setPadding(getPadding());

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
                requestLayout();
            }
        });

        ChangeListener<Boolean> axisPaintedListener = (ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            Platform.runLater(() -> {
                getChildren().stream().filter((node) -> (node instanceof Chart)).forEach((node) -> {
                    setPadding(computeRequiredInsets());
                    ((Chart) node).setPadding(getPadding());
                });
            });
        };
        rightAxisPaintedProperty().addListener(axisPaintedListener);
        leftAxisPaintedProperty().addListener(axisPaintedListener);
        topAxisPaintedProperty().addListener(axisPaintedListener);
        bottomAxisPaintedProperty().addListener(axisPaintedListener);

        requestLayout();

    }

    /**
     * Construct a new {@code Chart} with the supplied {@code Chart} as a child.
     *
     * See the {@code parentProperty()} listener in the null constructor to see
     * exactly how this is handled.
     *
     * @param layer the child {@code Chart}
     */
    public Chart(Chart layer) {
        this();
        // Note: this will trigger the {@code Chart} parentProperty() listener
        getChildren().add(layer);
        setPadding(computeRequiredInsets());
        layer.setPadding(getPadding());
    }

    private void installMouseListeners() {
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
            mouseX.set(m2.getX());
            mouseY.set(m2.getY());
            if (isInnerAxisPainted()) {
                double x = toPositionX(m2.getX());
                double y = toPositionY(m2.getY());
                if (atOrigin(x, y)) {
                    view.setCursor(Cursor.HAND);
                    return;
                } else if (onXAxis(y)) {
                    view.setCursor(Cursor.E_RESIZE);
                    return;
                } else if (onYAxis(x)) {
                    view.setCursor(Cursor.N_RESIZE);
                    return;
                }
            }

            if (isMousePositionDisplayed()) {
                view.setCursor(Cursor.CROSSHAIR);
            } else {
                view.setCursor(Cursor.DEFAULT);
            }

        });

        view.setOnMouseDragged((MouseEvent m4) -> {

            if (m4.isStillSincePress()) {
                return;
            }
            mouseX.set(m4.getX());
            mouseY.set(m4.getY());

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

        view.setOnMouseEntered((MouseEvent m4) -> {
            mouseX.set(m4.getX());
            mouseY.set(m4.getY());
            view.setCursor(Cursor.DEFAULT);
        });

        view.setOnMouseExited((MouseEvent m5) -> {
            mouseX.set(Double.NaN);
            mouseY.set(Double.NaN);
            view.setCursor(Cursor.DEFAULT);
        });

    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     * @since JavaFX 8.0
     */
    public final static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    public StyleableBooleanProperty mousePositionDisplay() {
        return mousePositionDisplayed;
    }

    public void setMousePositionDisplayed(boolean flag) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> {
                setMousePositionDisplayed(flag);
            });
            return;
        }
        mousePositionDisplayed.set(flag);
        if (flag == true) {
            if (crossHair == null) {
                crossHair = new CrossHair(this);
            }
            getView().getChildren().add(crossHair);
            if (xCursorText == null) {
                xPosText = new Text("X");
                xPosText.setTextOrigin(VPos.CENTER);
                xPosText.setStyle("-fx-font-size: 8pt; -fx-font-style: italic;");
                xPosText.getStyleClass().add("xpostext");
                xCursorText = new CursorTextBox(xPosText);
                xPosText.textProperty().bind(mouseTextBinding(0, this, mouseX));

                getChildren().add(xCursorText);
                xCursorText.layoutXProperty().bind(getView().layoutXProperty().add(mouseX).add(xCursorText.widthProperty().divide(2d).negate()));
                xCursorText.layoutYProperty().bind(getView().layoutYProperty()
                        .add(getView().heightProperty()
                                .add(xCursorText.heightProperty().divide(2d).negate())));
            }
            if (yCursorText == null) {
                yPosText = new Text();
                yPosText.setTextOrigin(VPos.CENTER);
                yPosText.setStyle("-fx-font-size: 8pt; -fx-font-style: italic;");
                yPosText.getStyleClass().add("ypostext");
                yCursorText = new CursorTextBox(yPosText);
                yPosText.textProperty().bind(mouseTextBinding(1, this, mouseY));
                getChildren().add(yCursorText);
                yCursorText.layoutXProperty().bind(getView().layoutXProperty()
                        .add(yCursorText.widthProperty().negate().add(5d)));
                yCursorText.layoutYProperty().bind(mouseY().add(getView().layoutYProperty().add(yCursorText.heightProperty().divide(2d).negate())));
            }
        } else {
            if (xCursorText != null) {
                getChildren().remove(xCursorText);
                xCursorText.layoutYProperty().unbind();
                xCursorText = null;
                xPosText = null;
            }
            if (yCursorText != null) {
                getChildren().remove(yCursorText);
                yCursorText.layoutXProperty().unbind();
                yCursorText.layoutYProperty().unbind();
                yCursorText = null;
                yPosText = null;
            }
            if (crossHair != null) {
                crossHair.unbind();
                getView().getChildren().remove(crossHair);
            }
        }
    }

    public boolean isMousePositionDisplayed() {
        return mousePositionDisplayed.get();
    }

    public SimpleDoubleProperty mouseX() {
        return mouseX;
    }

    public SimpleDoubleProperty mouseY() {
        return mouseY;
    }

    /**
     * {@code StyleableObjectProperty<Paint>} controlling the color of the inner
     * axes, {@literal i.e.} the axes painted to the {@code Canvas} of the view.
     *
     * @return the innerAxisColorProperty
     */
    public final StyleableObjectProperty<Paint> innerAxisColorProperty() {
        return innerAxisColor;
    }

    /**
     * Returns the color of the inner axes, {@literal i.e.} the axes painted to
     * the {@code Canvas} of the view.
     *
     * @return the color
     */
    public final Paint getInnerAxisColor() {
        return innerAxisColor.get();
    }

    /**
     * Sets the color of the inner axes, {@literal i.e.} the axes painted to the
     * {@code Canvas} of the view.
     *
     * @param color a {@code Paint} instance
     */
    public final void setInnerAxisColor(Paint color) {
        innerAxisColor.set(color);
    }

    /**
     * The viewAlignment positions the display of the view on the longest axis
     * of the chart when the viewAspectRatio is a non-zero, non-NaN value.
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
    public final VIEWALIGNMENT getViewAlignment() {
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
    public final void setViewAlignment(VIEWALIGNMENT val) {
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
    public final StyleableObjectProperty<VIEWALIGNMENT> viewAlignmentProperty() {
        return viewAlignment;
    }

    /**
     * Calculate the offsets for each axis to accommodate their contents
     */
    private void enforceMinimumOffsets() {
        if (getLayers().size() > 1) {
            double sumxl = getFirstLayer().xLeftOffset;
            if (getFirstLayer().isLeftAxisPainted() || getFirstLayer().isLeftAxisLabelled()) {
                sumxl += getFirstLayer().axisLeft.computePrefWidth(-1d);
            }
            for (int k = 1; k < getLayers().size(); k++) {
                if (getLayers().get(k).isLeftAxisPainted() || getLayers().get(k).isLeftAxisLabelled()) {
                    getLayers().get(k).xLeftOffset = sumxl;
                    if (getLayers().get(k).axisLeft != null) {
                        sumxl += getLayers().get(k).axisLeft.computePrefWidth(-1d);
                    }
                }
            }
            sumxl = getFirstLayer().xRightOffset;
            if (getFirstLayer().isRightAxisPainted() || getFirstLayer().isRightAxisLabelled()) {
                sumxl += getFirstLayer().axisRight.computePrefWidth(-1d);
            }
            for (int k = 1; k < getLayers().size(); k++) {
                if (getLayers().get(k).isRightAxisPainted() || getLayers().get(k).isRightAxisLabelled()) {
                    getLayers().get(k).xRightOffset = sumxl;
                    if (getLayers().get(k).axisRight != null) {
                        sumxl += getLayers().get(k).axisRight.computePrefWidth(-1d);
                    }
                }
            }
            sumxl = getFirstLayer().yBottomOffset;
            if (getFirstLayer().isBottomAxisPainted() || getFirstLayer().isBottomAxisLabelled()) {
                sumxl += getFirstLayer().axisBottom.computePrefHeight(-1d);
            }
            for (int k = 1; k < getLayers().size(); k++) {
                if (getLayers().get(k).isBottomAxisPainted() || getLayers().get(k).isBottomAxisLabelled()) {
                    getLayers().get(k).yBottomOffset = sumxl;
                    if (getLayers().get(k).axisBottom != null) {
                        sumxl += getLayers().get(k).axisBottom.computePrefHeight(-1d);
                    }
                }
            }
            sumxl = getFirstLayer().yTopOffset;
            if (getFirstLayer().isTopAxisPainted() || getFirstLayer().isTopAxisLabelled()) {
                sumxl += getFirstLayer().axisTop.computePrefHeight(-1d);
            }
            for (int k = 1; k < getLayers().size(); k++) {
                if (getLayers().get(k).isTopAxisPainted() || getLayers().get(k).isTopAxisLabelled()) {
                    getLayers().get(k).yTopOffset = sumxl;
                    if (getLayers().get(k).axisTop != null) {
                        sumxl += getLayers().get(k).axisTop.computePrefHeight(-1d);
                    }
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
    public final double computePrefWidth(double height) {
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
    public final double computePrefHeight(double width) {
        return getPadding().getTop() + getPadding().getBottom() + view.getHeight();
    }

    /**
     * Returns a set of insets with dimensions just adequate to accommodate the
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
                    if (g.isLeftAxisPainted() || g.isLeftAxisLabelled()) {
                        xl = Math.max(xl, g.axisLeft.computePrefWidth(-1d) + g.xLeftOffset);
                    }
                    if (g.isRightAxisPainted() || g.isRightAxisLabelled()) {
                        xr = Math.max(xr, g.axisRight.computePrefWidth(-1d) + g.xRightOffset);
                    }
                    if (g.isBottomAxisPainted() || g.isBottomAxisLabelled()) {
                        yb = Math.max(yb, g.axisBottom.computePrefHeight(-1d) + g.yBottomOffset);
                    }
                    if (g.isTopAxisPainted() || g.isTopAxisLabelled()) {
                        yt = Math.max(yt, g.axisTop.computePrefHeight(-1d) + g.yTopOffset);
                    }
                }
                if (!isLeftAxisPainted()) {
                    xl = 7d;
                }
                if (!isRightAxisPainted()) {
                    xr = 7d;
                }
                if (!isTopAxisPainted()) {
                    yt = 7d;
                }
                if (!isBottomAxisPainted()) {
                    yb = 7d;
                }
                return new Insets(yt, xr, yb, xl);
            }
        }
    }

    /**
     *
     * @return the DoubleProperty wrapping the size of Font used to label the
     * inner axes
     */
    public final StyleableDoubleProperty innerAxisFontSizeProperty() {
        return innerAxisFontSize;
    }

    /**
     *
     * @return size of Font used to label the inner axes
     */
    public final double getInnerAxisFontSize() {
        return innerAxisFontSize.get();
    }

    /**
     * Sets the font size used to label the inner axes.
     *
     * @param val size of Font used to label the inner axes
     */
    public final void setInnerAxisFontSize(double val) {
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
    public final StyleableObjectProperty<Font> fontProperty() {
        return fontProperty;
    }

    /**
     * Returns the TRANSFORMTYPE for the x-axis.
     *
     * @return the TRANSFORMTYPE
     */
    public final TRANSFORMTYPE getXTransformType() {
        return xTransformType.get();
    }

    /**
     * Sets the TRANSFORMTYPE for the x-axis.
     *
     * @param type
     */
    public final void setXTransformType(TRANSFORMTYPE type) {
        axisSet.setXTransform(getTransformForType(type));
        xTransformType.set(type);
    }

    /**
     * ObjectProperty for the TRANSFORMTYPE of the x-axis.
     *
     * @return the property
     */
    public final StyleableObjectProperty<TRANSFORMTYPE> xTransformTypeProperty() {
        return xTransformType;
    }

    /**
     * Returns the TRANSFORMTYPE for the y-axis.
     *
     * @return the TRANSFORMTYPE
     */
    public final TRANSFORMTYPE getYTransformType() {
        return yTransformType.get();
    }

    /**
     * Sets the TRANSFORMTYPE for the y-axis.
     *
     * @param type
     */
    public final void setYTransformType(TRANSFORMTYPE type) {
        axisSet.setYTransform(getTransformForType(type));
        yTransformType.set(type);
    }

    /**
     * ObjectProperty for the TRANSFORMTYPE of the y-axis.
     *
     * @return the property
     */
    public final StyleableObjectProperty<TRANSFORMTYPE> yTransformTypeProperty() {
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
    public final double getViewAspectRatio() {
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
    public final void setViewAspectRatio(double val) {
        if (!viewAspectRatio.isBound()) {
            viewAspectRatio.set(val);
        }
    }

    /**
     * Returns the viewAspectRatio property.
     *
     * @return the property
     */
    public final StyleableDoubleProperty viewAspectRatioProperty() {
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
    public final Chart getFirstLayer() {
        if (getParent() != null && getParent() instanceof Chart) {
            return ((Chart) getParent()).getFirstLayer();
        } else {
            return this;
        }
    }

//    /**
//     *
//     */
//    public final Chart getParentChart() {
//        if (getParent() == null) {
//            return null;
//        } else {
//            return (getParent() instanceof Chart) ? (Chart) getParent() : null;
//        }
//    }
    /**
     * Returns an {@literal ArrayList<Chart>} with references to the layers of
     * this chart. For a single-layered chart, this will contain only one
     * element: i.e. a reference to the Chart instance the method was called on.
     *
     * @return an {@literal ArrayList<Chart>} with references to the layers
     */
    public final ArrayList<Chart> getLayers() {
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
    public final void layoutChildren() {
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

        axisTop.setLayoutX(view.getLayoutX());
        axisTop.setLayoutY(view.getLayoutY() - yTopOffset - axisTop.computePrefHeight(-1d));
        axisTop.setPrefHeight(Region.USE_COMPUTED_SIZE);
        axisTop.requestLayout();

        axisBottom.setLayoutX(view.getLayoutX());
        axisBottom.setLayoutY(view.getLayoutY() + view.getPrefHeight() + yBottomOffset);
        axisBottom.setPrefHeight(Region.USE_COMPUTED_SIZE);
        axisBottom.requestLayout();

        axisLeft.setLayoutX(view.getLayoutX() - axisLeft.computePrefWidth(-1d) - xLeftOffset);
        axisLeft.setLayoutY(view.getLayoutY());
        axisLeft.setPrefWidth(Region.USE_COMPUTED_SIZE);
        axisLeft.requestLayout();

        axisRight.setLayoutX(view.getLayoutX() + view.getPrefWidth() + xRightOffset);
        axisRight.setLayoutY(view.getLayoutY());
        axisRight.setPrefWidth(Region.USE_COMPUTED_SIZE);
        axisRight.requestLayout();

//        xPosText.textProperty().get();
//        yPosText.getText();
//        if (isMousePositionDisplayed()) {
//            if (xCursorText != null && Double.isFinite(mouseX().get())) {
//                Point2D p = view.localToParent(mouseX().get(), -1);
//                //xPosText.xProperty().set(p.getX() + 10d);
//                xPosText.textProperty().set(formatter.format(toPositionX(mouseX.get())));
//                //xCursorText.setVisible(true);
//            } else {
//                xPosText.textProperty().set("");
//                //xCursorText.setVisible(false);
//            }
//            if (yCursorText != null && Double.isFinite(mouseY().get())) {
//                yPosText.textProperty().set(formatter.format(toPositionY(mouseY.get())));
//                //yCursorText.setVisible(true);
//            } else {
//                yPosText.textProperty().set("");
//                //yCursorText.setVisible(false);
//            }
//        }
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
     * {@code StyleableDoubleProperty} containing the value on the y-axis at the
     * bottom of the view.
     *
     * @return the yBottomProperty
     */
    public final StyleableDoubleProperty yBottomProperty() {
        return yBottom;
    }

    /**
     * {@code StyleableDoubleProperty} containing the value on the x-axis at the
     * left of the view.
     *
     * @return the xLeftProperty
     */
    public final StyleableDoubleProperty xLeftProperty() {
        return xLeft;
    }

    /**
     * {@code StyleableDoubleProperty} containing the value on the x-axis at the
     * right of the view.
     *
     * @return the xRightProperty
     */
    public final StyleableDoubleProperty xRightProperty() {
        return xRight;
    }

    /**
     * {@code StyleableDoubleProperty} containing the value on the y-axis at the
     * top of the view.
     *
     * @return the yTopProperty
     */
    public final StyleableDoubleProperty yTopProperty() {
        return yTop;
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
     * Returns the x-axis value at the right of the view.
     *
     * @return the value
     */
    public final double getXRight() {
        return xRight.doubleValue();
    }

    /**
     * Sets the x-axis value at the right of the view.
     *
     * @param value the value to use
     */
    public final void setXRight(double value) {
        xRight.set(value);
    }

    /**
     * Returns the y-axis value at the bottom of the view.
     *
     * @return the value
     */
    public final double getYBottom() {
        return yBottom.doubleValue();
    }

    /**
     * Sets the y-axis value at the bottom of the view.
     *
     * @param val the value
     */
    public final void setYBottom(double val) {
        yBottom.set(val);
    }

    /**
     * Returns the y-axis value at the top of the view.
     *
     * @return the value
     */
    public final double getYTop() {
        return yTop.doubleValue();
    }

    /**
     * Sets the y-axis value at the top of the view.
     *
     * @param val the value
     */
    public final void setYTop(double val) {
        yTop.set(val);
    }

    /**
     * Returns the {@code StackPane} that forms the view of this {@code Chart}.
     *
     * @return the view
     */
    public final StackPane getView() {
        return view;
    }

    /**
     * Returns the {@code Canvas} from the view of this {@code Chart}.
     *
     * @return the canvas
     */
    public final Canvas getCanvas() {
        return canvas;
    }

    /**
     * An {@code Rectangle2D} containing the axes limits.
     *
     * @return the limits.
     */
    public final Rectangle2D getAxesBounds() {
        return axesBounds.get();
    }

    /**
     * Returns true if the left-most x-axis value is larger than the right-most.
     *
     * @return the boolean
     */
    public final boolean isReverseX() {
        return getXLeft() > getXRight();
    }

    /**
     * Flips the left and right extremes of the x-axis, if required.
     *
     * @param flag true to reverse the axes, false otherwise.
     */
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
     * Returns true if the top-most y-axis value is larger than the bottom-most.
     *
     * @return the boolean
     */
    public final boolean isReverseY() {
        return getYBottom() > getYTop();
    }

    /**
     * Flips the upper and lower extremes of the y-axis, if required.
     *
     * @param flag true to reverse the axes, false otherwise.
     */
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

    /**
     * The interval between major tick marks and grid lines in chart coordinate
     * space on the x-axis. This is calculated automatically unless the user
     * enforces a specified value.
     *
     * @return the interval.
     */
    public final double getMajorXInterval() {
        return majorXInterval.get();
    }

    /**
     * Sets the interval between major tick marks and grid lines in chart
     * coordinate space on the x-axis and supresses automatic calculation of
     * that value.
     *
     * To restore automatic calculation, set majorX to Double.NaN.
     *
     * @param majorX the interval to use
     */
    public final void setMajorXInterval(double majorX) {
        if (majorX <= 0.0) {
            majorX = 1e-15;
        }
        majorXInterval.getUserSpecifiedValue().set(majorX);
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

    /**
     * Sets the interval between major tick marks and grid lines in chart
     * coordinate space on the y-axis and supresses automatic calculation of
     * that value.
     *
     * To restore automatic calculation, set majorX to Double.NaN.
     *
     * @param majorY the interval to use
     */
    public final void setMajorYInterval(double majorY) {
        if (majorY <= 0.0) {
            majorY = 1e-15;
        }
        majorYInterval.getUserSpecifiedValue().set(majorY);
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
        return minorCountXHint.get();
    }

    /**
     * Sets a hint for the number of minor ticks/grids to paint between major
     * ticks/grids on the x-axis.
     *
     * @param minorCountX the number to paint
     */
    public final void setMinorCountXHint(int minorCountX) {
        if (minorCountX < 1) {
            return;
        }
        minorCountXHint.set(minorCountX);
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
        return minorCountYHint.get();
    }

    /**
     * Sets a hint for the number of minor ticks/grids to paint between major
     * ticks/grids on the y-axis.
     *
     * @param minorCountY the number to paint
     */
    public final void setMinorCountYHint(int minorCountY) {
        if (minorCountY < 1) {
            return;
        }
        minorCountYHint.set(minorCountY);
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

    /**
     * Returns a {@code StyleableDoubleProperty} for the origin of the x-axis
     * (i.e where it intersects the y-axis).
     *
     * @return the property
     */
    public final StyleableDoubleProperty xOriginProperty() {
        return xOrigin;
    }

    /**
     * Returns the value for the origin of the x-axis (i.e where it intersects
     * the y-axis).
     *
     * @return the value
     */
    public final double getXOrigin() {
        return xOrigin.doubleValue();
    }

    /**
     * Sets the value for the origin of the x-axis (i.e where it intersects the
     * y-axis).
     *
     * @param val the value
     */
    public final void setXOrigin(double val) {
        xOrigin.set(val);
    }

    /**
     * Returns a {@code StyleableDoubleProperty} for the origin of the y-axis
     * (i.e where it intersects the x-axis).
     *
     * @return the property
     */
    public final StyleableDoubleProperty yOriginProperty() {
        return yOrigin;
    }

    /**
     * Returns the value for the origin of the y-axis (i.e where it intersects
     * the x-axis).
     *
     * @return the value
     */
    public final double getYOrigin() {
        return yOrigin.doubleValue();
    }

    /**
     * Sets the value for the origin of the y-axis (i.e where it intersects the
     * x-axis).
     *
     * @param val the value
     */
    public final void setYOrigin(double val) {
        yOrigin.set(val);
    }

    /**
     * Returns a {@code StyleableDoubleProperty} for the stroke width of the
     * inner axes, {@literal i.e.} the axes painted in the {@code Canvas} within
     * the view.
     *
     * @return the property
     */
    public final StyleableDoubleProperty innerAxisStrokeWidthProperty() {
        return innerAxisStrokeWidth;
    }

    /**
     * Returns the value for the stroke width of the inner axes, {@literal i.e.}
     * the axes painted in the {@code Canvas} within the view.
     *
     * @return the value
     */
    public final double getInnerAxisStrokeWidth() {
        return innerAxisStrokeWidth.doubleValue();
    }

    /**
     * Sets the value for the stroke width of the inner axes, {@literal i.e.}
     * the axes painted in the {@code Canvas} within the view.
     *
     * @param val the value
     */
    public final void setInnerAxisStrokeWidth(double val) {
        innerAxisStrokeWidth.set(val);
    }

    /**
     * Returns a {@code StyleableDoubleProperty} for the stroke width of the
     * axes, {@literal i.e.} the axes painted in the sides of the view.
     *
     * @return the property
     */
    public final StyleableDoubleProperty axisStrokeWidthProperty() {
        return axisStrokeWidth;
    }

    /**
     * Returns a value for the stroke width of the axes, {@literal i.e.} the
     * axes painted in the sides of the view
     *
     * @return the value
     */
    public final double getAxisStrokeWidth() {
        return axisStrokeWidth.doubleValue();
    }

    /**
     * Sets the value for the stroke width of the axes, {@literal i.e.} the axes
     * painted in the sides of the view
     *
     * @param val the value
     */
    public final void setAxisStrokeWidth(double val) {
        axisStrokeWidth.set(val);
    }

    /**
     * Returns a {@code StyleableDoubleProperty} for the minor grid stroke
     * width.
     *
     * @return the property
     */
    public final StyleableDoubleProperty minorGridStrokeWidthProperty() {
        return minorGridStrokeWidth;
    }

    public final double getMinorGridStrokeWidth() {
        return minorGridStrokeWidth.doubleValue();
    }

    public final void setMinorGridStrokeWidth(double val) {
        minorGridStrokeWidth.set(val);
    }

    /**
     * Returns a {@code StyleableDoubleProperty} for the major grid stroke
     * width.
     *
     * @return the property
     */
    public final StyleableDoubleProperty majorGridStrokeWidthProperty() {
        return majorGridStrokeWidth;
    }

    public final double getMajorGridStrokeWidth() {
        return majorGridStrokeWidth.doubleValue();
    }

    public final void setMajorGridStrokeWidth(double val) {
        majorGridStrokeWidth.set(val);
    }

    /**
     * Returns a {@code StyleableBooleanProperty} for the minor grid. If true,
     * the grid will be painted.
     *
     * @return the property
     */
    public final StyleableBooleanProperty minorGridPaintedProperty() {
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

    /**
     * Returns a {@code StyleableBooleanProperty} for the major grid. If true,
     * the grid will be painted.
     *
     * @return the property
     */
    public final StyleableBooleanProperty majorGridPaintedProperty() {
        return majorGridPainted;
    }

    public final boolean isMajorGridPainted() {
        return majorGridPainted.get();
    }

    public final void setMajorGridPainted(boolean val) {
        majorGridPainted.set(val);
    }

    /**
     * Returns a {@code StyleableBooleanProperty} for the inner axis. If true,
     * the axis will be painted.
     *
     * @return the property
     */
    public final StyleableBooleanProperty innerAxisPaintedProperty() {
        return innerAxisPainted;
    }

    public final boolean isInnerAxisPainted() {
        return innerAxisPainted.get();
    }

    public final void setInnerAxisPainted(boolean val) {
        innerAxisPainted.set(val);
    }

    /**
     * Returns a {@code StyleableBooleanProperty} for the inner axes labelling.
     * If true, the axis will be labelled.
     *
     * @return the property
     */
    public final StyleableBooleanProperty innerAxisLabelledProperty() {
        return innerAxisLabelled;
    }

    public final boolean isInnerAxisLabelled() {
        return innerAxisLabelled.get();
    }

    public final void setInnerAxisLabelled(boolean val) {
        innerAxisLabelled.set(val);
    }

    /**
     * Returns a {@code StyleableBooleanProperty} for the ledt axis. If true,
     * the axis will be painted.
     *
     * @return the property
     */
    public final StyleableBooleanProperty leftAxisPaintedProperty() {
        return leftAxisPainted;
    }

    public final boolean isLeftAxisPainted() {
        return leftAxisPainted.get();
    }

    public final void setLeftAxisPainted(boolean val) {
        leftAxisPainted.set(val);
    }

    /**
     * Returns a {@code StyleableBooleanProperty} for the right axis. If true,
     * the axis will be painted.
     *
     * @return the property
     */
    public final StyleableBooleanProperty rightAxisPaintedProperty() {
        return rightAxisPainted;
    }

    public final boolean isRightAxisPainted() {
        return rightAxisPainted.get();
    }

    public final void setRightAxisPainted(boolean val) {
        rightAxisPainted.set(val);
    }

    /**
     * Returns a {@code StyleableBooleanProperty} for the top axis. If true, the
     * axis will be painted.
     *
     * @return the property
     */
    public final StyleableBooleanProperty topAxisPaintedProperty() {
        return topAxisPainted;
    }

    public final boolean isTopAxisPainted() {
        return topAxisPainted.get();
    }

    public final void setTopAxisPainted(boolean val) {
        topAxisPainted.set(val);
    }

    /**
     * Returns a {@code StyleableBooleanProperty} for the bottom axis. If true,
     * the axis will be painted.
     *
     * @return the property
     */
    public final StyleableBooleanProperty bottomAxisPaintedProperty() {
        return bottomAxisPainted;
    }

    public final boolean isBottomAxisPainted() {
        return bottomAxisPainted.get();
    }

    public final void setBottomAxisPainted(boolean val) {
        bottomAxisPainted.set(val);
    }

    /**
     * Returns a {@code StyleableBooleanProperty} for the left axis labels. If
     * true, the labels will be painted.
     *
     * @return the property
     */
    public final StyleableBooleanProperty leftAxisLabelledProperty() {
        return leftAxisLabelled;
    }

    public final boolean isLeftAxisLabelled() {
        return leftAxisLabelled.get();
    }

    public final void setLeftAxisLabelled(boolean val) {
        leftAxisLabelled.set(val);
    }

    /**
     * Returns a {@code StyleableBooleanProperty} for the right axis labels. If
     * true, the labels will be painted.
     *
     * @return the property
     */
    public final StyleableBooleanProperty rightAxisLabelledProperty() {
        return rightAxisLabelled;
    }

    public final boolean isRightAxisLabelled() {
        return rightAxisLabelled.get();
    }

    public final void setRightAxisLabelled(boolean val) {
        rightAxisLabelled.set(val);
    }

    /**
     * Returns a {@code StyleableBooleanProperty} for the top axis labels. If
     * true, the labels will be painted.
     *
     * @return the property
     */
    public final StyleableBooleanProperty topAxisLabelledProperty() {
        return topAxisLabeled;
    }

    public final boolean isTopAxisLabelled() {
        return topAxisLabeled.get();
    }

    public final void setTopAxisLabelled(boolean val) {
        topAxisLabeled.set(val);
    }

    /**
     * Returns a {@code StyleableBooleanProperty} for the bottom axis labels. If
     * true, the labels will be painted.
     *
     * @return the property
     */
    public final StyleableBooleanProperty bottomAxisLabelledProperty() {
        return bottomAxisLabelled;
    }

    public final boolean isBottomAxisLabelled() {
        return bottomAxisLabelled.get();
    }

    public final void setBottomAxisLabelled(boolean val) {
        bottomAxisLabelled.set(val);
    }

    public final String getLeftAxisTitle() {
        return axisLeft.getAxisLabel().getText();
    }

    /**
     * Sets the title for the left axis.
     *
     * @param s the title
     */
    public final void setLeftAxisTitle(String s) {
        axisLeft.getAxisLabel().setText(s);
    }

    public final String getBottomAxisTitle() {
        return axisBottom.getAxisLabel().getText();
    }

    /**
     * Sets the title for the bottom axis.
     *
     * @param s the title
     */
    public final void setBottomAxisTitle(String s) {
        axisBottom.getAxisLabel().setText(s);
    }

    public final String getRightAxisTitle() {
        return axisRight.getAxisLabel().getText();
    }

    /**
     * Sets the title for the right axis.
     *
     * @param s the title
     */
    public final void setRightAxisTitle(String s) {
        axisRight.getAxisLabel().setText(s);
    }

    public final String getTopAxisTitle() {
        return axisTop.getAxisLabel().getText();
    }

    /**
     * Sets the title for the top axis.
     *
     * @param s the title
     */
    public final void setTopAxisTitle(String s) {
        axisTop.getAxisLabel().setText(s);
    }

    /**
     * Establishes a bi-directional binding between the x-axis limits of this
     * chart and x-axis limits of the specified chart.
     *
     * @param chart
     */
    public final void addAxisLinkXX(Chart chart) {
        xLeft.bindBidirectional(chart.xLeft);
        xRight.bindBidirectional(chart.xRight);
    }

    /**
     * Establishes a bi-directional binding between the x-axis limits of this
     * chart and y-axis limits of the specified chart.
     *
     * @param chart
     */
    public final void addAxisLinkXY(Chart chart) {
        xLeft.bindBidirectional(chart.yBottom);
        xRight.bindBidirectional(chart.yTop);
    }

    /**
     * Establishes a bi-directional binding between the y-axis limits of this
     * chart and y-axis limits of the specified chart.
     *
     * @param chart
     */
    public final void addAxisLinkYY(Chart chart) {
        yBottom.bindBidirectional(chart.yBottom);
        yTop.bindBidirectional(chart.yTop);
    }

    /**
     * Establishes a bi-directional binding between the y-axis limits of this
     * chart and x-axis limits of the specified chart.
     *
     * @param chart
     */
    public final void addAxisLinkYX(Chart chart) {
        yBottom.bindBidirectional(chart.xLeft);
        yTop.bindBidirectional(chart.xRight);
    }

    /**
     * Removes a bi-directional binding between the x-axis limits of this chart
     * and x-axis limits of the specified chart.
     *
     * @param chart
     */
    public final void removeAxisLinkXX(Chart chart) {
        xLeft.unbindBidirectional(chart.xLeft);
        xRight.unbindBidirectional(chart.xRight);
    }

    /**
     * Removes a bi-directional binding between the x-axis limits of this chart
     * and y-axis limits of the specified chart.
     *
     * @param chart
     */
    public final void removeAxisLinkXY(Chart chart) {
        xLeft.unbindBidirectional(chart.yBottom);
        xRight.unbindBidirectional(chart.yTop);
    }

    /**
     * Removes a bi-directional binding between the y-axis limits of this chart
     * and y-axis limits of the specified chart.
     *
     * @param chart
     */
    public final void removeAxisLinkYY(Chart chart) {
        yBottom.unbindBidirectional(chart.yBottom);
        yTop.unbindBidirectional(chart.yTop);
    }

    /**
     * Removes a bi-directional binding between the y-axis limits of this chart
     * and x-axis limits of the specified chart.
     *
     * @param chart
     */
    public final void removeAxisLinkYX(Chart chart) {
        yBottom.unbindBidirectional(chart.xLeft);
        yTop.unbindBidirectional(chart.xRight);
    }

    /**
     * Returns a {@code StyleableBooleanProperty} for polar coordinates. If
     * true, polar coordinates will be used for drawing.
     *
     * @return the property
     */
    public final StyleableBooleanProperty polarProperty() {
        return polar;
    }

    public final boolean isPolar() {
        return polar.get();
    }

    public final void setPolar(boolean val) {
        polar.set(val);
    }

    /**
     * Returns a {@code StyleableObjectProperty<Paint>} object for the color of
     * the major grid lines
     *
     * @return the property
     */
    public final StyleableObjectProperty<Paint> majorGridColorProperty() {
        return majorGridColor;
    }

    public final Paint getMajorGridColor() {
        return majorGridColor.get();
    }

    public final void setMajorGridColor(Paint val) {
        majorGridColor.set(val);
    }

    /**
     * Returns a {@code StyleableObjectProperty<Paint>} object for the color of
     * the minor grid lines
     *
     * @return the property
     */
    public final StyleableObjectProperty<Paint> minorGridColorProperty() {
        return minorGridColor;
    }

    public final Paint getMinorGridColor() {
        return minorGridColor.get();
    }

    public final void setMinorGridColor(Paint val) {
        minorGridColor.set(val);
    }

    /**
     * Returns a {@code StyleableObjectProperty<Paint>} object for the color of
     * the axes associated with this {@code Chart}
     *
     * @return the property
     */
    public final StyleableObjectProperty<Paint> axisColorProperty() {
        return axisColor;
    }

    public final Paint getAxisColor() {
        return axisColor.get();
    }

    public final void setAxisColor(Paint val) {
        axisColor.set(val);
    }

    /**
     * Property wrapping the Paint instance to use when filling alternate
     * vertical major grid divisions.
     *
     * @return the property.
     */
    public final StyleableObjectProperty<Paint> altFillVerticalProperty() {
        return altFillVertical;
    }

    /**
     * @return the alternateBackground
     */
    public final Paint getAltFillVertical() {
        return altFillVertical.get();
    }

    /**
     * @param alternateBackground the alternateBackground to getParent
     */
    public final void setAltFillVertical(Paint alternateBackground) {
        altFillVertical.set(alternateBackground);
    }

    public StyleableBooleanProperty altFillVerticalPainted() {
        return altFillVerticalPainted;
    }

    public void setAltFillVerticalPainted(boolean flag) {
        altFillVerticalPainted.set(flag);
    }

    public boolean isAltFillVerticalPainted() {
        return altFillVerticalPainted.get();
    }

    public StyleableBooleanProperty altFillHorizontalPainted() {
        return altFillHorizontalPainted;
    }

    public void setAltFillHorizontalPainted(boolean flag) {
        altFillHorizontalPainted.set(flag);
    }

    public boolean isAltFillHorizontalPainted() {
        return altFillHorizontalPainted.get();
    }

    /**
     * Property wrapping the Paint instance to use when filling alternate
     * horizontal major grid divisions.
     *
     * @return the property.
     */
    public final StyleableObjectProperty<Paint> altFillHorizontalProperty() {
        return altFillHorizontal;
    }

    /**
     * Returns the Paint instance to used filling alternate horizontal major
     * grid divisions.
     *
     * @return the Paint instance.
     */
    public final Paint getAltFillHorizontal() {
        return altFillHorizontal.get();
    }

    /**
     * Sets the Paint instance to used filling alternate horizontal major grid
     * divisions.
     *
     * @param alternateBackground the Paint instance
     */
    public final void setAltFillHorizontal(Paint alternateBackground) {
        altFillHorizontal.set(alternateBackground);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     * @since JavaFX 8.0
     */
    @Override
    public final List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
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
        return onXAxis(y) && onYAxis(x);
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

    /**
     * Requests that the {@code Chart} is repainted.
     *
     */
    final void requestPaint() {
        requestLayout();
        axisSet.paintGrid(canvas.getGraphicsContext2D());
    }

    /**
     * Returns the position in chart coordinates on the x-axis given a pixel
     * location in the {@code Chart}
     *
     * @param pixel a pixel location
     * @return the x position in chart coordinates
     */
    public final double toPositionX(double pixel) {
        return getXLeft() + pixel * (getXRight() - getXLeft()) / view.getWidth();
    }

    /**
     * Returns the position in chart coordinates on the y-axis given a pixel
     * location in the {@code Chart}
     *
     * @param pixel a pixel location
     * @return the y position in chart coordinates
     */
    public final double toPositionY(double pixel) {
        return getYBottom() + (view.getHeight() - pixel) * (getYTop() - getYBottom()) / view.getHeight();
    }

    public final double toPixelX(double x) {
        return (x - getXLeft()) * getView().getWidth() / (getXRight() - getXLeft());
    }

    public final double toPixelY(double y) {
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
    public final Point2D toPixel(double x, double y) {
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
    public final Point2D toPixel(Point2D p) {
        return new Point2D(toPixelX(p.getX()), toPixelY(p.getY()));
    }

    public final double getPixelWidth() {
        return (getXMax() - getXMin()) / view.getWidth();
    }

    public final double getPixelHeight() {
        return (getYMax() - getYMin()) / view.getHeight();
    }

    public final ArrayList<Point2D> toPixel(Collection<Point2D> p) {
        ArrayList<Point2D> val = new ArrayList<>();
        p.stream().forEach((Point2D x) -> {
            val.add(toPixel(x));
        });
        return val;
    }

    public final Point2D pixelToPos(Point2D p) {
        return toPixel(axisSet.getInverse(p.getX(), p.getY()));

    }

    /**
     * @return the axisFontSize
     */
    public final double getAxisFontSize() {
        return axisFontSize.get();
    }

    /**
     * @param value the axisFontSize to set
     */
    public final void setAxisFontSize(double value) {
        this.axisFontSize.set(value);
    }

    public final StyleableDoubleProperty axisFontSizeProperty() {
        return axisFontSize;

    }

    /**
     * @return the axisPane
     */
    public final Pane getAxisPane() {
        return axisPane;
    }

    /**
     * @return the formatter
     */
    public static final NumberFormat getFormatter() {
        return formatter;
    }

    /**
     * @param formatter the formatter to set
     */
    public static final void setFormatter(NumberFormat formatter) {
        Chart.formatter = formatter;

    }

    public static enum TRANSFORMTYPE {

        LINEAR, LOG, LOG10, LOG2
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

    /**
     * @treatAsPrivate implementation detail
     */
    private static class StyleableProperties {

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private static final CssMetaData<Chart, Number> XLEFT
                = new CssMetaData<Chart, Number>("-w-xleft",
                        StyleConverter.getSizeConverter(), -1) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.xLeft != null && !n.xLeft.isBound();
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.xLeft;
                    }
                };
        private static final CssMetaData<Chart, Number> XRIGHT
                = new CssMetaData<Chart, Number>("-w-xright",
                        StyleConverter.getSizeConverter(), 1) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.xRight != null && !n.xRight.isBound();
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.xRight;
                    }
                };
        private static final CssMetaData<Chart, Number> YBOTTOM
                = new CssMetaData<Chart, Number>("-w-ybottom",
                        StyleConverter.getSizeConverter(), -1) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.yBottom != null && !n.yBottom.isBound();
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.yBottom;
                    }
                };
        private static final CssMetaData<Chart, Number> YTOP
                = new CssMetaData<Chart, Number>("-w-ytop",
                        StyleConverter.getSizeConverter(), 1) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.yTop != null && !n.yTop.isBound();
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.yTop;
                    }
                };
        private static final CssMetaData<Chart, Number> MAJORXINTERVAL
                = new CssMetaData<Chart, Number>("-w-majorx",
                        StyleConverter.getSizeConverter(), Double.NaN) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.majorXInterval != null;
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.majorXInterval.getUserSpecifiedValue();
                    }
                };
        private static final CssMetaData<Chart, Number> MAJORYINTERVAL
                = new CssMetaData<Chart, Number>("-w-majory",
                        StyleConverter.getSizeConverter(), Double.NaN) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.majorYInterval != null;
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.majorYInterval.getUserSpecifiedValue();
                    }
                };
        private static final CssMetaData<Chart, Number> MINORCOUNTXHINT
                = new CssMetaData<Chart, Number>("-w-minor-count-x",
                        StyleConverter.getSizeConverter(), 4) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.minorCountXHint != null && !n.minorCountXHint.isBound();
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.minorCountXHint;
                    }
                };
        private static final CssMetaData<Chart, Number> MINORCOUNTYHINT
                = new CssMetaData<Chart, Number>("-w-minor-count-y",
                        StyleConverter.getSizeConverter(), 4) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.minorCountYHint != null && !n.minorCountYHint.isBound();
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.minorCountYHint;
                    }
                };
        private static final CssMetaData<Chart, Number> XORIGIN
                = new CssMetaData<Chart, Number>("-w-xorigin",
                        StyleConverter.getSizeConverter(), 0d) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.xOrigin != null && !n.xOrigin.isBound();
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.xOrigin;
                    }
                };
        private static final CssMetaData<Chart, Number> YORIGIN
                = new CssMetaData<Chart, Number>("-w-yorigin",
                        StyleConverter.getSizeConverter(), 0d) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.yOrigin != null && !n.yOrigin.isBound();
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.yOrigin;
                    }
                };
        private static final CssMetaData<Chart, Number> AXISSTROKEWIDTH
                = new CssMetaData<Chart, Number>("-w-axis-stroke-width",
                        StyleConverter.getSizeConverter(), 1d) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.axisStrokeWidth != null && !n.axisStrokeWidth.isBound();
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.axisStrokeWidth;
                    }
                };
        private static final CssMetaData<Chart, Number> INNERAXISSTROKEWIDTH
                = new CssMetaData<Chart, Number>("-w-inner-axis-stroke-width",
                        StyleConverter.getSizeConverter(), 1.5d) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.innerAxisStrokeWidth != null && !n.innerAxisStrokeWidth.isBound();
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.innerAxisStrokeWidth;
                    }
                };
        private static final CssMetaData<Chart, Number> MINORGRIDSTROKEWIDTH
                = new CssMetaData<Chart, Number>("-w-minor-grid-stroke-width",
                        StyleConverter.getSizeConverter(), 1.1) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.yTop != null && !n.yTop.isBound();
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.yTop;
                    }
                };
        private static final CssMetaData<Chart, Number> MAJORGRIDSTROKEWIDTH
                = new CssMetaData<Chart, Number>("-w-major-grid-stroke-width",
                        StyleConverter.getSizeConverter(), 1.3) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.yTop != null && !n.yTop.isBound();
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.yTop;
                    }
                };
        private static final CssMetaData<Chart, Boolean> MINORGRIDPAINTED
                = new CssMetaData<Chart, Boolean>("-w-minor-grid-painted",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.minorGridPainted != null && !n.minorGridPainted.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.minorGridPainted;
                    }
                };
        private static final CssMetaData<Chart, Boolean> MAJORGRIDPAINTED
                = new CssMetaData<Chart, Boolean>("-w-major-grid-painted",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.majorGridPainted != null && !n.majorGridPainted.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.majorGridPainted;
                    }
                };
        private static final CssMetaData<Chart, Boolean> INNERAXISPAINTED
                = new CssMetaData<Chart, Boolean>("-w-inner-axis-painted",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.innerAxisPainted != null && !n.innerAxisPainted.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.innerAxisPainted;
                    }
                };
        private static final CssMetaData<Chart, Number> INNERAXISFONTSIZE
                = new CssMetaData<Chart, Number>("-w-inner-axis-font-size",
                        StyleConverter.getSizeConverter(), Font.getDefault().getSize()) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.innerAxisFontSize != null && !n.innerAxisFontSize.isBound();
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.innerAxisFontSize;
                    }
                };
        private static final CssMetaData<Chart, Boolean> INNERAXISLABELLED
                = new CssMetaData<Chart, Boolean>("-w-inner-axis-labelled",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.innerAxisLabelled != null && !n.innerAxisLabelled.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.innerAxisLabelled;
                    }
                };
        private static final CssMetaData<Chart, Boolean> LEFTAXISPAINTED
                = new CssMetaData<Chart, Boolean>("-w-left-axis-painted",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.leftAxisPainted != null && !n.leftAxisPainted.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.leftAxisPainted;
                    }
                };
        private static final CssMetaData<Chart, Boolean> RIGHTAXISPAINTED
                = new CssMetaData<Chart, Boolean>("-w-right-axis-painted",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.rightAxisPainted != null && !n.rightAxisPainted.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.rightAxisPainted;
                    }
                };
        private static final CssMetaData<Chart, Boolean> TOPAXISPAINTED
                = new CssMetaData<Chart, Boolean>("-w-top-axis-painted",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.topAxisPainted != null && !n.topAxisPainted.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.topAxisPainted;
                    }
                };
        private static final CssMetaData<Chart, Boolean> BOTTOMAXISPAINTED
                = new CssMetaData<Chart, Boolean>("-w-bottom-axis-painted",
                        StyleConverter.getBooleanConverter(), Boolean.TRUE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.bottomAxisPainted != null && !n.bottomAxisPainted.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.bottomAxisPainted;
                    }
                };
        private static final CssMetaData<Chart, Boolean> LEFTAXISLABELLED
                = new CssMetaData<Chart, Boolean>("-w-left-axis-labelled",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.leftAxisLabelled != null && !n.leftAxisLabelled.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.leftAxisLabelled;
                    }
                };
        private static final CssMetaData<Chart, Boolean> BOTTOMAXISLABELLED
                = new CssMetaData<Chart, Boolean>("-w-bottom-axis-labelled",
                        StyleConverter.getBooleanConverter(), Boolean.TRUE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.bottomAxisLabelled != null && !n.bottomAxisLabelled.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.bottomAxisLabelled;
                    }
                };
        private static final CssMetaData<Chart, Boolean> TOPAXISLABELLED
                = new CssMetaData<Chart, Boolean>("-w-top-axis-labelled",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.topAxisLabeled != null && !n.topAxisLabeled.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.topAxisLabeled;
                    }
                };
        private static final CssMetaData<Chart, Boolean> RIGHTAXISLABELLED
                = new CssMetaData<Chart, Boolean>("-w-right-axis-labelled",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.rightAxisLabelled != null && !n.rightAxisLabelled.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.rightAxisLabelled;
                    }
                };
        private static final CssMetaData<Chart, Boolean> ALTFILLVERTICALPAINTED
                = new CssMetaData<Chart, Boolean>("-w-fill-vertical-painted",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.altFillVerticalPainted != null && !n.altFillVerticalPainted.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.altFillVerticalPainted;
                    }
                };
        private static final CssMetaData<Chart, Boolean> ALTFILLHORIZONTALPAINTED
                = new CssMetaData<Chart, Boolean>("-w-fill-horizontal-painted",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.altFillHorizontalPainted != null && !n.altFillHorizontalPainted.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.altFillHorizontalPainted;
                    }
                };
        private static final CssMetaData<Chart, Boolean> POLAR
                = new CssMetaData<Chart, Boolean>("-w-polar",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.polar != null && !n.polar.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.polar;
                    }
                };
        private static final CssMetaData<Chart, Paint> MAJORGRIDCOLOR
                = new CssMetaData<Chart, Paint>("-w-major-grid-color",
                        StyleConverter.getPaintConverter(), Color.SLATEBLUE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.majorGridColor != null && !n.majorGridColor.isBound();
                    }

                    @Override
                    public final StyleableProperty<Paint> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Paint>) n.majorGridColor;
                    }
                };
        private static final CssMetaData<Chart, Paint> MINORGRIDCOLOR
                = new CssMetaData<Chart, Paint>("-w-minor-grid-color",
                        StyleConverter.getPaintConverter(), Color.SLATEBLUE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.minorGridColor != null && !n.minorGridColor.isBound();
                    }

                    @Override
                    public final StyleableProperty<Paint> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Paint>) n.minorGridColor;
                    }
                };
        private static final CssMetaData<Chart, Font> FONT
                = new FontCssMetaData<Chart>("-w-font", Font.getDefault()) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.fontProperty != null && !n.fontProperty.isBound();
                    }

                    @Override
                    public final StyleableProperty<Font> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Font>) n.fontProperty;
                    }
                };
        private static final CssMetaData<Chart, Paint> AXISCOLOR
                = new CssMetaData<Chart, Paint>("-w-axis-color",
                        StyleConverter.getPaintConverter(), Color.BLACK) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.axisColor != null && !n.axisColor.isBound();
                    }

                    @Override
                    public final StyleableProperty<Paint> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Paint>) n.axisColor;
                    }
                };
        private static final CssMetaData<Chart, Paint> INNERAXISCOLOR
                = new CssMetaData<Chart, Paint>("-w-inner-axis-color",
                        StyleConverter.getPaintConverter(), Color.BLACK) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.innerAxisColor != null && !n.innerAxisColor.isBound();
                    }

                    @Override
                    public final StyleableProperty<Paint> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Paint>) n.innerAxisColor;
                    }
                };
        private static final CssMetaData<Chart, Paint> ALTFILLVERTICAL
                = new CssMetaData<Chart, Paint>("-w-alt-fill-vertical",
                        StyleConverter.getPaintConverter(), altFillColor) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.altFillVertical != null && !n.altFillVertical.isBound();
                    }

                    @Override
                    public final StyleableProperty<Paint> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Paint>) n.altFillVertical;
                    }
                };
        private static final CssMetaData<Chart, Paint> ALTFILLHORIZONTAL
                = new CssMetaData<Chart, Paint>("-w-alt-fill-horizontal",
                        StyleConverter.getPaintConverter(), altFillColor) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.altFillHorizontal != null && !n.altFillHorizontal.isBound();
                    }

                    @Override
                    public final StyleableProperty<Paint> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Paint>) n.altFillHorizontal;
                    }
                };
        private static final CssMetaData<Chart, Number> ASPECTRATIO
                = new CssMetaData<Chart, Number>("-w-view-aspectratio",
                        StyleConverter.getSizeConverter(), Double.NaN) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.viewAspectRatio != null && !n.viewAspectRatio.isBound();
                    }

                    @Override
                    public final StyleableProperty<Number> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Number>) n.viewAspectRatio;
                    }
                };

        private static final CssMetaData<Chart, TRANSFORMTYPE> XTRANSFORMTYPE
                = new CssMetaData<Chart, TRANSFORMTYPE>("-w-xtransformtype",
                        (StyleConverter<?, TRANSFORMTYPE>) StyleConverter.getEnumConverter(TRANSFORMTYPE.class), TRANSFORMTYPE.LINEAR) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return true;
                    }

                    @Override
                    public final StyleableProperty<TRANSFORMTYPE> getStyleableProperty(Chart n) {
                        return (StyleableProperty<TRANSFORMTYPE>) n.xTransformType;
                    }
                };

        private static final CssMetaData<Chart, TRANSFORMTYPE> YTRANSFORMTYPE
                = new CssMetaData<Chart, TRANSFORMTYPE>("-w-ytransformtype",
                        (StyleConverter<?, TRANSFORMTYPE>) StyleConverter.getEnumConverter(TRANSFORMTYPE.class), TRANSFORMTYPE.LINEAR) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return true;
                    }

                    @Override
                    public final StyleableProperty<TRANSFORMTYPE> getStyleableProperty(Chart n) {
                        return (StyleableProperty<TRANSFORMTYPE>) n.yTransformType;
                    }
                };

        private static final CssMetaData<Chart, VIEWALIGNMENT> VIEWALIGN
                = new CssMetaData<Chart, VIEWALIGNMENT>("-w-view-alignment",
                        (StyleConverter<?, VIEWALIGNMENT>) StyleConverter.getEnumConverter(VIEWALIGNMENT.class),
                        VIEWALIGNMENT.CENTER) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return true;
                    }

                    @Override
                    public final StyleableProperty<VIEWALIGNMENT> getStyleableProperty(Chart n) {
                        return (StyleableProperty<VIEWALIGNMENT>) n.viewAlignment;
                    }
                };
        private static final CssMetaData<Chart, Boolean> MOUSEPOSITION
                = new CssMetaData<Chart, Boolean>("-w-mouse-position",
                        StyleConverter.getBooleanConverter(), Boolean.FALSE) {

                    @Override
                    public final boolean isSettable(Chart n) {
                        return n.mousePositionDisplayed != null && !n.mousePositionDisplayed.isBound();
                    }

                    @Override
                    public final StyleableProperty<Boolean> getStyleableProperty(Chart n) {
                        return (StyleableProperty<Boolean>) n.mousePositionDisplayed;
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

            styleables.add(MAJORXINTERVAL);
            styleables.add(MAJORYINTERVAL);
            styleables.add(MINORCOUNTXHINT);
            styleables.add(MINORCOUNTYHINT);

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
            styleables.add(ALTFILLVERTICALPAINTED);
            styleables.add(ALTFILLHORIZONTALPAINTED);

            styleables.add(VIEWALIGN);

            STYLEABLES = Collections.unmodifiableList(styleables);
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



    double calcSpacing(double value, double minspace) {
        double lg = Math.log10(value);
        double rem = lg - Math.floor(lg);
        double scope = Math.pow(10, rem);
        double scale = Math.pow(10, Math.floor(lg));
        if (scope >= 5) {
            scope = scale;
        } else if (scope >= 2) {
            scope = scale / 2d;
        } else {
            scope = scale / 5d;
        }
        return scope;
//        if (scope >= minspace * 100) {
//            return scope;
//        } else {
//            return calcSpacing(value*1.2, minspace);
//        }
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

    public static class CrossHair extends Path {

        private final Chart layer;

        private CrossHair(Chart chart) {
            layer = chart;
            parentProperty().addListener((ObservableValue<? extends Parent> ov, Parent t, Parent t1) -> {
                Pane pane = (Pane) getParent();
                getElements().add(new MoveTo(0, layer.mouseY().get() + layer.getLayoutY()));
                getElements().add(new LineTo(pane.getWidth(), layer.mouseY().get() + layer.getLayoutY()));
                getElements().add(new MoveTo(layer.mouseX().get(), 0));
                getElements().add(new LineTo(pane.getWidth() / 2d, pane.getHeight()));
                bind();
            });
            setStroke(Color.DARKGREEN);
            getStyleClass().add("crosshair");
        }

        private void bind() {
            Pane pane = (Pane) getParent();
            ((MoveTo) getElements().get(0)).yProperty().bind(layer.mouseY().add(layer.layoutYProperty()));

            ((LineTo) getElements().get(1)).xProperty().bind(pane.widthProperty());
            ((LineTo) getElements().get(1)).yProperty().bind(layer.mouseY().add(layer.layoutYProperty()));

            ((MoveTo) getElements().get(2)).xProperty().bind(layer.mouseX());

            ((LineTo) getElements().get(3)).xProperty().bind(layer.mouseX());
            ((LineTo) getElements().get(3)).yProperty().bind(pane.heightProperty());

        }

        private void unbind() {
            ((MoveTo) getElements().get(0)).yProperty().unbind();
            ((LineTo) getElements().get(1)).xProperty().unbind();
            ((LineTo) getElements().get(1)).yProperty().unbind();
            ((MoveTo) getElements().get(2)).xProperty().unbind();
            ((LineTo) getElements().get(3)).xProperty().unbind();
            ((LineTo) getElements().get(3)).yProperty().unbind();
            layer.getView().getChildren().remove(this);
        }

    }

    private static class CursorTextBox extends HBox {

        private CursorTextBox(Text text) {
            super(text);
            this.setMouseTransparent(true);
            if (text.getText().isEmpty()) {
                text.setText("0.000");
            }
            this.setPrefSize(text.prefWidth(-1d), text.prefHeight(-1d));
            setStyle("-fx-background-color: white; -fx-border-style: solid; -fx-border-color: black");
            text.setText("");
        }
    }

    private static StringExpression mouseTextBinding(int axis, final Chart chart,
            final SimpleDoubleProperty obs) {
        return new StringBinding() {

            {
                super.bind(obs);
            }

            @Override
            protected String computeValue() {
                final Double value = obs.getValue();
                if (axis == 0) {
                    return (Double.isFinite(value)) ? formatter.format(chart.toPositionX(value)) : "";
                } else {
                    return (Double.isFinite(value)) ? formatter.format(chart.toPositionY(value)) : "";
                }
            }

            @Override
            public void dispose() {
                super.unbind(obs);
            }

            @Override
            public ObservableList<ObservableValue<?>> getDependencies() {
                return FXCollections.<ObservableValue<?>>singletonObservableList(obs);
            }
        };

    }
}
