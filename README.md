# Magnolia Dictionary
System        | Status
--------------|------------------------------------------------
CI master     | ![release](https://github.com/namics/magnolia-dictionary/workflows/release%20and%20deploy/badge.svg)
CI develop    | ![snapshot](https://github.com/namics/magnolia-dictionary/workflows/deploy%20snapshot/badge.svg)
Dependency    | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.namics.oss.magnolia/magnolia-dictionary/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.namics.oss.magnolia/magnolia-dictionary)


Magnolia dictionary app to manage i18n labels.

## Usage

### Maven Dependency (Latest Version in `pom.xml`):
```xml
<dependency>
    <groupId>com.namics.oss.magnolia</groupId>
    <artifactId>magnolia-dictionary</artifactId>
    <version>1.3.4</version>
</dependency>
```

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.namics.oss.magnolia/magnolia-dictionary/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.namics.oss.magnolia/magnolia-dictionary)

### Available Languages

Languages are read from the configured sites.

### Configuration load labels on startup
By default all labels are loaded on module start. This can be adjusted by the following config in the configuration workspace:
```
/modules/magnolia-dictionary/config@loadLabelsOnStartup=false
```