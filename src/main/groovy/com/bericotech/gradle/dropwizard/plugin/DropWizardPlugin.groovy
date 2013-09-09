package com.berico.gradle.dropwizard.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.application.CreateStartScripts
import eu.appsatori.gradle.fatjar.FatJarPlugin


class DropWizardPlugin implements Plugin<Project> {


	private Project project

	private DropWizardPluginExtension pluginExtension

	static final String TASK_DIST_TAR_NAME = "distTar"

	static final String DROPWIZ_GROUP = "dropwizard"

	static final String TASK_START_SCRIPTS_NAME = "startScripts"

	static final String FAT_JAR_TASK_NAME = "fatJar"
	
	static final String FATJAR_PREPARE_FILES = "fatJarPrepareFiles"

	public void apply(Project project) {
		project.extensions.create('dropwizard', DropWizardPluginExtension)
		this.project = project
		project.apply(plugin: 'java')
		project.plugins.apply FatJarPlugin
		configureMergeFiles()
		addJavaExecTask()
		addFatJarTask()
		addCreateScriptsTask()
		addArchiveTask()
	}
	
	private void configureMergeFiles( ) {
		def preparefiles = project.tasks.findByName(FATJAR_PREPARE_FILES)
		preparefiles.group = DROPWIZ_GROUP
		preparefiles.doFirst( {
				preparefiles.include('META-INF/spring.schemas')
				preparefiles.include('META-INF/spring.handlers')
			}
		)
	}


	private void addJavaExecTask( ) {
		def run = project.tasks.create("run", JavaExec)
		run.description = "Runs this project as a JVM application"
		run.group = "dropwizard"
		run.classpath = project.sourceSets.main.runtimeClasspath
		run.conventionMapping.main = { project.dropwizard.mainClassName }
	}

	private void addFatJarTask( ) {
		def fatjar = project.tasks.findByName(FAT_JAR_TASK_NAME)
		def jar = project.tasks.findByName("jar")
		File stageDir = new File(project.buildDir.absolutePath + FatJarPlugin.FATJAR_STAGE_DIR)
		jar.dependsOn project.fatJarPrepareFiles
		jar.from stageDir
		jar.doFirst {
			jar.manifest( {
				attributes("Main-Class": project.dropwizard.mainClassName)
			} )
			jar.exclude("META-INF/*.DSA")
			jar.exclude("META-INF/*.RSA")
			jar.exclude("META-INF/*.SF")
		}
		project.tasks.remove(fatjar)
	}
	
	private void addArchiveTask( ) {
		def archiveTask = project.tasks.create(TASK_DIST_TAR_NAME, Tar )
		archiveTask.description = "Bundles the project as a JVM application with libs and OS specific scripts."
		archiveTask.group = DROPWIZ_GROUP
		archiveTask.conventionMapping.baseName = { project.name }
		def baseDir = { archiveTask.archiveName - ".${archiveTask.extension}" }
		archiveTask.into(baseDir) { with(configureDistSpec()) }
	}

	private void addCreateScriptsTask() {
		def startScripts = project.tasks.create(TASK_START_SCRIPTS_NAME, CreateStartScripts)
		startScripts.description = "Creates OS specific scripts to run the project as a JVM application."
		//since this is a fat jar app we don't need to include the runtime deps
		//startScripts.classpath = project.tasks[FAT_JAR_TASK_NAME].outputs.files
		startScripts.classpath = project.tasks["jar"].outputs.files
		startScripts.conventionMapping.mainClassName = { project.dropwizard.mainClassName }
		startScripts.conventionMapping.applicationName = {project.name}
		startScripts.conventionMapping.outputDir = { new File(project.buildDir, 'scripts') }
	}

	private CopySpec configureDistSpec() {
		def distSpec = project.copySpec {}
		//def jar = project.tasks[FAT_JAR_TASK_NAME]
		def jar = project.tasks["jar"]
		def startScripts = project.tasks[TASK_START_SCRIPTS_NAME]

		distSpec.with {
			from(project.file("src/dist"))

			into("lib") { from(jar) }
			into("bin") {
				from(startScripts)
				fileMode = 0755
			}
		}

		distSpec
	}


}
