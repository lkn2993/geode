/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.geode.management.internal.rest;

import org.springframework.test.context.web.GenericXmlWebContextLoader;
import org.springframework.test.context.web.WebMergedContextConfiguration;
import org.springframework.web.context.support.GenericWebApplicationContext;

import org.apache.geode.internal.cache.HttpService;
import org.apache.geode.security.SimpleTestSecurityManager;
import org.apache.geode.test.junit.rules.LocatorStarterRule;

class LocatorWithSecurityManagerContextLoader extends GenericXmlWebContextLoader {

  private LocatorStarterRule locator = new LocatorStarterRule()
      .withSecurityManager(SimpleTestSecurityManager.class)
      .withAutoStart();

  @Override
  protected void loadBeanDefinitions(GenericWebApplicationContext context,
      WebMergedContextConfiguration webMergedConfig) {

    locator.before();

    super.loadBeanDefinitions(context, webMergedConfig);
    context.getServletContext().setAttribute(HttpService.SECURITY_SERVICE_SERVLET_CONTEXT_PARAM,
        locator.getCache().getSecurityService());
    context.getServletContext().setAttribute(HttpService.CLUSTER_MANAGEMENT_SERVICE_CONTEXT_PARAM,
        locator.getLocator().getClusterManagementService());

    context.getServletContext().setAttribute("locator", locator);
  }

}
