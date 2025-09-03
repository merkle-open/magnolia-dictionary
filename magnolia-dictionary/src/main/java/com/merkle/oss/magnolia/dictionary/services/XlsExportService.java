package com.merkle.oss.magnolia.dictionary.services;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration.ImportExport;
import com.merkle.oss.magnolia.dictionary.util.LocaleUtils;
import com.vaadin.server.StreamResource;
import info.magnolia.jcr.util.PropertyUtil;
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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.merkle.oss.magnolia.dictionary.DictionaryConfiguration.ImportExport.FILENAME_TEMPLATE;

public class XlsExportService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ByteArrayOutputStream exportXls(List<Node> nodes) throws IOException, RepositoryException {
		List<String> exportProperties = getExportProperties();
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet(ImportExport.SHEET_NAME);
		CellStyle dateCellStyle = getDateCellStyle(workbook);

		LOG.info("Start node export for '{}' nodes with properties '{}'", CollectionUtils.size(nodes), exportProperties);

		int rowNum = 0;
		int headColNum = 0;

		Row headRow = sheet.createRow(rowNum++);

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

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);

		return outputStream;
	}

	public CellStyle getDateCellStyle(XSSFWorkbook workbook) {
		CellStyle dateCellStyle = workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(ImportExport.DATE_FORMAT_PATTERN));
		return dateCellStyle;
	}

	private void setCellValue(Node node, Cell cell, String property, CellStyle dateCellStyle) throws RepositoryException {
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

	public StreamResource getDownloadStream(ByteArrayOutputStream outputStream) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(ImportExport.DATE_FORMAT_PATTERN);
		StreamResource.StreamSource source = (StreamResource.StreamSource) () -> new ByteArrayInputStream(outputStream.toByteArray());
		String filename = MessageFormat.format(FILENAME_TEMPLATE, DictionaryConfiguration.REPOSITORY, dateFormat.format(new Date()));
		StreamResource resource = new StreamResource(source, filename);
		resource.setCacheTime(-1);
		resource.getStream().setParameter("Content-Disposition", "attachment; filename=" + filename);
		return resource;
	}

	private List<String> getExportProperties() {
		List<String> props = new LinkedList<>();
		props.addAll(ImportExport.STATIC_EXPORT_PROPERTIES);
		props.addAll(getAllLanguages());
		props.addAll(ImportExport.DATE_EXPORT_PROPERTIES);
		props.addAll(ImportExport.STATUS_EXPORT_PROPERTIES);
		return props;
	}

	private List<String> getAllLanguages() {
		return LocaleUtils.getLocalesOfAllSiteDefinitions().stream()
				.map(LocaleUtils::getLocaleString)
				.collect(Collectors.toList());
	}
}
