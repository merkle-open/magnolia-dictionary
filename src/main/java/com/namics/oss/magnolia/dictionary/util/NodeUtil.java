package com.namics.oss.magnolia.dictionary.util;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.nodebuilder.AbstractNodeOperation;
import info.magnolia.jcr.nodebuilder.ErrorHandler;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.List;

/**
 * @author haug, Namics AG
 */
public class NodeUtil extends info.magnolia.jcr.util.NodeUtil {
	private static final Logger LOG = LoggerFactory.getLogger(NodeUtil.class);

	public static Node getWorkspaceRootNode(String workspaceName) throws RepositoryException {
		return MgnlContext.getJCRSession(workspaceName).getRootNode();
	}

	public static Node getNode(Node node, String relPath) {
		Node childNode = null;

		try {
			if (node.hasNode(relPath)) {
				childNode = node.getNode(relPath);
			}
		} catch (RepositoryException e) {
			LOG.debug("Could not get node", e);
		}

		return childNode;
	}

	public static Iterable<Node> getNodes(Node node, String nodeTypeName) {
		try {
			return info.magnolia.jcr.util.NodeUtil.getNodes(node, nodeTypeName);
		} catch (RepositoryException e) {
			return IterableUtils.emptyIterable();
		}
	}

	public static Node getNodeByPathOrNull(String workspace, String path) {
		Node node = null;

		try {
			node = MgnlContext.getJCRSession(workspace).getNode(path);
		} catch (RepositoryException e) {
			node = null;
		}

		return node;
	}

	public static String replaceIllegalJcrChars(String nodeName, char replacement) {
		return StringUtils.replaceChars(nodeName, "%/:[]*'\"|\t\r\n", String.valueOf(replacement));
	}

	public static String replaceIllegalJcrChars(String nodeName) {
		return replaceIllegalJcrChars(nodeName, '_');
	}

	public static String createValidNodeName(String nodeName) {
		return replaceIllegalJcrChars(nodeName);
	}

	public static List<Node> asList(NodeIterator iterator) {
		return asList(asIterable(iterator));
	}

	public static Node getOrCreateNode(Node node, String relPath) {
		return getOrCreateNode(node, relPath, NodeTypes.Content.NAME);
	}

	public static Node getOrCreateNode(Node node, String relPath, String primaryNodeTypeName) {
		Node childNode = null;
		try {
			if (node.hasNode(relPath)) {
				childNode = node.getNode(relPath);
			} else {
				childNode = node.addNode(relPath, primaryNodeTypeName);
			}
		} catch (RepositoryException e) {
			LOG.debug("Could not get or create node", e);
		}

		return childNode;
	}

	public static Node overwriteOrCreateNode(Node parentNode, String nodeName, String nodeTypeIfMissing) throws RepositoryException {
		return overwriteOrCreateNode(parentNode, nodeName, nodeTypeIfMissing, null);
	}

	public static Node overwriteOrCreateNode(Node parentNode, String nodeName, String nodeTypeIfMissing, String nodeTypeForced) throws RepositoryException {
		if (parentNode == null) {
			throw new NullPointerException("parentNode cannot be null");
		}

		String newNodeType;

		Node nodeToOverwrite = NodeUtil.getNode(parentNode, nodeName);
		Node siblingAfter = null;

		if (nodeToOverwrite != null) { // node exists. use original node type
			newNodeType = nodeToOverwrite.getPrimaryNodeType().getName();
			siblingAfter = getSiblingAfter(nodeToOverwrite);
			nodeToOverwrite.remove();
		} else if (nodeTypeIfMissing != null) { // use missing node type if provided
			newNodeType = nodeTypeIfMissing;
		} else { // use parent node type as fallback
			String parentNodeType = parentNode.getPrimaryNodeType().getName();
			newNodeType = parentNodeType;
		}

		// use forced node type if provided
		if (nodeTypeForced != null) {
			newNodeType = nodeTypeForced;
		}

		Node newNode = parentNode.addNode(nodeName, newNodeType);

		if (siblingAfter != null) {
			orderBefore(newNode, siblingAfter.getName());
		} else {
			// the node is already at the bottom which is correct
		}

		return newNode;
	}

	public static NodeOperation setOrAddProperty(final String name, final Object newValue) {
		return new AbstractNodeOperation() {
			@Override
			protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
				Object value = newValue;
				if (value instanceof Integer) {
					// prevent java.lang.IllegalArgumentException for integer values
					value = Long.valueOf((Integer) value);
				}

				StringUtils.trim(name); // prevent "trailing spaces not allowed exception

				PropertyUtil.setProperty(context, name, value);
				return context;
			}
		};
	}

	public static boolean hasNode(Node node, String childName) {
		try {
			return node.hasNode(childName);
		} catch (RepositoryException e) {
			return false;
		}
	}
}
