# Rundeck File Lookup Plugins

This plugin provides utilities to read data from the filesystem into Rundeck step data.

## Features

### Scan File Step Plugin

Scans a file for a regular expression pattern. If the specified pattern has one
replaceable field, the scan stops on first match for performance reasons. If the
pattern has two capture fields, the scan will go to the end of the file and the
last value for matches will be returned.

### JSON Lookup Step Plugin

Finds the first matching key in a JSON file.
