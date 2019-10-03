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

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.reallifegames.atlas.Atlas;

import java.net.URL;

/**
 * JavaFX application class endpoint.
 *
 * @author Tyler Bucher
 */
public class FxApplication extends Application {

    /**
     * The main entry point for all JavaFX applications. The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which the application scene can be set. The
     *                     primary stage will be embedded in the browser if the application was launched as an applet.
     *                     Applications may create other stages, if needed, but they will not be primary stages and will
     *                     not be embedded in the browser.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        URL loc = getClass().getResource("/fx/ui.fxml");
        Parent parent = FXMLLoader.load(loc);
        primaryStage.setTitle("AtlasMaker UI Controller");
        Scene scene = new Scene(parent);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setX((Atlas.videoModeWidth - (scene.getWidth())) / 2 + (Atlas.width / 2.0) + scene.getX());
        primaryStage.setY((Atlas.videoModeHeight - scene.getHeight()) / 2.0 - scene.getY());
        primaryStage.setOnCloseRequest(event->{
            if (!Atlas.closed) {
                event.consume();
            }
        });
    }
}
