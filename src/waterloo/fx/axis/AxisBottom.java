/* 
 * <http://waterloo.sourceforge.net/>
 *
 * Copyright King's College London 2014.
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
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.text.TextAlignment;
import waterloo.fx.plot.Chart;

/**
 *
 * @author ML
 */
public class AxisBottom extends AbstractAxisRegion {

    public AxisBottom(Chart layer) {
        super(layer);
        getChildren().add(getLine().get());
        getAxisLabel().setTextAlignment(TextAlignment.CENTER);
        setCursor(Cursor.DEFAULT);
        getAxisLabel().setFont(getFont());
        getAxisLabel().setTextOrigin(VPos.TOP);
        getChildren().add(getAxisLabel());
        prefWidthProperty().bind(layer.getFirstLayer().getView().prefWidthProperty());
        // Bind mouse sensitivity to the layer painted poperty for this axis
        mouseTransparentProperty().set(!layer.isBottomAxisPainted());
        mouseTransparentProperty().bind(layer.bottomAxisPaintedProperty().not());
        requestLayout();
    }

    @Override
    public double computePrefHeight(double w) {
        if (getTickLabels().size() > 0) {
            return getTickLabels().get(0).getBoundsInParent().getMaxY() + getAxisLabel().prefHeight(-1);
        } else {
            return 50d;
        }
    }

    private void doLayout() {
        getLine().get();
        computeValue();
        double p = getLine().get().getBoundsInParent().getMaxY();
        if (getLayer().isBottomAxisLabelled()) {
            if (getTickLabels().size() > 0) {
                getChildren().stream().filter(x -> x instanceof TickLabel).forEach((Node x) -> {
                    TickLabel text = (TickLabel) x;
                    //text.setFont(getFont());
                    text.setLayoutX(text.getXpos());
                    text.setLayoutY(p);
                });
            }
            addAxisLabel();
            if (getTickLabels().size() > 0) {
                getAxisLabel().setFont(getFont());
                getAxisLabel().setLayoutY(getTickLabels().get(0).getBoundsInParent().getMaxY());
                getAxisLabel().setLayoutX(getLayer().getView().getWidth() / 2d
                        - getAxisLabel().getBoundsInParent().getWidth() / 2d);
            }
        } else {
            getTickLabels().stream().forEach(x -> getChildren().remove(x));
            removeAxisLabel();
        }
    }

    @Override
    public void layoutChildren() {
        doLayout();
        super.layoutChildren();
    }

    private void computeValue() {
        getTickLabels().stream().forEach((TickLabel x) -> {
            getChildren().remove(x);
        });

        if (getLayer().isBottomAxisLabelled()) {
            final double w = this.calcTickLabelWidth();
            getLayer().getAxisSet().getXTransform().get().stream()
                    .filter(x -> x >= getLayer().getXMin() && x <= getLayer().getXMax())
                    .forEach((Double x) -> {
                        Point2D p1;
                        TickLabel text = null;
                        if (isCategorical()) {
                            if (getCategories().containsKey(x.intValue())) {
                                text = new TickLabel(getCategories().get(x.intValue()));
                            }
                        } else {
                            text = new TickLabel(getLayer().getAxisSet().getXTransform().getTickLabel(x));
                        }
                        if (text != null) {
                            p1 = getLayer().toPixel(x, getLayer().getYBottom());
                            p1 = getLayer().getView().localToParent(p1);
                            p1 = parentToLocal(p1);
                            text.setFont(getFont());
                            text.setFill(getLayer().getAxisColor());
                            text.setXpos(p1.getX() - text.prefWidth(0) / 2d);
                            text.setTextOrigin(VPos.TOP);
                            getChildren().add(text);

                        }
                    });
        } 
    }
}
