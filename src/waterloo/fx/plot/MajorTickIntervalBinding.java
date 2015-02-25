/*
 *
 * <http://sigtool.github.io/waterlooFX/>
 *
 * Copyright Malcolm Lidierth 2015-.
 *
 * @author Malcolm Lidierth <a href="https://github.com/sigtool/waterlooFX/issues"> [Contact]</a>
 *
 * waterlooFX is free software:  you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * waterlooFX is distributed in the hope that it will  be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package waterloo.fx.plot;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.StyleableDoubleProperty;

/**
 *
 * @author Malcolm Lidierth
 */
public abstract class MajorTickIntervalBinding extends ObjectBinding<Double> {
    
    public abstract StyleableDoubleProperty getUserSpecifiedValue();

    public abstract void reset();

    static MajorTickIntervalBinding setupMajorYInterval(final Chart chart, final StyleableDoubleProperty userSpecifiedValue) {
        return new MajorTickIntervalBinding() {
            {
                bind(chart.yTopProperty());
                bind(chart.yBottomProperty());
            }

            @Override
            public StyleableDoubleProperty getUserSpecifiedValue() {
                return userSpecifiedValue;
            }

            @Override
            protected Double computeValue() {
                if (Double.isNaN(userSpecifiedValue.get())) {
                    double height = Math.abs(chart.getAxesBounds().getHeight());
                    double inc = chart.calcSpacing(height, chart.getPixelHeight());
                    return inc;
                } else {
                    return userSpecifiedValue.get();
                }
            }

            @Override
            public final void reset() {
                userSpecifiedValue.set(Double.NaN);
            }

            @Override
            public void dispose() {
                super.unbind(chart.yTopProperty());
                super.unbind(chart.yBottomProperty());
            }

            @Override
            public ObservableList<ObservableValue<?>> getDependencies() {
                ObservableList list = FXCollections.observableArrayList();
                list.add(chart.yTopProperty());
                list.add(chart.yBottomProperty());
                return list;
            }
        };
    }


    static MajorTickIntervalBinding setupMajorXInterval(final Chart chart, final StyleableDoubleProperty userSpecifiedValue) {
        return new MajorTickIntervalBinding() {
            {
                bind(chart.xLeftProperty());
                bind(chart.xRightProperty());
            }

            @Override
            public StyleableDoubleProperty getUserSpecifiedValue() {
                return userSpecifiedValue;
            }

            @Override
            protected Double computeValue() {
                if (Double.isNaN(userSpecifiedValue.get())) {
                    double width = Math.abs(chart.getAxesBounds().getWidth());
                    double inc = chart.calcSpacing(width, chart.getPixelWidth());
                    return inc;
                } else {
                    return userSpecifiedValue.get();
                }
            }

            @Override
            public final void reset() {
                userSpecifiedValue.set(Double.NaN);
            }

            @Override
            public void dispose() {
                super.unbind(chart.xLeftProperty());
                super.unbind(chart.xRightProperty());
            }

            @Override
            public ObservableList<ObservableValue<?>> getDependencies() {
                ObservableList list = FXCollections.observableArrayList();
                list.add(chart.xLeftProperty());
                list.add(chart.xRightProperty());
                return list;
            }
        };
    }

}
