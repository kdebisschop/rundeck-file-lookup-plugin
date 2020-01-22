/*
 * Copyright 2019 BioRAFT, Inc. (http://bioraft.com)
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.dtolabs.rundeck.core.execution.workflow.SharedOutputContext;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;

/**
 * Tests for ScanFileStepPlugin.
 *
 * @author Karl DeBisschop <kdebisschop@gmail.com>
 * @since 2019-12-11
 */
@RunWith(MockitoJUnitRunner.class)
public class ScanFileStepPluginTest {

	ScanFileStepPlugin plugin;

	@Mock
	PluginStepContext context;

	@Mock
	SharedOutputContext sharedOutputContext;

	@Captor
	ArgumentCaptor<String> nameCaptor;

	@Captor
	ArgumentCaptor<String> valueCaptor;

	Map<String, Object> configuration;

	@Before
	public void setUp() {
		this.plugin = new ScanFileStepPlugin();
		configuration = Stream.of(new String[][] { { "path", "testData/test.yaml" }, { "group", "example" },
				{ "name", "key" }, { "regex", "single" }, })
				.collect(Collectors.toMap(data -> data[0], data -> data[1]));
	}

	@Test(expected = StepException.class)
	public void noFileThrowsException() throws StepException {
		configuration.put("path", "nosuchfile");
		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		this.plugin.executeStep(context, configuration);
	}

	@Test
	public void canRunWithoutMatch() throws StepException {
		configuration.put("regex", "com[.]example[.]label3: (.*)");
		when(context.getOutputContext()).thenReturn(sharedOutputContext);

		this.plugin.executeStep(context, configuration);
		verify(context, never()).getOutputContext();
		verify(sharedOutputContext, never()).addOutput(anyString(), anyString(), anyString());
	}

	@Test
	public void canFindSingleCapture() throws StepException {
		configuration.put("regex", "com[.]example[.]label2: (.*)");
		when(context.getOutputContext()).thenReturn(sharedOutputContext);

		this.plugin.executeStep(context, configuration);
		verify(context).getOutputContext();
		verify(sharedOutputContext).addOutput(matches("^example$"), nameCaptor.capture(), valueCaptor.capture());

		List<String> names = nameCaptor.getAllValues();
		List<String> values = valueCaptor.getAllValues();
		Map<String, String> found = mergeLists(names, values);

		// Returns first match.
		assertEquals("another", found.get("key"));
	}

	@Test
	public void canFindMultipleCapture() throws StepException {
		configuration.put("regex", "^\\s*com[.]example[.](label1|label2): (.*)");
		when(context.getOutputContext()).thenReturn(sharedOutputContext);

		this.plugin.executeStep(context, configuration);
		verify(context, times(2)).getOutputContext();
		verify(sharedOutputContext, times(2)).addOutput(matches("^example$"), nameCaptor.capture(),
				valueCaptor.capture());

		List<String> names = nameCaptor.getAllValues();
		List<String> values = valueCaptor.getAllValues();
		Map<String, String> found = mergeLists(names, values);

		assertEquals("firstValue", found.get("label1"));
		// Returns first match.
		assertEquals("another", found.get("label2"));
	}

	private Map<String, String> mergeLists(List<String> keys, List<String> values) {
		if (keys.size() != values.size()) {
			throw new IllegalArgumentException("Cannot combine lists with dissimilar sizes");
		}

		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < keys.size(); i++) {
			map.put(keys.get(i), values.get(i));
		}

		return map;
	}

}
