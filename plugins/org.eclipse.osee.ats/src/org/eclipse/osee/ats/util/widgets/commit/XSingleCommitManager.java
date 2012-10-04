/*
 * Created on Sep 21, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.commit;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.core.client.branch.AtsBranchManagerCore;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowManager;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.filter.IEventFilter;
import org.eclipse.osee.framework.skynet.core.event.listener.IBranchEventListener;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.Sender;
import org.eclipse.osee.framework.ui.skynet.widgets.GenericXWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.IArtifactWidget;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class XSingleCommitManager extends GenericXWidget implements IArtifactWidget, IBranchEventListener {

   public static final String WIDGET_NAME = "XSingleCommitManager";
   public static final String NAME = "Single Commit Manager";
   public static final String DESCRIPTION = "Commit a single branch to the targets destination branch.";
   private TeamWorkFlowArtifact teamArt;
   private Composite mainComp;
   private Composite parentComp;
   private Label extraInfoLabel;

   public XSingleCommitManager() {
      super(NAME);
      OseeEventManager.addListener(this);
   }

   @Override
   public Artifact getArtifact() {
      return teamArt;
   }

   @Override
   public void saveToArtifact() throws OseeCoreException {
   }

   @Override
   public void revert() throws OseeCoreException {
   }

   @Override
   public Result isDirty() {
      return Result.FalseResult;
   }

   @Override
   public List<? extends IEventFilter> getEventFilters() {
      return null;
   }

   @Override
   public void handleBranchEvent(Sender sender, BranchEvent branchEvent) {
   }

   @Override
   public void setArtifact(Artifact artifact) throws OseeCoreException {
      if (!(artifact.isOfType(AtsArtifactTypes.TeamWorkflow))) {
         throw new OseeStateException("Must be TeamWorkflowArtifact, set was a [%s]", artifact.getArtifactTypeName());
      }
      this.teamArt = TeamWorkFlowManager.cast(artifact);
      //loadTable();
   }

   @Override
   public Control getControl() {
      return parentComp;
   }

   @Override
   protected void createControls(Composite parent, int horizontalSpan) {
      parentComp = new Composite(parent, SWT.FLAT);
      parentComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      parentComp.setLayout(ALayout.getZeroMarginLayout());
      Composite buttonBar = new Composite(parentComp, SWT.NONE);
      buttonBar.setLayoutData(new GridData(SWT.None, SWT.None, false, false));
      buttonBar.setLayout(new GridLayout(3, false));
      toolkit.adapt(buttonBar);

      Button button = toolkit.createButton(buttonBar, "Commit", SWT.PUSH);
      button.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            CommitPortOperation operation = new CommitPortOperation(teamArt);
            try {
               operation.doWork(null);
            } catch (Exception ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });

      button = toolkit.createButton(buttonBar, "Merge", SWT.PUSH);
      button.setGrayed(true);
   }

   public void handleCommit() {
      try {
         if (teamArt != null) {
            Collection<Object> commitMgrInputObjs =
               AtsBranchManagerCore.getCommitTransactionsAndConfigItemsForTeamWf(teamArt);
            System.out.println(commitMgrInputObjs.size());
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }
}
