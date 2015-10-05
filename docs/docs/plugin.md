---
title: Extend Marathon with Plugins
---


# Extend Marathon with Plugins

<div class="alert alert-danger" role="alert">
  <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> Available in Marathon Version 0.12+ <br/>
  The Marathon plugin functionality is considered Alpha! <br/>
  You can use this feature at you own risk. <br/>
  We might want to add, change or delete any functionality described in this document. 
</div>

## Overview

<p class="text-center">
  <img src="{{ site.baseurl}}/img/plugin-mechanism.png" width="552" height="417" alt="">
</p>


Starting with Marathon 0.12 we provide the ability to extend functionality in Marathon with plugins.
Every extension comprises this functionality:

- __Extension Aware Functionality:__ Marathon makes specific functionality available for customization. There are hooks implemented into the main system, that can be refined and extended with external plugins.
- __Extension Interface:__ The extended functionality is defined as Scala trait (Java interface). In order to extend this functionality, the plugin writer has to implement that interface.
- __Plugin Extension:__ This is the implementation of a defined Extension Interface. The implementation needs to be compiled and packaged into one or more separate jar files. 
- __Plugin Descriptor:__ The descriptor defines, which set of plugins should be enabled in one instance of Marathon.
  


## Plugin Interface

A separate Marathon Plugin interface jar is published with every Marathon Release on the following Maven repository:

```
http://downloads.mesosphere.io/maven
```

with following dependency:

SBT:

```
"mesosphere.marathon" %% "plugin-interface" % "x.x.x" % "provided"

```

Maven:

```
<dependency>
    <groupId>mesosphere.marathon</groupId>
    <artifactId>plugin-interface_2.11</artifactId>
    <version>x.x.x</version>
</dependency>
```

Please make sure, that you always use the same version of the plugin interface as the version of Marathon in production.



## Extension Functionality

To create an extension, you have to implement an extension interface from the plugin interface method and put it in a location available to Marathon, so that it can be loaded by the ServiceLoader.

### Service Loader

Marathon makes use of the [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) 
to make service provider available to a running Marathon instance. For background information on that topic, please see [Creating Extensible Applications](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html).

### Dependencies to other libraries

The plugin writer can define dependencies to other libraries and use those dependencies, if following requirements are met:

- Marathon already has a dependency to that library. You can see all Marathon dependencies by checking out Marathon and running `sbt dependencyTree`. 
  Such a dependency can be defined as `provided` in your build tool of choice.
  _Please make sure, you use the exact same version of that library!_
- A library that does not conflict with any library bundled with Marathon (e.g. different byte code manipulation libraries are known to interfere). 
  Such a dependency should be defined as `compile` dependency in your build tool of choice.
  
Dependant libraries that are not already provided by Marathon have to be shipped along with the plugin itself.
 
### Packaging

The plugin has to be compiled and packaged as `jar` file.
Inside that jar file the [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) specific provider-configuration file needs to be included.
 

### Logging

Marathon uses `slf4j` for logging. The dependency to this library is already available with the plugin interface package.
If you use these logger classes, all log statement will be available in Marathon.

### Configuration

Since the plugin mechanism in Marathon relies on the ServiceLoader, the plugin class has to implement a default constructor.
To pass a certain configuration to the plugin, you can use whatever functionality you like.

We built one optional way, that can be used by your plugin: If you implement the interface [PluginConfiguration](https://github.com/mesosphere/marathon/blob/master/plugin-interface/src/main/scala/mesosphere/marathon/plugin/plugin/PluginConfiguration.scala)
then the configuration from the plugin descriptor is passed to every created instance of that plugin, before any other method is called.


## Plugin Descriptor

You can extend Marathon with a set of plugins. 
To make sure only valid combinations of plugins are taken, you have to provide a plugin descriptor.
That descriptor defines which plugin interface is extended by which service provider.
Marathon will load this descriptor with all defined plugins during start up.
 
Example:

```json
{
  "plugins": [
    {
      "plugin": "mesosphere.marathon.plugin.auth.Authorizer",
      "implementation": "my.company.ExampleAuthorizer"
    },
    {
      "plugin": "mesosphere.marathon.plugin.auth.Authenticator",
      "implementation": "my.company.ExampleAuthenticator",
      "configuration": {
        "users": [
          {
            "user": "ernie",
            "password" : "ernie",
            "permissions": [
              { "allowed": "create", "on": "/dev/" }
            ]
          }
        ]
      }
    }
  ]
}
```

The configuration section of one plugin is optional. If provided, the json object is given as is to the plugin.

## Start Marathon with plugins

In order to make your plugins available to Marathon you have to:

- place your plugin.jar with all dependant jars (that are not already provided by Marathon) into a directory
- write a plugin descriptor, that list the plugins to use

Now you can start Marathon with this options:

- `--plugin_dir path` where path is /the/directory/with/your/plugin.jar
- `--plugin_conf conf.json` where conf.json is /path/to/plugin-descriptor.json


# Available Plugin Extensions

## Security

#### [mesosphere.marathon.plugin.auth.Authenticator](https://github.com/mesosphere/marathon/blob/master/plugin-interface/src/main/scala/mesosphere/marathon/plugin/auth/Authenticator.scala)

Marathon exposes an HTTP REST API. To make sure only authenticated identities can access the system, the plugin developer has to implement the Authenticator interface.
This interface relies purely on the HTTP Layer. Please see the [Authenticator](https://github.com/mesosphere/marathon/blob/master/plugin-interface/src/main/scala/mesosphere/marathon/plugin/auth/Authenticator.scala) trait for documentation, as well as the [Example Scala Plugin](https://github.com/mesosphere/marathon-example-plugins/tree/master/auth) or the [Example Java Plugin](https://github.com/mesosphere/marathon-example-plugins/tree/master/javaauth)

#### [mesosphere.marathon.plugin.auth.Authorizer](https://github.com/mesosphere/marathon/blob/master/plugin-interface/src/main/scala/mesosphere/marathon/plugin/auth/Authorizer.scala)

This plugin plays along the Authentication plugin. With this interface you can refine, what actions an authenticated identity can perform on Marathon's resources. 
Please see the [Authorizer](https://github.com/mesosphere/marathon/blob/master/plugin-interface/src/main/scala/mesosphere/marathon/plugin/auth/Authorizer.scala) trait for documentation as well as [Example Scala Plugin](https://github.com/mesosphere/marathon-example-plugins/tree/master/auth) or the [Example Java Plugin](https://github.com/mesosphere/marathon-example-plugins/tree/master/javaauth)   



