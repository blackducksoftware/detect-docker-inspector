/**
 * blackduck-docker-inspector
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.dockerinspector.httpclient;

import com.blackduck.integration.blackduck.dockerinspector.ProcessId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContainerName {

  @Autowired
  private ProcessId processId;

  public String deriveContainerNameFromImageInspectorRepo(final String imageInspectorRepo) {
    String extractorContainerName;
    final int slashIndex = imageInspectorRepo.lastIndexOf('/');
    if (slashIndex < 0) {
      extractorContainerName = imageInspectorRepo;
    } else {
      extractorContainerName = imageInspectorRepo.substring(slashIndex + 1);
    }
    return processId.addProcessIdToName(extractorContainerName);
  }

}
