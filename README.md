# Introduction 

The [Simplifier](http://simplifier.io) Plugin-API library defines the Communication Slots and Messages for a 
[Plugin](https://community.simplifier.io/doc/current-release/extend/plugins/) to interact with the Application Server.


# Build

Build the artifact with sbt for local use

```sbt publishLocal```


# Library Usage

## build.sbt
```
libraryDependencies += "io.github.com.simplifier-ag" %% "simplifier-plugin-api" % "0.6.0"
```

## Plugin code

I.e. implement class [PluginApp](./src/main/scala/io/simplifier/pluginapi/PluginApp.scala)


