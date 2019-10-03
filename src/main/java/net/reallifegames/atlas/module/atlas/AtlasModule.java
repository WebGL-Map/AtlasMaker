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

import net.reallifegames.atlas.Atlas;
import net.reallifegames.atlas.TextureLoader;
import net.reallifegames.atlas.TextureManager;
import net.reallifegames.atlas.module.Module;
import net.reallifegames.atlas.module.ModuleInfo;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Creates a new atlas and holds the object.
 *
 * @author Tyler Bucher
 */
@ModuleInfo ("")
public class AtlasModule implements Module {

    /**
     * The global block texture atlas.
     */
    private TextureAtlas textureAtlas;

    /**
     * @param args constructor arguments.
     */
    public AtlasModule(@Nonnull final String[] args) {
        setup(args[0], args[1], args[2]);
    }

    /**
     * Setup code for creating the texture atlas.
     *
     * @param name the name of the file to extract.
     */
    private void setup(@Nonnull final String name, @Nonnull final String texturePackName, @Nonnull final String useOpenGL) {
        try {
            // Unzip
            System.out.println("Extracting Minecraft.jar assets.");
            ZipManager.extractZip(name, Atlas.FULL_TEMP_FOLDER_DIR + File.separator);
            if (!texturePackName.isEmpty()) {
                System.out.println("Extracting TexturePack assets.");
                ZipManager.extractZip(texturePackName, Atlas.FULL_TEMP_FOLDER_DIR + File.separator);
            }
            // Create atlas
            System.out.println("Creating TextureAtlas from asset images.");
            final File texturesDir = new File(Atlas.FULL_TEMP_FOLDER_DIR + "/assets/minecraft/textures/blocks");
            final File[] textureFileList = texturesDir.listFiles();
            final SortedMap<SortedTexture, BufferedImage> textureMap = new TreeMap<>((sortedTexture, t1)->
                    t1.area - sortedTexture.area == 0 ? 1 : t1.area - sortedTexture.area);

            if (textureFileList != null) {
                final ArrayList<String> subFinal = new ArrayList<>();
                for (File f : textureFileList) {
                    subFinal.add(f.getName());
                }
                Arrays.sort(textureFileList);
                int area = 0;
                int lWidth = 0;
                int lHeight = 0;
                for (final File file : textureFileList) {
                    if (!file.getName().endsWith("png") || subFinal.contains(file.getName() + ".mcmeta")) {
                        if (!file.isDirectory() && !file.getName().endsWith("mcmeta")) {
                            BufferedImage img = ImageIO.read(file);
                            img = img.getSubimage(0, 0, img.getWidth(), img.getWidth());
                            textureMap.put(new SortedTexture(file.getName().substring(0, file.getName().length() - 4),
                                    img.getWidth(), img.getHeight()), img);
                            area += img.getWidth() * img.getHeight();
                            if (img.getWidth() > lWidth) {
                                lWidth = img.getWidth();
                            }
                            if (img.getHeight() > lHeight) {
                                lHeight = img.getHeight();
                            }
                        }
                    } else {
                        BufferedImage img = ImageIO.read(file);
                        textureMap.put(new SortedTexture(file.getName().substring(0, file.getName().length() - 4),
                                img.getWidth(), img.getHeight()), img);
                        area += img.getWidth() * img.getHeight();
                        if (img.getWidth() > lWidth) {
                            lWidth = img.getWidth();
                        }
                        if (img.getHeight() > lHeight) {
                            lHeight = img.getHeight();
                        }
                    }
                }
                area = (int) Math.ceil(Math.sqrt(area));
                int wh = closestPow2(area, lWidth, lHeight);
                boolean[][] setArray = new boolean[wh][wh];
                textureAtlas = new TextureAtlas(new BufferedImage(wh, wh, BufferedImage.TYPE_INT_ARGB), wh);
                final Graphics raster = textureAtlas.getAtlas().getGraphics();
                for (Map.Entry<SortedTexture, BufferedImage> entry : textureMap.entrySet()) {
                    if (!packImage(setArray, raster, entry.getValue(), entry.getKey())) {
                        System.out.println("missed a texture error.");
                    }
                }
                raster.dispose();
                final File atlas = new File(Atlas.FULL_TEMP_FOLDER_DIR + "/export/textures", "atlas.png");
                if (!atlas.getParentFile().exists()) {
                    Files.createDirectories(atlas.toPath());
                }
                ImageIO.write(textureAtlas.getAtlas(), "png", atlas);
            }
            if (Boolean.parseBoolean(useOpenGL)) {
                textureSetup();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the texture atlas for OpenGL to use.
     */
    public void textureSetup() {
        TextureManager.registerTexture("atlas", Atlas.FULL_TEMP_FOLDER_DIR + "/export/textures/atlas.png", ((integer, imgPath)->{
            try {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, integer);
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, textureAtlas.getLength(), textureAtlas.getLength(), 0,
                        GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, TextureLoader.loadImage(imgPath, 4));
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    /**
     * @return the global block texture atlas.
     */
    public TextureAtlas getTextureAtlas() {
        return textureAtlas;
    }

    @Override
    public void input(double displacement) {

    }

    @Override
    public void update(double displacement) {

    }

    @Override
    public void render(double displacement) {

    }

    /**
     * Gets the closes power of two for one of the item lengths
     *
     * @param items the list of items to check.
     * @return the closes power of two.
     */
    private static int closestPow2(int... items) {
        int height = items[0];
        for (final int i : items) {
            if (i > height) {
                height = i;
            }
        }
        for (int i = 0; i < 16; i++) {
            if (1 << i >= height) {
                return 1 << i;
            }
        }
        return -1;
    }

    /**
     * Holds information about the size of a texture.
     *
     * @author Tyler Bucher
     */
    private class SortedTexture {

        /**
         * The id of the texture.
         */
        final String id;

        /**
         * The width of the texture.
         */
        final int width;

        /**
         * The height of the texture.
         */
        final int height;

        /**
         * The total area of the texture.
         */
        final int area;

        /**
         * @param id     id of the texture.
         * @param width  width of the texture.
         * @param height height of the texture.
         */
        SortedTexture(@Nonnull final String id, final int width, final int height) {
            this.id = id;
            this.width = width;
            this.height = height;
            this.area = this.width * this.height;
        }
    }

    /**
     * Attempts to write a texture to the file.
     *
     * @param setArray the array of texture slots which have been used.
     * @param graphics the system graphics writer.
     * @param image    the image to write into.
     * @param texture  the texture to pack or write.
     * @return true if the image was written false otherwise.
     */
    private boolean packImage(@Nonnull final boolean[][] setArray,
                              @Nonnull final Graphics graphics,
                              @Nonnull final BufferedImage image,
                              @Nonnull final SortedTexture texture) {
        for (int i = 0; i < setArray[0].length; i++) {
            for (int j = 0; j < setArray.length; j++) {
                if (checkBounds(setArray, j, i, image.getWidth(), image.getHeight())) {
                    setBounds(setArray, j, i, image.getWidth(), image.getHeight());
                    graphics.drawImage(image, j, i, null);
                    textureAtlas.getUvMap().put(texture.id, new Vector4f(
                            j / (float) textureAtlas.getLength(), i / (float) textureAtlas.getLength(),
                            (j + image.getWidth()) / (float) textureAtlas.getLength(),
                            (i + image.getHeight()) / (float) textureAtlas.getLength()));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Makes sure the texture is within the bounds of the image.
     *
     * @param setArray the array of texture slots which have been used.
     * @param x        the x coordinate to check.
     * @param y        the y coordinate to check.
     * @param width    the maximum width.
     * @param height   the maximum height.
     * @return true if the texture is within the bounds false otherwise.
     */
    private boolean checkBounds(@Nonnull final boolean[][] setArray,
                                final int x,
                                final int y,
                                final int width,
                                final int height) {
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                if (j < setArray.length && i < setArray[0].length) {
                    if (setArray[j][i]) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Sets the array slot to true.
     *
     * @param setArray the array of texture slots which have been used.
     * @param x        the x coordinate to check.
     * @param y        the y coordinate to check.
     * @param width    the maximum width.
     * @param height   the maximum height.
     */
    private void setBounds(@Nonnull final boolean[][] setArray,
                           final int x,
                           final int y,
                           final int width,
                           final int height) {
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                setArray[j][i] = true;
            }
        }
    }
}
