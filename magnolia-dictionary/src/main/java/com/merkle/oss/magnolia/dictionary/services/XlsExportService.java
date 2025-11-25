package com.merkle.oss.magnolia.dictionary.services;

import info.magnolia.jcr.util.PropertyUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration.ImportExport;
import com.merkle.oss.magnolia.dictionary.util.LocaleUtils;

import jakarta.inject.Inject;

public class XlsExportService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final LocaleUtils localeUtils;

    @Inject
    public XlsExportService(final LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    public ByteArrayOutputStream exportXls(final Collection<Node> nodes) throws IOException, RepositoryException {
		final List<String> exportProperties = getExportProperties();
        final XSSFWorkbook workbook = new XSSFWorkbook();
        final XSSFSheet sheet = workbook.createSheet(ImportExport.SHEET_NAME);
        final CellStyle dateCellStyle = getDateCellStyle(workbook);

		LOG.info("Start node export for '{}' nodes with properties '{}'", CollectionUtils.size(nodes), exportProperties);

		int rowNum = 0;
		int headColNum = 0;

        final Row headRow = sheet.createRow(rowNum++);

		for (String property : exportProperties) {
			Cell cell = headRow.createCell(headColNum++);
			cell.setCellValue(property);
		}

		for (Node node : nodes) {
			LOG.debug("Exporting node '{}'", node.getName());
			Row row = sheet.createRow(rowNum++);
			int colNum = 0;
			for (String property : exportProperties) {
				Cell cell = row.createCell(colNum++);
				setCellValue(node, cell, property, dateCellStyle);
			}
		}

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		return outputStream;
	}

	public CellStyle getDateCellStyle(final XSSFWorkbook workbook) {
        final CellStyle dateCellStyle = workbook.createCellStyle();
        final CreationHelper createHelper = workbook.getCreationHelper();
		dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(ImportExport.DATE_FORMAT_PATTERN));
		return dateCellStyle;
	}

	private void setCellValue(final Node node, final Cell cell, final String property, final CellStyle dateCellStyle) throws RepositoryException {
		if (ImportExport.JCR_NAME.equals(property)) {
			cell.setCellValue(node.getName());
			return;
		}

		if (ImportExport.DATE_EXPORT_PROPERTIES.contains(property)) {
			cell.setCellStyle(dateCellStyle);
			Optional.ofNullable(PropertyUtil.getDate(node, property))
					.ifPresentOrElse(
							cell::setCellValue,
							() -> cell.setCellValue(StringUtils.EMPTY)
					);
			return;
		}

		cell.setCellValue(PropertyUtil.getString(node, property, StringUtils.EMPTY));
	}

	private List<String> getExportProperties() {
		return Stream.of(
                ImportExport.STATIC_EXPORT_PROPERTIES,
                getAllLanguages(),
                ImportExport.DATE_EXPORT_PROPERTIES,
                ImportExport.STATUS_EXPORT_PROPERTIES
        ).flatMap(Collection::stream).collect(Collectors.toList());
	}

	private List<String> getAllLanguages() {
		return localeUtils.streamLocalesOfAllSites()
				.map(localeUtils::getLocaleString)
				.collect(Collectors.toList());
	}
}
