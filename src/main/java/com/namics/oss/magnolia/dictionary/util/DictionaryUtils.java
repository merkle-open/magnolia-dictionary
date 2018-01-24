package com.namics.oss.magnolia.dictionary.util;

import com.namics.oss.magnolia.dictionary.DictionaryConfiguration;
import com.namics.oss.magnolia.dictionary.i18nsystem.DictionaryMessageBundlesInstaller;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author mrauch, Namics AG
 * @since 26.04.2016
 */
public class DictionaryUtils {
	private static final Logger LOG = LoggerFactory.getLogger(DictionaryUtils.class);

	public static List<String> getAllMessageNames() {
		try {
			Node root = NodeUtil.getNodeByPathOrNull(DictionaryConfiguration.REPOSITORY, "/");
			List<Node> nodes = NodeUtil.asList(NodeUtil.getNodes(root));

			return nodes.stream()
					.map(node -> PropertyUtil.getString(node, "name"))
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		} catch (RepositoryException e) {
			return new ArrayList<>();
		}
	}

	public static String getValidMessageNodeName(String messageNodeName) {
		return NodeUtil.createValidNodeName(messageNodeName);
	}

	public static Optional<Long> getLastLoadedTime() throws RepositoryException {
		Node rootNode = NodeUtil.getWorkspaceRootNode(DictionaryConfiguration.REPOSITORY);
		Node lastLoadedTimeNode = NodeUtil.getNode(rootNode, DictionaryMessageBundlesInstaller.LAST_LOADED_TIME);
		if (lastLoadedTimeNode != null) {
			Long lastLoadedTime = PropertyUtil.getLong(lastLoadedTimeNode, NodeTypes.LastModified.LAST_MODIFIED);
			if (lastLoadedTime != null) {
				return Optional.of(lastLoadedTime);
			} else {
				LOG.error("last modified time not set on node {}. this should never occur.", lastLoadedTimeNode);
			}
		} else {
			LOG.warn("no node found lastLoadedTime. check if it has been set correctly during loading of labels");
		}
		return Optional.empty();
	}
}
