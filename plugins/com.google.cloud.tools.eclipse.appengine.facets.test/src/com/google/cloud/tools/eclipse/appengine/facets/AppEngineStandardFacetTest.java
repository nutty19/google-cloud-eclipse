/*******************************************************************************
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/

package com.google.cloud.tools.eclipse.appengine.facets;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jst.server.core.FacetUtil;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.junit.Assert;
import org.junit.Test;

public class AppEngineStandardFacetTest {
  @Test
  public void testCreateAppEngineServerRuntime() throws CoreException {
    org.eclipse.wst.server.core.IRuntime runtime =
        AppEngineStandardFacet.createAppEngineServerRuntime(null);
    Assert.assertNotNull(runtime);
    Assert.assertEquals(AppEngineStandardFacet.DEFAULT_RUNTIME_ID, runtime.getRuntimeType().getId());
  }
  
  @Test
  public void testCreateAppEngineFacetRuntime() throws CoreException {
    IRuntime facetRuntime = AppEngineStandardFacet.createAppEngineFacetRuntime(null);
    org.eclipse.wst.server.core.IRuntime serverRuntime =
        FacetUtil.getRuntime(facetRuntime);
    Assert.assertNotNull(serverRuntime);
    Assert.assertEquals(AppEngineStandardFacet.DEFAULT_RUNTIME_ID, serverRuntime.getRuntimeType().getId());    
  }
}
