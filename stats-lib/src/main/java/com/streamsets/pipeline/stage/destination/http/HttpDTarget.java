/**
 * Copyright 2016 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.stage.destination.http;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.StageDef;
import com.streamsets.pipeline.api.Target;
import com.streamsets.pipeline.api.el.SdcEL;
import com.streamsets.pipeline.configurablestage.DTargetOffsetCommitTrigger;

@StageDef(
  version = 1,
  label = "Http Destination",
  description = "Writes data to Http destination",
  icon = "httpclient.png",
  onlineHelpRefUrl = ""
)
@ConfigGroups(value = Groups.class)
@GenerateResourceBundle
public class HttpDTarget extends DTargetOffsetCommitTrigger {

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.STRING,
    defaultValue = "${REMOTE_TIMESERIES_URL}",
    label = "Target URL",
    description = "The target URL into which the data must be written",
    displayPosition = 10,
    group = "HTTP"
  )
  public String targetUrl;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.STRING,
    defaultValue = "${sdc:authToken()}",
    label = "Auth Token",
    description = "The auth token generated by DPM",
    displayPosition = 20,
    group = "HTTP"
  )
  public String authToken;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.STRING,
    defaultValue = "${sdc:id()}",
    label = "Sdc Id",
    displayPosition = 30,
    group = "HTTP",
    elDefs = SdcEL.class
  )
  public String sdcId;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.STRING,
    defaultValue = "${PIPELINE_COMMIT_ID}",
    label = "Pipeline commit Id",
    displayPosition = 40,
    group = "HTTP"
  )
  public String pipelineCommitId;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.STRING,
    defaultValue = "${JOB_ID}",
    label = "Job Id",
    displayPosition = 50,
    group = "HTTP"
  )
  public String jobId;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.NUMBER,
    defaultValue = "${UPDATE_WAIT_TIME_MS}",
    label = "Wait Time (ms) between updates",
    description = "Time to wait between stats updates",
    displayPosition = 60,
    group = "HTTP",
    min = 0,
    max = Integer.MAX_VALUE
  )
  public int waitTimeBetweenUpdates;

  @Override
  protected Target createTarget() {
    return new HttpTarget(targetUrl, authToken, sdcId, pipelineCommitId, jobId, waitTimeBetweenUpdates);
  }
}
