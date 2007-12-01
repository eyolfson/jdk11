/*
 * Copyright 2001-2006 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

/*
 * PerfData Version Constants
 *   - Major Version - change whenever the structure of PerfDataEntry changes
 *   - Minor Version - change whenever the data within the PerfDataEntry
 *                     structure changes. for example, new unit or variability
 *                     values are added or new PerfData subtypes are added.
 */
#define PERFDATA_MAJOR_VERSION 2
#define PERFDATA_MINOR_VERSION 0

/* Byte order of the PerfData memory region. The byte order is exposed in
 * the PerfData memory region as the data in the memory region may have
 * been generated by a little endian JVM implementation. Tracking the byte
 * order in the PerfData memory region allows Java applications to adapt
 * to the native byte order for monitoring purposes. This indicator is
 * also useful when a snapshot of the PerfData memory region is shipped
 * to a machine with a native byte order different from that of the
 * originating machine.
 */
#define PERFDATA_BIG_ENDIAN     0
#define PERFDATA_LITTLE_ENDIAN  1

/*
 * The PerfDataPrologue structure is known by the PerfDataBuffer Java class
 * libraries that read the PerfData memory region. The size and the position
 * of the fields must be changed along with their counterparts in the
 * PerfDataBuffer Java class. The first four bytes of this structure
 * should never change, or compatibility problems between the monitoring
 * applications and Hotspot VMs will result. The reserved fields are
 * available for future enhancements.
 */
typedef struct {
  jint   magic;              // magic number - 0xcafec0c0
  jbyte  byte_order;         // byte order of the buffer
  jbyte  major_version;      // major and minor version numbers
  jbyte  minor_version;
  jbyte  accessible;         // ready to access
  jint   used;               // number of PerfData memory bytes used
  jint   overflow;           // number of bytes of overflow
  jlong  mod_time_stamp;     // time stamp of last structural modification
  jint   entry_offset;       // offset of the first PerfDataEntry
  jint   num_entries;        // number of allocated PerfData entries
} PerfDataPrologue;

/* The PerfDataEntry structure defines the fixed portion of an entry
 * in the PerfData memory region. The PerfDataBuffer Java libraries
 * are aware of this structure and need to be changed when this
 * structure changes.
 */
typedef struct {

  jint entry_length;      // entry length in bytes
  jint name_offset;       // offset of the data item name
  jint vector_length;     // length of the vector. If 0, then scalar
  jbyte data_type;        // type of the data item -
                          // 'B','Z','J','I','S','C','D','F','V','L','['
  jbyte flags;            // flags indicating misc attributes
  jbyte data_units;       // unit of measure for the data type
  jbyte data_variability; // variability classification of data type
  jint  data_offset;      // offset of the data item

/*
  body of PerfData memory entry is variable length

  jbyte[name_length] data_name;        // name of the data item
  jbyte[pad_length] data_pad;          // alignment of data item
  j<data_type>[data_length] data_item; // array of appropriate types.
                                       // data_length is > 1 only when the
                                       // data_type is T_ARRAY.
*/
} PerfDataEntry;

// Prefix of performance data file.
static const char PERFDATA_NAME[] = "hsperfdata";

// UINT_CHARS contains the number of characters holding a process id
// (i.e. pid). pid is defined as unsigned "int" so the maximum possible pid value
// would be 2^32 - 1 (4294967295) which can be represented as a 10 characters
// string.
static const size_t UINT_CHARS = 10;

// Add 1 for the '_' character between PERFDATA_NAME and pid. The '\0' terminating
// character will be included in the sizeof(PERFDATA_NAME) operation.
static const size_t PERFDATA_FILENAME_LEN = sizeof(PERFDATA_NAME) +
                                            UINT_CHARS + 1;

/* the PerfMemory class manages creation, destruction,
 * and allocation of the PerfData region.
 */
class PerfMemory : AllStatic {
    friend class VMStructs;
  private:
    static char*  _start;
    static char*  _end;
    static char*  _top;
    static size_t _capacity;
    static PerfDataPrologue*  _prologue;
    static jint   _initialized;

    static void create_memory_region(size_t sizep);
    static void delete_memory_region();

  public:
    enum PerfMemoryMode {
      PERF_MODE_RO = 0,
      PERF_MODE_RW = 1
    };

    static char* alloc(size_t size);
    static char* start() { return _start; }
    static char* end() { return _end; }
    static size_t used() { return (size_t) (_top - _start); }
    static size_t capacity() { return _capacity; }
    static bool is_initialized() { return _initialized != 0; }
    static bool contains(char* addr) {
      return ((_start != NULL) && (addr >= _start) && (addr < _end));
    }
    static void mark_updated();

    // methods for attaching to and detaching from the PerfData
    // memory segment of another JVM process on the same system.
    static void attach(const char* user, int vmid, PerfMemoryMode mode,
                       char** addrp, size_t* size, TRAPS);
    static void detach(char* addr, size_t bytes, TRAPS);

    static void initialize();
    static void destroy();
    static void set_accessible(bool value) {
      if (UsePerfData) {
        _prologue->accessible = value;
      }
    }

    // filename of backing store or NULL if none.
    static char* backing_store_filename();

    // returns the complete file path of hsperfdata.
    // the caller is expected to free the allocated memory.
    static char* get_perfdata_file_path();
};

void perfMemory_init();
void perfMemory_exit();
