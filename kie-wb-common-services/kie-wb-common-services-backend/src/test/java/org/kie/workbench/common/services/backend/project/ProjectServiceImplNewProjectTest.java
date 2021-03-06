/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.services.backend.project;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import javax.enterprise.event.Event;

import org.guvnor.common.services.backend.util.CommentedOptionFactory;
import org.guvnor.common.services.project.backend.server.AbstractProjectService;
import org.guvnor.common.services.project.backend.server.DeleteProjectObserverBridge;
import org.guvnor.common.services.project.builder.events.InvalidateDMOProjectCacheEvent;
import org.guvnor.common.services.project.events.DeleteProjectEvent;
import org.guvnor.common.services.project.events.NewPackageEvent;
import org.guvnor.common.services.project.events.NewProjectEvent;
import org.guvnor.common.services.project.events.RenameProjectEvent;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.POMService;
import org.guvnor.structure.backend.backcompat.BackwardCompatibleUtil;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.server.config.ConfigurationFactory;
import org.guvnor.structure.server.config.ConfigurationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.FileSystemNotFoundException;
import org.uberfire.java.nio.file.FileSystems;
import org.uberfire.rpc.SessionInfo;
import org.uberfire.security.authz.AuthorizationManager;
import org.uberfire.workbench.events.ResourceDeletedEvent;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class ProjectServiceImplNewProjectTest {

    @Mock
    private IOService ioService;

    @Mock
    private ProjectSaver saver;

    private KieProjectServiceImpl projectService;

    @Before
    public void setup() {

        final Event<NewProjectEvent> newProjectEvent = mock( Event.class );
        final Event<NewPackageEvent> newPackageEvent = mock( Event.class );
        final Event<RenameProjectEvent> renameProjectEvent = mock( Event.class );
        final Event<InvalidateDMOProjectCacheEvent> invalidateDMOCache = mock( Event.class );

        projectService = new KieProjectServiceImpl( ioService,
                                                    saver,
                                                    mock( POMService.class ),
                                                    mock( ConfigurationService.class ),
                                                    mock( ConfigurationFactory.class ),
                                                    newProjectEvent,
                                                    newPackageEvent,
                                                    renameProjectEvent,
                                                    invalidateDMOCache,
                                                    mock( SessionInfo.class ),
                                                    mock( AuthorizationManager.class ),
                                                    mock( BackwardCompatibleUtil.class ),
                                                    mock( CommentedOptionFactory.class ),
                                                    mock( KieResourceResolver.class ) ) {
        };

        assertNotNull( projectService );
    }

    @Test
    public void testNewProjectCreation() throws URISyntaxException {
        final Repository repository = mock( Repository.class );
        final POM pom = new POM();
        final String baseURL = "/";

        final KieProject expected = new KieProject();

        when( saver.save( repository,
                          pom,
                          baseURL ) ).thenReturn( expected );

        final Project project = projectService.newProject( repository,
                                                              pom,
                                                              baseURL );

        assertEquals( expected,
                      project );
    }

    @Test
    public void testDeleteProjectObserverBridge() throws URISyntaxException {
        final URI fs = new URI( "git://test" );
        try {
            FileSystems.getFileSystem( fs );
        } catch ( FileSystemNotFoundException e ) {
            FileSystems.newFileSystem( fs,
                                       new HashMap<String, Object>() );
        }

        final Path path = mock( Path.class );
        final org.uberfire.java.nio.file.Path nioPath = mock( org.uberfire.java.nio.file.Path.class );
        when( path.getFileName() ).thenReturn( "pom.xml" );
        when( path.toURI() ).thenReturn( "git://test/p0/pom.xml" );
        when( nioPath.getParent() ).thenReturn( nioPath );
        when( nioPath.resolve( any( String.class ) ) ).thenReturn( nioPath );
        when( nioPath.toUri() ).thenReturn( URI.create( "git://test/p0/pom.xml" ) );
        when( nioPath.getFileSystem() ).thenReturn( FileSystems.getFileSystem( fs ) );
        when( ioService.get( any( URI.class ) ) ).thenReturn( nioPath );

        final SessionInfo sessionInfo = mock( SessionInfo.class );
        final Event<DeleteProjectEvent> deleteProjectEvent = mock( Event.class );
        final AbstractProjectService projectServiceSpy = spy( projectService );

        final DeleteProjectObserverBridge bridge = new DeleteProjectObserverBridge( ioService,
                                                                                    projectServiceSpy,
                                                                                    deleteProjectEvent );

        bridge.onBatchResourceChanges( new ResourceDeletedEvent( path,
                                                                 "message",
                                                                 sessionInfo ) );

        verify( deleteProjectEvent,
                times( 1 ) ).fire( any( DeleteProjectEvent.class ) );

        verify( projectServiceSpy,
                times( 0 ) ).newProject( any( Repository.class ),
                                         any( POM.class ),
                                         any( String.class ) );
        verify( projectServiceSpy,
                times( 1 ) ).simpleProjectInstance( any( org.uberfire.java.nio.file.Path.class ) );
    }

}
