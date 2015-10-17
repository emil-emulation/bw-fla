/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.common.utils;

import com.google.common.base.CharMatcher;

/**
 * This class provides common String operations
 * 
 * @author Leander
 * 
 */
public class StringUtils {


  /**
   * This method will filter all characters from a String not contained in {@link CharMatcher}.
   * 
   * @param string
   * @return
   */
  public static String filterNonASCII(String string) {
	CharMatcher desired = CharMatcher.ASCII; // CharMatcher.ASCII.precomputed();
	return desired.retainFrom(string);
  }

  /**
   * This method will filter all characters from a String not contained in {@link CharMatcher}.
   * 
   * @param string
   * @return
   */
  public static String filterWhitespace(String string) {
	CharMatcher desired = CharMatcher.WHITESPACE; // CharMatcher.ASCII.precomputed();
	return desired.removeFrom(string);
  }

  /**
   * Removes whitepsaces and non ASCI characters from a String.
   * 
   * @param string
   * @return
   */
  public static String makePathSafe(String string) {
	return (filterNonASCII(filterWhitespace(string)));
  }
}
