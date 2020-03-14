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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.mockito.junit.MockitoJUnitRunner;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.SharedOutputContext;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;

/**
 * Tests for JsonFindValueStepPlugin.
 *
 * @author Karl DeBisschop <kdebisschop@gmail.com>
 * @since 2019-12-11
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonFindValueNodeStepPluginTest {

	JsonFindValueNodeStepPlugin plugin;

	@Mock
	PluginStepContext context;

	@Mock
	INodeEntry entry;

	@Mock
	SharedOutputContext sharedOutputContext;

	@Captor
	ArgumentCaptor<String> groupCaptor;

	@Captor
	ArgumentCaptor<String> nameCaptor;

	@Captor
	ArgumentCaptor<String> valueCaptor;

	Map<String, Object> configuration;

	@Before
	public void setUp() {
		this.plugin = new JsonFindValueNodeStepPlugin();
		configuration = Stream.of(new String[][] { { "path", "testData/test.json" }, { "group", "raft" },
				{ "name", "key" }, { "fieldName", "single" }, })
				.collect(Collectors.toMap(data -> data[0], data -> data[1]));
	}

	@Test(expected = StepException.class)
	public void noFileThrowsException() throws StepException {
		configuration.put("path", "nosuchfile");
		this.plugin.executeNodeStep(context, configuration, entry);
	}

	@Test
	public void returnsValueForSimpleMatch() throws StepException {
		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		this.plugin.executeNodeStep(context, configuration, entry);
		verify(context).getOutputContext();
		verify(sharedOutputContext).addOutput(groupCaptor.capture(), nameCaptor.capture(), valueCaptor.capture());

		List<String> groups = groupCaptor.getAllValues();
		List<String> names = nameCaptor.getAllValues();
		List<String> values = valueCaptor.getAllValues();

		int i = 0;
		assertEquals("raft", groups.get(i));
		assertEquals("key", names.get(i));
		assertEquals("v1", values.get(i));
	}

	@Test
	public void returnsValueForMatchInTree() throws StepException {
		configuration.put("fieldName", "address");
		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		this.plugin.executeNodeStep(context, configuration, entry);
		verify(context).getOutputContext();
		verify(sharedOutputContext).addOutput(groupCaptor.capture(), nameCaptor.capture(), valueCaptor.capture());

		List<String> groups = groupCaptor.getAllValues();
		List<String> names = nameCaptor.getAllValues();
		List<String> values = valueCaptor.getAllValues();

		int i = 0;
		assertEquals("raft", groups.get(i));
		assertEquals("key", names.get(i));
		assertEquals("school", values.get(i));
	}

	@Test
	public void returnsValueInLaterTree() throws StepException {
		configuration.put("fieldName", "number");
		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		this.plugin.executeNodeStep(context, configuration, entry);
		verify(context).getOutputContext();
		verify(sharedOutputContext).addOutput(groupCaptor.capture(), nameCaptor.capture(), valueCaptor.capture());

		List<String> groups = groupCaptor.getAllValues();
		List<String> names = nameCaptor.getAllValues();
		List<String> values = valueCaptor.getAllValues();

		int i = 0;
		assertEquals("raft", groups.get(i));
		assertEquals("key", names.get(i));
		assertEquals("212 555-1234", values.get(i));
	}

	@Test
	public void returnsValueForInteger() throws StepException {
		configuration.put("fieldName", "postalCode");
		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		this.plugin.executeNodeStep(context, configuration, entry);
		verify(context).getOutputContext();
		verify(sharedOutputContext).addOutput(groupCaptor.capture(), nameCaptor.capture(), valueCaptor.capture());

		List<String> groups = groupCaptor.getAllValues();
		List<String> names = nameCaptor.getAllValues();
		List<String> values = valueCaptor.getAllValues();

		int i = 0;
		assertEquals("raft", groups.get(i));
		assertEquals("key", names.get(i));
		assertEquals("10021", values.get(i));
	}

	@Test
	public void returnsValueForDouble() throws StepException {
		configuration.put("fieldName", "age");
		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		this.plugin.executeNodeStep(context, configuration, entry);
		verify(context).getOutputContext();
		verify(sharedOutputContext).addOutput(groupCaptor.capture(), nameCaptor.capture(), valueCaptor.capture());

		List<String> groups = groupCaptor.getAllValues();
		List<String> names = nameCaptor.getAllValues();
		List<String> values = valueCaptor.getAllValues();

		int i = 0;
		assertEquals("raft", groups.get(i));
		assertEquals("key", names.get(i));
		assertEquals("25.2", values.get(i));
	}

	@Test
	public void returnsValueForBoolean() throws StepException {
		configuration.put("fieldName", "deceased");
		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		this.plugin.executeNodeStep(context, configuration, entry);
		verify(context).getOutputContext();
		verify(sharedOutputContext).addOutput(groupCaptor.capture(), nameCaptor.capture(), valueCaptor.capture());

		List<String> groups = groupCaptor.getAllValues();
		List<String> names = nameCaptor.getAllValues();
		List<String> values = valueCaptor.getAllValues();

		int i = 0;
		assertEquals("raft", groups.get(i));
		assertEquals("key", names.get(i));
		assertEquals("true", values.get(i));
	}

	@Test
	public void notCalledOnNoMatch() throws StepException {
		configuration.put("fieldName", "no_such_key");
		this.plugin.executeNodeStep(context, configuration, entry);
		verify(context, never()).getOutputContext();
		verify(sharedOutputContext, never()).addOutput(anyString(), anyString(), anyString());
	}
}
