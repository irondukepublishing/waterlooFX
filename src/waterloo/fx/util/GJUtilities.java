 /* 
*
 * <http://waterloo.sourceforge.net/>
 *
 * Copyright King's College London 2011-, 2014
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
package waterloo.fx.util;


import javafx.scene.paint.Color;

/**
 * <p>
 * This code is part of the Waterloo Scientific Graphics package from King's
 * College London {@literal http://waterloo.sourceforge.net/}
 * </p>
 *
 * @author Malcolm Lidierth <a
 * href="http://sourceforge.net/p/waterloo/discussion/"> [Contact]</a>
 */
public final class GJUtilities {

    private static Color[] colorseries = {Color.BLUE,
        Color.rgb(0, 128, 0),
        Color.RED,
        Color.rgb(0, 192, 192),
        Color.rgb(192, 0, 192),
        Color.rgb(192, 192, 0),
        Color.rgb(64, 64, 64)};


    /*
     * Private constructor - non-instantiable class
     */
    private GJUtilities() {
    }

    public static Color getColor(int n) {
        n = n % (colorseries.length - 1);
        return colorseries[n];
    }

    public static void setColors(Color[] c) {
        colorseries = c.clone();
    }

    public static void setGray() {
        colorseries = new Color[8];
        int v = 32;
        for (int k = 0; k < 8; k += 2) {
            colorseries[k] = Color.rgb(v, v, v);
            colorseries[k] = Color.rgb(v + 48, v + 48, v + 48);
            v += 32;
        }
    }

    public void setBlack() {
        colorseries = new Color[8];
        for (int k = 0; k < 8; k++) {
            colorseries[k] = Color.BLACK;
        }
    }


    public static String getSuperscripts(String s) {
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < s.length(); k++) {
            switch (s.charAt(k)) {
                case '0':
                    sb.append("\u2070");
                    break;
                case '1':
                    sb.append("\u00B9");
                    break;
                case '2':
                    sb.append("\u00B2");
                    break;
                case '3':
                    sb.append("\u00B3");
                    break;
                case '4':
                    sb.append("\u2074");
                    break;
                case '5':
                    sb.append("\u2075");
                    break;
                case '6':
                    sb.append("\u2076");
                    break;
                case '7':
                    sb.append("\u2077");
                    break;
                case '8':
                    sb.append("\u2078");
                    break;
                case '9':
                    sb.append("\u2079");
                    break;
                case '+':
                    sb.append("\u207A");
                    break;
                case '-':
                    sb.append("\u207B");
                    break;
                case '=':
                    sb.append("\u207C");
                    break;
                case '(':
                    sb.append("\u207D");
                    break;
                case ')':
                    sb.append("\u207E");
                    break;
                case 'n':
                    sb.append("\u207F");
                    break;
                case '.':
                    sb.append("\u00B7");
                    break;
                default:
                    sb.append(" ");
            }
        }
        sb.trimToSize();
        return sb.toString();
    }
}
