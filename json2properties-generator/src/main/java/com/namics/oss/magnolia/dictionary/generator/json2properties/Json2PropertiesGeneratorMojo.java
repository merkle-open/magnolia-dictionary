package com.namics.oss.magnolia.dictionary.generator.json2properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.nio.file.Path;
import java.util.Set;


@Mojo(name = "json2properties-generator", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public class Json2PropertiesGeneratorMojo extends AbstractMojo {
	@Parameter(property = "source", required = true)
	private String source;
	@Parameter(property = "destination", required = true)
	private String destination;
	@Parameter(property = "ignoreKeyPrefixes")
	private Set<String> ignoreKeyPrefixes;

	@Override
	public void execute() throws MojoExecutionException {
		try {
			new I18nPropertiesFileGenerator(new PropertiesWriter()).writePropertiesFile(
					Path.of(source),
					Path.of(destination),
					ignoreKeyPrefixes
			);
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to generate dictionary properties from json", e);
		}
	}
}
