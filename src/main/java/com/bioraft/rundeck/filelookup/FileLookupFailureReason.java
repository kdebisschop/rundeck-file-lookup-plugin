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

import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;

/**
 * @author Karl DeBisschop <kdebisschop@gmail.com>
 * @since 2019-12-23
 */
public enum FileLookupFailureReason implements FailureReason {
	FileNotFound,
	FileNotReadable
}
