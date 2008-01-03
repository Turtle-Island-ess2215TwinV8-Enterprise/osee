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

package org.eclipse.osee.ats.editor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osee.ats.AtsPlugin;
import org.eclipse.osee.ats.artifact.ATSAttributes;
import org.eclipse.osee.ats.artifact.NoteItem;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.editor.service.ServicesArea;
import org.eclipse.osee.ats.util.AtsLib;
import org.eclipse.osee.ats.workflow.AtsWorkPage;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.ui.plugin.util.ALayout;
import org.eclipse.osee.framework.ui.plugin.util.Displays;
import org.eclipse.osee.framework.ui.skynet.XFormToolkit;
import org.eclipse.osee.framework.ui.skynet.artifact.annotation.AnnotationComposite;
import org.eclipse.osee.framework.ui.skynet.ats.IActionable;
import org.eclipse.osee.framework.ui.skynet.ats.OseeAts;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author Donald G. Dunne
 */
public class SMAWorkFlowTab extends FormPage implements IActionable {
   private SMAManager smaMgr;
   private ArrayList<SMAWorkFlowSection> sections = new ArrayList<SMAWorkFlowSection>();
   private XFormToolkit toolkit;
   private static String ORIGINATOR = "Originator:";
   private static String TEAM_ACTIONABLE_ITEMS = "Team Actionable Items: ";
   private static String ACTION_ACTIONABLE_ITEMS = "Action Actionable Items: ";
   private Label origLabel, teamActionableItemLabel, actionActionableItemsLabel;
   private List<AtsWorkPage> pages = new ArrayList<AtsWorkPage>();
   private ScrolledForm scrolledForm;
   private Integer HEADER_COMP_COLUMNS = 4;
   private static Map<String, Integer> guidToScrollLocation = new HashMap<String, Integer>();
   private final TeamWorkFlowArtifact teamWf;
   private SMARelationsComposite smaRelationsComposite;

   public SMAWorkFlowTab(SMAManager smaMgr) {
      super(smaMgr.getEditor(), "overview", "Workflow");
      this.smaMgr = smaMgr;
      toolkit = smaMgr.getEditor().getToolkit();
      if (smaMgr.getSma() instanceof TeamWorkFlowArtifact)
         teamWf = (TeamWorkFlowArtifact) smaMgr.getSma();
      else
         teamWf = null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
    */
   @Override
   protected void createFormContent(IManagedForm managedForm) {
      try {
         scrolledForm = managedForm.getForm();
         scrolledForm.addDisposeListener(new DisposeListener() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
             */
            public void widgetDisposed(DisposeEvent e) {
               storeScrollLocation();
            }
         });
         scrolledForm.setText(getEditorInput().getName());
         fillBody(managedForm);
         managedForm.refresh();

         ServicesArea toolbarArea = new ServicesArea(smaMgr);
         toolbarArea.createToolbarServices(scrolledForm.getToolBarManager());

         OseeAts.addButtonToEditorToolBar(smaMgr.getEditor(), this, AtsPlugin.getInstance(),
               scrolledForm.getToolBarManager(), SMAEditor.EDITOR_ID, "ATS Editor");

         if (smaMgr.getSma().getHelpContext() != null) AtsPlugin.getInstance().setHelp(scrolledForm,
               smaMgr.getSma().getHelpContext());

         scrolledForm.updateToolBar();
         // restoreScrollLocation();
      } catch (Exception ex) {
         OSEELog.logException(getClass(), ex, true);
      }
   }

   public void dispose() {
      for (SMAWorkFlowSection section : sections)
         section.dispose();
      toolkit.dispose();
   }

   public String getActionDescription() {
      return "Workflow Tab";
   }

   public final static String normalColor = "#EEEEEE";
   private final static String activeColor = "#9CCCFF";

   public String getHtml() {
      StringBuffer sb = new StringBuffer();
      for (WorkPage wPage : pages) {
         AtsWorkPage page = (AtsWorkPage) wPage;
         if (smaMgr.isCurrentState(page) || smaMgr.isStateVisited(page.getName())) {
            sb.append(page.getHtml(smaMgr.isCurrentState(page) ? activeColor : normalColor));
            sb.append(AHTML.newline());
         }
      }
      return sb.toString();
   }

   private void fillBody(IManagedForm managedForm) {
      Composite body = managedForm.getForm().getBody();
      GridLayout gridLayout = new GridLayout(1, false);
      body.setLayout(gridLayout);
      body.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, true, false));

      Composite headerComp = toolkit.createComposite(body);
      headerComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      headerComp.setLayout(ALayout.getZeroMarginLayout(1, false));
      // mainComp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));

      createTopLineHeader(headerComp, toolkit);
      createLatestHeader(headerComp, toolkit);
      createTeamWorkflowHeader(headerComp, toolkit);
      createNotesHeader(headerComp, toolkit);
      createAnnotationsHeader(headerComp, toolkit);

      sections.clear();
      pages.clear();

      // Display relations
      try {
         if (SMARelationsComposite.relationExists(smaMgr.getSma())) {
            smaRelationsComposite = new SMARelationsComposite(body, toolkit, SWT.NONE);
            smaRelationsComposite.create(smaMgr);
         }
      } catch (SQLException ex) {
         OSEELog.logException(AtsPlugin.class, ex, false);
      }

      // Only display current or past states
      for (WorkPage wPage : smaMgr.getSma().getWorkFlow().getPagesOrdered()) {
         AtsWorkPage page = (AtsWorkPage) wPage;
         if (smaMgr.isCurrentState(page) || smaMgr.isStateVisited(page.getName())) {
            // Don't show completed or cancelled state if not currently those state
            if (page.isCompletePage() && !smaMgr.isCompleted()) continue;
            if (page.isCancelledPage() && !smaMgr.isCancelled()) continue;
            SMAWorkFlowSection section = null;
            if (page.isCancelledPage())
               section = new SMAWorkFlowCancelledSection(body, toolkit, SWT.NONE, page, smaMgr);
            else if (page.isCompletePage())
               section = new SMAWorkFlowCompletedSection(body, toolkit, SWT.NONE, page, smaMgr);
            else {
               section = new SMAWorkFlowSection(body, toolkit, SWT.NONE, page, smaMgr);
            }
            control = section.getMainComp();
            sections.add(section);
            managedForm.addPart(section);
            pages.add(page);
         }
      }

      logSection = new SMAWorkFlowLogSection(body, toolkit, SWT.NONE, smaMgr);
      control = logSection.getMainComp();
      sections.add(logSection);
      managedForm.addPart(logSection);

      if (AtsPlugin.isAtsAdmin()) {
         SMAWorkFlowDebugSection section = new SMAWorkFlowDebugSection(body, toolkit, SWT.NONE, smaMgr);
         control = section.getMainComp();
         sections.add(section);
         managedForm.addPart(section);
         section.getSection().setExpanded(true);
      }

      body.setFocus();
      // Jump to scroll location if set
      Integer selection = guidToScrollLocation.get(smaMgr.getSma().getGuid());
      if (selection != null) {
         JumpScrollbarJob job = new JumpScrollbarJob("");
         job.schedule(500);
      }
   }

   private Control control = null;
   private SMAWorkFlowLogSection logSection;

   private void storeScrollLocation() {
      if (scrolledForm != null) {
         Integer selection = scrolledForm.getVerticalBar().getSelection();
         // System.out.println("Storing selection => " + selection);
         guidToScrollLocation.put(smaMgr.getSma().getGuid(), selection);
      }
   }

   private class JumpScrollbarJob extends Job {
      public JumpScrollbarJob(String name) {
         super(name);
      }

      @Override
      protected IStatus run(IProgressMonitor monitor) {
         Displays.ensureInDisplayThread(new Runnable() {
            public void run() {
               Integer selection = guidToScrollLocation.get(smaMgr.getSma().getGuid());
               // System.out.println("Restoring selection => " + selection);

               // Find the ScrolledComposite operating on the control.
               ScrolledComposite sComp = null;
               if (control == null || control.isDisposed()) return;
               Composite parent = control.getParent();
               while (parent != null) {
                  if (parent instanceof ScrolledComposite) {
                     sComp = (ScrolledComposite) parent;
                     break;
                  }
                  parent = parent.getParent();
               }

               if (sComp != null) {
                  sComp.setOrigin(0, selection);
               }
            }
         });
         return Status.OK_STATUS;

      }
   }

   private void createTeamWorkflowHeader(Composite comp, XFormToolkit toolkit) {
      try {
         if (!(smaMgr.getSma() instanceof TeamWorkFlowArtifact)) return;

         Composite actionComp = new Composite(comp, SWT.NONE);
         toolkit.adapt(actionComp);
         actionComp.setLayout(ALayout.getZeroMarginLayout(2, false));
         GridData gd = new GridData(GridData.FILL_HORIZONTAL);
         gd.horizontalSpan = 4;
         actionComp.setLayoutData(gd);

         // Show Action's AIs
         if (!smaMgr.isCancelled() && !smaMgr.isCompleted()) {
            Hyperlink link = toolkit.createHyperlink(actionComp, ACTION_ACTIONABLE_ITEMS, SWT.NONE);
            link.addHyperlinkListener(new IHyperlinkListener() {

               public void linkEntered(HyperlinkEvent e) {
               }

               public void linkExited(HyperlinkEvent e) {
               }

               public void linkActivated(HyperlinkEvent e) {
                  try {
                     AtsLib.editActionActionableItems(teamWf.getParentActionArtifact());
                  } catch (Exception ex) {
                     OSEELog.logException(AtsPlugin.class, ex, true);
                  }
               }
            });
            link.setToolTipText("Edit Actionable Items for the parent Action (this may add Team Workflows)");
            if (teamWf.getParentActionArtifact().getActionableItemsDam().getActionableItems().size() == 0) {
               Label errorLabel = toolkit.createLabel(actionComp, "Error: No Actionable Items identified.");
               errorLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
            } else {
               actionActionableItemsLabel =
                     toolkit.createLabel(actionComp,
                           teamWf.getParentActionArtifact().getActionableItemsDam().getActionableItemsStr());
               actionActionableItemsLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            }
         } else {
            if (teamWf.getParentActionArtifact().getActionableItemsDam().getActionableItems().size() == 0) {
               Label errorLabel = toolkit.createLabel(actionComp, "Error: No Actionable Items identified.");
               errorLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
            } else {
               Label label =
                     toolkit.createLabel(
                           actionComp,
                           ACTION_ACTIONABLE_ITEMS + teamWf.getParentActionArtifact().getActionableItemsDam().getActionableItemsStr());
               label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            }
         }

         Composite teamComp = new Composite(comp, SWT.NONE);
         toolkit.adapt(teamComp);
         teamComp.setLayout(ALayout.getZeroMarginLayout(2, false));
         gd = new GridData(GridData.FILL_HORIZONTAL);
         gd.horizontalSpan = 4;
         teamComp.setLayoutData(gd);

         // Show Team Workflow's AIs
         if (!smaMgr.isCancelled() && !smaMgr.isCompleted()) {
            Hyperlink link = toolkit.createHyperlink(teamComp, TEAM_ACTIONABLE_ITEMS, SWT.NONE);
            link.addHyperlinkListener(new IHyperlinkListener() {

               public void linkEntered(HyperlinkEvent e) {
               }

               public void linkExited(HyperlinkEvent e) {
               }

               public void linkActivated(HyperlinkEvent e) {
                  try {
                     AtsLib.editTeamActionableItems(teamWf);
                  } catch (Exception ex) {
                     OSEELog.logException(AtsPlugin.class, ex, true);
                  }
               }

            });
            link.setToolTipText("Edit Actionable Items for this Team Workflow");
            if (teamWf.getActionableItemsDam().getActionableItems().size() == 0) {
               Label errorLabel = toolkit.createLabel(teamComp, "Error: No Actionable Items identified.");
               errorLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
            } else {
               teamActionableItemLabel =
                     toolkit.createLabel(teamComp, teamWf.getActionableItemsDam().getActionableItemsStr());
               teamActionableItemLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            }
         } else {
            if (teamWf.getActionableItemsDam().getActionableItems().size() == 0) {
               Label errorLabel = toolkit.createLabel(teamComp, "Error: No Actionable Items identified.");
               errorLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
            } else {
               Label label =
                     toolkit.createLabel(teamComp,
                           TEAM_ACTIONABLE_ITEMS + teamWf.getActionableItemsDam().getActionableItemsStr());
               label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            }
         }
      } catch (Exception ex) {
         OSEELog.logException(AtsPlugin.class, ex, false);
      }
   }

   private void createTopLineHeader(Composite comp, XFormToolkit toolkit) {
      Composite topLineComp = new Composite(comp, SWT.NONE);
      topLineComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      topLineComp.setLayout(ALayout.getZeroMarginLayout(8, false));
      toolkit.adapt(topLineComp);
      // mainComp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));

      toolkit.createLabel(topLineComp, smaMgr.getEditorHeaderString(), SWT.NO_TRIM);
      createOriginatorHeader(topLineComp, toolkit);

      try {
         toolkit.createLabel(topLineComp, "Action Id:");
         Text text = new Text(topLineComp, SWT.NONE);
         toolkit.adapt(text, true, true);
         text.setText(smaMgr.getSma().getParentActionArtifact() == null ? "??" : smaMgr.getSma().getParentActionArtifact().getHumanReadableId());
      } catch (SQLException ex) {
         // Do nothing
      }

      toolkit.createLabel(topLineComp, smaMgr.getSma().getArtifactSuperTypeName() + " Id:");
      Text text = new Text(topLineComp, SWT.NONE);
      toolkit.adapt(text, true, true);
      text.setText(smaMgr.getSma().getHumanReadableId());

   }

   private void createLatestHeader(Composite comp, XFormToolkit toolkit) {
      if (smaMgr.isHistoricalVersion()) {
         Label label =
               toolkit.createLabel(
                     comp,
                     "This is a historical version of this " + smaMgr.getSma().getArtifactTypeName() + " and can not be edited; Select \"Open Latest\" to view/edit latest version.");
         label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
      }
   }

   private void createAnnotationsHeader(Composite comp, XFormToolkit toolkit) {
      if (smaMgr.getSma().getAnnotations().size() > 0) {
         new AnnotationComposite(toolkit, comp, SWT.None, smaMgr.getSma());
      }
   }

   private void createNotesHeader(Composite comp, XFormToolkit toolkit) {
      // Display SMA Note
      String note = smaMgr.getSma().getSoleAttributeValue(ATSAttributes.SMA_NOTE_ATTRIBUTE.getStoreName());
      if (!note.equals("")) {
         Label label = toolkit.createLabel(comp, "Note: " + note);
         GridData gd = new GridData(GridData.FILL_HORIZONTAL);
         gd.horizontalSpan = HEADER_COMP_COLUMNS;
         label.setLayoutData(gd);
      }
      // Display global Notes
      for (NoteItem noteItem : smaMgr.getSma().getNotes().getNoteItems()) {
         if (noteItem.getState().equals("")) {
            Label label = toolkit.createLabel(comp, noteItem.toHTML());
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = HEADER_COMP_COLUMNS;
            label.setLayoutData(gd);
         }
      }
   }

   private void createOriginatorHeader(Composite comp, XFormToolkit toolkit) {
      if (!smaMgr.isCancelled() && !smaMgr.isCompleted()) {
         toolkit.createLabel(comp, "     ");
         Hyperlink link = toolkit.createHyperlink(comp, ORIGINATOR, SWT.NONE);
         link.addHyperlinkListener(new IHyperlinkListener() {

            public void linkEntered(HyperlinkEvent e) {
            }

            public void linkExited(HyperlinkEvent e) {
            }

            public void linkActivated(HyperlinkEvent e) {
               if (smaMgr.promptChangeOriginator()) {
                  updateOrigLabel();
                  smaMgr.getEditor().onDirtied();
               }
            }

         });
         if (smaMgr.getOriginator() == null) {
            Label errorLabel = toolkit.createLabel(comp, "Error: No originator identified.");
            errorLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
         } else {
            origLabel = toolkit.createLabel(comp, smaMgr.getOriginator().getName());
            origLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         }
      } else {
         if (smaMgr.getOriginator() == null) {
            Label errorLabel = toolkit.createLabel(comp, "Error: No originator identified.");
            errorLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
         } else {
            Label origLabel = toolkit.createLabel(comp, "     " + ORIGINATOR + smaMgr.getOriginator().getName());
            origLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         }
      }
   }

   public void updateOrigLabel() {
      origLabel.setText(smaMgr.getOriginator().getName());
      origLabel.getParent().layout();
      if (teamWf != null) {
         teamActionableItemLabel.setText(teamWf.getActionableItemsDam().getActionableItemsStr());
         actionActionableItemsLabel.setText(teamWf.getActionableItemsDam().getActionableItemsStr());
      }
   }

   public void refresh() {
      for (SMAWorkFlowSection section : sections)
         section.refresh();
      if (smaRelationsComposite != null) smaRelationsComposite.refresh();
   }

}