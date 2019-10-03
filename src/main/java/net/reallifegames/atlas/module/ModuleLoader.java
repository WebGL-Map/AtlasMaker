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
package net.reallifegames.atlas.module;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Registers, prioritizes and creates modules for use in a program.
 *
 * @author Tyler Bucher
 */
public class ModuleLoader {

    /**
     * Stores the class name and the class its self.
     */
    private final HashMap<String, Class<Module>> classMap = new HashMap<>();

    /**
     * Stores the constructor arguments for creating an instance of the class.
     */
    private final HashMap<String, String[]> classMapArgs = new HashMap<>();

    /**
     * Stores the module dependencies for a class
     */
    private final HashMap<String, String[]> moduleRegisterMap = new HashMap<>();

    /**
     * The priority tree for the list of modules.
     */
    private final HashMap<String, Integer> modulePriorityMap = new HashMap<>();

    /**
     * The instantiated module list.
     */
    private final HashMap<String, Module> moduleMap = new HashMap<>();

    /**
     * Register a module for this system to use.
     *
     * @param module the module class to register.
     * @param args   the constructor arguments for the class.
     * @param <T>    a type {@code T} which extends {@link Module}.
     * @return true if the module was registered false otherwise.
     */
    @SuppressWarnings ("unchecked")
    public <T extends Module> boolean registerModule(@Nonnull final Class<T> module, @Nonnull final String[] args) {
        // Fetch module annotation information
        final ModuleInfo annotation = module.getAnnotation(ModuleInfo.class);
        if (annotation == null) {
            return false;
        }
        // Register module information for later prioritization
        classMap.put(module.getSimpleName(), (Class<Module>) module);
        classMapArgs.put(module.getSimpleName(), args);
        moduleRegisterMap.put(module.getSimpleName(), annotation.value());
        return true;
    }

    /**
     * @return true if there are any circular dependencies false otherwise.
     */
    private boolean areDependenciesCircular() {
        for (Map.Entry<String, String[]> kvp : moduleRegisterMap.entrySet()) {
            for (String dependent : kvp.getValue()) {
                if (!dependent.equals("")) {
                    for (String dDependent : moduleRegisterMap.get(dependent)) {
                        if (dDependent.equals(kvp.getKey())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Prioritize the registered modules according to their dependency list.
     *
     * @return true if all the modules were prioritized false if there were circular dependencies.
     */
    public boolean prioritizeModules() {
        // Check for circular dependencies
        if (areDependenciesCircular()) {
            return false;
        }
        // Loop though module list till it is empty
        final Set<Map.Entry<String, String[]>> entrySet = moduleRegisterMap.entrySet();
        while (!entrySet.isEmpty()) {
            // Loop through the current list of modules left
            final Iterator<Map.Entry<String, String[]>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String[]> kvp = iterator.next();
                final String[] dependents = kvp.getValue();
                // Check to see if this module has any dependencies
                if (checkModuleDependencies(dependents)) {
                    // Check to see if the module's dependencies have already been prioritized
                    final boolean prioritiesSet = areModulePrioritiesSet(dependents);
                    if (prioritiesSet) {
                        modulePriorityMap.put(kvp.getKey(), getHighestPriority(dependents) + 1);
                        iterator.remove();
                    }
                } else {
                    modulePriorityMap.put(kvp.getKey(), 0);
                    iterator.remove();
                }
            }
        }
        return true;
    }

    /**
     * Create instances of the modules in priority order.
     */
    public void instantiateModules() {
        modulePriorityMap.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .forEach(entry->{
                    final String className = entry.getKey();
                    try {
                        final String[] param = classMapArgs.get(className);
                        final Module mm = classMap.get(className).getDeclaredConstructor(String[].class).newInstance((Object) param);
                        moduleMap.put(className, mm);
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Get the current highest module priority.
     *
     * @param moduleNames the list of modules to check from.
     * @return the highest module priority.
     */
    private int getHighestPriority(@Nonnull final String[] moduleNames) {
        int priority = 0;
        int currentPriority;
        for (String dependent : moduleNames) {
            currentPriority = modulePriorityMap.get(dependent);
            if (currentPriority > priority) {
                priority = currentPriority;
            }
        }
        return priority;
    }

    /**
     * Check to see if a list of modules have been prioritized.
     *
     * @param moduleNames the list of modules to check.
     * @return true if the have all been prioritized false otherwise.
     */
    private boolean areModulePrioritiesSet(@Nonnull final String[] moduleNames) {
        for (String dependent : moduleNames) {
            if (!modulePriorityMap.containsKey(dependent)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check to see if a module has any dependencies.
     *
     * @param moduleNames the list of module dependencies to check.
     * @return return false if the list of modules is empty or contains only a empty string true otherwise.
     */
    private boolean checkModuleDependencies(@Nonnull final String[] moduleNames) {
        if (moduleNames.length == 0) {
            return false;
        } else {
            return moduleNames.length != 1 || !moduleNames[0].isEmpty();
        }
    }

    /**
     * Retrieve a module.
     *
     * @param moduleName the name of a module to get.
     * @return an optional module if it exists.
     */
    public Optional<Module> getModule(@Nonnull final String moduleName) {
        return Optional.ofNullable(moduleMap.get(moduleName));
    }

    /**
     * @return a list of all available modules.
     */
    public Collection<Module> getModules() {
        return moduleMap.values();
    }
}
