package com.merkle.oss.magnolia.dictionary.actions.importexport;

import com.merkle.oss.magnolia.dictionary.services.XlsExportService;
import com.merkle.oss.magnolia.dictionary.util.NodeUtil;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Notification;
import info.magnolia.ui.AlertBuilder;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.merkle.oss.magnolia.dictionary.DictionaryConfiguration.ImportExport.REP_ROOT;

public class XlsExportAction extends AbstractAction<XlsExportActionDefinition> {
	private List<Node> nodes;
	private XlsExportService exportService;

	public XlsExportAction(XlsExportActionDefinition definition, List<JcrItemAdapter> items, XlsExportService exportService) {
		super(definition);
		this.exportService = exportService;
		this.nodes = setNodes(items);
	}

	private List<Node> setNodes(List<JcrItemAdapter> items) {
		if (CollectionUtils.isEmpty(items)) {
			return Collections.emptyList();
		}

		if (items.size() > 1) {
			return itemsToNodes(items);
		}

		if (!isRootNode(items.get(0))) {
			return itemsToNodes(items);
		}

		return itemToNode(items.get(0))
				.map(node -> NodeUtil.getNodes(node, getDefinition().getNodeType()))
				.map(NodeUtil::asList)
				.orElse(Collections.emptyList());
	}

	@Override
	public void execute() throws ActionExecutionException {
		try (ByteArrayOutputStream outputStream = exportService.exportXls(nodes)) {
			StreamResource resource = exportService.getDownloadStream(outputStream);
			Page.getCurrent().open(resource, StringUtils.EMPTY, Boolean.TRUE);
		} catch (IOException | RepositoryException e) {
			showErrorMessage();
		}
	}

	private Optional<Node> itemToNode(JcrItemAdapter item) {
		Item jcrItem = item.getJcrItem();
		if (jcrItem.isNode()) {
			return Optional.of((Node) jcrItem);
		}
		return Optional.empty();
	}

	private List<Node> itemsToNodes(List<JcrItemAdapter> items) {
		return items.stream()
				.map(this::itemToNode)
				.flatMap(Optional::stream)
				.collect(Collectors.toList());
	}

	private boolean isRootNode(JcrItemAdapter item) {
		Item jcrItem = item.getJcrItem();
		if (jcrItem.isNode()) {
			try {
				return NodeUtil.isNodeType((Node) jcrItem, REP_ROOT);
			} catch (RepositoryException e) {
				return false;
			}
		}
		return false;
	}

	private void showErrorMessage() {
		AlertBuilder
				.alert("Error creating xls file for download.")
				.withLevel(Notification.Type.ERROR_MESSAGE)
				.withTitle("Error creating xls file for download.")
				.withBody("Could not create xls file.")
				.buildAndOpen();
	}
}
