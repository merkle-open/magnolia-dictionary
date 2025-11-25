package com.merkle.oss.magnolia.dictionary.services;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration.ImportExport;
import com.merkle.oss.magnolia.dictionary.util.LocaleUtils;
import com.merkle.oss.magnolia.dictionary.util.NodeUtil;
import com.vaadin.ui.Notification;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.AlertBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class XlsImportService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final LocaleUtils localeUtils;

    @Inject
    public XlsImportService(final LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    public void importXls(final String repository, final InputStream inputStream) throws IOException, RepositoryException {
		LOG.info("Star import to repository '{}'", repository);
        final Workbook workbook = new XSSFWorkbook(inputStream);
        final Sheet firstSheet = workbook.getSheetAt(0);

        final Node startNode = MgnlContext.getJCRSession(repository).getRootNode();

        final Row headerRow = firstSheet.getRow(0);
        final List<String> importProps = validateHeaderRow(headerRow);

		firstSheet.forEach(row -> {
			if (shouldSkipRow(row, startNode)) {
				return;
			}

            final Cell jcrNameCell = row.getCell(0);
            final Node node = NodeUtil.getNode(startNode, jcrNameCell.getStringCellValue());
			LOG.debug("Import values for node '{}'", NodeUtil.getPathIfPossible(node));

			int colNumber = 1;
			while (colNumber < importProps.size()) {
                final String property = importProps.get(colNumber);
                final Cell cell = row.getCell(colNumber);
                final Object value = getCellValue(cell);
				setProperty(node, property, value);
				colNumber++;
			}
		});

		startNode.getSession().save();
		inputStream.close();
	}

	private boolean shouldSkipRow(final Row row, final Node startNode) {
		if (row.getRowNum() == 0) {
			//skip header row;
			return true;
		}
        final Cell jcrNameCell = row.getCell(0);
		if (jcrNameCell == null) {
			// skip rows where first cell is empty
			return true;
		}
		if (!NodeUtil.hasNode(startNode, jcrNameCell.getStringCellValue())) {
			LOG.info("Can't find label node with name '{}', skipping this row", jcrNameCell.getStringCellValue());
			return true;
		}
		return false;
	}

	@Nullable
	public Object getCellValue(final Cell cell) {
		if (cell == null) {
			return null;
		}
        return switch (cell.getCellType()) {
            case STRING -> StringUtils.defaultIfEmpty(cell.getStringCellValue(), null);
            case BOOLEAN -> cell.getBooleanCellValue();
            case NUMERIC -> cell.getNumericCellValue();
            default -> null;
        };
	}

	private void setProperty(final Node node, final String property, final Object value) {
		try {
			LOG.debug("Set value {} on property {}", value, property);
			PropertyUtil.setProperty(node, property, value);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	private List<String> validateHeaderRow(final Row row) {
        final List<String> importProps = new LinkedList<>();
        final List<String> validateProps = new LinkedList<>(ImportExport.STATIC_IMPORT_PROPERTIES);
		validateProps.addAll(getAllLanguages());

		row.forEach(cell -> {
			String cellTitle = cell.getStringCellValue();
			if (validateProps.contains(cellTitle)) {
				importProps.add(cellTitle);
			}
		});

        final boolean startsWithJcrName = importProps.stream()
				.findFirst()
				.map(prop -> StringUtils.equals(prop, ImportExport.JCR_NAME))
				.orElse(false);

		if (!CollectionUtils.isEqualCollection(importProps, validateProps) || !startsWithJcrName) {
			AlertBuilder.alert("Invalid import format")
					.withLevel(Notification.Type.ERROR_MESSAGE)
					.withTitle("Invalid import format")
					.withBody("The header of the import file must start with 'jcrName' and should contain the following properties: " + validateProps)
					.withOkButtonCaption("Ok")
					.buildAndOpen();
			throw new IllegalArgumentException("Invalid import format");
		}

		LOG.debug("Properties to import '{}'", importProps);
		return importProps;
	}

	private List<String> getAllLanguages() {
		return localeUtils.streamLocalesOfAllSites()
				.map(localeUtils::getLocaleString)
				.collect(Collectors.toList());
	}
}
