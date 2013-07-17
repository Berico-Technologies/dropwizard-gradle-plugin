# Gradle Dropwizard Plugin
Gradle Dropwizard Plugin allows you to easily create apps in the style that dropwizard likes which is fat runnable jars.  It will build your java projects and create a fat jar that you can then run.  Tasks are also provided to create a tar distribution and start up scripts for your application.

## Installation
```groovy
buildscript {
    repositories {
        mavenCentral()
	maven {
	   url 'nexus.bericotechnologies.com/content/repositories/releases'
	}
    }
    
    dependencies {
       classpath group: 'com.berico', name: 'DropwizardPlugin', version: '1.0'
    }
}

apply plugin: 'dropwizard'
```

All configuration files such as your YAML file can be placed under PROJECT_ROOT_DIR/src/main/dist.  The distribution task provided will bundle these external from the jar file.


## Tasks

### `createStartupScripts`
will generate startup scripts for your application for unix and windows

### `distTar`
Will bundle your application and startup scripts into a tar file

### `run`
allows you to run the application

## Example Usage and configuration

```groovy
apply plugin: 'dropwizard'

ext.sourceCompatibility = 1.6
version = '1.0'


buildscript {
	repositories {
		maven {
			mavenCentral()
		}
		maven {
			url 'nexus.bericotechnologies.com/content/repositories/releases'
		}
	}
	dependencies {
		classpath group: 'com.berico', name: 'DropwizardPlugin', version: '1.0'
	}
}

repositories {
    mavenCentral()
}

dependencies {
    compile group: 'commons-collections', name: 'commons-collections', version: '3.2'
    testCompile group: 'junit', name: 'junit', version: '4.+'
}


dropwizard {
	mainClassName = "your.main.class.name"
}
```
