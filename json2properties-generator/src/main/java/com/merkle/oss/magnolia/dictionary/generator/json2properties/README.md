# Handlebars reference generator
This maven plugin generates a properties from a json file.

example:
```json
{
	"web": {
		"header": {
			"menu": {
				"buttonText": "Menu"
			},
			"overlay": {
				"closeButtonText": "Close",
				"backButtonText": "Back"
			}
		},
		"video": {
			"playButtonText": "Play Video"
		}
	},
	"test.key": {
		"shouldNotBeInPropertiesFile": "yes, should not be in properties file"
	}
}
```
```properties
#Automatically generated from .../namics-mgnl-blitzdings/theme/src/main/resources/frontend/project/locales/default/translation.json!
#Thu Jul 06 08:34:07 CEST 2023
"web.header.menu.buttonText", "Menu",
"web.header.overlay.closeButtonText", "Close",
"web.header.overlay.backButtonText", "Back",
"web.video.playButtonText", "Play Video"
```

## Configuration 
```xml
<build>
	<plugins>
		<plugin>
			<groupId>com.merkle.oss.magnolia</groupId>
			<artifactId>json2properties-generator</artifactId>
			<version>${merkle.oss.dictionary.version}</version>
			<configuration>
				<source>${basedir}/src/main/resources/frontend/project/locales/default/translation.json</source>
				<destination>${project.build.outputDirectory}/blitzdings-web-theme/i18n/fe-generated-dictionary-messages.properties</destination>
				<ignoreKeyPrefixes>
					<ignoreKeyPrefix>test.</ignoreKeyPrefix>
				</ignoreKeyPrefixes>
			</configuration>
			<executions>
				<execution>
					<goals>
						<goal>json2properties-generator</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```

### Parameters

| Key                | Type         | Mandatory | Description                                                                                                                                           
|--------------------|--------------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------
| source             | String       | x         | Source json file                                                                                                                 
| destination        | String       | x         | Destination for generated properties file                  
| ignoreKeyPrefixes  | Set<String>	| 	        | json key prefixes, that should be ignored          

### Trigger generation
```shell
mvn generate-resources
```