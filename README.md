# bootstrap

[![Build Status](https://travis-ci.com/SpineEventEngine/bootstrap.svg?branch=master)](https://travis-ci.com/SpineEventEngine/bootstrap)

The Gradle plugin for bootstrapping projects built with Spine.

## Applying to the project

In order to apply to a single-module project, reference the plugin in the `plugins` block 
in `build.gralde`:
```gradle
plugins {
    id 'io.spine.tools.gradle.bootstrap' version '0.15.3'
}
```

In order to apply to a multi-module project:
 1. declare the plugin in the root module:
```gradle
plugins {
    id 'io.spine.tools.gradle.bootstrap' version '0.15.3' apply false
}
```
 2. apply the plugin in the `subprojects` section:
```gradle
subprojects {
    apply plugin: 'io.spine.tools.gradle.bootstrap'
}
```
Alternatively, apply the plugin only to some modules. In the `build.gradle` of the target module:
```gradle
apply plugin: 'io.spine.tools.gradle.bootstrap'
```

## Java Projects

In order to mark a (sub-)project as a Java project for Spine, apply the following config:
```gradle
spine.enableJava()
```

This configuration:
 - applies `java` Gradle plugin;
 - applies `com.google.protobuf` Gradle plugin;
 - configures Java code generation from Protobuf;
 - adds a dependency onto the `io.spine:spine-base` module;
 - applies the Spine Model Compiler plugin and performs its minimal configuration.
 
More often than not, a user would also like to mark a Java project as a client or a server module.
To do that, apply the following configuration:
 - for client modules:
```gradle
spine.enableJava().client()
```
 - for server modules:
```gradle
spine.enableJava().server()
```

This config will add required dependencies for developing a Spine-based Java client and server 
respectively.

### gRPC code generation

Spine relies on [gRPC](https://grpc.io/).

All the required gRPC Java stubs and services are already included into the Spine artifacts. 
However, if users would like to declare gRPC services of their own, they may use the following 
configuration to set up the generation seamlessly:
```gradle
spine.enableJava {
    grpc = true
    client() // or server()
}
```

Note that it is required to enable mark the module as either `client()` or `server()`. Otherwise, 
the user would have to add all the gRPC-related dependencies on their own.

## JavaScript Projects

In order to mark a (sub-)project as a JS project for Spine, apply the following config:
```gradle
spine.enableJavaScript()
```

This configuration:
 - applies `com.google.protobuf` and `java` Gradle plugin (as the former depends on the latter);
 - configures JS code generation from Protobuf.
 
If only JS generation is configured, the Java code will not be generated (and the other way around).
