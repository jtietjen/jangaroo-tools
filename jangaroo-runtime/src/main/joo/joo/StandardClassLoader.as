/*
 * Copyright 2009 CoreMedia AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 */

// JangarooScript runtime support. Author: Frank Wienberg

package joo {

// this makes jooc generate a with(joo) statement:
import joo.*;

public class StandardClassLoader extends joo.SystemClassLoader {

  private static var classDeclarations : Array = [];

  private var importMap : ImportMap;

  public function StandardClassLoader() {
    this.importMap = new ImportMap();
  }

  override protected function createClassDeclaration(packageDef : String, directives : Array, classDef : String, memberFactory : Function,
                                                  publicStaticMethodNames : Array):SystemClassDeclaration {
    var cd : ClassDeclaration = new ClassDeclaration(packageDef, directives, classDef, memberFactory, publicStaticMethodNames);
    classDeclarations.push(cd); // remember all created classes for later initialization.
    return cd;
  }

  public function loadScript(uri : String) : void {
    var script : * = window.document.createElement("script");
    script.type = "text/javascript";
    window.document.body.appendChild(script);
    script.src = uri;
  }

  /**
   * Import the class given by its fully qualified class name (package plus name).
   * All imports are collected in a hash and can be used in the #complete() callback function.
   * @param fullClassName : String the fully qualified class name (package plus name) of the class to load and import.
   */
  public function import_(fullClassName : String) : void {    
    this.importMap.addImport(fullClassName);
  }

  /**
   * Run the static main method of a class given by its fully qualified name (package plus name), handing through the
   * given arguments (args).
   * The main method is executed after all classes are completed (see #complete()).
   * @param mainClassName : String the fully qualified name (package plus name) or the constructor function
   *   of the class to run.
   * @param args the arguments to hand over to the main method of the given class.
   */
  public function run(mainClassName : String, ...args) : void {
    this.complete(function() : void {
      var mainClass : SystemClassDeclaration = this.getRequiredClassDeclaration(mainClassName) as SystemClassDeclaration;
      mainClass.publicConstructor["main"].apply(null,args);
    });
  }

  /**
   * Explicitly initialize the static members of the given class (constructor function).
   * If the class is not yet initialized, Initializers of static variables and static code blocks are called (once).
   * This is only necessary when you want to access constants of a class without importing the class,
   * or when you have loaded the class explicitly and want its static code to execute.
   * Explicit initializing is <i>not</i> necessary when you
   * - import the class or
   * - load the class and use the constructor or a static method of the class. This will trigger initialization
   *   automatically.
   * @param classes the classes (type Function) to initialize.
   * @return Function the initialized class (constructor function).
   */
  public function init(... classes) : Function {
    var clazz : Function;
    for (var i:int=0; i<classes.length; ++i) {
      if ("$class" in classes[i]) {
        ((clazz = classes[i])["$class"] as NativeClassDeclaration).init();
      }
    }
    return clazz;
  }

  /**
   * Tell Jangaroo to load and initialize all required classes, then call the given function.
   * The function receives an import hash, which can be used in pure JavaScript in a 'with' statement
   * (Jangaroo does not support 'with', there, you would use import declarations!) like this:
   * <pre>
   * joo.classLoader.import_("com.custom.Foo");
   * joo.classLoader.complete(function(imports){with(imports){
   *   Foo.doSomething("bar");
   * }});
   * </pre>
   * @param onCompleteCallback : Function
   * @return void
   */
  public function complete(onCompleteCallback : Function = undefined) : void {
    this.completeAll();
    if (onCompleteCallback) {
      this.doCompleteCallbacks([onCompleteCallback]);
    }
  }

  protected function completeAll() : void {
    classDeclarations.forEach(function(classDeclaration : ClassDeclaration) : void {
      classDeclaration.complete();
      // init native class patches immediately:
      if (classDeclaration.isNative()) {
        classDeclaration.init();
      }
    });
  }

  protected function doCompleteCallbacks(onCompleteCallbacks : Array/*Function*/) : void {
    if (onCompleteCallbacks.length) {
      this.importMap.init();
      var importMap : Object = this.importMap.addToMap({});
      for (var i:int = 0; i < onCompleteCallbacks.length; ++i) {
        (onCompleteCallbacks[i] as Function)(importMap);
      }
    }
  }
}
}