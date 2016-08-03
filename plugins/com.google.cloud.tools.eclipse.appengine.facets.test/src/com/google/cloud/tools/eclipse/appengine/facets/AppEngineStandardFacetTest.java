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

import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jst.server.core.FacetUtil;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AppEngineStandardFacetTest {
  @Mock private org.eclipse.wst.server.core.IRuntime serverRuntime;
  @Mock private IRuntimeType runtimeType;

  @Test
  public void testIsAppEngineRuntime_appEngineRuntime() {
    when(runtimeType.getId()).thenReturn(AppEngineStandardFacet.DEFAULT_RUNTIME_ID);
    when(serverRuntime.getRuntimeType()).thenReturn(runtimeType);

    Assert.assertTrue(AppEngineStandardFacet.isAppEngineRuntime(serverRuntime));
  }

  @Test
  public void testIsAppEngineRuntime_nonAppEngineRuntime() {
    when(runtimeType.getId()).thenReturn("some id");
    when(serverRuntime.getRuntimeType()).thenReturn(runtimeType);

    Assert.assertFalse(AppEngineStandardFacet.isAppEngineRuntime(serverRuntime));
  }

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
