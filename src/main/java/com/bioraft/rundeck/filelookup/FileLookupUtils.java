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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dtolabs.rundeck.core.Constants.DEBUG_LEVEL;
import static com.dtolabs.rundeck.core.Constants.ERR_LEVEL;

public class FileLookupUtils {

	PluginStepContext pluginStepContext;

	public FileLookupUtils(PluginStepContext context) {
		this.pluginStepContext = context;
	}

	void scanJsonFile(String path, String fieldName, String group, String name, boolean elevateToGlobal)
			throws IOException {

		FileReader reader;
		try {
			reader = new FileReader(path);
		} catch (FileNotFoundException e) {
			String message = "Could not find file '" + path + "'";
			pluginStepContext.getLogger().log(ERR_LEVEL, message);
			throw(e);
		}
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode;
		try {
			rootNode = objectMapper.readTree(reader);
		} catch (IOException e) {
			String message = "Could parse JSON file '" + path + "'";
			pluginStepContext.getLogger().log(ERR_LEVEL, message);
			throw(e);
		}
		String value = searchTree(rootNode, fieldName);
		if (value != null) {
			addFieldToOutput(group, name, value, elevateToGlobal);
		}
	}


	void scanPropertiesFile(String path, String group, String name, String regex, boolean elevateToGlobal)
			throws IOException {

		Pattern pattern = Pattern.compile(regex);

		Map<String, String> map = new HashMap<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			// Scan lines for a match.
			// Optimize by returning immediately when there is only one capture field.
			do {
				String line;
				try {
					line = reader.readLine();
				} catch (IOException e) {
					pluginStepContext.getLogger().log(ERR_LEVEL, "Could read file '" + path + "'");
					throw(e);
				}
				if (line == null) {
					return;
				}
				Matcher match = pattern.matcher(line);
				if (match.find()) {
					pluginStepContext.getLogger().log(DEBUG_LEVEL, "Matched " + line);
					if (match.groupCount() == 1) {
						addFieldToOutput(group, name, match.group(1), elevateToGlobal);
						return;
					} else if (match.groupCount() == 2) {
						pluginStepContext.getLogger().log(DEBUG_LEVEL, "Found '" + match.group(1) + "' : '" + match.group(2) + "'");
						// Take first value and do not overwrite, even though scanning proceeds
						// through the rest of the file to find other matches to the pattern.
						if (!map.containsKey(match.group(1))) {
							addFieldToOutput(group, match.group(1), match.group(2), elevateToGlobal);
							map.put(match.group(1), match.group(2));
						}
					}
				}
			} while (true);
		} catch (FileNotFoundException e) {
			String message = "Could not find file '" + path + "'";
			pluginStepContext.getLogger().log(ERR_LEVEL, message);
			throw(e);
		}
	}

	/**
	 * Performs the tree search in a recursive depth-first manner.
	 *
	 * @param node The node tree to search.
	 * @return The textual form of the first matched field, or null if not matched.
	 */
	private String searchTree(JsonNode node, String fieldName) {
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

	private void addFieldToOutput(String group, String name, String value, boolean elevate) {
		pluginStepContext.getOutputContext().addOutput(group, name, value);
		if (elevate) {
			String groupName = group + "." + name;
			pluginStepContext.getOutputContext().addOutput(ContextView.global(), "export", groupName, value);
			pluginStepContext.getLogger().log(Constants.DEBUG_LEVEL, "Elevating to global ${export." + groupName + "}.");
		}
	}
}