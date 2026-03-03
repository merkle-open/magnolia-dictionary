package com.merkle.oss.magnolia.dictionary.services.importexport;

import info.magnolia.ui.AlertBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.jcr.RepositoryException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Streams;
import com.merkle.oss.magnolia.dictionary.i18nsystem.Label;
import com.merkle.oss.magnolia.dictionary.util.LocaleUtil;
import com.merkle.oss.magnolia.dictionary.util.SiteProvider;
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.PowerNodeService;
import com.vaadin.ui.Notification;

import jakarta.inject.Inject;

public class XlsImportService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Label.Persistor labelPersistor;
    private final LocaleUtil localeUtil;
    private final PowerNodeService powerNodeService;

    @Inject
    public XlsImportService(
			final Label.Persistor labelPersistor,
			final LocaleUtil localeUtil,
			final PowerNodeService powerNodeService
	) {
        this.labelPersistor = labelPersistor;
        this.localeUtil = localeUtil;
        this.powerNodeService = powerNodeService;
    }

    public void importXls(final String repository, final InputStream inputStream) throws IOException, RepositoryException {
		LOG.info("Start import to repository '{}'", repository);
        final Workbook workbook = new XSSFWorkbook(inputStream);
        final Sheet sheet = workbook.getSheetAt(0);
        final PowerNode dictionaryRootNode = powerNodeService.getRootNode(repository).orElseThrow(() ->
			new NullPointerException("dictionary root node not present!")
		);

        final Row headerRow = sheet.getRow(0);
        validateHeaderRow(headerRow);
		final List<Locale> locales = getLanguages(headerRow);

		StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(sheet.rowIterator(), Spliterator.ORDERED),false)
				.skip(1) //header row
				.map(row -> parseRow(locales, row))
				.filter(label -> isValid(label, dictionaryRootNode))
				.forEach(label ->
						labelPersistor.persist(dictionaryRootNode, label)
				);
		dictionaryRootNode.getSession().save();
		inputStream.close();
		LOG.info("Done importing to repository '{}'", repository);
	}

	private Label parseRow(final List<Locale> locales, final Row row) {
		return new Label(
				getValue(row, 0),
				"".equals(getValue(row, 1)) ? SiteProvider.GENERIC_SITE_NAME : getValue(row, 1),
				getValue(row, 2),
				Streams
						.mapWithIndex(locales.stream(), (locale, index) ->
								Map.entry(locale, getValue(row, XlsExportService.FIXED_CELLS.size() + (int)index))
						)
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
		);
	}

	private String getValue(final Row row, final int index) {
		return row.getCell(index).getStringCellValue();
	}

	private boolean isValid(final Label label, final PowerNode dictionaryRootNode) {
		if (!dictionaryRootNode.hasNode(label.key())) {
			LOG.info("Can't find label '{}', skipping this row", label);
			return false;
		}
		return true;
	}

	private void validateHeaderRow(final Row headerRow) {
		final List<String> actual = StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(headerRow.cellIterator(), Spliterator.ORDERED), false)
				.limit(XlsExportService.FIXED_CELLS.size())
				.map(Cell::getStringCellValue)
				.toList();

		if (!Objects.equals(XlsExportService.FIXED_CELLS, actual)) {
			final IllegalArgumentException exception = new IllegalArgumentException("The cells of the import file must start with " + XlsExportService.FIXED_CELLS + " followed by languages");
			AlertBuilder.alert("Invalid import format")
					.withLevel(Notification.Type.ERROR_MESSAGE)
					.withTitle("Invalid import format")
					.withBody(exception.getMessage())
					.withOkButtonCaption("Ok")
					.buildAndOpen();
			throw exception;
		}
	}

	private List<Locale> getLanguages(final Row headerRow) {
		return StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(headerRow.cellIterator(), Spliterator.ORDERED),false)
				.skip(XlsExportService.FIXED_CELLS.size())
				.map(Cell::getStringCellValue)
				.map(localeUtil::fromLocaleString)
				.toList();
	}
}
