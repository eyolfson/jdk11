/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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
package jdk.tools.jlink.internal;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jdk.tools.jlink.internal.Archive.Entry;
import jdk.tools.jlink.internal.Archive.Entry.EntryType;
import jdk.tools.jlink.internal.ModulePoolImpl.CompressedModuleData;
import jdk.tools.jlink.plugin.ExecutableImage;
import jdk.tools.jlink.plugin.PluginException;
import jdk.tools.jlink.plugin.ModuleEntry;

/**
 * An image (native endian.)
 * <pre>{@code
 * {
 *   u4 magic;
 *   u2 major_version;
 *   u2 minor_version;
 *   u4 resource_count;
 *   u4 table_length;
 *   u4 location_attributes_size;
 *   u4 strings_size;
 *   u4 redirect[table_length];
 *   u4 offsets[table_length];
 *   u1 location_attributes[location_attributes_size];
 *   u1 strings[strings_size];
 *   u1 content[if !EOF];
 * }
 * }</pre>
 */
public final class ImageFileCreator {
    private final Map<String, List<Entry>> entriesForModule = new HashMap<>();
    private final ImagePluginStack plugins;
    private ImageFileCreator(ImagePluginStack plugins) {
        this.plugins = Objects.requireNonNull(plugins);
    }

    public static ExecutableImage create(Set<Archive> archives,
            ImagePluginStack plugins)
            throws IOException {
        return ImageFileCreator.create(archives, ByteOrder.nativeOrder(),
                plugins);
    }

    public static ExecutableImage create(Set<Archive> archives,
            ByteOrder byteOrder)
            throws IOException {
        return ImageFileCreator.create(archives, byteOrder,
                new ImagePluginStack());
    }

    public static ExecutableImage create(Set<Archive> archives,
            ByteOrder byteOrder,
            ImagePluginStack plugins)
            throws IOException
    {
        ImageFileCreator image = new ImageFileCreator(plugins);
        try {
            image.readAllEntries(archives);
            // write to modular image
            image.writeImage(archives, byteOrder);
        } finally {
            //Close all archives
            for (Archive a : archives) {
                a.close();
            }
        }

        return plugins.getExecutableImage();
    }

    private void readAllEntries(Set<Archive> archives) {
        archives.stream().forEach((archive) -> {
            Map<Boolean, List<Entry>> es;
            try (Stream<Entry> entries = archive.entries()) {
                es = entries.collect(Collectors.partitioningBy(n -> n.type()
                        == EntryType.CLASS_OR_RESOURCE));
            }
            String mn = archive.moduleName();
            List<Entry> all = new ArrayList<>();
            all.addAll(es.get(false));
            all.addAll(es.get(true));
            entriesForModule.put(mn, all);
        });
    }

    public static boolean isClassPackage(String path) {
        return path.endsWith(".class") && !path.endsWith("module-info.class");
    }

    public static void recreateJimage(Path jimageFile,
            Set<Archive> archives,
            ImagePluginStack pluginSupport)
            throws IOException {
        try {
            Map<String, List<Entry>> entriesForModule
                    = archives.stream().collect(Collectors.toMap(
                                    Archive::moduleName,
                                    a -> {
                                        try (Stream<Entry> entries = a.entries()) {
                                            return entries.collect(Collectors.toList());
                                        }
                                    }));
            ByteOrder order = ByteOrder.nativeOrder();
            BasicImageWriter writer = new BasicImageWriter(order);
            ModulePoolImpl pool = createPools(archives, entriesForModule, order, writer);
            try (OutputStream fos = Files.newOutputStream(jimageFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    DataOutputStream out = new DataOutputStream(bos)) {
                generateJImage(pool, writer, pluginSupport, out);
            }
        } finally {
            //Close all archives
            for (Archive a : archives) {
                a.close();
            }
        }
    }

    private void writeImage(Set<Archive> archives,
            ByteOrder byteOrder)
            throws IOException {
        BasicImageWriter writer = new BasicImageWriter(byteOrder);
        ModulePoolImpl allContent = createPools(archives,
                entriesForModule, byteOrder, writer);
        ModulePoolImpl result = generateJImage(allContent,
             writer, plugins, plugins.getJImageFileOutputStream());

        //Handle files.
        try {
            plugins.storeFiles(allContent, result, writer);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    private static ModulePoolImpl generateJImage(ModulePoolImpl allContent,
            BasicImageWriter writer,
            ImagePluginStack pluginSupport,
            DataOutputStream out
    ) throws IOException {
        ModulePoolImpl resultResources;
        try {
            resultResources = pluginSupport.visitResources(allContent);
        } catch (PluginException pe) {
            throw pe;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
        Set<String> duplicates = new HashSet<>();
        long[] offset = new long[1];

        List<ModuleEntry> content = new ArrayList<>();
        List<String> paths = new ArrayList<>();
                 // the order of traversing the resources and the order of
        // the module content being written must be the same
        resultResources.entries().forEach(res -> {
            if (res.getType().equals(ModuleEntry.Type.CLASS_OR_RESOURCE)) {
                String path = res.getPath();
                content.add(res);
                long uncompressedSize = res.getLength();
                long compressedSize = 0;
                if (res instanceof CompressedModuleData) {
                    CompressedModuleData comp
                            = (CompressedModuleData) res;
                    compressedSize = res.getLength();
                    uncompressedSize = comp.getUncompressedSize();
                }
                long onFileSize = res.getLength();

                if (duplicates.contains(path)) {
                    System.err.format("duplicate resource \"%s\", skipping%n",
                            path);
                    // TODO Need to hang bytes on resource and write
                    // from resource not zip.
                    // Skipping resource throws off writing from zip.
                    offset[0] += onFileSize;
                    return;
                }
                duplicates.add(path);
                writer.addLocation(path, offset[0], compressedSize, uncompressedSize);
                paths.add(path);
                offset[0] += onFileSize;
            }
        });

        ImageResourcesTree tree = new ImageResourcesTree(offset[0], writer, paths);

        // write header and indices
        byte[] bytes = writer.getBytes();
        out.write(bytes, 0, bytes.length);

        // write module content
        content.stream().forEach((res) -> {
            res.write(out);
        });

        tree.addContent(out);

        out.close();

        return resultResources;
    }

    private static ModulePoolImpl createPools(Set<Archive> archives,
            Map<String, List<Entry>> entriesForModule,
            ByteOrder byteOrder,
            BasicImageWriter writer) throws IOException {
        ModulePoolImpl resources = new ModulePoolImpl(byteOrder, new StringTable() {

            @Override
            public int addString(String str) {
                return writer.addString(str);
            }

            @Override
            public String getString(int id) {
                return writer.getString(id);
            }
        });
        for (Archive archive : archives) {
            String mn = archive.moduleName();
            for (Entry entry : entriesForModule.get(mn)) {
                String path;
                if (entry.type() == EntryType.CLASS_OR_RESOURCE) {
                    // Removal of "classes/" radical.
                    path = entry.name();
                    if (path.endsWith("module-info.class")) {
                        path = "/" + path;
                    } else {
                        path = "/" + mn + "/" + path;
                    }
                } else {
                    // Entry.path() contains the kind of file native, conf, bin, ...
                    // Keep it to avoid naming conflict (eg: native/jvm.cfg and config/jvm.cfg
                    path = "/" + mn + "/" + entry.path();
                }

                resources.add(new ArchiveEntryModuleEntry(mn, path, entry));
            }
        }
        return resources;
    }

    /**
     * Helper method that splits a Resource path onto 3 items: module, parent
     * and resource name.
     *
     * @param path
     * @return An array containing module, parent and name.
     */
    public static String[] splitPath(String path) {
        Objects.requireNonNull(path);
        String noRoot = path.substring(1);
        int pkgStart = noRoot.indexOf("/");
        String module = noRoot.substring(0, pkgStart);
        List<String> result = new ArrayList<>();
        result.add(module);
        String pkg = noRoot.substring(pkgStart + 1);
        String resName;
        int pkgEnd = pkg.lastIndexOf("/");
        if (pkgEnd == -1) { // No package.
            resName = pkg;
        } else {
            resName = pkg.substring(pkgEnd + 1);
        }

        pkg = toPackage(pkg, false);
        result.add(pkg);
        result.add(resName);

        String[] array = new String[result.size()];
        return result.toArray(array);
    }

    private static String toPackage(String name, boolean log) {
        int index = name.lastIndexOf('/');
        if (index > 0) {
            return name.substring(0, index).replace('/', '.');
        } else {
            // ## unnamed package
            if (log) {
                System.err.format("Warning: %s in unnamed package%n", name);
            }
            return "";
        }
    }
}
