package com.namics.oss.magnolia.dictionary.generator.json2properties;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class PropertiesWriter {

	public void write(final Path source, final Path destination, final Properties properties) throws IOException {
		Files.createDirectories(destination.getParent());
		try (final Writer writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
			properties.store(writer, "Automatically generated from "+source.toString()+"!");
		} catch (IOException e) {
			throw new IOException("Could not write tranlation properties to Json file '" + destination + "'", e);
		}
	}
}
