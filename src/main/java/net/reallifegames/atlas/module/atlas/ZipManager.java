/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Tyler Bucher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.reallifegames.atlas.module.atlas;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Helps with zip operations.
 *
 * @author Tyler Bucher
 */
public class ZipManager {

    /**
     * Attempts to extract a zip file to a location.
     *
     * @param filePath the path to the zip file.
     * @param location the location to put the extracted.
     * @throws IOException if an I/O error has occurred.
     */
    public static void extractZip(@Nonnull final String filePath, @Nonnull final String location) throws IOException {
        // Load the zip file and unzip it
        final byte[] buffer = new byte[8192];
        final ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(filePath));
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        // Loop through zip entries
        while (zipEntry != null) {
            // Crate zip save location
            final File path = new File(location + zipEntry.getName());
            if (!zipEntry.getName().startsWith("assets")) {
                zipEntry = zipInputStream.getNextEntry();
                continue;
            }
            if (zipEntry.isDirectory()) {
                if (!path.exists()) {
                    if (!path.mkdirs()) {
                        System.out.println("Unable to make dir.");
                    }
                }
                zipEntry = zipInputStream.getNextEntry();
                continue;
            }
            if (!path.getParentFile().exists()) {
                if (!path.getParentFile().mkdirs()) {
                    System.out.println("Unable to make dir.");
                }
            }
            final FileOutputStream fos = new FileOutputStream(path);
            int len;
            // Loop through entry bytes
            while ((len = zipInputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            // Save entry
            fos.close();
            zipEntry = zipInputStream.getNextEntry();
        }
        // Close zip
        zipInputStream.closeEntry();
        zipInputStream.close();
    }

    /**
     * Recursively deletes a path.
     *
     * @param path the path to delete.
     */
    public static void delete(@Nonnull final File path) {
        if (path.isDirectory()) {
            final File[] fileList = path.listFiles();
            if (fileList != null) {
                for (File c : fileList) {
                    delete(c);
                }
                if (!path.delete()) {
                    System.out.println("Unable to delete dir.");
                }
            }
        } else {
            if (!path.delete()) {
                System.out.println("Unable to delete file.");
            }
        }
    }

    public static void compressFiles(final List<File> files, final String zipName) throws IOException {
        final FileOutputStream fos = new FileOutputStream(zipName);
        final ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (final File srcFile : files) {
            if (srcFile.isDirectory()) {
                for (final File file : Objects.requireNonNull(srcFile.listFiles())) {
                    final FileInputStream fis = new FileInputStream(file);
                    final ZipEntry zipEntry = new ZipEntry(srcFile.getName() + File.separator + file.getName());
                    zipOut.putNextEntry(zipEntry);

                    int length;
                    final byte[] bytes = new byte[1024];
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                    fis.close();
                }
            }
        }
        zipOut.close();
        fos.close();
    }
}
