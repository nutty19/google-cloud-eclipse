/*******************************************************************************
 * Copyright 2016 Google Inc.
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
 *******************************************************************************/
package com.google.cloud.tools.eclipse.appengine.libraries.repository;

import java.util.Collections;
import java.util.List;

import org.apache.maven.cli.transfer.Slf4jMavenTransferListener;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.internal.impl.DefaultRemoteRepositoryManager;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.repository.IRepositoryRegistry;
import org.eclipse.aether.transport.wagon.WagonTransporterFactory;
import org.eclipse.aether.version.Version;

import com.google.cloud.tools.eclipse.appengine.libraries.MavenCoordinates;
import com.google.cloud.tools.eclipse.util.status.StatusUtil;

import io.takari.aether.connector.AetherRepositoryConnectorFactory;

/**
 * Implementation of {@link ILibraryRepositoryService} that relies on M2Eclipse to download the artifacts and store
 * them in the local Maven repository pointed to by M2Eclipse's M2_REPO variable.
 */
// FIXME For now this class is just a mock, to be implemented soon
public class M2RepositoryService implements ILibraryRepositoryService {

  @Override
  public IPath getJarLocation(MavenCoordinates mavenCoordinates) {
    try {
      
      RepositorySystem system = newRepositorySystem();

      RepositorySystemSession session = newRepositorySystemSession( system );

      String version;
      if (mavenCoordinates.getVersion().equals(MavenCoordinates.LATEST_VERSION)) {
        version = "[0,)";
      } else {
        version = mavenCoordinates.getVersion();
      }
      
      Artifact artifact = new DefaultArtifact(mavenCoordinates.getGroupId(), mavenCoordinates.getArtifactId(), mavenCoordinates.getType(), version );
      
      VersionRangeRequest rangeRequest = new VersionRangeRequest();
      rangeRequest.setArtifact(artifact);
      rangeRequest.setRepositories(Collections.singletonList(new RemoteRepository.Builder( "central", "default", "http://central.maven.org/maven2/" ).build()));
      VersionRangeResult versionRangeResult = system.resolveVersionRange(session, rangeRequest);
      Version newestVersion = versionRangeResult.getHighestVersion();
      
      artifact = artifact.setVersion(newestVersion.toString());
      ArtifactRequest artifactRequest = new ArtifactRequest();
      artifactRequest.setArtifact( artifact );
      artifactRequest.setRepositories(Collections.singletonList(new RemoteRepository.Builder( "central", "default", "http://central.maven.org/maven2/" ).build()));
      ArtifactResult artifactResult = system.resolveArtifact( session, artifactRequest );
      if (artifactResult.isMissing()) {
        throw new CoreException(StatusUtil.error(this, "Could not download artifact"));
      }
    } catch (ArtifactResolutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (VersionRangeResolutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
      
    return new Path("/path/to/jar/file/in/m2_repo/" + mavenCoordinates.getArtifactId() + "." + mavenCoordinates.getType());
  }

  @Override
  public IPath getSourceJarLocation(MavenCoordinates mavenCoordinates) {
    return new Path("/path/to/source/jar/file/in/m2_repo/" + mavenCoordinates.getArtifactId() + "." + mavenCoordinates.getType());
  }

  public static RepositorySystem newRepositorySystem()
  {
      /*
       * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the
       * prepopulated DefaultServiceLocator, we only need to register the repository connector and transporter
       * factories.
       */
      DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
      locator.addService( RepositoryConnectorFactory.class, AetherRepositoryConnectorFactory.class );
      locator.addService( TransporterFactory.class, WagonTransporterFactory.class );

      locator.setErrorHandler( new DefaultServiceLocator.ErrorHandler()
      {
          @Override
          public void serviceCreationFailed( Class<?> type, Class<?> impl, Throwable exception )
          {
              exception.printStackTrace();
          }
      } );

      return locator.getService( RepositorySystem.class );
  }
  
  public static DefaultRepositorySystemSession newRepositorySystemSession( RepositorySystem system )
  {
      DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

      LocalRepository localRepo = new LocalRepository( "target/local-repo" );
      session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );

      session.setTransferListener( new Slf4jMavenTransferListener() );
      session.setRepositoryListener( new AbstractRepositoryListener() {  } );

      // uncomment to generate dirty trees
      // session.setDependencyGraphTransformer( null );

      return session;
  }
}
