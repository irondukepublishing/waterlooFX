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

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import waterloo.fx.plot.Chart;
import waterloo.fx.transforms.AbstractTransform;
import waterloo.fx.transforms.NOPTransform;

/**
 * AxisSet class.
 *
 * This class collects that axes associated with a {@code Chart} and paints the
 * grid for those axes in the {@code Chart}'s view.
 *
 * The {@code AxisSet} also provides support for the transforms for the x- an y-
 * data, allowing a single transform instance to be shared by top/bottom and
 * left/right axes.
 *
 * <strong>Note:</strong> The axes in a set should all be in the same
 * {@code Chart} instance.
 *
 */
public class AxisSet {

    private final AxisTop axisTop;
    private final AxisBottom axisBottom;
    private final AxisLeft axisLeft;
    private final AxisRight axisRight;
    private final Chart layer;
    private AbstractTransform xTransform = new NOPTransform();
    private AbstractTransform yTransform = new NOPTransform();

    /**
     * Constructs the axis set.
     *
     * <strong>Note:</strong> The supplied axes should all be in the same
     * {@code Chart} instance.
     *
     * @param axisRight - right axis instance.
     * @param axisTop - top axis instance.
     * @param axisLeft - left axis instance.
     * @param axisBottom - bottom axis instance.
     */
    public AxisSet(AxisRight axisRight, AxisTop axisTop, AxisLeft axisLeft, AxisBottom axisBottom) {
        this.axisRight = axisRight;
        this.axisTop = axisTop;
        this.axisLeft = axisLeft;
        this.axisBottom = axisBottom;
        layer = (Chart) axisRight.getLayer();
        xTransform.updateBindings(layer, AbstractTransform.AXIS.HORIZONTAL);
        yTransform.updateBindings(layer, AbstractTransform.AXIS.VERTICAL);
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
     * Returns a Point2D instance containing the inverse transform for the data
     * point represented by the supplied [x,y] pair (in axis co-oordinate
     * space).
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the inverse transformed values as a Point2D instance.
     */
    public Point2D getInverse(double x, double y) {
        return new Point2D(xTransform.getInverse(x, Double.NaN).getX(), yTransform.getInverse(Double.NaN, y).getY());
    }

    /**
     * Returns a Point2D instance containing the transform for the data point
     * represented by the supplied [x,y] pair (in axis co-oordinate space).
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the transformed values as a Point2D instance.
     */
    public Point2D getData(double x, double y) {
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
    public void setXTransform(AbstractTransform xTransform) {
        xTransform.updateBindings(layer, AbstractTransform.AXIS.HORIZONTAL);
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
    public void setYTransform(AbstractTransform yTransform) {
        yTransform.updateBindings(layer, AbstractTransform.AXIS.VERTICAL);
        this.yTransform = yTransform;
    }

    /**
     * Renders the canvas for this chart view. This includes the grid, internal
     * axes etc.
     *
     * @param g the GraphicsContext retrieved from the canvas instance.
     */
    public void paintGrid(GraphicsContext g) {

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
                 * This paints the concentric rings of the grid. Note that this
                 * is done for both x and y (which may have different ranges).
                 * It will generally make sense for the interval on the two axes
                 * to be the same.
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

    private static Point2D getIntersection(Ellipse e0, double x0, double y0) {
        //See: http://mathworld.wolfram.com/Ellipse-LineIntersection.html
        double a = e0.getRadiusX();
        double b = e0.getRadiusY();
        double x = a * b * x0 / (Math.sqrt(a * a * y0 * y0 + b * b * x0 * x0));
        double y = a * b * y0 / (Math.sqrt(a * a * y0 * y0 + b * b * x0 * x0));
        return new Point2D(x, y);
    }
}
