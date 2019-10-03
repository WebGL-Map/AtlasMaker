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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import javafx.application.Application;
import net.reallifegames.atlas.Atlas;
import net.reallifegames.atlas.asset.blockmodels.BlockModel;
import net.reallifegames.atlas.asset.blockmodels.Element;
import net.reallifegames.atlas.asset.blockmodels.Face;
import net.reallifegames.atlas.asset.blockstates.BlockState;
import net.reallifegames.atlas.asset.blockstates.Model;
import net.reallifegames.atlas.asset.blockstates.Multipart;
import net.reallifegames.atlas.asset.blockstates.Variant;
import net.reallifegames.atlas.module.Module;
import net.reallifegames.atlas.module.ModuleInfo;
import net.reallifegames.atlas.module.atlas.AtlasModule;
import net.reallifegames.atlas.modules.CameraModule;
import net.reallifegames.atlas.renderable.RenderableBlockModel;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * A module for handling the FxApplication and Controller.
 *
 * @author Tyler Bucher
 */
@ModuleInfo ({"CameraModule", "AtlasModule"})
public class FxModule implements Module {

    /**
     * The Fx runnable thread.
     */
    private Thread fxApplicationThread;

    /**
     * The list of available block states.
     */
    private final Map<String, BlockState> blockStateList;

    /**
     * The renderable for models which use variants.
     */
    public volatile RenderableBlockModel variantModel;

    /**
     * The list of renderable modules which use multiparts.
     */
    private volatile List<RenderableBlockModel> renderModelList = Collections.synchronizedList(new ArrayList<>());

    /**
     * The module for an Atlas object.
     */
    private AtlasModule atlasModule;

    /**
     * Creates a new module for the Fx objects.
     *
     * @param args constructor arguments.
     */
    public FxModule(@Nonnull final String[] args) {
        // Create fx thread
        fxApplicationThread = new Thread(()->Application.launch(FxApplication.class));
        // Add listeners for the camera module
        Atlas.moduleLoader.getModule("CameraModule").ifPresent(module->{
            if (module instanceof CameraModule) {
                final CameraModule cameraModule = (CameraModule) module;
                // Move fx sliders when camera moves
                cameraModule.getAutoRotateVectorProperty().addListener((oldValue, newValue)->{
                    FxController.getInstance().updateAutoRotateRadiusSlider();
                    FxController.getInstance().updateAutoRotatePitchSlider();
                });
                // Cancel fx auto rotate when camera moves
                cameraModule.getAutoRotateProperty().addListener((oldValue, newValue)->{
                    if (!newValue) {
                        FxController.getInstance().cancelAutoRotate();
                    }
                });
            }
        });
        Atlas.moduleLoader.getModule("AtlasModule").ifPresent(module->{
            if (module instanceof AtlasModule) {
                atlasModule = (AtlasModule) module;
            }
        });
        // Load block states
        final File blockStatesDir = new File(Atlas.FULL_TEMP_FOLDER_DIR + "/assets/minecraft/blockstates");
        final File[] fileList = blockStatesDir.listFiles();
        final HashMap<String, String> jsonExport = new HashMap<>();
        blockStateList = new HashMap<>();
        System.out.println("Loading block states from file.");
        if (fileList != null) {
            Arrays.sort(fileList);
            try {
                for (final File file : fileList) {
                    final BlockState blockState = Loader.loadBlockState(file, atlasModule.getTextureAtlas(), Boolean.parseBoolean(args[0]));
                    final String blockStateName = file.getName().replace(".json", "");
                    blockStateList.put(blockStateName, blockState);
                    if (blockState.useMultipart) {
                        try {
                            final StringWriter stringWriter = new StringWriter();
                            final JsonGenerator generator = new JsonFactory().createGenerator(stringWriter);
                            // Primary object start
                            generator.writeStartObject();
                            // Multipart array start
                            generator.writeArrayFieldStart("data");
                            for (final Multipart multipart : blockState.multiparts) {
                                for (final Model model : multipart.modelList) {
                                    writeJsonModelData(generator, model, multipart.conditionalOr, multipart.stateList);
                                }
                            }
                            // Multipart array end
                            generator.writeEndArray();
                            // Primary object End
                            generator.writeEndObject();
                            generator.flush();
                            jsonExport.put(blockStateName, stringWriter.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        final StringWriter stringWriter = new StringWriter();
                        final JsonGenerator generator = new JsonFactory().createGenerator(stringWriter);
                        // Primary object start
                        generator.writeStartObject();
                        // Multipart array start
                        generator.writeArrayFieldStart("data");
                        for (final Variant variant : blockState.blockVariants) {
                            for (final Model model : variant.modelList) {
                                writeJsonModelData(generator, model, false, transformVariantNameToList(variant.name));
                            }
                        }
                        // Multipart array end
                        generator.writeEndArray();
                        // Primary object End
                        generator.writeEndObject();
                        generator.flush();
                        jsonExport.put(blockStateName, stringWriter.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File list is null");
        }
        System.out.println("Exporting WebGL Map block states.");
        final File path = new File(Atlas.FULL_TEMP_FOLDER_DIR + "/export/blockstates");
        if (!path.exists()) {
            path.mkdirs();
        }
        jsonExport.forEach((k, v)->{
            final FileOutputStream outputStream;
            try {
                outputStream = new FileOutputStream(new File(path, k + ".json"));
                outputStream.write(v.getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * @param generator     the json writing generator.
     * @param model         the model to get vertex data for.
     * @param conditionalOr should the states be interpreted with an or.
     * @param stateLists    the list of states for a model to be applied.
     * @throws IOException if the {@link JsonGenerator} can not start an object.
     */
    private void writeJsonModelData(@Nonnull final JsonGenerator generator,
                                    @Nonnull final Model model,
                                    final boolean conditionalOr,
                                    @Nonnull final List<List<Map.Entry<String, String>>> stateLists) throws IOException {
        final RenderableBlockModel renderableBlockModel = (RenderableBlockModel) model.blockModel;
        // Model obj start
        generator.writeStartObject();
        // Obj when field start
        generator.writeObjectFieldStart("when");
        generator.writeBooleanField("conditionalOr", conditionalOr);
        generator.writeArrayFieldStart("states");
        for (final List<Map.Entry<String, String>> stateList : stateLists) {
            generator.writeStartObject();
            for (final Map.Entry<String, String> kvp : stateList) {
                generator.writeStringField(kvp.getKey(), kvp.getValue());
            }
            generator.writeEndObject();
        }
        generator.writeEndArray();
        // Obj when field end
        generator.writeEndObject();
        // Obj apply field start
        generator.writeObjectFieldStart("apply");
        // Obj apply data member start
        generator.writeArrayFieldStart("data");
        generator.writeArray(convertFloatArray(renderableBlockModel.getVertexData()), 0, renderableBlockModel.getVertexData().length);
        generator.writeEndArray();
        // Obj tintindex member field
        generator.writeBooleanField("tintindex", useTintIndex(model.blockModel));
        // Obj apply field end
        generator.writeEndObject();
        // Model obj end
        generator.writeEndObject();
    }

    /**
     * Transforms a string into a list for the json generating function.
     *
     * @param variantName the name to transform.
     * @return the transformed name.
     */
    private List<List<Map.Entry<String, String>>> transformVariantNameToList(@Nonnull final String variantName) {
        final List<Map.Entry<String, String>> returnList = new ArrayList<>();
        final String[] subSplit = variantName.split(",");
        for (String s : subSplit) {
            final String[] finalSplit = s.split("=");
            returnList.add(new AbstractMap.SimpleEntry<>(finalSplit[0], finalSplit.length != 2 ? "<n/a>" : finalSplit[1]));
        }
        return new ArrayList<>(Collections.singleton(returnList));
    }

    /**
     * Gets the tint index state for the first face to return it.
     *
     * @param blockModel the model to check.
     * @return true if the model should use a tint index.
     */
    private boolean useTintIndex(@Nonnull final BlockModel blockModel) {
        for (final Element element : blockModel.elements) {
            for (final Map.Entry<String, Face> entry : element.faces.entrySet()) {
                if (entry.getValue().tintIndex != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Converts a float array to a double array.
     *
     * @param items the items to convert.
     * @return the converted items.
     */
    private double[] convertFloatArray(@Nonnull final float[] items) {
        final double[] convertedItems = new double[items.length];
        for (int i = 0; i < items.length; i++) {
            convertedItems[i] = items[i];
        }
        return convertedItems;
    }

    /**
     * @return the FxApplication thread handler.
     */
    public Thread getFxApplicationThread() {
        return fxApplicationThread;
    }

    /**
     * @return the list of renderable modules which use multiparts.
     */
    public List<RenderableBlockModel> getRenderModelList() {
        return renderModelList;
    }

    /**
     * @return the list of available block states.
     */
    public Map<String, BlockState> getBlockStateList() {
        return blockStateList;
    }

    @Override
    public void input(final double displacement) {

    }

    @Override
    public void update(final double displacement) {

    }

    @Override
    public void render(final double displacement) {
        if (variantModel != null) {
            variantModel.draw();
        } else if (!renderModelList.isEmpty()) {
            renderModelList.forEach(RenderableBlockModel::draw);
        }
    }
}
