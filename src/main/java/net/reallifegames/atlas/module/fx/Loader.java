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
package net.reallifegames.atlas.module.fx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.reallifegames.atlas.Atlas;
import net.reallifegames.atlas.asset.blockmodels.BlockModel;
import net.reallifegames.atlas.asset.blockmodels.Element;
import net.reallifegames.atlas.asset.blockmodels.Face;
import net.reallifegames.atlas.asset.blockstates.BlockState;
import net.reallifegames.atlas.asset.blockstates.Model;
import net.reallifegames.atlas.asset.blockstates.Multipart;
import net.reallifegames.atlas.asset.blockstates.Variant;
import net.reallifegames.atlas.module.atlas.TextureAtlas;
import net.reallifegames.atlas.renderable.RenderableBlockModel;
import net.reallifegames.atlas.renderable.RenderableModel;
import org.joml.Vector3d;
import org.joml.Vector4d;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Loads Minecraft blocks from json files.
 *
 * @author Tyler Bucher
 */
public class Loader {

    /**
     * The list of models to keep loaded.
     */
    private static final Map<String, BlockModel> modelMap = new HashMap<>();

    /**
     * Loads a {@link BlockState} file from the system.
     *
     * @param blockStateFile the file to load.
     * @param textureAtlas   the global block texture atlas.
     * @param useOpenGL      states if we should use OpenGL.
     * @return a new {@link BlockState} from a json file.
     *
     * @throws IOException if the model file is unable to be read.
     */
    public static BlockState loadBlockState(@Nonnull final File blockStateFile, @Nonnull final TextureAtlas textureAtlas, final boolean useOpenGL) throws IOException {
        final JsonNode actualObj = new ObjectMapper().readTree(blockStateFile);
        final JsonNode variantNode = actualObj.get("variants");
        final List<Variant> variants = variantNode == null ? null : new ArrayList<>();
        if (variantNode != null) {
            variantNode.fields().forEachRemaining(entry->{
                final List<Model> modelList = new ArrayList<>();
                if (entry.getValue().isArray()) {
                    entry.getValue().elements().forEachRemaining(jsonNode->{
                        try {
                            modelList.add(Loader.loadModel(jsonNode, textureAtlas, useOpenGL));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    try {
                        modelList.add(Loader.loadModel(entry.getValue(), textureAtlas, useOpenGL));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                variants.add(new Variant(entry.getKey(), modelList));
            });
        }
        final JsonNode multipartNode = actualObj.get("multipart");
        final List<Multipart> multiparts = multipartNode == null ? null : new ArrayList<>();
        if (multipartNode != null) {
            multipartNode.elements().forEachRemaining(node->{
                final List<Model> modelList = new ArrayList<>();
                if (node.get("apply").isArray()) {
                    node.get("apply").elements().forEachRemaining(elements->{
                        try {
                            modelList.add(Loader.loadModel(elements, textureAtlas, useOpenGL));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    try {
                        modelList.add(Loader.loadModel(node.get("apply"), textureAtlas, useOpenGL));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                final List<List<Map.Entry<String, String>>> stateList = new ArrayList<>();
                if (node.get("when") != null) {
                    if (node.get("when").get("OR") != null) {
                        node.get("when").get("OR").elements().forEachRemaining(element->{
                            final List<Map.Entry<String, String>> superList = new ArrayList<>();
                            element.fields().forEachRemaining(entry->{
                                superList.add(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue().asText()));
                            });
                            stateList.add(superList);
                        });
                    } else {
                        final List<Map.Entry<String, String>> superList = new ArrayList<>();
                        node.get("when").fields().forEachRemaining(entry->{
                            superList.add(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue().asText()));
                        });
                        stateList.add(superList);
                    }
                }
                multiparts.add(new Multipart(modelList, stateList, node.get("when") != null && node.get("when").get("OR") != null));
            });
        }
        return new BlockState(variants, multiparts);
    }

    /**
     * Attempts to load a model from a json file.
     *
     * @param jsonNode     the node to interpret.
     * @param textureAtlas the global block texture atlas.
     * @param useOpenGL    states if we should use OpenGL.
     * @return a new {@link Model} from the jsonNode.
     *
     * @throws IOException if the model file is unable to be read.
     */
    public static Model loadModel(@Nonnull final JsonNode jsonNode, @Nonnull final TextureAtlas textureAtlas, final boolean useOpenGL) throws IOException {
        final RenderableBlockModel renderableBlockModel = new RenderableBlockModel(loadBlockModel(jsonNode.get("model").asText()), textureAtlas, useOpenGL);
        final Model model = new Model(
                jsonNode.get("model").asText(),
                renderableBlockModel,
                jsonNode.get("x") == null ? 0 : jsonNode.get("x").asInt(0),
                jsonNode.get("y") == null ? 0 : jsonNode.get("y").asInt(0),
                jsonNode.get("uvlock") != null && jsonNode.get("uvlock").asBoolean(false),
                jsonNode.get("weight") == null ? 1 : jsonNode.get("weight").asInt(1)
        );
        return new RenderableModel(model, textureAtlas, useOpenGL);
    }

    /**
     * Load and return a {@link BlockModel} from the given name.
     *
     * @param blockName the name of a block to load.
     * @return a new {@link BlockModel} from a json file.
     *
     * @throws IOException if the model file is unable to be read.
     */
    public static BlockModel loadBlockModel(@Nonnull final String blockName) throws IOException {
        if (modelMap.containsKey(blockName)) {
            return modelMap.get(blockName);
        }
        final File modelFile = new File(Atlas.FULL_TEMP_FOLDER_DIR + "/assets/minecraft/models/block", blockName + ".json");
        final JsonNode jsonObj = new ObjectMapper().readTree(modelFile);
        BlockModel parentModel = null;
        if (jsonObj.get("parent") != null) {
            String block = jsonObj.get("parent").asText();
            if (block.contains("block/")) {
                block = block.replace("block/", "");
            }
            parentModel = loadBlockModel(block);
        }
        final Map<String, String> textureList = new HashMap<>();
        if (jsonObj.get("textures") != null) {
            jsonObj.get("textures").fields().forEachRemaining(entry->
                    textureList.put(entry.getKey(),
                            entry.getValue().asText().replace("blocks/", "")));
        }
        final List<Element> elements = new ArrayList<>();
        if (jsonObj.get("elements") != null) {
            Iterator<JsonNode> itr = jsonObj.get("elements").elements();
            while (itr.hasNext()) {
                JsonNode node = itr.next();
                final Map<String, Face> faces = new HashMap<>();
                node.get("faces").fields().forEachRemaining(entry->faces.put(entry.getKey(), new Face(
                        entry.getValue().get("uv") == null || entry.getValue().get("uv").toString().length() < 2 ? null :
                                Loader.convertStringToVector4(entry.getValue().get("uv").toString()),
                        entry.getValue().get("texture") == null ? "" : entry.getValue().get("texture").asText().replace("#", ""),
                        entry.getValue().get("cullface") == null ? "" : entry.getValue().get("cullface").toString(),
                        entry.getValue().get("rotation") == null ? 0 : entry.getValue().get("rotation").asInt(),
                        entry.getValue().get("tintindex") == null ? -1 : entry.getValue().get("tintindex").asInt()
                )));
                elements.add(new Element(
                        node.get("from").toString().length() < 2 ? null : Loader.convertStringToVector(node.get("from").toString()),
                        node.get("to").toString().length() < 2 ? null : Loader.convertStringToVector(node.get("to").toString()),
                        node.get("rotation") == null ? new Vector3d() : node.get("rotation").get("origin").toString().length() < 2 ? null :
                                Loader.convertStringToVector(node.get("rotation").get("origin").toString()),
                        node.get("rotation") == null ? "" : node.get("rotation").get("axis").asText(),
                        node.get("rotation") == null ? 0 : node.get("rotation").get("angle").asInt(),
                        node.get("rotation") != null && node.get("rotation").get("rescale") != null
                                && node.get("rotation").get("rescale").asBoolean(false),
                        node.get("shade") == null || node.get("shade").asBoolean(true),
                        faces
                ));
            }
        }
        final BlockModel nModel = new BlockModel(
                parentModel,
                jsonObj.get("ambientocclusion") == null || jsonObj.get("ambientocclusion").asBoolean(true),
                textureList,
                elements
        );
        modelMap.put(blockName, nModel);
        return nModel;
    }

    /**
     * Attempts to transform a string into a {@link Vector3d}.
     *
     * @param str the sting to transform into a vector.
     * @return the converted string.
     */
    private static Vector3d convertStringToVector(@Nonnull final String str) {
        final String subStr = str.substring(1, str.length() - 1);
        final String[] strings = subStr.replace(" ", "").split(",");
        return new Vector3d(Double.parseDouble(strings[0]), Double.parseDouble(strings[1]), Double.parseDouble(strings[2]));
    }

    /**
     * Attempts to transform a string into a {@link Vector4d}.
     *
     * @param str the sting to transform into a vector.
     * @return the converted string.
     */
    private static Vector4d convertStringToVector4(@Nonnull final String str) {
        final String subStr = str.substring(1, str.length() - 1);
        final String[] strings = subStr.replace(" ", "").split(",");
        return new Vector4d(
                Double.parseDouble(strings[0]),
                Double.parseDouble(strings[1]),
                Double.parseDouble(strings[2]),
                Double.parseDouble(strings[3])
        );
    }
}
