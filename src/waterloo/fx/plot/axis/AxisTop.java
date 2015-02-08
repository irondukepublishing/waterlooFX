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
package waterloo.fx.plot.axis;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.text.TextAlignment;
import waterloo.fx.plot.Chart;

/**
 * Implementation of the {@code AbstractAxisRegion} for a top axis.
 *
 * @author ML
 */
public class AxisTop extends AbstractAxisRegion {

    public AxisTop(Chart layer) {
        super(layer);
        getChildren().add(getLine().get());
        getAxisLabel().setTextAlignment(TextAlignment.CENTER);
        getAxisLabel().setTextOrigin(VPos.BOTTOM);
        setCursor(Cursor.DEFAULT);
        getChildren().add(getAxisLabel());
        prefWidthProperty().bind(layer.getFirstLayer().getView().prefWidthProperty());
        // Bind mouse sensitivity to the layer painted poperty for this axis
        mouseTransparentProperty().set(!layer.isTopAxisPainted());
        mouseTransparentProperty().bind(layer.topAxisPaintedProperty().not());
        requestLayout();
    }

    /**
     * Returns the required height of the axis to accommodate the labels.
     *
     * @param w the width of the axis region. Use 0d in practice.
     *
     * @return the required height
     */
    @Override
    public double computePrefHeight(double w) {
        if (getTickLabels().size() > 0) {
            return getAxisLabel().prefHeight(-1d) * 3d;
        } else {
            return 50d;
        }
    }

    private void doLayout() {
        getLine().get();
        computeValue();
        if (getLayer().isTopAxisLabelled()) {
            double p = getLayer().isTopAxisPainted()
                    ? getLine().get().getBoundsInParent().getMinY()
                    : getHeight() - 2d;
            if (getTickLabels().size() > 0) {
                getChildren().stream().filter(x -> x instanceof TickLabel).forEach((Node x) -> {
                    TickLabel text = (TickLabel) x;
                    text.setLayoutX(text.getXpos());
                    text.setLayoutY(p);
                });
            }
            addAxisLabel();
            if (getTickLabels().size() > 0) {
                getAxisLabel().setFont(getFont());
                getAxisLabel().setLayoutY(getTickLabels().get(0).getBoundsInParent().getMinY());
                getAxisLabel().setLayoutX(getLayer().getView().prefWidth(0d) / 2d
                        - getAxisLabel().prefWidth(0d) / 2d);
            }
        } else {
            getTickLabels().stream().forEach(x -> getChildren().remove(x));
            removeAxisLabel();
        }
    }

    /**
     * Recalculates the layout for {@code Nodes} in this axis and calls
     * {@code super.layoutChildren()} to do the real work.
     */
    @Override
    public void layoutChildren() {
        doLayout();
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
            getLayer().getAxisSet().getXTransform().get().stream()
                    .filter(x -> x >= getLayer().getXMin() && x <= getLayer().getXMax())
                    .forEach((Double x) -> {
                        TickLabel text = null;
                        if (isCategorical()) {
                            if (getCategories().containsKey(x.intValue())) {
                                text = new TickLabel(getCategories().get(x.intValue()));
                            }
                        } else {
                            text = new TickLabel(getLayer().getAxisSet().getXTransform().getTickLabel(x));
                        }
                        if (text != null) {
                            Point2D p1 = getLayer().toPixel(x, getLayer().getYTop());
                            p1 = getLayer().getView().localToParent(p1);
                            p1 = parentToLocal(p1);
                            text.setFont(getFont());
                            text.setFill(getLayer().getAxisColor());
                            text.setXpos(p1.getX() - text.prefWidth(0) / 2d);
                            text.setTextOrigin(VPos.BOTTOM);
                            getChildren().add(text);
                        }
                    });
        }
    }

}
