/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
 *
 */
#ifndef SHARE_VM_LOGGING_LOGCONFIGURATION_HPP
#define SHARE_VM_LOGGING_LOGCONFIGURATION_HPP

#include "logging/logLevel.hpp"
#include "memory/allocation.hpp"
#include "utilities/globalDefinitions.hpp"

class LogOutput;
class LogDecorators;
class LogTagLevelExpression;

// Global configuration of logging. Handles parsing and configuration of the logging framework,
// and manages the list of configured log outputs. The actual tag and level configuration is
// kept implicitly in the LogTagSets and their LogOutputLists. During configuration the tagsets
// are iterated over and updated accordingly.
class LogConfiguration : public AllStatic {
 private:
  static LogOutput**  _outputs;
  static size_t       _n_outputs;

  // Create a new output. Returns NULL if failed.
  static LogOutput* new_output(char* name, const char* options, outputStream* errstream);

  // Add an output to the list of configured outputs. Returns the assigned index.
  static size_t add_output(LogOutput* out);

  // Delete a configured output. The stderr/stdout outputs can not be removed.
  // Output should be completely disabled before it is deleted.
  static void delete_output(size_t idx);

  // Disable all logging to the specified output and then delete it (unless it is stdout/stderr).
  static void disable_output(size_t idx);

  // Get output index by name. Returns SIZE_MAX if output not found.
  static size_t find_output(const char* name);

  // Configure output (add or update existing configuration) to log on tag-level combination using specified decorators.
  static void configure_output(size_t idx, const LogTagLevelExpression& tag_level_expression, const LogDecorators& decorators);

 public:
  // Initialization and finalization of log configuration, to be run at vm startup and shutdown respectively.
  static void initialize(jlong vm_start_time);
  static void finalize();

  // Perform necessary post-initialization after VM startup. Enables reconfiguration of logging.
  static void post_initialize();

  // Disable all logging, equivalent to -Xlog:disable.
  static void disable_logging();

  // Configures logging on stdout for the given tags and level combination.
  // Intended for mappings between -XX: flags and Unified Logging configuration.
  // If exact_match is true, only tagsets with precisely the specified tags will be configured
  // (exact_match=false is the same as "-Xlog:<tags>*=<level>", and exact_match=true is "-Xlog:<tags>=<level>").
  // Tags should be specified using the LOG_TAGS macro, e.g.
  // LogConfiguration::configure_stdout(LogLevel::<level>, <true/false>, LOG_TAGS(<tags>));
  static void configure_stdout(LogLevelType level, bool exact_match, ...);

  // Parse command line configuration. Parameter 'opts' is the string immediately following the -Xlog: argument ("gc" for -Xlog:gc).
  static bool parse_command_line_arguments(const char* opts = "all");

  // Parse separated configuration arguments (from JCmd/MBean and command line).
  static bool parse_log_arguments(const char* outputstr,
                                  const char* what,
                                  const char* decoratorstr,
                                  const char* output_options,
                                  outputStream* errstream);

  // Prints log configuration to outputStream, used by JCmd/MBean.
  static void describe(outputStream* out);

  // Prints usage help for command line log configuration.
  static void print_command_line_help(FILE* out);

  // Rotates all LogOutput
  static void rotate_all_outputs();
};

#endif // SHARE_VM_LOGGING_LOGCONFIGURATION_HPP
