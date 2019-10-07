package com.namics.oss.magnolia.dictionary.actions;

import com.namics.oss.magnolia.dictionary.services.XlsExportService;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang.StringUtils;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class XlsExportAction extends AbstractAction<XlsExportActionDefinition> {
	List<JcrItemAdapter> items;
	List<Node> nodes;
	UiContext uiContext;
	XlsExportService exportService;
	String workspace = StringUtils.EMPTY;
	SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

	public XlsExportAction(XlsExportActionDefinition definition, JcrItemAdapter item, UiContext uiContext, XlsExportService exportService) {
		this(definition, Collections.singletonList(item), uiContext, exportService);
	}

	public XlsExportAction(XlsExportActionDefinition definition, List<JcrItemAdapter> items, UiContext uiContext, XlsExportService exportService) {
		super(definition);
		this.items = items;
		this.uiContext = uiContext;
		this.exportService = exportService;
		setNodes(items);
		setWorkspace(items);
	}

	private void setWorkspace(List<JcrItemAdapter> items) {
		if (CollectionUtils.isNotEmpty(items)) {
			this.workspace = items.get(0).getWorkspace();
		}
	}

	private void setNodes(List<JcrItemAdapter> items) {
		if (CollectionUtils.isNotEmpty(items)) {
			if (items.size() > 1) {
				this.nodes = itemsToNodes(items);
			} else {
				if (isRootNode(items.get(0))) {
					itemToNode(items.get(0)).ifPresent(node -> {
						try {
							this.nodes = IteratorUtils.toList(NodeUtil.getNodes(node, getDefinition().getNodeType()).iterator());
						} catch (RepositoryException e) {
							uiContext.openNotification(MessageStyleTypeEnum.ERROR, true, "Error reading nodes.");
						}
					});
				} else {
					this.nodes = itemsToNodes(items);
				}
			}
		} else {
			this.nodes = Collections.emptyList();
		}
	}

	@Override
	public void execute() throws ActionExecutionException {
		try (ByteArrayOutputStream outputStream = exportService.exportXls(nodes, getDefinition().getProperties())) {
			StreamResource.StreamSource source = (StreamResource.StreamSource) () -> new ByteArrayInputStream(outputStream.toByteArray());

			String filename = "export-" + this.workspace + "-" + df.format(new Date()) + ".xlsx";
			StreamResource resource = new StreamResource(source, filename);
			resource.setCacheTime(-1);
			resource.getStream().setParameter("Content-Disposition", "attachment; filename=" + filename + "\"");
			Page.getCurrent().open(resource, "", true);
		} catch (IOException | RepositoryException e) {
			uiContext.openNotification(MessageStyleTypeEnum.ERROR, true, "Error creating xls File for download.");
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
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	private boolean isRootNode(JcrItemAdapter item) {
		Item jcrItem = item.getJcrItem();
		if (jcrItem.isNode()) {
			try {
				return ((Node) jcrItem).getPrimaryNodeType().isNodeType("rep:root");
			} catch (RepositoryException e) {
				return false;
			}
		}

		return false;
	}
}
