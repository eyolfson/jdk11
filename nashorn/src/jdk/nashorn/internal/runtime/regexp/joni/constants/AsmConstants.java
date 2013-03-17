/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package jdk.nashorn.internal.runtime.regexp.joni.constants;

public interface AsmConstants {
    final int THIS = 0;

    // argument indexes
    final int RANGE             = 1;
    final int SSTART            = 2;
    final int SPREV             = 3;

    // local var indexes
    final int S                 = 4;            // current index
    final int BYTES             = 5;            // string
    final int LAST_INDEX        = BYTES + 1;

    // frequently used field names (all ints)
    final String STR            = "str";
    final String END            = "end";
    final String MSA_START      = "msaStart";
    final String MSA_OPTONS     = "msaOptions";
    final String MSA_BEST_LEN   = "msaBestLen";
    final String MSA_BEST_S     = "msaBestS";
    final String MSA_BEGIN      = "msaBegin";
    final String MSA_END        = "msaEnd";

    // generated field names
    final String BITSET         = "bitset";
    final String CODERANGE      = "range";
    final String TEMPLATE       = "template";
}
