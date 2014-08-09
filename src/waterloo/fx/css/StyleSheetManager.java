/* 
*
 * <http://waterloo.sourceforge.net/>
 *
 * Copyright King's College London  2013-
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
package waterloo.fx.css;

import java.io.File;

/**
 * This simple manager allows a single css style-sheet to be set for use by the 
 * Waterloo code.
 * 
 * By default, the getCss() function returns a String path to the embedded
 * waterloo.css file in the jar distribution.
 * 
 * Use setCss(String path) to specify a local file on the users machine.
 * 
 * @author Malcolm Lidierth
 */
public class StyleSheetManager {

    private static String css=null;

    private StyleSheetManager() {

    }

    /**
     * @return the css
     */
    public static String getCss() {
        if (css==null){
            css = StyleSheetManager.class.getClassLoader().getResource("kcl/waterloo/fx/css/waterloofx.css").toExternalForm();
        }
        return css;
    }

    /**
     * @param CSS file to use as a fully qualified String path
     */
    public static void setCss(String CSS) {
        css = CSS;
    }

    /**
     * @param CSS file to use as a java.io.File instance
     */
    public static void setCss(File CSS) {
        css = CSS.getPath();
    }
}
