package com.merkle.oss.magnolia.dictionary.generator.json2properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class I18nPropertiesFileGeneratorTest {
	private I18nPropertiesFileGenerator i18nPropertiesFileGenerator;
	private PropertiesWriter propertiesWriterMock;

	@BeforeEach
	void setUp() {
		propertiesWriterMock = Mockito.mock(PropertiesWriter.class);
		i18nPropertiesFileGenerator = new I18nPropertiesFileGenerator(propertiesWriterMock);
	}

	@Test
	void writePropertiesFile() throws IOException, URISyntaxException {
		final Path destination = Path.of("output.properties");
		i18nPropertiesFileGenerator.writePropertiesFile(
				Path.of(getClass().getClassLoader().getResource("test-translation.json").toURI()),
				destination,
				Set.of("test\\..*")
		);
		final ArgumentCaptor<Path> outputCaptor = ArgumentCaptor.forClass(Path.class);
		final ArgumentCaptor<Properties> propertiesCaptor = ArgumentCaptor.forClass(Properties.class);
		Mockito.verify(propertiesWriterMock).write(Mockito.any(), outputCaptor.capture(), propertiesCaptor.capture());

		final Properties expectedProperties = new Properties();
		expectedProperties.putAll(Map.of(
				"web.header.menu.buttonText", "Menu",
				"web.header.overlay.closeButtonText", "Close",
				"web.header.overlay.backButtonText", "Back",
				"web.video.playButtonText", "Play Video"
		));
		assertEquals(destination, outputCaptor.getValue());
		assertEquals(expectedProperties, propertiesCaptor.getValue());
	}
}