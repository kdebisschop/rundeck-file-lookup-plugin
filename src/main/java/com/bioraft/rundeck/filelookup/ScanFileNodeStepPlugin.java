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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.step.NodeStepPlugin;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;

/**
 * Workflow Step Plug-in to find value of first matching text file.
 * 
 * Scans the specified file for the indicated pattern and returns the indicated
 * capture fields. Can be used for unstructured text as well as structured text
 * files like YAML and properties files.
 * 
 * When one capture field is given, the desired variable name is required as an
 * option. When two capture fields are given, the variable name will taken from
 * the first capture field and the value from the second.
 * 
 * In cases where there are multiple matches found, the first is returned.
 *
 * @author Karl DeBisschop <kdebisschop@gmail.com>
 * @since 2019-12-11
 */
@Plugin(name = ScanFileNodeStepPlugin.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowNodeStep)
@PluginDescription(title = "Scan a file to capture values", description = "Use a regular expression to look for key/value pairs in a text file.")
public class ScanFileNodeStepPlugin implements NodeStepPlugin {
	public static final String SERVICE_PROVIDER_NAME = "com.bioraft.rundeck.filelookup.ScanFileStepPlugin";

	@PluginProperty(title = "Path", description = "Path to the file to scan", required = true)
	private String path;

	@PluginProperty(title = "Group", description = "Variable group (i.e., ${group.x}}", required = true, defaultValue = "data")
	private String group;

	@PluginProperty(title = "Name", description = "Variable name (i.e., ${group.name}) [ignored when Pattern has 2 capture fields]", required = false)
	private String name;

	@PluginProperty(title = "Pattern", description = "Regular expression to find, with one or two capture fields", required = true)
	private String regex;

	@PluginProperty(title = "Make global?", description = "Elevate this variable to global scope (default: false)", required = false)
	private boolean elevateToGlobal;

	@Override
	public void executeNodeStep(PluginStepContext context, Map<String, Object> configuration, INodeEntry node)
			throws NodeStepException {
		String path = configuration.getOrDefault("path", this.path).toString();
		String group = configuration.getOrDefault("group", this.group).toString();
		String name = configuration.getOrDefault("name", this.name).toString();
		String regex = configuration.getOrDefault("regex", this.regex).toString();
		boolean elevateToGlobal = configuration.getOrDefault("elevateToGlobal", this.elevateToGlobal).toString()
				.equals("true");

		Pattern pattern = Pattern.compile(regex);
		MatchResult match;
		Matcher matcher;
		String line;

		if (name == null || name.equals("")) {
			name = "data";
		}

		Map<String, String> map = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			// Scan lines for a match.
			// Optimize by returning immediately when there is only one capture field.
			while ((line = reader.readLine()) != null) {
				matcher = pattern.matcher(line);
				if (matcher.find()) {
					match = matcher.toMatchResult();
					if (match.groupCount() == 1) {
						FileLookupUtils.addOutput(context, group, name, match.group(1), elevateToGlobal);
						return;
					} else if (match.groupCount() == 2) {
						// Take first value and do not overwrite, even though scanning proceeds
						// through the rest of the file to find other matches to the pattern.
						if (map.get(match.group(1)) == null) {
							FileLookupUtils.addOutput(context, group, match.group(1), match.group(2), elevateToGlobal);
							map.put(match.group(1), match.group(2));
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			String msg = "Could not find file " + path;
			String nodeName = node.getNodename();
			throw new NodeStepException(msg, e, FileLookupFailureReason.FileNotFound, nodeName);
		} catch (IOException e) {
			String msg = "Could not read file " + path;
			String nodeName = node.getNodename();
			throw new NodeStepException(msg, e, FileLookupFailureReason.FileNotReadable, nodeName);
		}
	}
}
