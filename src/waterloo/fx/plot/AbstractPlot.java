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
package waterloo.fx.plot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleableStringProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import waterloo.fx.markers.ArrowHead;
import waterloo.fx.markers.Asterisk;
import waterloo.fx.markers.CenteredShapeInterface;
import waterloo.fx.markers.Cross;
import waterloo.fx.markers.Diamond;
import waterloo.fx.markers.Hexagon;
import waterloo.fx.markers.InvertedTriangle;
import waterloo.fx.markers.LeftTriangle;
import waterloo.fx.markers.Pentagon;
import waterloo.fx.markers.Plus;
import waterloo.fx.markers.RightTriangle;
import waterloo.fx.markers.Square;
import waterloo.fx.markers.Triangle;
import waterloo.fx.plot.model.DataModel;
import waterloo.fx.util.GJCyclicArrayList;

/**
 * This is the base class for all plots in waterlooFX. An {@code AbstractPlot}
 * extends {@code StackPane} and, by default, contains two panes for rendering
 * <ul>
 * <li>the plot - this is the graphicsPane</li>
 * <li>annotations that can be superimposed on the plot - this is the
 * annotationPane</li>
 * </ul>
 *
 * Each of these panes has {@code PickOnBounds} set (@code false} by default so
 * mouse events will fall through to the underlying {@code Chart} for empty
 * areas of the plot.
 *
 * Each plot has a {@code visualElement} of a type that extends
 * {@code List<? extends Node>}. These nodes provided the visual representation
 * of the plot.
 *
 * Subclasses of {@code AbstractPlot} need to implement an
 * {@code updateElements} method that will
 * <ol>
 * <li>Create this list based on the entries in the dataModel and the
 * visualModel</li>
 * <li>Add/remove elements from the list when the data are changed</li>
 * </ol>
 *
 * Subclasses also need to implement an {@code arrangePlot} method. This method
 * is responsible for arranging the contents of visualElement list within the
 * graphicsPane.
 *
 * @author Malcolm Lidierth
 * @param <T>
 */
public abstract class AbstractPlot<T extends List<? extends Node>> extends StackPane implements ListChangeListener<Number> {

    public static enum MARKERTYPE {

        CIRCLE, SQUARE, TRIANGLE, INVERTED_TRIANGLE, RIGHT_TRIANGLE, LEFT_TRIANGLE, DIAMOND, PENTAGON, HEXAGON, CROSS, PLUS, ASTERISK, SPHERE, ARROWHEAD
    }

    public static enum LABELORIENTATION {

        AUTO, VERTICAL, HORIZONTAL, CUSTOM
    }

    /**
     * This pane is used to render the plot
     */
    private Pane graphicsPane;
    /**
     * This Pane sits above the graphicsPane in the z-order. Annotations can be
     * added to this layer and will always appear above the plot.
     */
    private final AnnotationPane annotationPane;
    /**
     * Data model for this plot.
     */
    final DataModel dataModel = new DataModel();
    /**
     * Visual model for this plot.
     */
    final VisualModel visualModel = new VisualModel();
    /**
     * {@code visualElement} contains the Nodes that are used to represent the
     * data for this plot.
     *
     * For a marker-based plot these will often be references to markers
     * supplied from the visual model.
     *
     */
    T visualElement;

    //private final NumberFormat formatter = new DecimalFormat();
    
    /**
     * When nodesNeedUpdate is true, nodes required by the plot will be
     * regenerated/reset before calling arrangePlot to layout those nodes.
     *
     * When nodesNeedUpdate is false, the existing nodes will be used but their
     * layout will be updated on each layout pass.
     *
     * nodesNeedUpdate should only be altered on the FX application thread.
     *
     * nodesNeedUpdate can be set true via listeners on the relevant properties
     * of the dataModel.
     *
     * The {@code update()} method provides a thread-safe way to set
     * nodesNeedUpdate true and update the display.
     */
    protected AtomicBoolean nodesNeedUpdate = new AtomicBoolean(true);

    /**
     *
     */
    private final StringProperty xData = new StyleableStringProperty("") {

        @Override
        public void set(String s) {
            dataModel.getXData().clear();
            if (!s.isEmpty()) {
                List<String> items = Arrays.asList(s.split("\\s*,\\s*"));
                items.forEach(x -> dataModel.getXData().add(Double.valueOf(x)));
            }
        }

        @Override
        public String get() {
            return dataModel.getXData().stream().map(x -> x.toString()).collect(Collectors.joining(", "));
        }

        @Override
        public Object getBean() {
            return AbstractPlot.this;
        }

        @Override
        public String getName() {
            return "xData(Angle)";
        }

        @Override
        public CssMetaData<? extends Styleable, String> getCssMetaData() {
            return StyleableProperties.XDATA;
        }

    };
    private final StringProperty yData = new StyleableStringProperty("") {

        @Override
        public void set(String s) {
            dataModel.getYData().clear();
            if (!s.isEmpty()) {
                List<String> items = Arrays.asList(s.split("\\s*,\\s*"));
                items.forEach(y -> dataModel.getYData().add(Double.valueOf(y)));
            }
        }

        @Override
        public String get() {
            return dataModel.getYData().stream().map(x -> x.toString()).collect(Collectors.joining(", "));
        }

        @Override
        public Object getBean() {
            return AbstractPlot.this;
        }

        @Override
        public String getName() {
            return "yData(Radius)";
        }

        @Override
        public CssMetaData<? extends Styleable, String> getCssMetaData() {
            return StyleableProperties.YDATA;
        }

    };
    private final StringProperty extraDataEast = new StyleableStringProperty("") {

        @Override
        public void set(String s) {
            dataModel.getExtraData0().clear();
            if (!s.isEmpty()) {
                List<String> items = Arrays.asList(s.split("\\s*,\\s*"));
                items.forEach(x -> dataModel.getExtraData0().add(Double.valueOf(x)));
            }
        }

        @Override
        public String get() {
            return dataModel.getExtraData0().stream().map(x -> x.toString()).collect(Collectors.joining(", "));
        }

        @Override
        public Object getBean() {
            return AbstractPlot.this;
        }

        @Override
        public String getName() {
            return "eastData";
        }

        @Override
        public CssMetaData<? extends Styleable, String> getCssMetaData() {
            return StyleableProperties.EASTDATA;
        }

    };
    private final StringProperty extraDataNorth = new StyleableStringProperty("") {

        @Override
        public void set(String s) {
            dataModel.getExtraData1().clear();
            if (!s.isEmpty()) {
                List<String> items = Arrays.asList(s.split("\\s*,\\s*"));
                items.forEach(x -> dataModel.getExtraData1().add(Double.valueOf(x)));
            }
        }

        @Override
        public String get() {
            return dataModel.getExtraData1().stream().map(x -> x.toString()).collect(Collectors.joining(", "));
        }

        @Override
        public Object getBean() {
            return AbstractPlot.this;
        }

        @Override
        public String getName() {
            return "northData";
        }

        @Override
        public CssMetaData<? extends Styleable, String> getCssMetaData() {
            return StyleableProperties.NORTHDATA;
        }

    };
    private final StringProperty westData = new StyleableStringProperty("") {

        @Override
        public void set(String s) {
            dataModel.getExtraData2().clear();
            if (!s.isEmpty()) {
                List<String> items = Arrays.asList(s.split("\\s*,\\s*"));
                items.forEach(x -> dataModel.getExtraData2().add(Double.valueOf(x)));
            }
        }

        @Override
        public String get() {
            return dataModel.getExtraData2().stream().map(x -> x.toString()).collect(Collectors.joining(", "));
        }

        @Override
        public Object getBean() {
            return AbstractPlot.this;
        }

        @Override
        public String getName() {
            return "westData";
        }

        @Override
        public CssMetaData<? extends Styleable, String> getCssMetaData() {
            return StyleableProperties.WESTDATA;
        }

    };
    private final StringProperty southData = new StyleableStringProperty("") {

        @Override
        public void set(String s) {
            dataModel.getExtraData3().clear();
            if (!s.isEmpty()) {
                List<String> items = Arrays.asList(s.split("\\s*,\\s*"));
                items.forEach(x -> dataModel.getExtraData3().add(Double.valueOf(x)));
            }
        }

        @Override
        public String get() {
            return dataModel.getExtraData3().stream().map(x -> x.toString()).collect(Collectors.joining(", "));
        }

        @Override
        public Object getBean() {
            return AbstractPlot.this;
        }

        @Override
        public String getName() {
            return "southData";
        }

        @Override
        public CssMetaData<? extends Styleable, String> getCssMetaData() {
            return StyleableProperties.SOUTHDATA;
        }

    };
    private final StringProperty labelData = new StyleableStringProperty("") {

        @Override
        public void set(String s) {
            List<String> items = Arrays.asList(s.split("\\s*,\\s*"));
            ArrayList<Text> textArray = new ArrayList<>();
            items.forEach(x -> textArray.add(new Text(x)));
            setLabels(textArray.toArray(new Text[textArray.size()]));
        }

        @Override
        public String get() {
            return visualModel.getLabels().stream().map(x -> x.getText()).collect(Collectors.joining(", "));
        }

        @Override
        public Object getBean() {
            return AbstractPlot.this;
        }

        @Override
        public String getName() {
            return "labels";
        }

        @Override
        public CssMetaData<? extends Styleable, String> getCssMetaData() {
            return StyleableProperties.LABELDATA;
        }

    };
    public LABELORIENTATION labelOrientation = LABELORIENTATION.AUTO;

    BooleanProperty dataPolar = new StyleableBooleanProperty(false) {
        @Override
        public boolean get() {
            return dataModel.isDataPolar();
        }

        @Override
        public void set(boolean tf) {
            dataModel.setDataPolar(tf);
        }

        @Override
        public Object getBean() {
            return AbstractPlot.this;
        }

        @Override
        public String getName() {
            return "dataPolar";
        }

        @Override
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
            return StyleableProperties.DATAPOLAR;
        }
    };
    /**
     * The base value should be used only by plots that implement the
     * BaseValueSensitiveInterface.
     *
     */
    DoubleProperty baseValue = new StyleableDoubleProperty(0d) {

        @Override
        public double get() {
            return dataModel.getBaseValue();
        }

        @Override
        public void set(double val) {
            dataModel.setBaseValue(val);
        }

        @Override
        public Object getBean() {
            return AbstractPlot.this;
        }

        @Override
        public String getName() {
            return "baseValue";
        }

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.BASEVALUE;
        }

    };

//    /**
//     * The CSS styling index for the plot.
//     *
//     * When added to a chart or a PlotCollection, "plot-N" where N is the
//     * plotSyleIndex will be added to the list of classes returned by
//     * {@code getStyleClass}.
//     */
//    IntegerProperty plotStyleIndex = new SimpleIntegerProperty(-1) {
//
//        @Override
//        public void set(int index) {
//            if (index >= 0) {
//                super.set(index);
//                List<String> list = new ArrayList<>();
//                list.addAll(getStyleClass());
//                list.stream().filter((s) -> (s.startsWith("plot-"))).forEach((s) -> {
//                    getStyleClass().remove(s);
//                });
//                getStyleClass().add("plot-" + index);
//            }
//        }
//    };
    /**
     * Default constructor
     */
    public AbstractPlot() {
        super();

        setBackground(Background.EMPTY);
//        getStyleClass().add("plot-");

        graphicsPane = new Pane();
        graphicsPane.setBackground(Background.EMPTY);
        getChildren().add(graphicsPane);
        graphicsPane.setPickOnBounds(false);

        annotationPane = new AnnotationPane();
        annotationPane.setBackground(Background.EMPTY);
        getChildren().add(annotationPane);
        annotationPane.setPickOnBounds(false);
//        annotationPane.getStyleClass().add("annotationpane");

        // Add a ListChangeListener to the x and y data.
        dataModel.getXData().addListener(this);
        dataModel.getYData().addListener(this);

        graphicsPane.getChildren().addListener((ListChangeListener.Change<? extends Node> c) -> {
            while (c.next()) {
                List<? extends Node> list = c.getAddedSubList();
                list.stream().filter((node) -> (node instanceof AbstractPlot)).forEach((node) -> {
                    node.setLayoutX(0d);
                    node.setLayoutY(0d);
                    ((AbstractPlot) node).prefWidthProperty().bind(widthProperty());
                    ((AbstractPlot) node).prefHeightProperty().bind(heightProperty());
                });
            }
            requestLayout();
        });

        getChildren().addListener((ListChangeListener.Change<? extends Node> c) -> {
            while (c.next()) {
                List<? extends Node> list = c.getAddedSubList();
                list.stream().filter((node) -> (node instanceof Annotation)).forEach((node) -> {
                    getChildren().remove(node);
                    annotationPane.getChildren().add(node);
                });
            }
        });

    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     * @since JavaFX 8.0
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return AbstractPlot.StyleableProperties.STYLEABLES;
    }

    public boolean isDataPolar() {
        return dataPolar.get();
    }

    public void setDataPolar(boolean tf) {
        dataPolar.set(tf);
    }

    public BooleanProperty dataPolarProperty() {
        return dataPolar;
    }

    public double getBaseValue() {
        return baseValue.get();
    }

    public void setBaseValue(double val) {
        baseValue.set(val);
    }

    public DoubleProperty baseValueProperty() {
        return baseValue;
    }

    /**
     * @return the graphicsPane
     */
    public Pane getGraphicsPane() {
        return graphicsPane;
    }

    /**
     * @param graphicsPane the graphicsPane to set
     */
    public void setGraphicsPane(Pane graphicsPane) {
        this.graphicsPane = graphicsPane;
    }

    /**
     * @return the annotationPane
     */
    public Pane getAnnotationPane() {
        return annotationPane;
    }

    public void add(AbstractPlot p1) {
        getChildren().add(p1);
    }

    public boolean isValid() {
        return dataModel.isValid();
    }

    public Stream<Node> getPlots() {
        return getChildren().stream().filter(x -> x instanceof AbstractPlot);
    }

    @SuppressWarnings("unchecked")
    public List<Node> getAllPlots() {
        List<Node> list = getPlots().collect(Collectors.toList());
        getPlots().collect(Collectors.toList()).forEach(x -> {
            list.addAll(((AbstractPlot) x).getAllPlots());
        });
        return list;
    }

//    IntegerProperty plotStyleIndex() {
//        return plotStyleIndex;
//    }
//
//    public void setPlotStyleIndex(int index) {
//        plotStyleIndex.set(index);
//    }
//    
//    public int getPlotStyleIndex(){
//        return plotStyleIndex.get();
//    }
//    public String getPlotStyle() {
//        for (String s : getStyleClass()) {
//            if (s.startsWith("plot-")) {
//                return s;
//            }
//        }
//        return "UNSET";
//    }
    public VisualModel getVisualModel() {
        return visualModel;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

//    public void setDataModel(DataModel model) {
//        this.dataModel = model;
//    }
    public String getXData() {
        return xData.get();
    }

    public void setXData(String s) {
        xData.set(s);
    }

    public StringProperty xDataProperty() {
        return xData;
    }

    public String getYData() {
        return yData.get();
    }

    public void setYData(String s) {
        yData.set(s);
    }

    public StringProperty yDataString() {
        return yData;
    }

    public StringProperty extraDataEastProperty() {
        return extraDataEast;
    }

    public String getExtraDataEast() {
        return extraDataEast.get();
    }

    public void setExtraDataEast(String s) {
        extraDataEast.set(s);
    }

    public StringProperty extraDataNorthProperty() {
        return extraDataNorth;
    }

    public String getExtraDataNorth() {
        return extraDataNorth.get();
    }

    public void setExtraDataNorth(String s) {
        extraDataNorth.set(s);
    }

    public StringProperty extraDataWestProperty() {
        return westData;
    }

    public String getExtraDataWest() {
        return westData.get();
    }

    public void setExtraDataWest(String s) {
        westData.set(s);
    }

    public StringProperty extraDataSouthProperty() {
        return southData;
    }

    public String getExtraDataSouth() {
        return southData.get();
    }

    public void setExtraDataSouth(String s) {
        southData.set(s);
    }

    public StringProperty labelDataProperty() {
        return labelData;
    }

    public String getLabelData() {
        return labelData.get();
    }

    public void setLabelData(String s) {
        labelData.set(s);
    }

    public final Point2D getData(Chart chart, double x, double y) {
        if (!dataModel.isDataPolar()) {
            // Cartesian data so return these data via the axisSet
            return chart.getAxisSet().getData(x, y);
        } else {
            // The Plotting methods always use cartesian coordinates, so convert to those
            final double[] xy = new double[]{Math.cos(x) * y, Math.sin(x) * y};
            return chart.getAxisSet().getData(xy[0], xy[1]);
        }
    }

//    private static double[] cartesianToPolar(double x, double y) {
//        double[] xy = new double[]{Math.hypot(x, y), Math.atan2(x, y)};
//        return xy;
//    }
//
//    private static double[] polarToCartesian(double x, double y) {
//        double[] xy = new double[]{Math.cos(x) * y, Math.sin(x) * y};
//        return xy;
//    }
    //private final static Point2D zeroPoint = new Point2D(0,0);
    public final Point2D getInverse(Chart chart, double x, double y) {
        if (!chart.isPolar() && !dataModel.isDataPolar()) {
            // All cartesian so return the data from the axisSet
            return chart.getAxisSet().getInverse(x, y);
        } else if (chart.isPolar() && dataModel.isDataPolar()) {
            // Plotting is always done using cartesian coordinates, so convert to those
            x = Math.cos(x) * y;
            y = Math.sin(x) * y;
            return chart.getAxisSet().getInverse(x, y);
        }
        return Point2D.ZERO;//zeroPoint;//Point2D.ZERO;//Not recognized by javac in Java 8eas?
    }

    /**
     * Thread-safe method to update the plot, typically after changing elements
     * in the visual model.
     *
     * {@code update()} sets nodesNeedUpdate true, and requests a layout of the
     * plot that will update the display.
     */
    public final void update() {
        Platform.runLater(() -> {
            if (nodesNeedUpdate.compareAndSet(false, true)) {
                requestLayout();
            }
        });
    }

    /**
     * Arranges the plots that are children of this plot.
     *
     * @param chart
     */
    private void arrangePlots(Chart chart) {
        getPlots().forEach(plot -> {
            if ((((AbstractPlot) plot).isValid())) {
                ((AbstractPlot) plot).arrangePlot(chart);
            }
        });
    }

    /**
     * The arrangePlot method is a layout method that updates the position or
     * contents of the nodes representing this plot and its children via a call
     * to {@code arrangePlots}. This method does not call the {@code StackPane}
     * layout methods - those will be called during the normal scene layout pass
     * and will put into effect any layout changes made here.
     *
     * The method needs to be overridden in plot subclasses to perform
     * plot-specific operations. Most work will be done within the subclass
     * methods. Those will be called directly from the parent Chart's {@code
     * layoutChildren} method.
     *
     * The plot subclass methods should call this {@code AbstractPlot}
     * superclass method <em>before</em> they do any real other work as the
     * superclass method calls the {@code updateElements} method to create the
     * {@code Nodes} to layout as required.
     *
     * @param chart
     */
    public void arrangePlot(Chart chart) {
        if (nodesNeedUpdate.compareAndSet(true, false)) {
            //System.err.println("Updating visual element");
            visualElement.stream().forEach((node) -> {
                graphicsPane.getChildren().remove(node);
            });
            visualElement.clear();
            updateElements(chart);
        }
        arrangePlots(chart);
    }

    /**
     * Overridden layout children method. Ensures that labels are aligned in the
     * annotation pane. This is called in the normal scene layout pass
     * <em>after</em> plots have been arranged so the layoutX, layoutY, and
     * preferred dimensions of any marker nodes will already have been set and
     * can be used to align the labels.
     */
    @Override
    public final void layoutChildren() {
        arrangeLabels();
        super.layoutChildren();
    }

    /**
     * Positions the labels from the visual model over the relevant Node in
     * visualElement.
     *
     * This method provides default behaviour that may be overridden in
     * subclasses.
     *
     * The {@code labelOrientation} property determines whether the labels will
     * be drawn horizontally or vertically. If set to {@code AUTO}, they will
     * orient along the longest axis of the {@code visualElement}. If set to
     * {@code CUSTOM}, no layout will be performed.
     *
     * The {@code arrangeLabels()} method is called from the
     * {@code layoutChildren()} method before a call to
     * {@code Pane layoutChildren() super} method.
     *
     */
    protected void arrangeLabels() {
        for (int k = 0; k < visualModel.getLabels().size(); k++) {

            Node marker = visualElement.get(k);
            double w = marker.prefWidth(-1d);
            double h = marker.prefHeight(-1d);

            double x = 0, y = 0;
            try {
                Method xmethod = marker.getClass().getMethod("getX", new Class[]{});
                Method ymethod = marker.getClass().getMethod("getY", new Class[]{});
                x = (double) xmethod.invoke(marker, (Object[]) null);
                y = (double) ymethod.invoke(marker, (Object[]) null);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            } finally {
                if (marker instanceof CenteredShapeInterface) {
                    if (x == 0) {
                        x = marker.getLayoutX() - marker.prefWidth(-1d) / 2d;
                    }
                    if (y == 0) {
                        y = marker.getLayoutY() - marker.prefHeight(-1d) / 2d;
                    }
                } else {
                    if (x == 0) {
                        x = marker.getLayoutX();
                    }
                    if (y == 0) {
                        y = marker.getLayoutY();
                    }
                }
            }
            Text text = visualModel.getLabels().get(k);
            text.setX(x + (w / 2d) - (text.prefWidth(-1d) / 2d));
            text.setY(y + (h / 2d));
            switch (labelOrientation) {
                case AUTO:
                    if (h > w) {
                        text.setRotate(-90d);
                    }
                    break;
                case VERTICAL:
                    text.setRotate(-90d);
                    break;
                case HORIZONTAL:
                case CUSTOM:
                default:
                    break;
            }
        }
    }

    /**
     * Creates the visualElement(s) for the plot. These are created lazily -
     * when the data in the data model is altered, the nodesNeedUpdate flag is
     * set true.
     *
     * @param chart
     */
    protected abstract void updateElements(Chart chart);

    protected void addElements() {
        visualElement.forEach(x -> {
            if (x != null) {
                graphicsPane.getChildren().add(x);
            }
        });
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
     * Returns the {@code edgeColor} from the visual model.
     *
     * The edge color defines the {@code Paint} property used to color the edges
     * of e.g. markers in scatter plots.
     *
     * @return the Paint instance.
     */
    public Paint getEdgeColor() {
        return visualModel.getEdgeColor();
    }

    /**
     * Sets the {@code edgeColor} in the visual model.
     *
     * The edge color defines the {@code Paint} property used to color the edges
     * of e.g. markers in scatter plots.
     *
     * @param color (or {@code Paint}) to set
     */
    public void setEdgeColor(Paint color) {
        visualModel.setEdgeColor(color);
    }

    public Paint getLineColor() {
        return visualModel.getLineColor();
    }

    public void setLineColor(Paint color) {
        visualModel.setLineColor(color);
    }

    public double getLineWidth() {
        return visualModel.getLineWidth();
    }

    public void setLineWidth(double val) {
        visualModel.setLineWidth(val);
    }

    public Paint getFill() {
        return visualModel.getFill();
    }

    public void setFill(Paint color) {
        visualModel.setFill(color);
    }

    public double getMarkerRadius() {
        return visualModel.getMarkerRadius();
    }

    public void setMarkerRadius(double val) {
        visualModel.setMarkerRadius(val);
    }

    public double getEdgeWidth() {
        return visualModel.getEdgeWidth();
    }

    public void setEdgeWidth(double val) {
        visualModel.setEdgeWidth(val);
    }

    public MARKERTYPE getMarkerType() {
        return visualModel.getMarkerType();
    }

    public void setMarkerType(MARKERTYPE type) {
        visualModel.setMarkerType(type);
    }

    public void setLabels(Text... arr) {
        visualModel.getLabels().forEach(x -> annotationPane.getChildren().remove(x));
        visualModel.getLabels().clear();
        Arrays.stream(arr).forEach((Text text) -> {
            text.setTextAlignment(TextAlignment.CENTER);
            text.setTextOrigin(VPos.CENTER);
            visualModel.getLabels().add(text);
            annotationPane.getChildren().add(text);
        });
    }

    /**
     * This is the ListChangeListener
     *
     * @param c
     */
    @Override
    public void onChanged(Change<? extends Number> c) {
        // If the number on data points has altered, request an update...
        while (c.next()) {
            if (c.wasAdded() || c.wasRemoved()) {
                update();
                return;
            }
        }
        // ... otherwise just request a layout
        requestLayout();
    }

    /**
     * @treatAsPrivate implementation detail
     */
    private static class StyleableProperties {

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        //private final static Class<? extends Enum> clzz = MARKERTYPE.class;

        private static final CssMetaData<AbstractPlot, MARKERTYPE> MARKERSTYLE
                = new CssMetaData<AbstractPlot, MARKERTYPE>("-w-plot-marker-style",
                        (StyleConverter<?, MARKERTYPE>) StyleConverter.getEnumConverter(MARKERTYPE.class), MARKERTYPE.CIRCLE) {

                    @Override
                    public boolean isSettable(AbstractPlot node) {
                        return node instanceof MarkerInterface;
                    }

                    @Override
                    public StyleableProperty<MARKERTYPE> getStyleableProperty(AbstractPlot node) {
                        return (StyleableProperty<MARKERTYPE>) node.visualModel.markerType;
                    }

                };
        private static final CssMetaData<AbstractPlot, Number> MARKERRADIUS
                = new CssMetaData<AbstractPlot, Number>("-w-plot-marker-radius",
                        StyleConverter.getSizeConverter(), 5d) {

                    @Override
                    public boolean isSettable(AbstractPlot node) {
                        return node instanceof MarkerInterface;
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(AbstractPlot node) {
                        return (StyleableProperty<Number>) node.visualModel.markerRadius;
                    }
                };
        private static final CssMetaData<AbstractPlot, String> XDATA
                = new CssMetaData<AbstractPlot, String>("-w-plot-xdata",
                        StyleConverter.getStringConverter(),
                        "") {

                    @Override
                    public boolean isSettable(AbstractPlot n) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<String> getStyleableProperty(AbstractPlot n) {
                        return (StyleableProperty<String>) n.xData;
                    }
                };
        private static final CssMetaData<AbstractPlot, Paint> FILL
                = new CssMetaData<AbstractPlot, Paint>("-w-plot-marker-fill",
                        StyleConverter.getPaintConverter(), Color.GRAY) {

                    @Override
                    public boolean isSettable(AbstractPlot node) {
                        return node instanceof MarkerInterface
                        && node.visualModel.fill != null
                        && !node.visualModel.fill.isBound();
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public StyleableProperty<Paint> getStyleableProperty(AbstractPlot node) {
                        return (StyleableProperty<Paint>) node.visualModel.fill;
                    }
                };
        private static final CssMetaData<AbstractPlot, Paint> EDGECOLOR
                = new CssMetaData<AbstractPlot, Paint>("-w-plot-marker-edgecolor",
                        StyleConverter.getPaintConverter(), Color.DARKGREY) {

                    @Override
                    public boolean isSettable(AbstractPlot node) {
                        return node instanceof MarkerInterface
                        && node.visualModel.getEdgeColor() != null
                        && !node.visualModel.edgeColorProperty().isBound();
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public StyleableProperty<Paint> getStyleableProperty(AbstractPlot node) {
                        return (StyleableProperty<Paint>) node.visualModel.edgeColor;
                    }
                };
        private static final CssMetaData<AbstractPlot, Number> EDGEWIDTH
                = new CssMetaData<AbstractPlot, Number>("-w-plot-marker-edgewidth",
                        StyleConverter.getSizeConverter(), 1d) {

                    @Override
                    public boolean isSettable(AbstractPlot node) {
                        return node instanceof MarkerInterface
                        && node.visualModel.edgeWidth != null
                        && !node.visualModel.edgeWidth.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(AbstractPlot node) {
                        return (StyleableProperty<Number>) node.visualModel.edgeWidth;
                    }
                };
        private static final CssMetaData<AbstractPlot, String> YDATA
                = new CssMetaData<AbstractPlot, String>("-w-plot-ydata",
                        StyleConverter.getStringConverter(), "") {

                    @Override
                    public boolean isSettable(AbstractPlot n) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<String> getStyleableProperty(AbstractPlot n) {
                        return (StyleableProperty<String>) n.yData;
                    }
                };
        private static final CssMetaData<AbstractPlot, Paint> LINECOLOR
                = new CssMetaData<AbstractPlot, Paint>("-w-plot-line-color",
                        StyleConverter.getPaintConverter(), Color.DARKGREY) {

                    @Override
                    public boolean isSettable(AbstractPlot node) {
                        return node instanceof LineInterface
                        && node.getLineColor() != null
                        && !node.visualModel.lineColor.isBound();
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public StyleableProperty<Paint> getStyleableProperty(AbstractPlot node) {
                        return (StyleableProperty<Paint>) node.visualModel.lineColor;
                    }
                };
        private static final CssMetaData<AbstractPlot, Number> BASEVALUE
                = new CssMetaData<AbstractPlot, Number>("-w-plot-basevalue",
                        StyleConverter.getSizeConverter(), 0d) {

                    @Override
                    public boolean isSettable(AbstractPlot n) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(AbstractPlot n) {
                        return (StyleableProperty<Number>) n.baseValue;
                    }
                };
        private static final CssMetaData<AbstractPlot, Number> LINEWIDTH
                = new CssMetaData<AbstractPlot, Number>("-w-plot-line-width",
                        StyleConverter.getSizeConverter(), 1d) {

                    @Override
                    public boolean isSettable(AbstractPlot node) {
                        return node instanceof MarkerInterface
                        && node.visualModel.lineWidth != null
                        && !node.visualModel.lineWidth.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(AbstractPlot node) {
                        return (StyleableProperty<Number>) node.visualModel.lineWidth;
                    }
                };
        private static final CssMetaData<AbstractPlot, Boolean> DATAPOLAR
                = new CssMetaData<AbstractPlot, Boolean>("-w-plot-datapolar",
                        StyleConverter.getBooleanConverter(), false) {

                    @Override
                    public boolean isSettable(AbstractPlot n) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(AbstractPlot n) {
                        return (StyleableProperty<Boolean>) n.dataPolar;
                    }
                };

        private static final CssMetaData<AbstractPlot, String> EASTDATA
                = new CssMetaData<AbstractPlot, String>("-w-plot-eastdata",
                        StyleConverter.getStringConverter(),
                        "") {

                    @Override
                    public boolean isSettable(AbstractPlot n) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<String> getStyleableProperty(AbstractPlot n) {
                        return (StyleableProperty<String>) n.extraDataEast;
                    }
                };

        private static final CssMetaData<AbstractPlot, String> NORTHDATA
                = new CssMetaData<AbstractPlot, String>("-w-plot-northdata",
                        StyleConverter.getStringConverter(),
                        "") {

                    @Override
                    public boolean isSettable(AbstractPlot n) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<String> getStyleableProperty(AbstractPlot n) {
                        return (StyleableProperty<String>) n.extraDataNorth;
                    }
                };

        private static final CssMetaData<AbstractPlot, String> WESTDATA
                = new CssMetaData<AbstractPlot, String>("-w-plot-westdata",
                        StyleConverter.getStringConverter(),
                        "") {

                    @Override
                    public boolean isSettable(AbstractPlot n) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<String> getStyleableProperty(AbstractPlot n) {
                        return (StyleableProperty<String>) n.westData;
                    }
                };
        private static final CssMetaData<AbstractPlot, String> SOUTHDATA
                = new CssMetaData<AbstractPlot, String>("-w-plot-southdata",
                        StyleConverter.getStringConverter(),
                        "") {

                    @Override
                    public boolean isSettable(AbstractPlot n) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<String> getStyleableProperty(AbstractPlot n) {
                        return (StyleableProperty<String>) n.southData;
                    }
                };
        private static final CssMetaData<AbstractPlot, String> LABELDATA
                = new CssMetaData<AbstractPlot, String>("-w-plot-labels",
                        StyleConverter.getStringConverter(),
                        "") {

                    @Override
                    public boolean isSettable(AbstractPlot n) {
                        return true;
                    }

                    @Override
                    public StyleableProperty<String> getStyleableProperty(AbstractPlot n) {
                        return (StyleableProperty<String>) n.labelData;
                    }
                };

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables
                    = new ArrayList<>(Pane.getClassCssMetaData());

            styleables.add(XDATA);
            styleables.add(YDATA);

            styleables.add(EASTDATA);
            styleables.add(NORTHDATA);
            styleables.add(WESTDATA);
            styleables.add(SOUTHDATA);

            styleables.add(LABELDATA);

            styleables.add(MARKERSTYLE);
            styleables.add(MARKERRADIUS);
            styleables.add(FILL);
            styleables.add(EDGECOLOR);
            styleables.add(EDGEWIDTH);

            styleables.add(LINECOLOR);
            styleables.add(LINEWIDTH);
            STYLEABLES = Collections.unmodifiableList(styleables);

        }

    }

    /**
     * VisualModel class that represents the colors, line width and styles etc
     * for the plot.
     */
    public class VisualModel {

        private final ArrayList<Text> labels = new ArrayList<>();
        private final DoubleProperty markerRadius = new StyleableDoubleProperty(5d) {

            @Override
            public void set(double val) {
                super.set(val);
                markerType.set(markerType.get());
            }

            @Override
            public Object getBean() {
                return AbstractPlot.VisualModel.this;
            }

            @Override
            public String getName() {
                return "markerRadius";
            }

            @Override
            public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                return StyleableProperties.MARKERRADIUS;
            }

        };
        /**
         * The list of markers.The size of the list must match the size of the
         * available x and y data series
         */
        private final ArrayList<Node> markerArray = new ArrayList<>();
        private final StyleableObjectProperty<Paint> lineColor = new StyleableObjectProperty<Paint>(Color.GRAY) {

            @Override
            public Object getBean() {
                return AbstractPlot.VisualModel.this;
            }

            @Override
            public String getName() {
                return "fill";
            }

            @Override
            public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
                return AbstractPlot.StyleableProperties.LINECOLOR;
            }
        };
        /**
         * Paint for the default marker edges e.g. a Color
         */
        private final ObjectProperty<Paint> edgeColor = new StyleableObjectProperty<Paint>(Color.DARKGRAY) {

            @Override
            public Object getBean() {
                return AbstractPlot.VisualModel.this;
            }

            @Override
            public String getName() {
                return "edgeColor";
            }

            @Override
            public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
                return AbstractPlot.StyleableProperties.EDGECOLOR;
            }
        };
        private final StyleableObjectProperty<Paint> fill = new StyleableObjectProperty<Paint>(Color.SLATEGRAY) {

            @Override
            public Object getBean() {
                return AbstractPlot.VisualModel.this;
            }

            @Override
            public String getName() {
                return "fill";
            }

            @Override
            public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
                return AbstractPlot.StyleableProperties.FILL;
            }
        };
        private final ObjectProperty<MARKERTYPE> markerType = new StyleableObjectProperty<MARKERTYPE>(MARKERTYPE.CIRCLE) {

            @Override
            public void set(MARKERTYPE type) {
                super.set(type);
                switch (type) {
                    case CIRCLE:
                        setMarkerTemplate(new Circle(getMarkerRadius()));
                        break;
                    case SQUARE:
                        setMarkerTemplate(new Square(getMarkerRadius()));
                        break;
                    case TRIANGLE:
                        setMarkerTemplate(new Triangle(getMarkerRadius()));
                        break;
                    case INVERTED_TRIANGLE:
                        setMarkerTemplate(new InvertedTriangle(getMarkerRadius()));
                        break;
                    case LEFT_TRIANGLE:
                        setMarkerTemplate(new LeftTriangle(getMarkerRadius()));
                        break;
                    case RIGHT_TRIANGLE:
                        setMarkerTemplate(new RightTriangle(getMarkerRadius()));
                        break;
                    case DIAMOND:
                        setMarkerTemplate(new Diamond(getMarkerRadius()));
                        break;
                    case PENTAGON:
                        setMarkerTemplate(new Pentagon(getMarkerRadius()));
                        break;
                    case HEXAGON:
                        setMarkerTemplate(new Hexagon(getMarkerRadius()));
                        break;
                    case CROSS:
                        setMarkerTemplate(new Cross(getMarkerRadius()));
                        break;
                    case PLUS:
                        setMarkerTemplate(new Plus(getMarkerRadius()));
                        break;
                    case ASTERISK:
                        setMarkerTemplate(new Asterisk(getMarkerRadius()));
                        break;
                    case SPHERE:
                        setMarkerTemplate(new Sphere(getMarkerRadius()));
                        break;
                    case ARROWHEAD:
                        setMarkerTemplate(new ArrowHead(getMarkerRadius()));
                        break;
                }
            }

            @Override
            public Object getBean() {
                return AbstractPlot.VisualModel.this;
            }

            @Override
            public String getName() {
                return "markerType";
            }

            @Override
            public CssMetaData<? extends Styleable, MARKERTYPE> getCssMetaData() {
                return StyleableProperties.MARKERSTYLE;
            }

        };
        /**
         * Template for automatically generating markers. For simple scatter
         * plots etc, the markerTemplate will generally be a simple circle,
         * triangle, etc. However, the template can be any JavaFX node.
         *
         */
        private Node markerTemplate = new Circle(5d);
        /**
         *
         */
        private GJCyclicArrayList<Dimension2D> dynamicMarkerSize = new GJCyclicArrayList<>();
        /**
         * Stroke for the default marker edges
         */
        private DoubleProperty edgeWidth = new StyleableDoubleProperty(1d) {

            @Override
            public Object getBean() {
                return AbstractPlot.VisualModel.this;
            }

            @Override
            public String getName() {
                return "edgeWidth";
            }

            @Override
            public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                return StyleableProperties.EDGEWIDTH;
            }

        };
        /**
         * Stroke for line
         */
        private DoubleProperty lineWidth = new StyleableDoubleProperty(1d) {

            @Override
            public Object getBean() {
                return AbstractPlot.VisualModel.this;
            }

            @Override
            public String getName() {
                return "lineWidth";
            }

            @Override
            public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                return StyleableProperties.LINEWIDTH;
            }

        };
        /**
         * alpha level used for the all Graphics2D drawing. Default=1.0f
         */
        private double alpha = 1d;
        private Effect elementEffect = new DropShadow();
        private Constructor markerBuilder;

        public VisualModel() {
            createBuilder();
        }

        /**
         * @return the markerArray
         */
        public final Node getMarkerTemplate() {
            return markerTemplate;
        }

        public final void setMarkerTemplate(Node node) {
            markerTemplate = node;
            createBuilder();
            markerArray.clear();
        }

        /**
         * Returns the marker at the specified index. If the index exceeds the
         * length of the list, the list will be padded to the required length
         * with default markers.
         *
         * @param index of the marker to retrieve
         * @return the marker
         */
        public Node getMarker(int index) {
            if (index > markerArray.size() - 1) {
                for (int k = markerArray.size() - 1; k < index; k++) {
                    markerArray.add(createMarker(k));
                }
            }
            return markerArray.get(index);
        }

        /**
         * Replaces the default marker at the specified index with a
         * user-supplied marker. Pads the marker array as required.
         *
         * @param index the marker to replace
         * @param marker the new Marker which may be any JavaFX Node
         */
        public void setMarker(int index, Node marker) {
            if (index > markerArray.size() - 1) {
                for (int k = markerArray.size() - 1; k < index; k++) {
                    markerArray.add(createMarker(k));
                }
            }
            markerArray.set(index, marker);
        }

        /**
         * @return the edgeColor
         */
        public Paint getEdgeColor() {
            return edgeColor.get();
        }

        /**
         * @param edgeColor the edgeColor to set
         */
        public void setEdgeColor(Paint edgeColor) {
            this.edgeColor.set(edgeColor);
        }

        public ObjectProperty<Paint> edgeColorProperty() {
            return edgeColor;
        }

        /**
         * @return the edgeWidth
         */
        public double getEdgeWidth() {
            return edgeWidth.get();
        }

        /**
         * @param edgeWidth the edgeWidth to set
         */
        public void setEdgeWidth(double edgeWidth) {
            this.edgeWidth.set(edgeWidth);
        }

        /**
         * @return the lineColor
         */
        public Paint getLineColor() {
            return lineColor.get();
        }

        /**
         * @param lineColor the lineColor to set
         */
        public void setLineColor(Paint lineColor) {
            this.lineColor.set(lineColor);
        }

        /**
         * @return the fill
         */
        public Paint getFill() {
            return fill.get();
        }

        /**
         * @param fill the fill to set
         */
        public void setFill(Paint fill) {
            this.fill.set(fill);
        }

        //public ObjectProperty<Paint> fillProperty() {return fill;};
        /**
         * @return the lineWidth
         */
        public double getLineWidth() {
            return lineWidth.get();
        }

        /**
         * @param lineWidth the lineWidth to set
         */
        public void setLineWidth(double lineWidth) {
            this.lineWidth.set(lineWidth);
        }

        /**
         * @return the alpha
         */
        public double getAlpha() {
            return alpha;
        }

        /**
         * @param alpha the alpha to set
         */
        public void setAlpha(double alpha) {
            this.alpha = alpha;
        }

        /**
         * @return the elementEffect
         */
        public Effect getElementEffect() {
            return elementEffect;
        }

        /**
         * @param effect the elementEffect to set
         */
        public void setElementEffect(Effect effect) {
            this.elementEffect = effect;
        }

        /**
         * @return the dynamicMarkerSize
         */
        public GJCyclicArrayList<Dimension2D> getDynamicMarkerSize() {
            return dynamicMarkerSize;
        }

        /**
         * @param dynamicMarkerSize the dynamicMarkerSize to set
         */
        public void setDynamicMarkerSize(GJCyclicArrayList<Dimension2D> dynamicMarkerSize) {
            this.dynamicMarkerSize = dynamicMarkerSize;
        }

        private Node createMarker(int index) {
            Node node;
            double w;
            double h;
            if (dynamicMarkerSize.size() > 0) {
                w = dynamicMarkerSize.get(index).getWidth();
                h = dynamicMarkerSize.get(index).getHeight();
            } else if (markerTemplate != null) {
                w = markerTemplate.getLayoutBounds().getWidth();
                h = markerTemplate.getLayoutBounds().getHeight();
            } else {
                w = 10;
                h = 10;
            }
            if (markerTemplate instanceof Sphere) {
                node = new Sphere(w / 2d, ((Sphere) markerTemplate).getDivisions());
            } else {
                try {
                    //e.g. a Circle
                    node = (Node) markerBuilder.newInstance(w / 2d);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    try {
                        // e.g. a Rectangle
                        node = (Node) markerBuilder.newInstance(w, h);
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex1) {
                        try {
                            // e.g. a Box
                            node = (Node) markerBuilder.newInstance(w / 2d, h / 2d, h / 2d);
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex2) {
                            return null;
                        }
                    }
                }
            }
            if (!(node instanceof CenteredShapeInterface)) {
                node.setTranslateX(markerTemplate.getTranslateX());
                node.setTranslateY(markerTemplate.getTranslateY());
            }

            if (node instanceof Shape3D) {
                if (((Shape3D) markerTemplate).getMaterial() != null) {
                    // If the user has set a material on th template, use it....
                    ((Shape3D) node).setMaterial(((Shape3D) markerTemplate).getMaterial());
                } else {
                    //...otherwise use the fill as the diffuseColor
                    ((Shape3D) node).setMaterial(new PhongMaterial((Color) fill.get()));
                }
                ((Shape3D) node).setDrawMode(((Shape3D) markerTemplate).getDrawMode());
            }

            node.setEffect(markerTemplate.getEffect());

            return node;
        }

        private void createBuilder() {
            if (markerTemplate != null) {
                try {
                    markerBuilder = markerTemplate.getClass().getConstructor(new Class[]{double.class});
                } catch (NoSuchMethodException ex) {
                    try {
                        markerBuilder = markerTemplate.getClass().getConstructor(new Class[]{double.class, double.class});
                    } catch (NoSuchMethodException | SecurityException ex1) {
                        try {
                            markerBuilder = markerTemplate.getClass().getConstructor(new Class[]{double.class, double.class, double.class});
                        } catch (NoSuchMethodException | SecurityException ex2) {
                        }
                    }
                }
            }
        }

        /**
         * @return the markerType
         */
        public MARKERTYPE getMarkerType() {
            return markerType.get();
        }

        /**
         * @param markerType the markerType to set
         */
        public void setMarkerType(MARKERTYPE markerType) {
            this.markerType.set(markerType);
        }

        public ObjectProperty<MARKERTYPE> markerTypeProperty() {
            return markerType;
        }

        /**
         * @return the markerRadius
         */
        public double getMarkerRadius() {
            return markerRadius.get();
        }

        /**
         * @param markerRadius the markerRadius to set
         */
        public void setMarkerRadius(double markerRadius) {
            this.markerRadius.set(markerRadius);
        }

        public DoubleProperty markerRadius() {
            return markerRadius;
        }

        /**
         * @return the labels
         */
        public ArrayList<Text> getLabels() {
            return labels;
        }

    }
}
