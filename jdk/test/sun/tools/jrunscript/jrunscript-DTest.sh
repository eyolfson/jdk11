#!/bin/sh

#
# Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
# CA 95054 USA or visit www.sun.com if you need additional information or
# have any questions.
#


# @test
# @bug 6265810 6705893
# @build CheckEngine
# @run shell jrunscript-DTest.sh
# @summary Test that output of 'jrunscript -D' 

. ${TESTSRC-.}/common.sh

setup
${JAVA} -cp ${TESTCLASSES} CheckEngine
if [ $? -eq 2 ]; then
    echo "No js engine found and engine not required; test vacuously passes."
    exit 0
fi

# test whether value specifieD by -D option is passed
# to script as java.lang.System property.  sysProps is
# jrunscript shell built-in variable for System properties.

${JRUNSCRIPT} -Djrunscript.foo=bar <<EOF
if (sysProps["jrunscript.foo"] == "bar") { println("Passed"); exit(0); }
// unexpected value
println("Unexpected System property value");
exit(1);
EOF

if [ $? -ne 0 ]; then 
    exit 1
fi
