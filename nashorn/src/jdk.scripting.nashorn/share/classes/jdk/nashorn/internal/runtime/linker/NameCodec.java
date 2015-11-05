/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.nashorn.internal.runtime.linker;

/**
 * <p>
 * Implements the name mangling and demangling as specified by John Rose's
 * <a href="https://blogs.oracle.com/jrose/entry/symbolic_freedom_in_the_vm"
 * target="_blank">"Symbolic Freedom in the VM"</a> article. Normally, you would
 * mangle the names in the call sites as you're generating bytecode, and then
 * demangle them when you receive them in bootstrap methods.
 * </p>
 * <p>
 * This code is derived from sun.invoke.util.BytecodeName. Apart from subsetting that
 * class, we don't want to create dependency between non-exported package from java.base
 * to nashorn module.
 * </p>
 *
 * <h3>Comment from BytecodeName class reproduced here:</h3>
 *
 * Includes universal mangling rules for the JVM.
 *
 * <h3>Avoiding Dangerous Characters </h3>
 *
 * <p>
 * The JVM defines a very small set of characters which are illegal
 * in name spellings.  We will slightly extend and regularize this set
 * into a group of <cite>dangerous characters</cite>.
 * These characters will then be replaced, in mangled names, by escape sequences.
 * In addition, accidental escape sequences must be further escaped.
 * Finally, a special prefix will be applied if and only if
 * the mangling would otherwise fail to begin with the escape character.
 * This happens to cover the corner case of the null string,
 * and also clearly marks symbols which need demangling.
 * </p>
 * <p>
 * Dangerous characters are the union of all characters forbidden
 * or otherwise restricted by the JVM specification,
 * plus their mates, if they are brackets
 * (<code><big><b>[</b></big></code> and <code><big><b>]</b></big></code>,
 * <code><big><b>&lt;</b></big></code> and <code><big><b>&gt;</b></big></code>),
 * plus, arbitrarily, the colon character <code><big><b>:</b></big></code>.
 * There is no distinction between type, method, and field names.
 * This makes it easier to convert between mangled names of different
 * types, since they do not need to be decoded (demangled).
 * </p>
 * <p>
 * The escape character is backslash <code><big><b>\</b></big></code>
 * (also known as reverse solidus).
 * This character is, until now, unheard of in bytecode names,
 * but traditional in the proposed role.
 *
 * </p>
 * <h3> Replacement Characters </h3>
 *
 *
 * <p>
 * Every escape sequence is two characters
 * (in fact, two UTF8 bytes) beginning with
 * the escape character and followed by a
 * <cite>replacement character</cite>.
 * (Since the replacement character is never a backslash,
 * iterated manglings do not double in size.)
 * </p>
 * <p>
 * Each dangerous character has some rough visual similarity
 * to its corresponding replacement character.
 * This makes mangled symbols easier to recognize by sight.
 * </p>
 * <p>
 * The dangerous characters are
 * <code><big><b>/</b></big></code> (forward slash, used to delimit package components),
 * <code><big><b>.</b></big></code> (dot, also a package delimiter),
 * <code><big><b>;</b></big></code> (semicolon, used in signatures),
 * <code><big><b>$</b></big></code> (dollar, used in inner classes and synthetic members),
 * <code><big><b>&lt;</b></big></code> (left angle),
 * <code><big><b>&gt;</b></big></code> (right angle),
 * <code><big><b>[</b></big></code> (left square bracket, used in array types),
 * <code><big><b>]</b></big></code> (right square bracket, reserved in this scheme for language use),
 * and <code><big><b>:</b></big></code> (colon, reserved in this scheme for language use).
 * Their replacements are, respectively,
 * <code><big><b>|</b></big></code> (vertical bar),
 * <code><big><b>,</b></big></code> (comma),
 * <code><big><b>?</b></big></code> (question mark),
 * <code><big><b>%</b></big></code> (percent),
 * <code><big><b>^</b></big></code> (caret),
 * <code><big><b>_</b></big></code> (underscore), and
 * <code><big><b>{</b></big></code> (left curly bracket),
 * <code><big><b>}</b></big></code> (right curly bracket),
 * <code><big><b>!</b></big></code> (exclamation mark).
 * In addition, the replacement character for the escape character itself is
 * <code><big><b>-</b></big></code> (hyphen),
 * and the replacement character for the null prefix is
 * <code><big><b>=</b></big></code> (equal sign).
 * </p>
 * <p>
 * An escape character <code><big><b>\</b></big></code>
 * followed by any of these replacement characters
 * is an escape sequence, and there are no other escape sequences.
 * An equal sign is only part of an escape sequence
 * if it is the second character in the whole string, following a backslash.
 * Two consecutive backslashes do <em>not</em> form an escape sequence.
 * </p>
 * <p>
 * Each escape sequence replaces a so-called <cite>original character</cite>
 * which is either one of the dangerous characters or the escape character.
 * A null prefix replaces an initial null string, not a character.
 * </p>
 * <p>
 * All this implies that escape sequences cannot overlap and may be
 * determined all at once for a whole string.  Note that a spelling
 * string can contain <cite>accidental escapes</cite>, apparent escape
 * sequences which must not be interpreted as manglings.
 * These are disabled by replacing their leading backslash with an
 * escape sequence (<code><big><b>\-</b></big></code>).  To mangle a string, three logical steps
 * are required, though they may be carried out in one pass:
 * </p>
 * <ol>
 *   <li>In each accidental escape, replace the backslash with an escape sequence
 * (<code><big><b>\-</b></big></code>).</li>
 *   <li>Replace each dangerous character with an escape sequence
 * (<code><big><b>\|</b></big></code> for <code><big><b>/</b></big></code>, etc.).</li>
 *   <li>If the first two steps introduced any change, <em>and</em>
 * if the string does not already begin with a backslash, prepend a null prefix (<code><big><b>\=</b></big></code>).</li>
 * </ol>
 *
 * To demangle a mangled string that begins with an escape,
 * remove any null prefix, and then replace (in parallel)
 * each escape sequence by its original character.
 * <p>Spelling strings which contain accidental
 * escapes <em>must</em> have them replaced, even if those
 * strings do not contain dangerous characters.
 * This restriction means that mangling a string always
 * requires a scan of the string for escapes.
 * But then, a scan would be required anyway,
 * to check for dangerous characters.
 *
 * </p>
 * <h3> Nice Properties </h3>
 *
 * <p>
 * If a bytecode name does not contain any escape sequence,
 * demangling is a no-op:  The string demangles to itself.
 * Such a string is called <cite>self-mangling</cite>.
 * Almost all strings are self-mangling.
 * In practice, to demangle almost any name &ldquo;found in nature&rdquo;,
 * simply verify that it does not begin with a backslash.
 * </p>
 * <p>
 * Mangling is a one-to-one function, while demangling
 * is a many-to-one function.
 * A mangled string is defined as <cite>validly mangled</cite> if
 * it is in fact the unique mangling of its spelling string.
 * Three examples of invalidly mangled strings are <code><big><b>\=foo</b></big></code>,
 * <code><big><b>\-bar</b></big></code>, and <code><big><b>baz\!</b></big></code>, which demangle to <code><big><b>foo</b></big></code>, <code><big><b>\bar</b></big></code>, and
 * <code><big><b>baz\!</b></big></code>, but then remangle to <code><big><b>foo</b></big></code>, <code><big><b>\bar</b></big></code>, and <code><big><b>\=baz\-!</b></big></code>.
 * If a language back-end or runtime is using mangled names,
 * it should never present an invalidly mangled bytecode
 * name to the JVM.  If the runtime encounters one,
 * it should also report an error, since such an occurrence
 * probably indicates a bug in name encoding which
 * will lead to errors in linkage.
 * However, this note does not propose that the JVM verifier
 * detect invalidly mangled names.
 * </p>
 * <p>
 * As a result of these rules, it is a simple matter to
 * compute validly mangled substrings and concatenations
 * of validly mangled strings, and (with a little care)
 * these correspond to corresponding operations on their
 * spelling strings.
 * </p>
 * <ul>
 *   <li>Any prefix of a validly mangled string is also validly mangled,
 * although a null prefix may need to be removed.</li>
 *   <li>Any suffix of a validly mangled string is also validly mangled,
 * although a null prefix may need to be added.</li>
 *   <li>Two validly mangled strings, when concatenated,
 * are also validly mangled, although any null prefix
 * must be removed from the second string,
 * and a trailing backslash on the first string may need escaping,
 * if it would participate in an accidental escape when followed
 * by the first character of the second string.</li>
 * </ul>
 * <p>If languages that include non-Java symbol spellings use this
 * mangling convention, they will enjoy the following advantages:
 * </p>
 * <ul>
 *   <li>They can interoperate via symbols they share in common.</li>
 *   <li>Low-level tools, such as backtrace printers, will have readable displays.</li>
 *   <li>Future JVM and language extensions can safely use the dangerous characters
 * for structuring symbols, but will never interfere with valid spellings.</li>
 *   <li>Runtimes and compilers can use standard libraries for mangling and demangling.</li>
 *   <li>Occasional transliterations and name composition will be simple and regular,
 * for classes, methods, and fields.</li>
 *   <li>Bytecode names will continue to be compact.
 * When mangled, spellings will at most double in length, either in
 * UTF8 or UTF16 format, and most will not change at all.</li>
 * </ul>
 *
 *
 * <h3> Suggestions for Human Readable Presentations </h3>
 *
 *
 * <p>
 * For human readable displays of symbols,
 * it will be better to present a string-like quoted
 * representation of the spelling, because JVM users
 * are generally familiar with such tokens.
 * We suggest using single or double quotes before and after
 * mangled symbols which are not valid Java identifiers,
 * with quotes, backslashes, and non-printing characters
 * escaped as if for literals in the Java language.
 * </p>
 * <p>
 * For example, an HTML-like spelling
 * <code><big><b>&lt;pre&gt;</b></big></code> mangles to
 * <code><big><b>\^pre\_</b></big></code> and could
 * display more cleanly as
 * <code><big><b>'&lt;pre&gt;'</b></big></code>,
 * with the quotes included.
 * Such string-like conventions are <em>not</em> suitable
 * for mangled bytecode names, in part because
 * dangerous characters must be eliminated, rather
 * than just quoted.  Otherwise internally structured
 * strings like package prefixes and method signatures
 * could not be reliably parsed.
 * </p>
 * <p>
 * In such human-readable displays, invalidly mangled
 * names should <em>not</em> be demangled and quoted,
 * for this would be misleading.  Likewise, JVM symbols
 * which contain dangerous characters (like dots in field
 * names or brackets in method names) should not be
 * simply quoted.  The bytecode names
 * <code><big><b>\=phase\,1</b></big></code> and
 * <code><big><b>phase.1</b></big></code> are distinct,
 * and in demangled displays they should be presented as
 * <code><big><b>'phase.1'</b></big></code> and something like
 * <code><big><b>'phase'.1</b></big></code>, respectively.
 * </p>
 */
public final class NameCodec {
    private NameCodec() {
    }

    private static final char ESCAPE_C = '\\';
    // empty escape sequence to avoid a null name or illegal prefix
    private static final char NULL_ESCAPE_C = '=';
    private static final String NULL_ESCAPE = ESCAPE_C+""+NULL_ESCAPE_C;

    /**
     * Canonical encoding for the empty name.
     */
    public static final String EMPTY_NAME =  new String(new char[] { ESCAPE_C, NULL_ESCAPE_C });

    /**
     * Encodes ("mangles") an unencoded symbolic name.
     * @param name the symbolic name to mangle
     * @return the mangled form of the symbolic name.
     */
    public static String encode(final String name) {
        String bn = mangle(name);
        assert((Object)bn == name || looksMangled(bn)) : bn;
        assert(name.equals(decode(bn))) : name;
        return bn;
    }

    /**
     * Decodes ("demangles") an encoded symbolic name.
     * @param name the symbolic name to demangle
     * @return the demangled form of the symbolic name.
     */
    public static String decode(final String name) {
        String sn = name;
        if (!sn.isEmpty() && looksMangled(name)) {
            sn = demangle(name);
            assert(name.equals(mangle(sn))) : name+" => "+sn+" => "+mangle(sn);
        }
        return sn;
    }

    private static boolean looksMangled(String s) {
        return s.charAt(0) == ESCAPE_C;
    }

    private static String mangle(String s) {
        if (s.length() == 0)
            return NULL_ESCAPE;

        // build this lazily, when we first need an escape:
        StringBuilder sb = null;

        for (int i = 0, slen = s.length(); i < slen; i++) {
            char c = s.charAt(i);

            boolean needEscape = false;
            if (c == ESCAPE_C) {
                if (i+1 < slen) {
                    char c1 = s.charAt(i+1);
                    if ((i == 0 && c1 == NULL_ESCAPE_C)
                        || c1 != originalOfReplacement(c1)) {
                        // an accidental escape
                        needEscape = true;
                    }
                }
            } else {
                needEscape = isDangerous(c);
            }

            if (!needEscape) {
                if (sb != null)  sb.append(c);
                continue;
            }

            // build sb if this is the first escape
            if (sb == null) {
                sb = new StringBuilder(s.length()+10);
                // mangled names must begin with a backslash:
                if (s.charAt(0) != ESCAPE_C && i > 0)
                    sb.append(NULL_ESCAPE);
                // append the string so far, which is unremarkable:
                sb.append(s, 0, i);
            }

            // rewrite \ to \-, / to \|, etc.
            sb.append(ESCAPE_C);
            sb.append(replacementOf(c));
        }

        if (sb != null)   return sb.toString();

        return s;
    }

    private static String demangle(String s) {
        // build this lazily, when we first meet an escape:
        StringBuilder sb = null;

        int stringStart = 0;
        if (s.startsWith(NULL_ESCAPE))
            stringStart = 2;

        for (int i = stringStart, slen = s.length(); i < slen; i++) {
            char c = s.charAt(i);

            if (c == ESCAPE_C && i+1 < slen) {
                // might be an escape sequence
                char rc = s.charAt(i+1);
                char oc = originalOfReplacement(rc);
                if (oc != rc) {
                    // build sb if this is the first escape
                    if (sb == null) {
                        sb = new StringBuilder(s.length());
                        // append the string so far, which is unremarkable:
                        sb.append(s, stringStart, i);
                    }
                    ++i;  // skip both characters
                    c = oc;
                }
            }

            if (sb != null)
                sb.append(c);
        }

        if (sb != null)   return sb.toString();

        return s.substring(stringStart);
    }

    private static final String DANGEROUS_CHARS   = "\\/.;:$[]<>"; // \\ must be first
    private static final String REPLACEMENT_CHARS =  "-|,?!%{}^_";
    private static final int DANGEROUS_CHAR_FIRST_INDEX = 1; // index after \\

    private static final long[] SPECIAL_BITMAP = new long[2];  // 128 bits
    static {
        String SPECIAL = DANGEROUS_CHARS + REPLACEMENT_CHARS;
        for (char c : SPECIAL.toCharArray()) {
            SPECIAL_BITMAP[c >>> 6] |= 1L << c;
        }
    }

    private static boolean isSpecial(char c) {
        if ((c >>> 6) < SPECIAL_BITMAP.length)
            return ((SPECIAL_BITMAP[c >>> 6] >> c) & 1) != 0;
        else
            return false;
    }

    private static char replacementOf(char c) {
        if (!isSpecial(c))  return c;
        int i = DANGEROUS_CHARS.indexOf(c);
        if (i < 0)  return c;
        return REPLACEMENT_CHARS.charAt(i);
    }

    private static char originalOfReplacement(char c) {
        if (!isSpecial(c))  return c;
        int i = REPLACEMENT_CHARS.indexOf(c);
        if (i < 0)  return c;
        return DANGEROUS_CHARS.charAt(i);
    }

    private static boolean isDangerous(char c) {
        if (!isSpecial(c))  return false;
        return (DANGEROUS_CHARS.indexOf(c) >= DANGEROUS_CHAR_FIRST_INDEX);
    }
}
