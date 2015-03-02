/* 
 * <http://sigtool.github.io/waterlooFX/>
 *
 * Copyright King's College London  2014. Copyright Malcolm Lidierth 2014-.
 * 
 * @author Malcolm Lidierth, <a href="https://github.com/sigtool/waterlooFX/issues"> [Contact]</a>
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
package waterloo.fx.plot.axis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import waterloo.fx.plot.Chart;

/**
 * {@code AbstractAxisRegion} provides the base class for axes drawn outside of
 * the view area.
 *
 * Separate subclasses are used for the axes to the left and right and at the
 * top and bottom of the view area.
 *
 * @author Malcolm Lidierth
 */
public abstract class AbstractAxisRegion extends Region {

    /**
     * Enum thats indicates where the axis is drawn relative to the view area.
     */
    protected enum AXISPOSITION {

        LEFT, TOP, RIGHT, BOTTOM
    }

    /**
     * A LinClass object that contains the JavaFX nodes that represent the axis
     * on screen.
     */
    private final LineClass line;
    /**
     * Reference to the {@code Chart} that this axis belongs to. The axis range
     * and tick marks are set relative to the range of this chart.
     *
     * <stong>Implementation detail:</strong>
     * Note, however, that the nodes may not always be rendered to this
     * {@code Chart}. The standard {@code Chart} class moves all axes into the
     * hierarchy of the "top" {@code Chart} in the z-order when multiple
     * {@code Chart}s overlap as in a layered {@code Chart}. As the axis region
     * will therefore be at the top of the z-order rather than covered by other
     * components, it will always remain responsive to mouse events. This
     * behaviour is implemented via listeners in the standard {@code Chart} and
     * is not a feature of the AbstractAxisRegion. Custom chart classes
     * implemented by users might not implement this behaviour.
     *
     */
    private final Chart layer;
    /**
     * Text instance for the axis label. Note that the fill color of the text is
     * bound in the constructor to match the color of the lines drawn for this
     * axis. To customise the color, remove this binding.
     */
    private final Text axisLabel = new Text("Axis Label");
    /**
     * A {@code LinkedHashMap} containing Integer keys and String values. When
     * an axis tick position coincides with the Integer key, the corresponding
     * String value should be used for the tick label when the {code
     * categoricalProperty} is set true..
     */
    private final LinkedHashMap<Integer, String> categories = new LinkedHashMap<>();
    /**
     * Boolean indicating whether to use categorical labels for the tick marks
     */
    private final BooleanProperty categoricalProperty = new SimpleBooleanProperty(Boolean.FALSE);

    private double dragXStart = Double.NaN;
    private double dragYStart = Double.NaN;
    private double deltaX;
    private double deltaY;

    /**
     * Constructor which accepts a reference to a {@code Chart} as input. The
     * axis range and tick marks are set relative to the range of this
     * {@code Chart}.
     *
     * <strong>Implementation detail:</strong>
     * Note, however, that the nodes may not always be rendered to this
     * {@code Chart}. The standard {@code Chart} class moves all axes into the
     * hierarchy of the "top" {@code Chart} in the z-order when multiple
     * {@code Chart}s overlap as in a layered {@code Chart}. As the axis region
     * will therefore be at the top of the z-order rather than covered by other
     * components, it will always remain responsive to mouse events. This
     * behaviour is implemented via listeners in the standard {@code Chart} and
     * is not a feature of the AbstractAxisRegion. Custom chart classes
     * implemented by users might not implement this behaviour.
     *
     * @param layer
     */
    public AbstractAxisRegion(Chart layer) {
        

        // Add a reference to the layer
        this.layer = layer;
        
        getStyleClass().add("w-axis");

        // Generate the line - done on the constructor so we use the Platform thread.
        line = new LineClass(layer);

        // Create a binding betqeen the axisLabel color and the axis color.
        axisLabel.fillProperty().bind(layer.axisColorProperty());

        // For convenience, we add mouse listeners here with behaviour 
        // that depends on the position of the axis relative to the view rather than in the subclasses.
        // The mouse listeners will be installed when  the axis is added to a scene.
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
            // Drag events on the axis update the axis range of the
            // {@code Chart} referenced in layer
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
                                layer.setXLeft(layer.getXLeft() + deltaX);
                            }
                        }
                        break;
                    case LEFT:
                    case RIGHT:
                        deltaY = dragYStart - m2.getY();
                        if (deltaY != 0) {
                            deltaY = deltaY * this.layer.getPixelHeight();
                            if (layer.toPositionY(m2.getY()) >= layer.getYOrigin()) {
                                layer.setYTop(layer.getYTop() - deltaY);
                            } else {
                                layer.setYBottom(layer.getYBottom() - deltaY);
                            }
                        }
                        break;
                    default:
                }
                dragXStart = m2.getX();
                dragYStart = m2.getY();
                layer.requestLayout();
            });
        };
        sceneProperty().addListener(addedToScene);

    }

    /**
     * Returns a reference to the {@code Chart} that this axis belongs to. The
     * axis range and tick marks are set relative to the range of this chart.
     *
     * <strong>Implementation detail:</strong>
     * Note, however, that the nodes may not always be rendered to this
     * {@code Chart}. The standard {@code Chart} class moves all axes into the
     * hierarchy of the "top" {@code Chart} in the z-order when multiple
     * {@code Chart}s overlap as in a layered {@code Chart}. As the axis region
     * will therefore be at the top of the z-order rather than covered by other
     * components, it will always remain responsive to mouse events. This
     * behaviour is implemented via listeners in the standard {@code Chart} and
     * is not a feature of the AbstractAxisRegion. Custom chart classes
     * implemented by users might not implement this behaviour.
     *
     * @return the {@code Chart}
     */
    public final Chart getLayer() {
        return layer;
    }

    /**
     * Return the line used to render this axis. This is a subclass of
     * {@code ObjectBinding<Polyline>}. The stroke color and width are bound in
     * the constructor to the axis stroke width and color properties of the
     * layer. The position and length of the line is also bound to match that of
     * the appropriate dimension of the layer, which corresponds to that of the
     * view area in the {@code Chart}. For example, for an axis at the bottom of
     * the chart, the left and right positions of the line will match those of
     * the view.
     *
     * @return the line
     */
    public LineClass getLine() {
        return line;
    }

    /**
     * Returns true to use categorical labels stored in the {@code categories}
     * property to label the tick marks, or false if numerical values should be
     * used.
     *
     * @return the boolean - true to paint categorical labels
     */
    public boolean isCategorical() {
        return categoricalProperty().get();
    }

    /**
     * Sets the categoricalProperty.
     *
     * @param categorical true to use categorical labels stored in the
     * {@code categories} property to label the tick marks, or false if
     * numerical values should be used
     */
    public void setCategorial(boolean categorical) {
        categoricalProperty().set(categorical);
    }

    /**
     * Return the categoricalProperty.
     *
     * @return categoricalProperty
     */
    public BooleanProperty categoricalProperty() {
        return categoricalProperty;
    }

    /**
     * Returns the {@code Font} associated with the {@code Chart} in the
     * {@code layer} property.
     *
     * @return the Font
     */
    public Font getFont() {
        return Font.font(getLayer().fontProperty().get().getFamily(),
                FontPosture.valueOf(getLayer().fontProperty().get().getStyle().toUpperCase()),
                getLayer().getAxisFontSize());
    }

    /**
     * Returns a {@code LinkedHashMap<Integer, String>} of the category labels
     * to be drawn when {@code isCategoral()} returns true.
     *
     * @return the categories
     */
    public LinkedHashMap<Integer, String> getCategories() {
        return categories;
    }

    /**
     * Returns an an {@code ArrayList<TickLabel>} containing references to the
     * TickLabels for this axis.
     *
     * @return the tickLabels
     */
    ArrayList<TickLabel> getTickLabels() {
        ArrayList<TickLabel> arr = new ArrayList<>();
        getChildren().filtered(x -> x instanceof TickLabel).forEach((x) -> {
            arr.add((TickLabel) x);
        });
        return arr;
    }

    double calcTickLabelWidth() {
        double w = 0d;
        for (TickLabel t : getTickLabels()) {
            w = Math.max(w, t.getText().length());
        }
        return w;
    }

    /**
     * @return the axisPosition
     */
    private AXISPOSITION getAxisPosition() {
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

    /**
     * Returns the {@code Text} object used to label this axis.
     *
     * @return the axisLabel
     */
    public final Text getAxisLabel() {
        return axisLabel;
    }

    void removeAxisLabel() {
        if (getChildren().contains(getAxisLabel())) {
            getChildren().remove(getAxisLabel());
        }
    }

    void addAxisLabel() {
        if (!getChildren().contains(axisLabel)) {
            getChildren().add(getAxisLabel());
        }
    }

    /**
     * {@code ObjectBinding<Polyline>} used to represent the axis.
     */
    public final class LineClass extends ObjectBinding<Polyline> {

        private final Chart layer;

        private final Polyline value = new Polyline();


        public LineClass(Chart layer) {
            super();
            this.layer = layer;

            value.strokeProperty().bind(layer.axisColorProperty());
            value.strokeWidthProperty().bind(layer.axisStrokeWidthProperty());

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

        @Override
        public Polyline computeValue() {
            if (layer.getAxisSet() != null) {
                value.getPoints().clear();

                switch (getAxisPosition()) {
                    case TOP:
                        if (layer.isTopAxisPainted()) {
                            double h = layer.getAxisSet().getTopAxis().computePrefHeight(-1d);
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
                            value.getPoints().addAll(new Double[]{p0.getX(), p0.getY()});
                            p0 = layer.toPixel(layer.getXLeft(), layer.getYTop());
                            value.getPoints().addAll(new Double[]{p0.getX(), p0.getY()});
                            layer.getAxisSet().getYTransform().get().stream().filter((Double y) -> y >= layer.getYMin() && y <= layer.getYMax()).forEach((Double y) -> {
                                Point2D p1 = layer.toPixel(layer.getXLeft(), y);
                                value.getPoints().addAll(new Double[]{p1.getX(), p1.getY()});
                                value.getPoints().addAll(new Double[]{p1.getX() + layer.xRightTickLength, p1.getY()});
                                value.getPoints().addAll(new Double[]{p1.getX(), p1.getY()});
                            });
                            layer.getAxisSet().getYTransform().getMinorTicks().stream().filter((Double y) -> y >= layer.getYMin() && y <= layer.getYMax()).forEach((Double y) -> {
                                Point2D p1 = layer.toPixel(layer.getXLeft(), y);
                                value.getPoints().addAll(new Double[]{p1.getX(), p1.getY()});
                                value.getPoints().addAll(new Double[]{p1.getX() + layer.xRightTickLength * 0.7, p1.getY()});
                                value.getPoints().addAll(new Double[]{p1.getX(), p1.getY()});
                            });
                        }
                        break;
                }
            }
            return value;
        }
    }

}
