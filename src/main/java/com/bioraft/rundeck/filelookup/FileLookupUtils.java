/*
 * Copyright 2019 BioRAFT, Inc. (https://bioraft.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bioraft.rundeck.filelookup;

import com.dtolabs.rundeck.core.Constants;
import com.dtolabs.rundeck.core.dispatcher.ContextView;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class FileLookupUtils {

	private FileLookupUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static void addOutput(PluginStepContext context, String group, String name, String value, boolean elevate) {
		context.getOutputContext().addOutput(group, name, value);
		if (elevate) {
			String groupName = group + "." + name;
			context.getOutputContext().addOutput(ContextView.global(), "export", groupName, value);
			context.getLogger().log(Constants.DEBUG_LEVEL, "Elevating to global ${export." + groupName + "}.");
		}
	}

	/**
	 * Performs the tree search in a recursive depth-first manner.
	 *
	 * @param node The node tree to search.
	 * @return The textual form of the first matched field, or null if not matched.
	 */
	static String searchTree(JsonNode node, String fieldName) {
		List<JsonNode> values = node.findValues(fieldName);
		for (JsonNode value : values) {
			if (value.isValueNode()) {
				return value.asText();
			} else {
				String subTreeSearch = searchTree(value, fieldName);
				if (subTreeSearch != null) {
					return subTreeSearch;
				}
			}
		}
		return null;
	}

}