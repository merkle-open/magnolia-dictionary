package com.merkle.oss.magnolia.dictionary.services.importexport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration.ImportExport;
import com.merkle.oss.magnolia.dictionary.i18nsystem.Label;
import com.merkle.oss.magnolia.dictionary.util.LocaleUtil;
import com.merkle.oss.magnolia.dictionary.util.SiteProvider;
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.predicate.IsPrimaryNodeType;

import jakarta.inject.Inject;

public class XlsExportService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	static final List<String> FIXED_CELLS = List.of(
			DictionaryConfiguration.Prop.NAME,
			DictionaryConfiguration.Prop.SITE,
			DictionaryConfiguration.Prop.VALUE
	);
    private final Label.Factory labelFactory;
    private final SiteProvider siteProvider;
    private final LocaleUtil localeUtil;

    @Inject
    public XlsExportService(
			final Label.Factory labelFactory,
			final SiteProvider siteProvider,
			final LocaleUtil localeUtil
	) {
        this.labelFactory = labelFactory;
        this.siteProvider = siteProvider;
        this.localeUtil = localeUtil;
    }

    public ByteArrayOutputStream exportXls(final Collection<PowerNode> nodes) throws IOException {
		LOG.info("Start exporting labels...");
		final XSSFWorkbook workbook = new XSSFWorkbook();
        final XSSFSheet sheet = workbook.createSheet(ImportExport.SHEET_NAME);

		final List<Locale> locales = getAllLanguages();

		createHeaderRow(locales, sheet);
		nodes.stream()
				.flatMap(labelNode -> Stream.concat(
						Stream.of(labelNode),
						labelNode.streamChildren(new IsPrimaryNodeType<>(DictionaryConfiguration.SITE_SPECIFIC_LABEL_NODE_TYPE))
				))
				.map(labelFactory::create)
				.flatMap(Optional::stream)
				.forEach(label ->
						createRow(locales, label, sheet)
				);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		LOG.info("Done exporting labels");
		return outputStream;
	}

	private void createHeaderRow(final List<Locale> locales, final XSSFSheet sheet) {
		final Row row = sheet.createRow(sheet.getLastRowNum() + 1);
		FIXED_CELLS.forEach(fixedCell ->
				addCell(row, fixedCell)
		);
		locales.forEach(locale ->
				addCell(row, localeUtil.toLocaleString(locale))
		);
	}

	private void createRow(final List<Locale> locales, final Label label, final XSSFSheet sheet) {
		LOG.debug("Exporting label '{}'", label);
		final Row row = sheet.createRow(sheet.getLastRowNum() + 1);
		addCell(row, label.key());
		addCell(row, SiteProvider.GENERIC_SITE_NAME.equals(label.siteName()) ? "" : label.siteName());
		addCell(row, label.defaultValue());
		locales.forEach(locale ->
				addCell(row, label.values().getOrDefault(locale, ""))
		);
	}

	private void addCell(final Row row, final String value) {
		final Cell cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
		cell.setCellValue(value);
	}

	private List<Locale> getAllLanguages() {
		return siteProvider.getGenericSite().getI18n().getLocales().stream().sorted(Comparator.comparing(Locale::toLanguageTag)).collect(Collectors.toList());
	}
}
