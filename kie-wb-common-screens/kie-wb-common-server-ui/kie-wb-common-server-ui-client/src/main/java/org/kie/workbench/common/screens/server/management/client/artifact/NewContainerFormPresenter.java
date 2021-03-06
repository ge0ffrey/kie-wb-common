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
package org.kie.workbench.common.screens.server.management.client.artifact;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.guvnor.common.services.project.model.GAV;
import org.guvnor.m2repo.service.M2RepoService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.server.management.client.events.DependencyPathSelectedEvent;
import org.kie.workbench.common.screens.server.management.service.ServerManagementService;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchPopup;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

@Dependent
@WorkbenchPopup(identifier = "NewContainerForm")
public class NewContainerFormPresenter {

    public interface View extends UberView<NewContainerFormPresenter> {

        void setGroupId( final String groupId );

        void setAtifactId( final String artifactId );

        void setVersion( final String version );

        void setEndpoint( final String endpoint );
    }

    private final View view;

    private final PlaceManager placeManager;

    private final Caller<M2RepoService> m2RepoService;

    private final Caller<ServerManagementService> service;

    private final DependencyListWidgetPresenter dependencyListWidgetPresenter;

    private PlaceRequest place;

    private String serverId;
    private String endpoint;

    private String containerName;
    public String groupId;
    public String artifactId;
    public String version;

    @Inject
    public NewContainerFormPresenter( final View view,
                                      final PlaceManager placeManager,
                                      final Caller<M2RepoService> m2RepoService,
                                      final Caller<ServerManagementService> service,
                                      final DependencyListWidgetPresenter dependencyListWidgetPresenter ) {
        this.view = view;
        this.placeManager = placeManager;
        this.m2RepoService = m2RepoService;
        this.service = service;
        this.dependencyListWidgetPresenter = dependencyListWidgetPresenter;
        this.view.init( this );
    }

    @OnStartup
    public void onStartup( final PlaceRequest place ) {
        this.place = place;
        this.serverId = place.getParameter( "serverId", null );
        setEndpoint( serverId + "/containers/" );
    }

    @WorkbenchPartView
    public View getView() {
        return view;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "Create Container";
    }

    private void setEndpoint( final String value ) {
        this.endpoint = value;
        view.setEndpoint( endpoint );
    }

    public void setContainerName( final String value ) {
        this.containerName = value;
        setEndpoint( serverId + "/containers/" + containerName );
    }

    public void createContainer( final String containerName,
                                 final String groupId,
                                 final String artifactId,
                                 final String version ) {
        setContainerName( containerName );
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;

        service.call().createContainer( this.serverId,
                this.containerName,
                new GAV( this.groupId, this.artifactId, this.version ) );
    }

    void onDependencyPathSelectedEvent( @Observes final DependencyPathSelectedEvent event ) {
        if ( event.getContext().equals( dependencyListWidgetPresenter ) ) {
            m2RepoService.call( new RemoteCallback<GAV>() {
                @Override
                public void callback( GAV gav ) {
                    groupId = gav.getGroupId();
                    artifactId = gav.getArtifactId();
                    version = gav.getVersion();

                    view.setGroupId( groupId );
                    view.setAtifactId( artifactId );
                    view.setVersion( version );
                }
            } ).loadGAVFromJar( event.getPath() );
        }
    }

    public void close() {
        placeManager.forceClosePlace( this.place );
    }

    DependencyListWidgetPresenter getDependencyListWidgetPresenter() {
        return dependencyListWidgetPresenter;
    }

    String getServerId() {
        return serverId;
    }

    String getEndpoint() {
        return endpoint;
    }

    String getContainerName() {
        return containerName;
    }

    String getGroupId() {
        return groupId;
    }

    String getArtifactId() {
        return artifactId;
    }

    String getVersion() {
        return version;
    }
}
