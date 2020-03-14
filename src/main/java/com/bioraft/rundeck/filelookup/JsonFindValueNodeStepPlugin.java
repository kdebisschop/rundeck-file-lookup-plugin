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

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.step.NodeStepPlugin;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.defaultString;

/**
 * Node Step Plug-in to find value of first matching field name in JSON file.
 * 
 * Scans the specified file for the indicated field name and returns the value
 * of the first matching value node. Search through non-value nodes is performed
 * in a depth-first manner, so the match found is the first value match seen
 * when when scanning down the file and earlier matches will mask matches that
 * are less deep in the tree but later in the file. Breadth-first search could
 * be implemented as an option but is not done here.
 *
 * @author Karl DeBisschop <kdebisschop@gmail.com>
 * @since 2019-12-11
 */
@Plugin(name = JsonFindValueNodeStepPlugin.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowNodeStep)
@PluginDescription(title = "JSON Lookup Plugin", description = "Scans a JSON file for the indicated field and returns the first atomic value.")
public class JsonFindValueNodeStepPlugin implements NodeStepPlugin {
	public static final String SERVICE_PROVIDER_NAME = "JsonFileLookupNodeStepPlugin";

	@PluginProperty(title = "Path", description = "Path to the JSON file", required = true)
	private String path;

	@PluginProperty(title = "Group", description = "Variable group (i.e., ${group.x}", required = true)
	private String group;

	@PluginProperty(title = "Name", description = "Variable name (i.e., ${group.name}", required = true)
	private String name;

	@PluginProperty(title = "Field Name", description = "Field name to lookup in JSON", required = true)
	private String fieldName;

	@PluginProperty(title = "Make global?", description = "Elevate this variable to global scope (default: false)", defaultValue="false", required = true)
	private boolean elevateToGlobal;

	@Override
	public void executeNodeStep(final PluginStepContext context, final Map<String, Object> configuration,
			final INodeEntry node) throws NodeStepException {
		path = configuration.getOrDefault("path", defaultString(path)).toString();
		group = configuration.getOrDefault("group", defaultString(group)).toString();
		name = configuration.getOrDefault("name", defaultString(name)).toString();
		fieldName = configuration.getOrDefault("fieldName", defaultString(fieldName)).toString();
		elevateToGlobal = configuration.getOrDefault("elevateToGlobal", this.elevateToGlobal).toString()
				.equals("true");

		try {
			(new FileLookupUtils(context)).scanFile(path, fieldName, group, name, elevateToGlobal);
		} catch (FileNotFoundException e) {
			String msg = "Could not find file " + path;
			String nodeName = node.getNodename();
			throw new NodeStepException(msg, e, FileLookupFailureReason.FILE_NOT_FOUND, nodeName);
		} catch (IOException e) {
			String msg = "Could not read file " + path;
			String nodeName = node.getNodename();
			throw new NodeStepException(msg, e, FileLookupFailureReason.FILE_NOT_READABLE, nodeName);
		}
	}
}
