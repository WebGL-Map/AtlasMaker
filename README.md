# AtlasMaker [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

An application designed to extract assets from Minecraft and texture packs and convert them to usable OpenGL data for the
glm-client. Currently the application is in a beta state and does not support MinecraftForge. Bugs are expected to be 
present in the program while in the beta state.

## Building
**Note:** If you do not have Gradle installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows 
systems in place of any 'gradle' command.

In order to build AtlasMaker just run the `gradle build` command. Once that is finished you will find library, sources, and 
javadoc .jars exported into the `./build/libs` folder and the will be labeled like the following.
```
AtlasMaker-x.x.x.jar
AtlasMaker-x.x.x-javadoc.jar
AtlasMaker-x.x.x-sources.jar
```

However, if you wish to build the full AtlasMaker application please use the `shadowJar` command.
```
gradle shadowJar
```

## Usage
**Running the Atlas Maker**, you can run the atlas maker just like any other java application, just make sure to pass 
the program arguments.
```
java -jar <AtlasMaker.jar> <Launch preview window> <Minecraft.jar> <texurepack.zip optional>
```

**Example:**
```
java -jar AtlasMaker.jar false 1.12.2.jar jsmith.zip
```