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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleableStringProperty;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.web.WebView;

/**
 *
 * @author Malcolm Lidierth
 */
public class MathLabel extends AnchorPane {


    private WebView viewer;

    private final StyleableStringProperty content = new StyleableStringProperty("Not yet set") {

        @Override
        public String get() {
            return super.get();
        }

        @Override
        public void set(String s) {
        final String webcontent=str0.concat(s).concat(str1);
        super.set(webcontent);
            Platform.runLater(() -> {
                viewer.getEngine().loadContent(webcontent);
            });
        }

        @Override
        public Object getBean() {
            return MathLabel.this;
        }

        @Override
        public String getName() {
            return "content";
        }

        @Override
        public CssMetaData<? extends Styleable, String> getCssMetaData() {
            return StyleableProperties.CONTENT;
        }

    };

    private final String str0 = "<!DOCTYPE html><html><head>"
            + "<script async src='https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML'></script>"
            + "</head><body style='background-color: rgba(0,0,0,0);'>";

    private final String str1 = "</body></html>";

    public MathLabel() {
        Platform.runLater(() -> {
            setBackground(Background.EMPTY);
            viewer = new WebView();
            getChildren().add(viewer);
            viewer.setPrefHeight(50d);
            AnchorPane.setLeftAnchor(viewer, 0d);
            AnchorPane.setRightAnchor(viewer, 0d);
            AnchorPane.setTopAnchor(viewer, 0d);
            AnchorPane.setBottomAnchor(viewer, 0d); 
            viewer.setStyle("-fx-background-color: rgba(0,0,0,0);");
            viewer.getEngine().setJavaScriptEnabled(true);
            viewer.setBlendMode(BlendMode.MULTIPLY);
        });
    }

    public void setContent(String tex) {
        this.content.set(tex);
    }

    public String getContent() {
        return content.get();
    }

    public StyleableStringProperty content() {
        return content;
    }
    
    public double getFontScale() {
        if (Platform.isFxApplicationThread()) {
            return viewer.getFontScale();
        } else {
            return -1d;
        }
    }

    public void setFontScale(final double sc) {
        Platform.runLater(()->{
           viewer.setFontScale(sc); 
        });
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
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     * @since JavaFX 8.0
     */
    public final static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return MathLabel.StyleableProperties.STYLEABLES;
    }

    /**
     * @treatAsPrivate implementation detail
     */
    private static class StyleableProperties {

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private static final CssMetaData<MathLabel, String> CONTENT
                = new CssMetaData<MathLabel, String>("-w-content",
                        StyleConverter.getStringConverter(), "Not yet set") {

                    @Override
                    public final boolean isSettable(MathLabel n) {
                        return n.content != null && !n.content.isBound();
                    }

                    @Override
                    public final StyleableProperty<String> getStyleableProperty(MathLabel n) {
                        return (StyleableProperty<String>) n.content;
                    }
                };

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables
                    = new ArrayList<>(AnchorPane.getClassCssMetaData());

            styleables.add(CONTENT);

            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

}
