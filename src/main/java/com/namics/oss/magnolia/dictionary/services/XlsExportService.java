package com.namics.oss.magnolia.dictionary.services;

import com.namics.oss.magnolia.dictionary.util.NodeUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class XlsExportService {
	private static final Logger LOG = LoggerFactory.getLogger(XlsExportService.class);

	public ByteArrayOutputStream exportXls(List<Node> nodes, Collection<String> properties) throws IOException, RepositoryException {
		LOG.debug("Start node export for {} nodes with properties {}", CollectionUtils.size(nodes), properties);
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("export");

		int rowNum = 0;

		Row headRow = sheet.createRow(rowNum++);
		int headColNum = 0;
		for (String property : properties) {
			Cell cell = headRow.createCell(headColNum++);
			cell.setCellValue(property);
		}

		for (Node node : nodes) {
			LOG.trace("Exporting node {}", node.getName());
			Row row = sheet.createRow(rowNum++);
			int colNum = 0;
			for (String property : properties) {
				Cell cell = row.createCell(colNum++);
				if ("jcrName".equals(property)) {
					cell.setCellValue(node.getName());
				} else if ("mgnl:created".equals(property) || "mgnl:lastModified".equals(property) || "mgnl:lastActivated".equals(property)) {
					Calendar date = PropertyUtil.getDate(node, property);
					if(date != null) {
						cell.setCellValue(date);
					} else {
						cell.setCellValue(StringUtils.EMPTY);
					}
				} else {
					cell.setCellValue(PropertyUtil.getString(node, property, StringUtils.EMPTY));
				}
			}
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		//workbook.close();

		return outputStream;
	}

	public void importXls(String repository, String path, boolean createNodes, InputStream inputStream) throws IOException, RepositoryException {
		LOG.debug("Star import to repository {} on path {} creating not existion nodes {}", repository, path, createNodes);
		Workbook workbook = new XSSFWorkbook(inputStream);
		Sheet firstSheet = workbook.getSheetAt(0);

		Node rootNode = MgnlContext.getJCRSession(repository).getRootNode();
		Node startNode = "/".equals(path) ? rootNode : rootNode.getNode(path);

		if (firstSheet.iterator().hasNext()) {
			Row firstRow = firstSheet.iterator().next();
			List<String> properties = new ArrayList<>();
			Iterator<Cell> firstRowCellIterator = firstRow.cellIterator();
			while (firstRowCellIterator.hasNext()) {
				Cell cell = firstRowCellIterator.next();
				properties.add(cell.getStringCellValue());
			}

			LOG.debug("Properties to import {}", properties);

			if (CollectionUtils.isEmpty(properties) || !StringUtils.equals(properties.get(0), "jcrName")) {
				throw new IllegalArgumentException("Wrong format in input file");
			}

			for (Row nextRow : firstSheet) {
				int colNumber = 1;
				Cell jcrNameCell = nextRow.getCell(0);

				if (!createNodes && !startNode.hasNode(jcrNameCell.getStringCellValue())) {
					LOG.debug("Skip importing node {} due tu createNodes is false", jcrNameCell.getStringCellValue());
					continue;
				}

				Node node = NodeUtil.getOrCreateNode(startNode, jcrNameCell.getStringCellValue());
				LOG.debug("Start importing on node {}", node.getPath());

				while (colNumber < properties.size()) {

					Object value = null;
					String property = properties.get(colNumber);
					Cell cell = nextRow.getCell(colNumber);
					if (cell != null) {
						switch (cell.getCellType()) {
							case Cell.CELL_TYPE_STRING:
								value = StringUtils.defaultIfEmpty(cell.getStringCellValue(), null);
								break;
							case Cell.CELL_TYPE_BOOLEAN:
								value = cell.getBooleanCellValue();
								break;
							case Cell.CELL_TYPE_NUMERIC:
								value = cell.getNumericCellValue();
								break;
							case Cell.CELL_TYPE_BLANK:
								value = null;
								break;
						}
					}

					LOG.trace("Set value {} on property {}");
					PropertyUtil.setProperty(node, property, value);

					colNumber++;
				}
			}

			startNode.getSession().save();
		}

		//workbook.close();
		inputStream.close();
	}
}
