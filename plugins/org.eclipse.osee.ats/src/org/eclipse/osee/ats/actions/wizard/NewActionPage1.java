/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/

package org.eclipse.osee.ats.actions.wizard;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osee.ats.core.config.ActionableItemArtifact;
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.config.TeamDefinitionManagerCore;
import org.eclipse.osee.ats.core.workflow.ActionableItemManagerCore;
import org.eclipse.osee.ats.help.ui.AtsHelpContext;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.shared.AtsArtifactTypes;
import org.eclipse.osee.ats.shared.AtsAttributeTypes;
import org.eclipse.osee.ats.util.widgets.dialog.AITreeContentProvider;
import org.eclipse.osee.ats.workflow.ATSXWidgetOptionResolver;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.plugin.util.HelpUtil;
import org.eclipse.osee.framework.ui.skynet.ArtifactLabelProvider;
import org.eclipse.osee.framework.ui.skynet.util.ArtifactNameSorter;
import org.eclipse.osee.framework.ui.skynet.util.filteredTree.OSEECheckedFilteredTree;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XText;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.util.XWidgetPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * @author Donald G. Dunne
 */
public class NewActionPage1 extends WizardPage {
   private final NewActionWizard wizard;
   private XWidgetPage page;
   protected OSEECheckedFilteredTree treeViewer;
   private static PatternFilter patternFilter = new PatternFilter();
   private Text descriptionLabel;
   private boolean debugPopulated = false;
   private static Artifact atsAi;

   protected NewActionPage1(NewActionWizard actionWizard) {
      super("Create new ATS Action", "Create ATS Action", null);
      setMessage("Enter title and select impacted items.");
      this.wizard = actionWizard;
   }

   private final XModifiedListener xModListener = new XModifiedListener() {
      @Override
      public void widgetModified(XWidget widget) {
         getContainer().updateButtons();
      }
   };

   protected String getWidgetXml() {
      return "<WorkPage>" + //
      "<XWidget displayName=\"Title\" required=\"true\" xwidgetType=\"XText\" toolTip=\"" + AtsAttributeTypes.Title.getDescription() + "\"/>" + //
      "</WorkPage>";
   }

   @Override
   public void createControl(Composite parent) {

      try {
         String xWidgetXml = getWidgetXml();
         Composite comp = new Composite(parent, SWT.NONE);
         comp.setLayout(new GridLayout(1, false));
         comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

         page = new XWidgetPage(xWidgetXml, ATSXWidgetOptionResolver.getInstance());
         page.createBody(null, comp, null, xModListener, true);

         ((XText) getXWidget("Title")).getLabelWidget().addListener(SWT.MouseUp, new Listener() {
            @Override
            public void handleEvent(Event event) {
               if (event.button == 3) {
                  handlePopulateWithDebugInfo();
               }
            }
         });

         Composite aiComp = new Composite(comp, SWT.NONE);
         aiComp.setLayout(new GridLayout(1, false));
         aiComp.setLayoutData(new GridData(GridData.FILL_BOTH));

         new Label(aiComp, SWT.NONE).setText("Select Actionable Items:");
         treeViewer =
            new OSEECheckedFilteredTree(aiComp,
               SWT.CHECK | SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, patternFilter);
         treeViewer.getViewer().getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         treeViewer.getViewer().setContentProvider(new AITreeContentProvider(Active.Active));
         treeViewer.getViewer().setLabelProvider(new ArtifactLabelProvider());
         try {
            treeViewer.getViewer().setInput(ActionableItemManagerCore.getTopLevelActionableItems(Active.Active));
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
         treeViewer.getViewer().setSorter(new ArtifactNameSorter());
         treeViewer.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
               getContainer().updateButtons();
            }
         });
         GridData gridData1 = new GridData(GridData.FILL_BOTH);
         gridData1.heightHint = 400;
         treeViewer.setLayoutData(gridData1);

         new Label(aiComp, SWT.NONE).setText("Description of highlighted Actionable Item (if any):");
         descriptionLabel = new Text(aiComp, SWT.BORDER | SWT.WRAP);
         gridData1 = new GridData(GridData.FILL_BOTH);
         gridData1.heightHint = 15;
         descriptionLabel.setLayoutData(gridData1);
         descriptionLabel.setEnabled(false);

         treeViewer.getViewer().addSelectionChangedListener(new SelectionChangedListener());

         Button deselectAll = new Button(aiComp, SWT.PUSH);
         deselectAll.setText("De-Select All");
         deselectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
               treeViewer.clearChecked();
            };
         });

         setControl(comp);
         setHelpContexts();
         if (wizard.getInitialAias() != null) {
            treeViewer.setInitalChecked(wizard.getInitialAias());
         }
         ((XText) getXWidget("Title")).setFocus();
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   private void handlePopulateWithDebugInfo() {
      if (debugPopulated) {
         return;
      }
      try {
         ((XText) getXWidget("Title")).set("tt");
         if (atsAi == null) {
            atsAi =
               ArtifactQuery.getArtifactFromTypeAndName(AtsArtifactTypes.ActionableItem, "ATS",
                  BranchManager.getCommonBranch());
         }
         treeViewer.getViewer().setSelection(new StructuredSelection(Arrays.asList(atsAi)));
         treeViewer.setInitalChecked(Arrays.asList(atsAi));
         getContainer().updateButtons();
         debugPopulated = true;
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   private class SelectionChangedListener implements ISelectionChangedListener {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         IStructuredSelection sel = (IStructuredSelection) treeViewer.getViewer().getSelection();
         if (sel.isEmpty()) {
            return;
         }
         ActionableItemArtifact aia = (ActionableItemArtifact) sel.getFirstElement();
         try {
            descriptionLabel.setText(aia.getSoleAttributeValue(AtsAttributeTypes.Description, ""));
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
         }
      }
   }

   private void setHelpContexts() {
      HelpUtil.setHelp(this.getControl(), AtsHelpContext.NEW_ACTION_PAGE_1);
   }

   public Set<ActionableItemArtifact> getSelectedActionableItemArtifacts() {
      Set<ActionableItemArtifact> selected = new HashSet<ActionableItemArtifact>();
      for (Object obj : treeViewer.getChecked()) {
         selected.add((ActionableItemArtifact) obj);
      }
      return selected;
   }

   public XWidget getXWidget(String attrName) throws OseeCoreException {
      Conditions.checkNotNull(page, "WorkPage");
      return page.getLayoutData(attrName).getXWidget();
   }

   @Override
   public boolean isPageComplete() {
      if (treeViewer.getChecked().isEmpty()) {
         return false;
      }
      try {
         for (ActionableItemArtifact aia : getSelectedActionableItemArtifacts()) {
            if (!aia.isActionable()) {
               AWorkbench.popup("ERROR", ActionableItemManagerCore.getNotActionableItemError(aia));
               return false;
            }
         }
         Collection<TeamDefinitionArtifact> teamDefs =
            TeamDefinitionManagerCore.getImpactedTeamDefs(getSelectedActionableItemArtifacts());
         if (teamDefs.isEmpty()) {
            AWorkbench.popup("ERROR", "No Teams Associated with selected Actionable Items");
            return false;
         }
      } catch (Exception ex) {
         AWorkbench.popup("ERROR", ex.getLocalizedMessage());
         return false;
      }
      if (!page.isPageComplete().isTrue()) {
         return false;
      }
      return true;
   }

}
