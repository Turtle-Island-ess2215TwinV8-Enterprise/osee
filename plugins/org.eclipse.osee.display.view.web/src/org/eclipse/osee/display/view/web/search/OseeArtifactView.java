/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.display.view.web.search;

import org.eclipse.osee.display.api.components.ArtifactHeaderComponent;
import org.eclipse.osee.display.api.data.WebArtifact;
import org.eclipse.osee.display.api.search.SearchPresenter;
import org.eclipse.osee.display.view.web.components.OseeArtifactNameLinkComponent;
import org.eclipse.osee.display.view.web.components.OseeAttributeComponent;
import org.eclipse.osee.display.view.web.components.OseeBreadcrumbComponent;
import org.eclipse.osee.display.view.web.components.OseeRelationsComponent;
import org.eclipse.osee.vaadin.widgets.Navigator;
import com.vaadin.Application;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Shawn F. Cook
 */
@SuppressWarnings("serial")
public class OseeArtifactView extends CustomComponent implements Navigator.View, ArtifactHeaderComponent {

   protected SearchPresenter searchPresenter = null;
   protected OseeSearchHeaderComponent searchHeader;
   protected OseeRelationsComponent relationsComp = new OseeRelationsComponent();
   protected OseeAttributeComponent attributeComp = new OseeAttributeComponent();
   private final OseeBreadcrumbComponent breadcrumbComp = new OseeBreadcrumbComponent(null);
   private WebArtifact artifact;

   @Override
   public void attach() {
      //TODO: remove?
   }

   protected void createLayout() {
      setSizeFull();

      Label spacer = new Label();
      spacer.setHeight(5, UNITS_PIXELS);

      HorizontalLayout leftMarginAndBody = new HorizontalLayout();
      leftMarginAndBody.setSizeFull();
      Label leftMarginSpace = new Label("");
      leftMarginSpace.setWidth(80, UNITS_PIXELS);
      leftMarginAndBody.addComponent(leftMarginSpace);

      if (artifact != null) {
         VerticalLayout bodyVertLayout = new VerticalLayout();

         breadcrumbComp.setArtifact(artifact);
         bodyVertLayout.addComponent(breadcrumbComp);

         OseeArtifactNameLinkComponent artifactName = new OseeArtifactNameLinkComponent(artifact);
         bodyVertLayout.addComponent(artifactName);

         Label artifactType = new Label(String.format("[%s]", artifact.getArtifactType()), Label.CONTENT_XHTML);
         bodyVertLayout.addComponent(artifactType);

         VerticalLayout artRelSpacer = new VerticalLayout();
         artRelSpacer.setHeight(15, UNITS_PIXELS);
         bodyVertLayout.addComponent(artRelSpacer);

         bodyVertLayout.addComponent(relationsComp);

         VerticalLayout relAttrSpacer = new VerticalLayout();
         relAttrSpacer.setHeight(15, UNITS_PIXELS);
         bodyVertLayout.addComponent(relAttrSpacer);

         bodyVertLayout.addComponent(attributeComp);

         VerticalLayout bottomSpacer = new VerticalLayout();
         bodyVertLayout.addComponent(bottomSpacer);
         bodyVertLayout.setExpandRatio(bottomSpacer, 1.0f);

         leftMarginAndBody.addComponent(bodyVertLayout);
         bodyVertLayout.setSizeFull();
         leftMarginAndBody.setExpandRatio(bodyVertLayout, 1.0f);
      }

      final VerticalLayout vertLayout = new VerticalLayout();
      vertLayout.addComponent(searchHeader);
      vertLayout.addComponent(spacer);
      vertLayout.setComponentAlignment(searchHeader, Alignment.TOP_LEFT);
      searchHeader.setWidth(100, UNITS_PERCENTAGE);
      searchHeader.setHeight(null);
      vertLayout.addComponent(leftMarginAndBody);
      vertLayout.setExpandRatio(leftMarginAndBody, 1.0f);

      vertLayout.setSizeFull();
      setCompositionRoot(vertLayout);
   }

   @Override
   public void init(Navigator navigator, Application application) {
      //Do nothing.
   }

   @Override
   public String getWarningForNavigatingFrom() {
      return null;
   }

   @Override
   public void clearAll() {
      this.artifact = null;
      createLayout();
   }

   @Override
   public void setArtifact(WebArtifact artifact) {
      this.artifact = artifact;
      createLayout();
   }

   @Override
   public void setErrorMessage(String message) {
      //TODO:
   }

   @Override
   public void navigateTo(String requestedDataId) {
      if (searchHeader != null) {
         searchHeader.createLayout();
      }
      createLayout();
   }
}
