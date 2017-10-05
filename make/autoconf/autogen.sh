#!/bin/bash
#
# Copyright (c) 2011, 2017, Oracle and/or its affiliates. All rights reserved.
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
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#

generate_configure_script() {
  # First create a header
  cat > $1 << EOT
#!/bin/bash
#
# ##########################################################
# ### THIS FILE IS AUTOMATICALLY GENERATED. DO NOT EDIT. ###
# ##########################################################
#
EOT
  # Then replace "magic" variables in configure.ac and append the output
  # from autoconf. $2 is either cat (just a no-op) or a filter.
  cat $script_dir/configure.ac | sed -e "s|@DATE_WHEN_GENERATED@|$TIMESTAMP|" | \
      eval $2 | ${AUTOCONF} -W all -I$script_dir - >> $1
  rm -rf autom4te.cache
}

script_dir=`dirname $0`

# Create a timestamp as seconds since epoch
if test "x`uname -s`" = "xSunOS"; then
  TIMESTAMP=`date +%s`
  if test "x$TIMESTAMP" = "x%s"; then
    # date +%s not available on this Solaris, use workaround from nawk(1):
    TIMESTAMP=`nawk 'BEGIN{print srand()}'`
  fi
else
  TIMESTAMP=`date +%s`
fi

AUTOCONF="`which autoconf 2> /dev/null | grep -v '^no autoconf in'`"

if test "x${AUTOCONF}" = x; then
  echo "You need autoconf installed to be able to regenerate the configure script"
  echo "Error: Cannot find autoconf" 1>&2
  exit 1
fi

autoconf_version=`$AUTOCONF --version | head -1`
echo "Using autoconf at ${AUTOCONF} [$autoconf_version]"

echo "Generating generated-configure.sh"
generate_configure_script "$script_dir/generated-configure.sh" 'cat'

if test "x$CUSTOM_CONFIG_DIR" != "x"; then
  custom_hook=$CUSTOM_CONFIG_DIR/custom-hook.m4
  if test ! -e $custom_hook; then
    echo "CUSTOM_CONFIG_DIR set but $CUSTOM_CONFIG_DIR/custom-hook.m4 not present"
    echo "Error: Cannot continue" 1>&2
    exit 1
  fi

  # We have custom sources available; also generate configure script
  # with custom hooks compiled in.
  echo "Generating custom generated-configure.sh"
  generate_configure_script "$CUSTOM_CONFIG_DIR/generated-configure.sh" 'sed -e "s|#CUSTOM_AUTOCONF_INCLUDE|m4_include([$custom_hook])|"'
fi
