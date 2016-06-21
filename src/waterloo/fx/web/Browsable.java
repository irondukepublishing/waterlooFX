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
package waterloo.fx.web;

import org.w3c.dom.Element;

/**
 *
 * @author ML
 */
public interface Browsable {

    public Element getElementModel(Element element, StringBuilder strBuilder);

}