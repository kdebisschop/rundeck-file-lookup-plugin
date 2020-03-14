package com.bioraft.rundeck.filelookup;

import com.dtolabs.rundeck.core.execution.workflow.SharedOutputContext;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

import static com.dtolabs.rundeck.core.Constants.ERR_LEVEL;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FileLookupUtilsTest {

    @Mock
    PluginStepContext context;

    @Mock
    SharedOutputContext sharedOutputContext;

    @Mock
    PluginLogger logger;

    @Mock
    ObjectMapper objectMapper;

    @Test(expected = IOException.class)
    public void cannotParseJson() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String path = Objects.requireNonNull(classLoader.getResource("service.json")).getFile();

        when(context.getLogger()).thenReturn(logger);
        FileLookupUtils subject = new FileLookupUtils(context, objectMapper);
        when(objectMapper.readTree((Reader) any())).thenThrow(new IOException());
        subject.scanJsonFile(path, "field", "group", "name", false);
        verify(logger).log(eq(ERR_LEVEL), startsWith("Could parse JSON file "));
    }

    @Test
    public void matchTwoGroups() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String path = Objects.requireNonNull(classLoader.getResource("service.json")).getFile();
        String group = "group";

        when(context.getLogger()).thenReturn(logger);
        when(context.getOutputContext()).thenReturn(sharedOutputContext);
        FileLookupUtils subject = new FileLookupUtils(context, objectMapper);
        subject.scanPropertiesFile(path, group, "field", "\"(name|state)\": \"([a-z]+)\"", false);
        verify(sharedOutputContext, times(1)).addOutput(eq(group), eq("name"), eq("frontend"));
        verify(sharedOutputContext, times(1)).addOutput(eq(group), eq("state"), eq("active"));
    }

    @Test
    public void matchNoGroups() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String path = Objects.requireNonNull(classLoader.getResource("service.json")).getFile();
        String group = "group";

        when(context.getLogger()).thenReturn(logger);
        when(context.getOutputContext()).thenReturn(sharedOutputContext);
        FileLookupUtils subject = new FileLookupUtils(context, objectMapper);
        subject.scanPropertiesFile(path, group, "field", "\"name\": \"[a-z]+\"", false);
        verify(sharedOutputContext, times(1)).addOutput(eq(group), eq("field"), eq("\"name\": \"frontend\""));
    }

    @Test
    public void elevateToGlobal() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        String path = Objects.requireNonNull(classLoader.getResource("service.json")).getFile();
        String group = "group";

        when(context.getLogger()).thenReturn(logger);
        when(context.getOutputContext()).thenReturn(sharedOutputContext);
        FileLookupUtils subject = new FileLookupUtils(context, objectMapper);
        subject.scanPropertiesFile(path, group, "field", "\"name\": \"[a-z]+\"", true);
        verify(sharedOutputContext, times(1)).addOutput(eq(group), eq("field"), eq("\"name\": \"frontend\""));
        verify(sharedOutputContext, times(1)).addOutput(any(), eq("export"), eq(group + ".field"), eq("\"name\": \"frontend\""));
    }
}
