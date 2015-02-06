/* 
 * <http://waterloo.sourceforge.net/>
 *
 * Copyright King's College London  2014. Copyright Malcolm Lidierth 2014-.
 * 
 * @author Malcolm Lidierth, <a href="http://sourceforge.net/p/waterloo/discussion/"> [Contact]</a>
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
package waterloo.fx.axis;

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
 * These extend {@code javafx.scene.layout.Region} and use standard JavaFX
 * components rather than painting to the view.
 *
 * Separate subclasses are used for the axes to the left and right and at the
 * top and bottom of the view area.
 *
 * @author Malcolm Lidierth
 */
public abstract class AbstractAxisRegion extends Region {

    protected enum AXISPOSITION {

        LEFT, TOP, RIGHT, BOTTOM
    }

    private final LineClass line;
    private final Chart layer;
    /**
     * Text instance for the axis label
     */
    private final Text axisLabel = new Text("Axis Label");
    /**
     * A {@code LinkedHashMap} containing Integer keys and String values. When
     * an axis tick position coincides with the Integer key, the corresponding
     * String value will be used for the tick label.
     */
    private final LinkedHashMap<Integer, String> categories = new LinkedHashMap<>();
    /**
     * Boolean indicating whether to use categoricalProperty labels for the tick
     * marks
     */
    private final BooleanProperty categoricalProperty = new SimpleBooleanProperty(Boolean.FALSE);
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
        line = new LineClass(layer);

        axisLabel.fillProperty().bind(layer.axisColorProperty());

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


    public final Chart getLayer() {
        return layer;
    }

    /**
     * @return the line
     */
    public LineClass getLine() {
        return line;
    }

    /**
     * @return the categoricalProperty
     */
    public boolean isCategorical() {
        return categoricalProperty().get();
    }

    public void setCategorial(boolean categorical) {
        categoricalProperty().set(categorical);
    }

    public BooleanProperty categoricalProperty() {
        return categoricalProperty;
    }

    public Font getFont() {
        return Font.font(getLayer().fontProperty().get().getFamily(),
                FontPosture.valueOf(getLayer().fontProperty().get().getStyle().toUpperCase()),
                getLayer().getAxisFontSize());
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

    /**
     * @return the axisLabel
     */
    public final Text getAxisLabel() {
        return axisLabel;
    }

    protected void removeAxisLabel() {
        if (getChildren().contains(getAxisLabel())) {
            getChildren().remove(getAxisLabel());
        }
    }

    protected void addAxisLabel() {
        if (!getChildren().contains(axisLabel)) {
            getChildren().add(getAxisLabel());
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

            value.strokeProperty().bind(layer.axisColorProperty());
            value.strokeWidthProperty().bind(layer.axisStrokeWidthProperty());

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
                            value.getPoints().addAll(new Double[]{p0.getX()  , p0.getY()});
                            p0 = layer.toPixel(layer.getXLeft(), layer.getYTop());
                            value.getPoints().addAll(new Double[]{p0.getX() , p0.getY()});
                            layer.getAxisSet().getYTransform().get().stream().filter((Double y) -> y >= layer.getYMin() && y <= layer.getYMax()).forEach((Double y) -> {
                                Point2D p1 = layer.toPixel(layer.getXLeft(), y);
                                value.getPoints().addAll(new Double[]{p1.getX() , p1.getY()});
                                value.getPoints().addAll(new Double[]{p1.getX()  + layer.xRightTickLength, p1.getY()});
                                value.getPoints().addAll(new Double[]{p1.getX() , p1.getY()});
                            });
                            layer.getAxisSet().getYTransform().getMinorTicks().stream().filter((Double y) -> y >= layer.getYMin() && y <= layer.getYMax()).forEach((Double y) -> {
                                Point2D p1 = layer.toPixel(layer.getXLeft(), y);
                                value.getPoints().addAll(new Double[]{p1.getX() , p1.getY()});
                                value.getPoints().addAll(new Double[]{p1.getX()  + layer.xRightTickLength * 0.7, p1.getY()});
                                value.getPoints().addAll(new Double[]{p1.getX() , p1.getY()});
                            });
                        }
                        break;
                }
            }
            return value;
        }
    }

}
