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

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import net.reallifegames.atlas.Atlas;
import net.reallifegames.atlas.asset.blockstates.BlockState;
import net.reallifegames.atlas.asset.blockstates.Model;
import net.reallifegames.atlas.asset.blockstates.Multipart;
import net.reallifegames.atlas.asset.blockstates.Variant;
import net.reallifegames.atlas.module.platform.PlatformModule;
import net.reallifegames.atlas.modules.CameraModule;
import net.reallifegames.atlas.renderable.RenderableBlockModel;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The Fx controlling class to power the UI.
 *
 * @author Tyler Bucher
 */
public class FxController implements Initializable {

    private static FxController instance;

    private CameraModule cameraModule;

    private FxModule fxModule;

    private PlatformModule platformModule;

    @FXML
    private CheckBox shadowCheckBox;

    @FXML
    private Slider ambientSlider;

    @FXML
    private AnchorPane sunPosPane;

    @FXML
    private Slider sunRadiusSlider;

    @FXML
    private Slider sunPitchSlider;

    @FXML
    private Slider sunYawSlider;

    @FXML
    private CheckBox cameraAutoRotateCheckBox;

    @FXML
    private AnchorPane cameraAutoRotatePane;

    @FXML
    private Slider cameraAutoRotateSpeedSlider;

    @FXML
    private Slider cameraAutoRotateRadiusSlider;

    @FXML
    private Slider cameraAutoRotatePitchSlider;

    @FXML
    private CheckBox renderPlatformCheckBox;

    @FXML
    private AnchorPane renderPlatformPane;

    @FXML
    private Slider platformRadiusSlider;

    @FXML
    private CheckBox platformUseTextureCheckBox;

    @FXML
    private ColorPicker platformColorPicker;

    @FXML
    private CheckMenuItem platformFaceTop;

    @FXML
    private CheckMenuItem platformFaceBottom;

    @FXML
    private CheckMenuItem platformFaceNorth;

    @FXML
    private CheckMenuItem platformFaceEast;

    @FXML
    private CheckMenuItem platformFaceSouth;

    @FXML
    private CheckMenuItem platformFaceWest;

    @FXML
    private ColorPicker gridColorPicker;

    @FXML
    private Slider gridRadiusSlider;

    @FXML
    private ColorPicker microGridColorPicker;

    @FXML
    private Slider microGridRadiusSlider;

    @FXML
    private CheckBox renderGridCheckBox;

    @FXML
    private CheckBox renderMicroGridCheckBox;

    @FXML
    private AnchorPane gridPane;

    @FXML
    private AnchorPane microGridPane;

    @FXML
    private ChoiceBox<String> modelChoiceBox;

    @FXML
    private ChoiceBox<String> variantChoiceBox;

    @FXML
    private AnchorPane variantAnchorPane;

    @FXML
    private TextField variantXTextField;

    @FXML
    private TextField variantYTextField;

    @FXML
    private TextField variantUvTextField;

    @FXML
    private TextField variantWeightTextField;

    @FXML
    private ListView<String> variantListView;

    @FXML
    private AnchorPane multipartAnchorPane;

    @FXML
    private ListView<String> multipartListView;

    @FXML
    private ChoiceBox<String> multipartChoiceBox;

    @FXML
    private CheckBox multipartOrCheckBox;

    @FXML
    private TextField multipartXTextField;

    @FXML
    private TextField multipartYTextField;

    @FXML
    private TextField multipartUvTextField;

    @FXML
    private TextField multipartWeightTextField;

    @FXML
    private ListView<String> multipartModelListView;

    private static Runnable cancelAutoRotateRunnable = ()->{
        if (FxController.instance != null) {
            FxController.instance.cameraAutoRotateCheckBox.setSelected(false);
            FxController.instance.cameraAutoRotateCheckBox.fireEvent(new ActionEvent());
        }
    };

    private static Runnable updateAutoRotateRadiusSliderRunnable = ()->{
        if (FxController.instance != null) {
            FxController.instance.cameraAutoRotateRadiusSlider.adjustValue(FxController.instance.cameraModule.getAutoRotateVectorProperty().getProperty().z);
        }
    };

    private static Runnable updateAutoRotatePitchSliderRunnable = ()->{
        if (FxController.instance != null) {
            FxController.instance.cameraAutoRotatePitchSlider.adjustValue(Math.toDegrees(FxController.instance.cameraModule.getAutoRotateVectorProperty().getProperty().x));

        }
    };

    @FXML
    private void onShadowCheckBoxAction(@Nonnull final ActionEvent event) {
        Atlas.useLightingProperty.setProperty(shadowCheckBox.isSelected());
        sunPosPane.setDisable(!shadowCheckBox.isSelected());
    }

    @FXML
    private void onCameraAutoRotateCheckBoxAction(@Nonnull final ActionEvent event) {
        cameraModule.updateAutoRotate(cameraAutoRotateCheckBox.isSelected());
        cameraAutoRotatePane.setDisable(!cameraAutoRotateCheckBox.isSelected());
    }

    @FXML
    private void onRenderPlatformCheckBoxAction(@Nonnull final ActionEvent event) {
        platformModule.getRenderPlatformProperty().setProperty(renderPlatformCheckBox.isSelected());
        renderPlatformPane.setDisable(!renderPlatformCheckBox.isSelected());
    }

    @FXML
    private void onPlatformUseTextureCheckBoxAction(@Nonnull final ActionEvent event) {
        platformModule.getPlatform().getRenderTextureProperty().setProperty(platformUseTextureCheckBox.isSelected());
    }

    @FXML
    private void onPlatformFaceTop(@Nonnull final ActionEvent event) {
        if (platformFaceTop.isSelected()) {
            platformModule.getPlatform().renderPlatformFaces |= 0b000001;
        } else {
            platformModule.getPlatform().renderPlatformFaces ^= 0b000001;
        }
    }

    @FXML
    private void onPlatformFaceBottom(@Nonnull final ActionEvent event) {
        if (platformFaceBottom.isSelected()) {
            platformModule.getPlatform().renderPlatformFaces |= 0b100000;
        } else {
            platformModule.getPlatform().renderPlatformFaces ^= 0b100000;
        }
    }

    @FXML
    private void onPlatformFaceNorth(@Nonnull final ActionEvent event) {
        if (platformFaceNorth.isSelected()) {
            platformModule.getPlatform().renderPlatformFaces |= 0b000010;
        } else {
            platformModule.getPlatform().renderPlatformFaces ^= 0b000010;
        }
    }

    @FXML
    private void onPlatformFaceEast(@Nonnull final ActionEvent event) {
        if (platformFaceEast.isSelected()) {
            platformModule.getPlatform().renderPlatformFaces |= 0b010000;
        } else {
            platformModule.getPlatform().renderPlatformFaces ^= 0b010000;
        }
    }

    @FXML
    private void onPlatformFaceSouth(@Nonnull final ActionEvent event) {
        if (platformFaceSouth.isSelected()) {
            platformModule.getPlatform().renderPlatformFaces |= 0b001000;
        } else {
            platformModule.getPlatform().renderPlatformFaces ^= 0b001000;
        }
    }

    @FXML
    private void onPlatformFaceWest(@Nonnull final ActionEvent event) {
        if (platformFaceWest.isSelected()) {
            platformModule.getPlatform().renderPlatformFaces |= 0b000100;
        } else {
            platformModule.getPlatform().renderPlatformFaces ^= 0b000100;
        }
    }

    @FXML
    private void onRenderGridCheckBoxAction(@Nonnull final ActionEvent event) {
        platformModule.getUseGridProperty().setProperty(renderGridCheckBox.isSelected());
        gridPane.setDisable(!renderGridCheckBox.isSelected());
    }

    @FXML
    private void onRenderMicroGridCheckBoxAction(@Nonnull final ActionEvent event) {
        platformModule.getUseMicroGridProperty().setProperty(renderMicroGridCheckBox.isSelected());
        microGridPane.setDisable(!renderMicroGridCheckBox.isSelected());
    }

    /**
     * Called to initialize a controller after its root element has been completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  <tt>null</tt> if the location is not known.
     * @param resources The resources used to localize the root object, or <tt>null</tt> if
     */
    @Override
    public void initialize(@Nonnull final URL location, @Nonnull final ResourceBundle resources) {
        // Setup instance
        instance = this;
        Atlas.moduleLoader.getModule("CameraModule").ifPresent(module->{
            if (module instanceof CameraModule) {
                this.cameraModule = (CameraModule) module;
            }
        });
        Atlas.moduleLoader.getModule("FxModule").ifPresent(module->{
            if (module instanceof FxModule) {
                this.fxModule = (FxModule) module;
            }
        });
        Atlas.moduleLoader.getModule("PlatformModule").ifPresent(module->{
            if (module instanceof PlatformModule) {
                this.platformModule = (PlatformModule) module;
            }
        });
        initializeListeners();
        // shadow
        final Vector3f tempVector = new Vector3f();
        ambientSlider.adjustValue(Atlas.ambientProperty.getProperty());
        shadowCheckBox.setSelected(Atlas.useLightingProperty.getProperty());
        sunPosPane.setDisable(!shadowCheckBox.isSelected());
        sunRadiusSlider.adjustValue(Atlas.lightArcProperty.getProperty().z);
        sunPitchSlider.adjustValue(Math.toDegrees(Atlas.lightArcProperty.getProperty().x));
        sunYawSlider.adjustValue(Math.toDegrees(Atlas.lightArcProperty.getProperty().y));
        // auto rotate
        cameraAutoRotateCheckBox.setSelected(cameraModule.getAutoRotateProperty().getProperty());
        cameraAutoRotatePane.setDisable(!cameraAutoRotateCheckBox.isSelected());
        cameraAutoRotateSpeedSlider.adjustValue(cameraModule.getAutoRotateVectorProperty().getProperty().y);
        cameraAutoRotateRadiusSlider.adjustValue(cameraModule.getAutoRotateVectorProperty().getProperty().z);
        cameraAutoRotatePitchSlider.adjustValue(Math.toDegrees(cameraModule.getAutoRotateVectorProperty().getProperty().x));
        // platform
        renderPlatformCheckBox.setSelected(platformModule.getRenderPlatformProperty().getProperty());
        renderPlatformPane.setDisable(!renderPlatformCheckBox.isSelected());
        platformRadiusSlider.adjustValue(platformModule.getPlatform().getRadius());
        platformUseTextureCheckBox.setSelected(platformModule.getPlatform().getRenderTextureProperty().getProperty());
        tempVector.set(platformModule.getPlatform().getColor());
        platformColorPicker.setValue(Color.color(tempVector.x, tempVector.y, tempVector.z));
        // platform faces
        platformFaceTop.setSelected((platformModule.getPlatform().renderPlatformFaces & 0b000001) == 0b000001);
        platformFaceNorth.setSelected((platformModule.getPlatform().renderPlatformFaces & 0b000010) == 0b000010);
        platformFaceWest.setSelected((platformModule.getPlatform().renderPlatformFaces & 0b000100) == 0b000100);
        platformFaceSouth.setSelected((platformModule.getPlatform().renderPlatformFaces & 0b001000) == 0b001000);
        platformFaceEast.setSelected((platformModule.getPlatform().renderPlatformFaces & 0b010000) == 0b010000);
        platformFaceBottom.setSelected((platformModule.getPlatform().renderPlatformFaces & 0b100000) == 0b100000);
        // Grid
        tempVector.set(platformModule.getPlatform().getGrid().getColor());
        gridColorPicker.setValue(Color.color(tempVector.x, tempVector.y, tempVector.z));
        gridRadiusSlider.setValue(platformModule.getPlatform().getGrid().getRadius());
        renderGridCheckBox.setSelected(platformModule.getUseGridProperty().getProperty());
        gridPane.setDisable(!renderGridCheckBox.isSelected());
        // Micro Grid
        tempVector.set(platformModule.getPlatform().getMicroGrid().getColor());
        microGridColorPicker.setValue(Color.color(tempVector.x, tempVector.y, tempVector.z));
        microGridRadiusSlider.setValue(platformModule.getPlatform().getMicroGrid().getRadius());
        renderMicroGridCheckBox.setSelected(platformModule.getUseMicroGridProperty().getProperty());
        microGridPane.setDisable(!renderMicroGridCheckBox.isSelected());
        // Set choice box
        final List<String> blockSetList = new ArrayList<>(fxModule.getBlockStateList().keySet());
        blockSetList.sort(String.CASE_INSENSITIVE_ORDER);
        modelChoiceBox.getItems().addAll(blockSetList);
        modelChoiceBox.getSelectionModel().select(0);
        multipartListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @SuppressWarnings ("Duplicates")
    private void initializeListeners() {
        sunRadiusSlider.valueProperty().addListener((observable, oldValue, newValue)->{
            Atlas.lightArcProperty.setProperty(Atlas.lightArcProperty.getProperty().x, Atlas.lightArcProperty.getProperty().y, newValue.floatValue());
            Atlas.updateLighting = true;
        });
        sunPitchSlider.valueProperty().addListener((observable, oldValue, newValue)->{
            Atlas.lightArcProperty.setProperty((float) Math.toRadians(newValue.floatValue()), Atlas.lightArcProperty.getProperty().y, Atlas.lightArcProperty.getProperty().z);
            Atlas.updateLighting = true;
        });
        sunYawSlider.valueProperty().addListener((observable, oldValue, newValue)->{
            Atlas.lightArcProperty.setProperty(Atlas.lightArcProperty.getProperty().x, (float) Math.toRadians(newValue.floatValue()), Atlas.lightArcProperty.getProperty().z);
            Atlas.updateLighting = true;
        });
        ambientSlider.valueProperty().addListener((observable, oldValue, newValue)->{
            Atlas.ambientProperty.setProperty(newValue.floatValue());
            Atlas.updateLighting = true;
        });
        cameraAutoRotateSpeedSlider.valueProperty().addListener((observable, oldValue, newValue)->
                cameraModule.updateAutoRotateSpeed(newValue.floatValue()));
        cameraAutoRotateRadiusSlider.valueProperty().addListener((observable, oldValue, newValue)->
                cameraModule.updateAutoRotatePosition(cameraModule.getAutoRotateVectorProperty().getProperty().x, newValue.floatValue()));
        cameraAutoRotatePitchSlider.valueProperty().addListener((observable, oldValue, newValue)->
                cameraModule.updateAutoRotatePosition((float) Math.toRadians(newValue.doubleValue()), cameraModule.getAutoRotateVectorProperty().getProperty().z));

        platformRadiusSlider.valueProperty().addListener((observable, oldValue, newValue)->{
            platformModule.setPlatformRadius(newValue.floatValue());
        });
        platformColorPicker.valueProperty().addListener((observable, oldValue, newValue)->{
            platformModule.setPlatformColor((float) newValue.getRed(), (float) newValue.getGreen(), (float) newValue.getBlue());
        });
        gridColorPicker.valueProperty().addListener((observable, oldValue, newValue)->{
            platformModule.setGridColor((float) newValue.getRed(), (float) newValue.getGreen(), (float) newValue.getBlue());
        });
        gridRadiusSlider.valueProperty().addListener((observable, oldValue, newValue)->{
            gridRadiusSlider.setValue(Math.round(newValue.doubleValue()) - 0.5);
            platformModule.setGridRadius((float) gridRadiusSlider.getValue());
        });
        microGridColorPicker.valueProperty().addListener((observable, oldValue, newValue)->{
            platformModule.setMicroGridColor((float) newValue.getRed(), (float) newValue.getGreen(), (float) newValue.getBlue());
        });
        microGridRadiusSlider.valueProperty().addListener((observable, oldValue, newValue)->{
            microGridRadiusSlider.setValue(Math.round(newValue.doubleValue()) - 0.5);
            platformModule.setMicroGridRadius((float) microGridRadiusSlider.getValue());
        });
        modelChoiceBox.valueProperty().addListener((observable, oldValue, newValue)->{
            final BlockState state = fxModule.getBlockStateList().get(newValue);
            if (state.useMultipart) {
                variantAnchorPane.setDisable(true);
                multipartAnchorPane.setDisable(false);
                multipartChoiceBox.getItems().clear();
                for (int i = 0; i < state.multiparts.size(); i++) {
                    multipartChoiceBox.getItems().add(String.valueOf(i));
                }
                multipartChoiceBox.getSelectionModel().select(0);
                fxModule.variantModel = null;
                fxModule.getRenderModelList().clear();
            } else {
                multipartAnchorPane.setDisable(true);
                variantAnchorPane.setDisable(false);
                variantChoiceBox.getItems().clear();
                variantChoiceBox.getItems().addAll(state.blockVariants.stream().map(variant->variant.name).collect(Collectors.toList()));
                variantChoiceBox.getSelectionModel().select(0);
            }
        });

        multipartChoiceBox.valueProperty().addListener((observable, oldValue, newValue)->{
            if (newValue == null) {
                return;
            }
            final BlockState state = fxModule.getBlockStateList().get(modelChoiceBox.getValue());
            multipartListView.getItems().clear();
            if (state.multiparts != null) {
                final Multipart multipart = state.multiparts.get(Integer.valueOf(newValue));

                if (multipart.stateList.size() == 0) {
                    multipartListView.getItems().add("<All ways visible>");
                } else {
                    multipart.stateList.forEach(superList->{
                        final StringBuilder builder = new StringBuilder();
                        superList.forEach(stateList->builder.append(stateList.getKey()).append(": ").append(stateList.getValue()).append(", "));
                        if (builder.length() != 0) {
                            builder.delete(builder.length() - 2, builder.length() - 1);
                        }
                        multipartListView.getItems().add(builder.toString());
                    });
                }
                multipartOrCheckBox.setSelected(multipart.conditionalOr);
            }
        });
        multipartListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)->{
            if (newValue == null) {
                return;
            }
            final BlockState state = fxModule.getBlockStateList().get(modelChoiceBox.getValue());
            multipartModelListView.getItems().clear();
            final Multipart multipart = state.multiparts.get(Integer.valueOf(multipartChoiceBox.getValue()));

            multipartModelListView.getItems().addAll(IntStream.range(0, multipart.modelList.size())
                    .mapToObj(index->multipart.modelList.get(index).modelName + "_" + index).collect(Collectors.toList()));
            if (!multipartModelListView.getItems().isEmpty()) {
                multipartModelListView.getSelectionModel().clearAndSelect(0);
            }
        });
        multipartModelListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)->{
            final BlockState state = fxModule.getBlockStateList().get(modelChoiceBox.getValue());
            final Multipart multipart = state.multiparts.get(Integer.valueOf(multipartChoiceBox.getValue()));

            if (newValue != null) {
                Model model = multipart.modelList.get(Integer.parseInt(newValue.substring(newValue.length() - 1)));
                multipartXTextField.setText(String.valueOf(model.xRotation));
                multipartYTextField.setText(String.valueOf(model.yRotation));
                multipartUvTextField.setText(String.valueOf(model.uvLock));
                multipartWeightTextField.setText(String.valueOf(model.weight));
                if (!fxModule.getRenderModelList().contains(model.blockModel)) {
                    fxModule.getRenderModelList().add((RenderableBlockModel) model.blockModel);
                }
            } else {
                Model model = multipart.modelList.get(Integer.parseInt(oldValue.substring(oldValue.length() - 1)));
                if (fxModule.getRenderModelList().contains(model.blockModel)) {
                    fxModule.getRenderModelList().remove(model.blockModel);
                }
            }
        });

        variantChoiceBox.valueProperty().addListener((observable, oldValue, newValue)->{
            final BlockState state = fxModule.getBlockStateList().get(modelChoiceBox.getValue());
            variantListView.getItems().clear();
            final Optional<Variant> optionalVariant = state.blockVariants.stream().filter(variant->variant.name.equals(newValue)).findFirst();

            optionalVariant.ifPresent(variant->variantListView.getItems().addAll(IntStream.range(0, variant.modelList.size())
                    .mapToObj(index->variant.modelList.get(index).modelName + "_" + index).collect(Collectors.toList())));
            if (!variantListView.getItems().isEmpty()) {
                variantListView.getSelectionModel().clearAndSelect(0);
            }
        });
        variantListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue)->{
            final BlockState state = fxModule.getBlockStateList().get(modelChoiceBox.getValue());
            final Optional<Variant> optionalVariant = state.blockVariants.stream().filter(variant->variant.name.equals(variantChoiceBox.getValue())).findFirst();
            optionalVariant.ifPresent(variant->{
                if (newValue != null) {
                    Model model = variant.modelList.get(Integer.parseInt(newValue.substring(newValue.length() - 1)));
                    variantXTextField.setText(String.valueOf(model.xRotation));
                    variantYTextField.setText(String.valueOf(model.yRotation));
                    variantUvTextField.setText(String.valueOf(model.uvLock));
                    variantWeightTextField.setText(String.valueOf(model.weight));
                    fxModule.variantModel = (RenderableBlockModel) model.blockModel;
                }
            });
        });
    }

    void cancelAutoRotate() {
        Platform.runLater(cancelAutoRotateRunnable);
    }

    void updateAutoRotateRadiusSlider() {
        Platform.runLater(updateAutoRotateRadiusSliderRunnable);
    }

    void updateAutoRotatePitchSlider() {
        Platform.runLater(updateAutoRotatePitchSliderRunnable);
    }

    static FxController getInstance() {
        return instance;
    }
}
