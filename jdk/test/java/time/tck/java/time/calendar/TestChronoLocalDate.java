/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

/*
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package tck.java.time.calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import java.time.Duration;
import java.time.LocalDate;
import java.time.calendar.HijrahChrono;
import java.time.calendar.JapaneseChrono;
import java.time.calendar.MinguoChrono;
import java.time.calendar.ThaiBuddhistChrono;
import java.time.temporal.Chrono;
import java.time.temporal.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.SimplePeriod;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.format.DateTimeBuilder;
import java.time.temporal.TemporalAdder;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalSubtractor;
import java.time.temporal.ValueRange;
import java.time.temporal.TemporalUnit;
import java.time.temporal.ISOChrono;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test assertions that must be true for all built-in chronologies.
 */
@Test
public class TestChronoLocalDate {

    //-----------------------------------------------------------------------
    // regular data factory for names and descriptions of available calendars
    //-----------------------------------------------------------------------
    @DataProvider(name = "calendars")
    Chrono<?>[][] data_of_calendars() {
        return new Chrono<?>[][]{
                    {HijrahChrono.INSTANCE},
                    {ISOChrono.INSTANCE},
                    {JapaneseChrono.INSTANCE},
                    {MinguoChrono.INSTANCE},
                    {ThaiBuddhistChrono.INSTANCE}};
    }

    @Test(groups={"tck"}, dataProvider="calendars")
    public void test_badWithAdjusterChrono(Chrono<?> chrono) {
        LocalDate refDate = LocalDate.of(1900, 1, 1);
        ChronoLocalDate<?> date = chrono.date(refDate);
        for (Chrono<?>[] clist : data_of_calendars()) {
            Chrono<?> chrono2 = clist[0];
            ChronoLocalDate<?> date2 = chrono2.date(refDate);
            TemporalAdjuster adjuster = new FixedAdjuster(date2);
            if (chrono != chrono2) {
                try {
                    date.with(adjuster);
                    Assert.fail("WithAdjuster should have thrown a ClassCastException");
                } catch (ClassCastException cce) {
                    // Expected exception; not an error
                }
            } else {
                // Same chronology,
                ChronoLocalDate<?> result = date.with(adjuster);
                assertEquals(result, date2, "WithAdjuster failed to replace date");
            }
        }
    }

    @Test(groups={"tck"}, dataProvider="calendars")
    public void test_badPlusAdjusterChrono(Chrono<?> chrono) {
        LocalDate refDate = LocalDate.of(1900, 1, 1);
        ChronoLocalDate<?> date = chrono.date(refDate);
        for (Chrono<?>[] clist : data_of_calendars()) {
            Chrono<?> chrono2 = clist[0];
            ChronoLocalDate<?> date2 = chrono2.date(refDate);
            TemporalAdder adjuster = new FixedAdjuster(date2);
            if (chrono != chrono2) {
                try {
                    date.plus(adjuster);
                    Assert.fail("WithAdjuster should have thrown a ClassCastException");
                } catch (ClassCastException cce) {
                    // Expected exception; not an error
                }
            } else {
                // Same chronology,
                ChronoLocalDate<?> result = date.plus(adjuster);
                assertEquals(result, date2, "WithAdjuster failed to replace date");
            }
        }
    }

    @Test(groups={"tck"}, dataProvider="calendars")
    public void test_badMinusAdjusterChrono(Chrono<?> chrono) {
        LocalDate refDate = LocalDate.of(1900, 1, 1);
        ChronoLocalDate<?> date = chrono.date(refDate);
        for (Chrono<?>[] clist : data_of_calendars()) {
            Chrono<?> chrono2 = clist[0];
            ChronoLocalDate<?> date2 = chrono2.date(refDate);
            TemporalSubtractor adjuster = new FixedAdjuster(date2);
            if (chrono != chrono2) {
                try {
                    date.minus(adjuster);
                    Assert.fail("WithAdjuster should have thrown a ClassCastException");
                } catch (ClassCastException cce) {
                    // Expected exception; not an error
                }
            } else {
                // Same chronology,
                ChronoLocalDate<?> result = date.minus(adjuster);
                assertEquals(result, date2, "WithAdjuster failed to replace date");
            }
        }
    }

    @Test(groups={"tck"}, dataProvider="calendars")
    public void test_badPlusTemporalUnitChrono(Chrono<?> chrono) {
        LocalDate refDate = LocalDate.of(1900, 1, 1);
        ChronoLocalDate<?> date = chrono.date(refDate);
        for (Chrono<?>[] clist : data_of_calendars()) {
            Chrono<?> chrono2 = clist[0];
            ChronoLocalDate<?> date2 = chrono2.date(refDate);
            TemporalUnit adjuster = new FixedTemporalUnit(date2);
            if (chrono != chrono2) {
                try {
                    date.plus(1, adjuster);
                    Assert.fail("TemporalUnit.doAdd plus should have thrown a ClassCastException" + date.getClass()
                            + ", can not be cast to " + date2.getClass());
                } catch (ClassCastException cce) {
                    // Expected exception; not an error
                }
            } else {
                // Same chronology,
                ChronoLocalDate<?> result = date.plus(1, adjuster);
                assertEquals(result, date2, "WithAdjuster failed to replace date");
            }
        }
    }

    @Test(groups={"tck"}, dataProvider="calendars")
    public void test_badMinusTemporalUnitChrono(Chrono<?> chrono) {
        LocalDate refDate = LocalDate.of(1900, 1, 1);
        ChronoLocalDate<?> date = chrono.date(refDate);
        for (Chrono<?>[] clist : data_of_calendars()) {
            Chrono<?> chrono2 = clist[0];
            ChronoLocalDate<?> date2 = chrono2.date(refDate);
            TemporalUnit adjuster = new FixedTemporalUnit(date2);
            if (chrono != chrono2) {
                try {
                    date.minus(1, adjuster);
                    Assert.fail("TemporalUnit.doAdd minus should have thrown a ClassCastException" + date.getClass()
                            + ", can not be cast to " + date2.getClass());
                } catch (ClassCastException cce) {
                    // Expected exception; not an error
                }
            } else {
                // Same chronology,
                ChronoLocalDate<?> result = date.minus(1, adjuster);
                assertEquals(result, date2, "WithAdjuster failed to replace date");
            }
        }
    }

    @Test(groups={"tck"}, dataProvider="calendars")
    public void test_badTemporalFieldChrono(Chrono<?> chrono) {
        LocalDate refDate = LocalDate.of(1900, 1, 1);
        ChronoLocalDate<?> date = chrono.date(refDate);
        for (Chrono<?>[] clist : data_of_calendars()) {
            Chrono<?> chrono2 = clist[0];
            ChronoLocalDate<?> date2 = chrono2.date(refDate);
            TemporalField adjuster = new FixedTemporalField(date2);
            if (chrono != chrono2) {
                try {
                    date.with(adjuster, 1);
                    Assert.fail("TemporalField doSet should have thrown a ClassCastException" + date.getClass()
                            + ", can not be cast to " + date2.getClass());
                } catch (ClassCastException cce) {
                    // Expected exception; not an error
                }
            } else {
                // Same chronology,
                ChronoLocalDate<?> result = date.with(adjuster, 1);
                assertEquals(result, date2, "TemporalField doSet failed to replace date");
            }
        }
    }

    //-----------------------------------------------------------------------
    // isBefore, isAfter, isEqual, DATE_COMPARATOR
    //-----------------------------------------------------------------------
    @Test(groups={"tck"}, dataProvider="calendars")
    public void test_date_comparisons(Chrono<?> chrono) {
        List<ChronoLocalDate<?>> dates = new ArrayList<>();

        ChronoLocalDate<?> date = chrono.date(LocalDate.of(1900, 1, 1));

        // Insert dates in order, no duplicates
        dates.add(date.minus(1000, ChronoUnit.YEARS));
        dates.add(date.minus(100, ChronoUnit.YEARS));
        dates.add(date.minus(10, ChronoUnit.YEARS));
        dates.add(date.minus(1, ChronoUnit.YEARS));
        dates.add(date.minus(1, ChronoUnit.MONTHS));
        dates.add(date.minus(1, ChronoUnit.WEEKS));
        dates.add(date.minus(1, ChronoUnit.DAYS));
        dates.add(date);
        dates.add(date.plus(1, ChronoUnit.DAYS));
        dates.add(date.plus(1, ChronoUnit.WEEKS));
        dates.add(date.plus(1, ChronoUnit.MONTHS));
        dates.add(date.plus(1, ChronoUnit.YEARS));
        dates.add(date.plus(10, ChronoUnit.YEARS));
        dates.add(date.plus(100, ChronoUnit.YEARS));
        dates.add(date.plus(1000, ChronoUnit.YEARS));

        // Check these dates against the corresponding dates for every calendar
        for (Chrono<?>[] clist : data_of_calendars()) {
            List<ChronoLocalDate<?>> otherDates = new ArrayList<>();
            Chrono<?> chrono2 = clist[0];
            for (ChronoLocalDate<?> d : dates) {
                otherDates.add(chrono2.date(d));
            }

            // Now compare  the sequence of original dates with the sequence of converted dates
            for (int i = 0; i < dates.size(); i++) {
                ChronoLocalDate<?> a = dates.get(i);
                for (int j = 0; j < otherDates.size(); j++) {
                    ChronoLocalDate<?> b = otherDates.get(j);
                    int cmp = ChronoLocalDate.DATE_COMPARATOR.compare(a, b);
                    if (i < j) {
                        assertTrue(cmp < 0, a + " compare " + b);
                        assertEquals(a.isBefore(b), true, a + " isBefore " + b);
                        assertEquals(a.isAfter(b), false, a + " isAfter " + b);
                        assertEquals(a.isEqual(b), false, a + " isEqual " + b);
                    } else if (i > j) {
                        assertTrue(cmp > 0, a + " compare " + b);
                        assertEquals(a.isBefore(b), false, a + " isBefore " + b);
                        assertEquals(a.isAfter(b), true, a + " isAfter " + b);
                        assertEquals(a.isEqual(b), false, a + " isEqual " + b);
                    } else {
                        assertTrue(cmp == 0, a + " compare " + b);
                        assertEquals(a.isBefore(b), false, a + " isBefore " + b);
                        assertEquals(a.isAfter(b), false, a + " isAfter " + b);
                        assertEquals(a.isEqual(b), true, a + " isEqual " + b);
                    }
                }
            }
        }
    }

    //-----------------------------------------------------------------------
    // Test Serialization of Calendars
    //-----------------------------------------------------------------------
    @Test( groups={"tck"}, dataProvider="calendars")
    public <C extends Chrono<C>> void test_ChronoSerialization(C chrono) throws Exception {
        LocalDate ref = LocalDate.of(1900, 1, 5);
        ChronoLocalDate<C> orginal = chrono.date(ref);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(orginal);
        out.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);
        @SuppressWarnings("unchecked")
        ChronoLocalDate<C> ser = (ChronoLocalDate<C>) in.readObject();
        assertEquals(ser, orginal, "deserialized date is wrong");
    }

    /**
     * FixedAdjusted returns a fixed Temporal in all adjustments.
     * Construct an adjuster with the Temporal that should be returned from adjust.
     */
    static class FixedAdjuster implements TemporalAdjuster, TemporalAdder, TemporalSubtractor {
        private Temporal datetime;

        FixedAdjuster(Temporal datetime) {
            this.datetime = datetime;
        }

        @Override
        public Temporal adjustInto(Temporal ignore) {
            return datetime;
        }

        @Override
        public Temporal addTo(Temporal ignore) {
            return datetime;
        }

        @Override
        public Temporal subtractFrom(Temporal ignore) {
            return datetime;
        }

    }

    /**
     * FixedTemporalUnit returns a fixed Temporal in all adjustments.
     * Construct an FixedTemporalUnit with the Temporal that should be returned from doAdd.
     */
    static class FixedTemporalUnit implements TemporalUnit {
        private Temporal temporal;

        FixedTemporalUnit(Temporal temporal) {
            this.temporal = temporal;
        }

        @Override
        public String getName() {
            return "FixedTemporalUnit";
        }

        @Override
        public Duration getDuration() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isDurationEstimated() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isSupported(Temporal temporal) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R extends Temporal> R doPlus(R dateTime, long periodToAdd) {
            return (R) this.temporal;
        }

        @Override
        public <R extends Temporal> SimplePeriod between(R dateTime1, R dateTime2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /**
     * FixedTemporalField returns a fixed Temporal in all adjustments.
     * Construct an FixedTemporalField with the Temporal that should be returned from doSet.
     */
    static class FixedTemporalField implements TemporalField {
        private Temporal temporal;
        FixedTemporalField(Temporal temporal) {
            this.temporal = temporal;
        }

        @Override
        public String getName() {
            return "FixedTemporalField";
        }

        @Override
        public TemporalUnit getBaseUnit() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TemporalUnit getRangeUnit() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ValueRange range() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean doIsSupported(TemporalAccessor temporal) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ValueRange doRange(TemporalAccessor temporal) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long doGet(TemporalAccessor temporal) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R extends Temporal> R doWith(R temporal, long newValue) {
            return (R) this.temporal;
        }

        @Override
        public boolean resolve(DateTimeBuilder builder, long value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
