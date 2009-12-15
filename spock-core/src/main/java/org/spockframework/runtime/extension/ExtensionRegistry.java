/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.extension;

import java.net.URL;
import java.util.*;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

/**
 * Collects all Spock extensions found on the class path (via their descriptor).
 *
 * @author Peter Niederwieser
 */
public class ExtensionRegistry {
  static final String DESCRIPTOR_PATH = "META-INF/services/" + IGlobalExtension.class.getName();
  
  private static final ExtensionRegistry instance = new ExtensionRegistry(ExtensionRegistry.class.getClassLoader());

  private final List<IGlobalExtension> extensions = new ArrayList<IGlobalExtension>();
  private final ClassLoader classLoader;

  ExtensionRegistry(ClassLoader classLoader) {
    this.classLoader = classLoader;
    loadExtensions();
  }

  private void loadExtensions() {
    for (URL url : loadDescriptors())
      extensions.add(loadExtension(url));
  }

  private List<URL> loadDescriptors() {
    try {
      return Collections.list(classLoader.getResources(DESCRIPTOR_PATH));
    } catch (Exception e) {
      throw new ExtensionException("Failed to locate extension descriptors", e);
    }
  }

  private IGlobalExtension loadExtension(URL url) {
    return instantiateExtension(verifyExtensionClass(loadExtensionClass(readDescriptor(url))));
  }

  private String readDescriptor(URL url) {
    try {
      return DefaultGroovyMethods.getText(url).trim();
    } catch (Exception e) {
      throw new ExtensionException("Failed to read extension descriptor '%s'", e).format(url);
    }
  }

  private Class<?> loadExtensionClass(String className) {
    try {
      return classLoader.loadClass(className);
    } catch (Exception e) {
      throw new ExtensionException("Failed to load extension class '%s'", e).format(className);
    }
  }

  private Class<?> verifyExtensionClass(Class<?> clazz) {
    if (!IGlobalExtension.class.isAssignableFrom(clazz))
      throw new ExtensionException(
          "Class '%s' is not a valid extension because it is not derived from '%s'"
      ).format(clazz.getName(), IGlobalExtension.class.getName());
    return clazz;
  }

  private IGlobalExtension instantiateExtension(Class<?> clazz) {
    try {
      return (IGlobalExtension)clazz.newInstance();
    } catch (Exception e) {
      throw new ExtensionException("Failed to instantiate extension '%s'", e).format(clazz.getName());
    }
  }

  public static ExtensionRegistry getInstance() {
    return instance;
  }

  public List<IGlobalExtension> getExtensions() {
    return extensions;
  }
}
