package com.merkle.oss.magnolia.dictionary.generator.json2properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

public class I18nPropertiesFileGenerator {
	private final Gson gson;
	private final PropertiesWriter propertiesWriter;

	public I18nPropertiesFileGenerator(final PropertiesWriter propertiesWriter) {
		this.propertiesWriter = propertiesWriter;
		gson = new GsonBuilder().create();
	}

	public void writePropertiesFile(final Path source, final Path destination, final Set<String> ignoreKeyPatterns) throws IOException {
		final Map<String, Object> translations = read(source);
		final Properties properties = convert(translations, ignoreKeyPatterns);
		propertiesWriter.write(source, destination, properties);
	}

	private Map<String, Object> read(final Path translationJson) throws IOException {
		try (final Reader reader = Files.newBufferedReader(translationJson, StandardCharsets.UTF_8)) {
			return gson.fromJson(reader, Map.class);
		} catch (IOException e) {
			throw new IOException("Could not read Json from file '" + translationJson + "'", e);
		}
	}

	private Properties convert(final Map<String, Object> translations, final Set<String> ignoreKeyPatterns) {
		final Properties properties = new Properties();
		streamTranslations(translations, ignoreKeyPatterns).forEach(entry ->
				properties.put(entry.getKey(), entry.getValue())
		);
		return properties;
	}

	private Stream<Map.Entry<String, String>> streamTranslations(final Map<String, Object> translations, final Set<String> ignoreKeyPatterns) {
		return translations.entrySet().stream()
				.filter(entry ->
						ignoreKeyPatterns.stream().noneMatch(entry.getKey()::matches)
				)
				.flatMap(entry ->
						Optional
								.ofNullable(entry.getValue())
								.filter(Map.class::isInstance)
								.map(value -> (Map<String, Object>) value)
								.map(valueMap ->
										streamTranslations(valueMap, ignoreKeyPatterns).map(e ->
												Map.entry(entry.getKey() + "." + e.getKey(), e.getValue())
										)
								)
								.orElseGet(() -> Stream.of(Map.entry(entry.getKey(), (String) entry.getValue())))
				);
	}
}
