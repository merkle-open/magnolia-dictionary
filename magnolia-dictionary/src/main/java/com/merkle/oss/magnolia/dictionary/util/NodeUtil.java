package com.merkle.oss.magnolia.dictionary.util;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.nodebuilder.AbstractNodeOperation;
import info.magnolia.jcr.nodebuilder.ErrorHandler;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.util.PropertyUtil;

import java.util.List;
import java.util.Optional;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nullable;

/**
 * @author haug, Namics AG
 * @deprecated
 */
@Deprecated
//TODO replace
public class NodeUtil extends info.magnolia.jcr.util.NodeUtil {
	private static final Logger LOG = LoggerFactory.getLogger(NodeUtil.class);

	public static Node getWorkspaceRootNode(String workspaceName) throws RepositoryException {
		return MgnlContext.getJCRSession(workspaceName).getRootNode();
	}

	public static Optional<Node> getNode(Node node, String relPath) {
		try {
			if (node.hasNode(relPath)) {
				return Optional.of(node.getNode(relPath));
			}
		} catch (RepositoryException e) {}
		return Optional.empty();
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

	private static Node overwriteOrCreateNode(Node parentNode, String nodeName, String nodeTypeIfMissing, String nodeTypeForced) throws RepositoryException {
		if (parentNode == null) {
			throw new NullPointerException("parentNode cannot be null");
		}

		String newNodeType;

		@Nullable
		Node nodeToOverwrite = NodeUtil.getNode(parentNode, nodeName).orElse(null);
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

	public static boolean hasNode(Node node, String childName) {
		try {
			return node.hasNode(childName);
		} catch (RepositoryException e) {
			return false;
		}
	}
}
