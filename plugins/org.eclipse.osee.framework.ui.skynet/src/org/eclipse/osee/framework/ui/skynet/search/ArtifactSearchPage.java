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
package org.eclipse.osee.framework.ui.skynet.search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.NamedIdentity;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.help.ui.OseeHelpContext;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationTypeManager;
import org.eclipse.osee.framework.ui.plugin.util.HelpUtil;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.search.filter.FilterModel;
import org.eclipse.osee.framework.ui.skynet.search.filter.FilterModelList;
import org.eclipse.osee.framework.ui.skynet.search.filter.FilterTableViewer;
import org.eclipse.osee.framework.ui.skynet.search.page.AbstractArtifactSearchViewPage;
import org.eclipse.osee.framework.ui.skynet.util.DbConnectionExceptionComposite;
import org.eclipse.osee.framework.ui.skynet.widgets.XBranchSelectWidget;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.HyperLinkLabel;
import org.eclipse.search.ui.IReplacePage;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/**
 * @author Michael S. Rodgers
 */
public class ArtifactSearchPage extends DialogPage implements ISearchPage, IReplacePage {
   private static final Pattern storageStringPattern = Pattern.compile("(.*?);(.*?);(.*?);(.*)");
   private static final Pattern notSearchPrimitivePattern = Pattern.compile("Not \\[(.*)\\]");
   private static final String FILTERS_STORAGE_KEY = ".filters";

   private static ISearchPageContainer aContainer;

   private Button addButton;
   private Button allButton;
   private Button atLeastOneButton;
   private ComboViewer searchTypeList;
   private Button notButton;

   private StackLayout selectionLayout;
   private static FilterTableViewer filterviewer;
   private Composite artifactTypeControls;
   private ListViewer artifactTypeList;

   private XBranchSelectWidget branchSelect;

   private SearchFilter HRID_VALUE_FILTER;
   private SearchFilter ATTRIBUTE_VALUE_FILTER;
   private static int lastSearchTypeListSelected = 2; // Attribute
   private static int lastAttributeTypeListSelected = 0; // Name

   private final Matcher storageStringMatcher = storageStringPattern.matcher("");
   private final Matcher notSearchPrimitiveMatcher = notSearchPrimitivePattern.matcher("");

   @Override
   public void createControl(Composite parent) {
      initializeDialogUnits(parent);
      boolean isConnectionOk = DbConnectionExceptionComposite.dbConnectionIsOk(parent);
      if (isConnectionOk != false) {
         Composite mainComposite = new Composite(parent, SWT.NONE);
         mainComposite.setFont(parent.getFont());
         mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         mainComposite.setLayout(new GridLayout());

         branchSelect = new XBranchSelectWidget("Branch To Search");
         branchSelect.setDisplayLabel(false);
         branchSelect.setSelection(BranchManager.getLastBranch());
         branchSelect.createWidgets(mainComposite, 2);

         addFilterControls(mainComposite);
         addTableControls(mainComposite);
         addSearchScope(mainComposite);
         addFilterListeners();

         setControl(parent);
         aContainer.setPerformActionEnabled(false);

         HelpUtil.setHelp(mainComposite, OseeHelpContext.ARTIFACT_SEARCH);

         updateWidgets();

         loadState();
      } else {
         setControl(parent);
      }
   }

   private IOseeBranch getSelectedBranch() {
      IOseeBranch branch = branchSelect.getData();
      if (branch == null) {
         branch = BranchManager.getLastBranch();
      }
      try {
         if (branch == null) {
            branch = BranchManager.getCommonBranch();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return branch;
   }

   /**
    * Controls to allow the user to select wether all the filters are combined using AND or OR
    */
   private void addSearchScope(Composite composite) {
      Group allSelectionGroup = new Group(composite, SWT.NONE);
      allSelectionGroup.setText("Artifacts that match");
      allSelectionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

      allSelectionGroup.setLayout(new GridLayout(2, false));

      allButton = new Button(allSelectionGroup, SWT.RADIO);
      allButton.setText("All filters (AND)");
      allButton.setSelection(true);

      atLeastOneButton = new Button(allSelectionGroup, SWT.RADIO);
      atLeastOneButton.setText("At least one filter (OR)");
   }

   private void createArtifactTypeSearchControls(Composite optionsComposite) {
      artifactTypeControls = new Composite(optionsComposite, SWT.NONE);
      artifactTypeControls.setLayout(new GridLayout(1, true));

      artifactTypeList = new ListViewer(artifactTypeControls);
      GridData gd = new GridData();
      gd.heightHint = 100;
      artifactTypeList.getList().setLayoutData(gd);
      artifactTypeList.setContentProvider(new SearchContentProvider());
      artifactTypeList.setLabelProvider(new SearchLabelProvider());
      artifactTypeList.setSorter(new SearchSorter());

      try {
         for (IArtifactType artType : ArtifactTypeManager.getValidArtifactTypes(getSelectedBranch())) {
            artifactTypeList.add(artType);
            artifactTypeList.setData(artType.getName(), artType);
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Error encountered while getting list of artifact types",
            ex);
      }
      addToSearchTypeList(new ArtifactTypeFilter(artifactTypeControls, artifactTypeList));
   }

   private void createOrphanSearchControls(Composite optionsComposite) {
      //uses the artifactTypeList from Artifact_type_filter
      artifactTypeList.getList().select(0);
      addToSearchTypeList(new OrphanSearchFilter("Orphan Search", artifactTypeControls, artifactTypeList));
   }

   private void addToSearchTypeList(SearchFilter filter) {
      searchTypeList.add(filter.getFilterName());
      searchTypeList.setData(filter.getFilterName(), filter);
   }

   private void createRelationSearchControls(Composite optionsComposite) {
      Composite relationControls = new Composite(optionsComposite, SWT.NONE);
      relationControls.setLayout(new GridLayout(2, true));

      final ComboViewer relationTypeList = new ComboViewer(relationControls, SWT.DROP_DOWN | SWT.READ_ONLY);
      relationTypeList.setContentProvider(new SearchContentProvider());
      relationTypeList.setLabelProvider(new SearchLabelProvider());
      relationTypeList.setSorter(new SearchSorter());
      final ComboViewer relationSideList = new ComboViewer(relationControls, SWT.DROP_DOWN | SWT.READ_ONLY);
      relationSideList.setContentProvider(new SearchContentProvider());
      relationSideList.setLabelProvider(new StringSearchLabelProvider());
      relationSideList.setSorter(new SearchSorter());

      try {
         for (RelationType linkDescriptor : RelationTypeManager.getValidTypes(getSelectedBranch())) {
            relationTypeList.add(linkDescriptor);
            relationTypeList.setData(linkDescriptor.getName(), linkDescriptor);
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

      relationTypeList.getCombo().addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            relationSideList.getCombo().removeAll();
            RelationType linkDescriptor =
               (RelationType) relationTypeList.getData(relationTypeList.getCombo().getText());
            relationSideList.add(linkDescriptor.getSideAName());
            relationSideList.add(linkDescriptor.getSideBName());
            relationSideList.getCombo().select(0);
         }
      });
      relationTypeList.getCombo().setVisibleItemCount(Math.min(relationTypeList.getCombo().getItemCount(), 15));

      if (relationTypeList.getCombo().getItemCount() > 0) { // ensure we don't get a null pointer
         // exception when there are no relation types in the db
         relationTypeList.getCombo().select(0);
         RelationType linkDescriptor = (RelationType) relationTypeList.getData(relationTypeList.getCombo().getText());
         relationSideList.add(linkDescriptor.getSideAName());
         relationSideList.add(linkDescriptor.getSideBName());
         relationSideList.getCombo().select(0);
      }

      addToSearchTypeList(new InRelationFilter(relationControls, relationTypeList, relationSideList));
   }

   private void createHridSearchControls(Composite optionsComposite) {
      Composite hridControls = new Composite(optionsComposite, SWT.NONE);
      hridControls.setLayout(new GridLayout(2, false));

      Label typeLabel = new Label(hridControls, SWT.HORIZONTAL);
      typeLabel.setText("Human Readable ID:");
      Text hridValue = new Text(hridControls, SWT.BORDER);
      hridValue.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

      hridValue.addModifyListener(new ModifyListener() {
         @Override
         public void modifyText(ModifyEvent e) {
            addButton.setEnabled(HRID_VALUE_FILTER.isValid());
         }
      });

      new Label(hridControls, SWT.NONE);

      Label wildLabel = new Label(hridControls, SWT.NONE);
      wildLabel.setText("(* = any string, \\* = literal *)");

      HRID_VALUE_FILTER = new HridValueFilter(hridControls, hridValue);
      addToSearchTypeList(HRID_VALUE_FILTER);
   }

   private void createAttributeSearchControls(Composite optionsComposite) {
      Composite attributeControls = new Composite(optionsComposite, SWT.NONE);
      attributeControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
      attributeControls.setLayout(new GridLayout(2, false));

      Label typeLabel = new Label(attributeControls, SWT.HORIZONTAL);
      typeLabel.setText("Attribute Type:");

      final ComboViewer attributeTypeList = new ComboViewer(attributeControls, SWT.DROP_DOWN | SWT.READ_ONLY);
      attributeTypeList.setContentProvider(new SearchContentProvider());
      attributeTypeList.setLabelProvider(new SearchLabelProvider());
      attributeTypeList.setSorter(new SearchSorter());

      Label valueLabel = new Label(attributeControls, SWT.HORIZONTAL);
      valueLabel.setText("Attribute Value:");

      Text attributeValue = new Text(attributeControls, SWT.BORDER);
      attributeValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

      try {
         for (IAttributeType type : AttributeTypeManager.getValidAttributeTypes(getSelectedBranch())) {
            attributeTypeList.add(type);
            attributeTypeList.setData(type.getName(), type);
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP,
            "Error encountered while getting list of attribute types", ex);
      }
      attributeTypeList.getCombo().setVisibleItemCount(Math.min(attributeTypeList.getCombo().getItemCount(), 15));
      attributeTypeList.getCombo().select(lastAttributeTypeListSelected);
      attributeTypeList.addSelectionChangedListener(new ISelectionChangedListener() {
         @Override
         public void selectionChanged(SelectionChangedEvent event) {
            lastAttributeTypeListSelected = attributeTypeList.getCombo().getSelectionIndex();
         }
      });

      attributeValue.addModifyListener(new ModifyListener() {
         @Override
         public void modifyText(ModifyEvent e) {
            addButton.setEnabled(ATTRIBUTE_VALUE_FILTER.isValid());
         }
      });

      new Label(attributeControls, SWT.NONE); // spacerLabelSoTheNextOneWillBeInColumnTwo

      Label wildLabel = new Label(attributeControls, SWT.NONE);
      wildLabel.setText("(* = any string, \\* = literal *)");

      ATTRIBUTE_VALUE_FILTER = new AttributeValueFilter(attributeControls, attributeTypeList, attributeValue);
      addToSearchTypeList(ATTRIBUTE_VALUE_FILTER);
   }

   private void addFilterControls(Composite mainComposite) {
      Group filterGroup = new Group(mainComposite, SWT.NONE);
      filterGroup.setText("Create a Filter");
      filterGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
      filterGroup.setLayout(new GridLayout());

      Composite composite = new Composite(filterGroup, SWT.BORDER);
      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
      composite.setLayout(new GridLayout(2, false));

      searchTypeList = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
      searchTypeList.setContentProvider(new SearchContentProvider());
      searchTypeList.setLabelProvider(new StringSearchLabelProvider());
      searchTypeList.setSorter(new SearchSorter());

      notButton = new Button(composite, SWT.CHECK);
      notButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, false));
      notButton.setText("Not Equal");

      selectionLayout = new StackLayout();

      Composite optionsComposite = new Composite(filterGroup, SWT.BORDER);
      optionsComposite.setLayout(new GridLayout());
      optionsComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

      optionsComposite.setLayout(selectionLayout);
      createAttributeSearchControls(optionsComposite);
      createArtifactTypeSearchControls(optionsComposite);
      createOrphanSearchControls(optionsComposite);
      createRelationSearchControls(optionsComposite);
      createHridSearchControls(optionsComposite);

      searchTypeList.getCombo().setVisibleItemCount(7);
      searchTypeList.getCombo().select(lastSearchTypeListSelected);
      searchTypeList.addSelectionChangedListener(new ISelectionChangedListener() {
         @Override
         public void selectionChanged(SelectionChangedEvent event) {
            lastSearchTypeListSelected = searchTypeList.getCombo().getSelectionIndex();
         }
      });
      addButton = new Button(filterGroup, SWT.PUSH);
      addButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));
      addButton.setText("Add Filter");
   }

   private void addFilterListeners() {
      addButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            SearchFilter searchFilter = (SearchFilter) searchTypeList.getData(searchTypeList.getCombo().getText());
            searchFilter.setNot(notButton.getSelection());
            searchFilter.addFilterTo(filterviewer);
            updateOKStatus();
         }
      });

      searchTypeList.getCombo().addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            updateWidgets();
         }
      });
   }

   private void updateWidgets() {
      SearchFilter searchFilter = (SearchFilter) searchTypeList.getData(searchTypeList.getCombo().getText());
      addButton.setEnabled(searchFilter.isValid());
      selectionLayout.topControl = searchFilter.optionsControl;
      selectionLayout.topControl.getParent().layout();
   }

   private void addTableControls(Composite composite) {
      Composite filterComposite = new Composite(composite, SWT.NONE);
      filterComposite.setFont(composite.getFont());
      filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      filterComposite.setLayout(new GridLayout(2, false));

      Label tableLabel = new Label(filterComposite, SWT.FILL);
      tableLabel.setText("Filters    ");

      HyperLinkLabel clearAllLabel = new HyperLinkLabel(filterComposite, SWT.NONE);
      clearAllLabel.setText("clear all");
      clearAllLabel.addListener(SWT.MouseUp, new Listener() {
         @Override
         public void handleEvent(Event event) {
            for (FilterModel filterModel : new CopyOnWriteArrayList<FilterModel>(
               filterviewer.getFilterList().getFilters())) {
               filterviewer.removeFilter(filterModel);
            }
            filterviewer.refresh();
         }
      });

      Table table = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.HIDE_SELECTION);
      filterviewer = new FilterTableViewer(table);
      GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
      gridData.heightHint = 100;
      gridData.widthHint = 500;
      table.setLayoutData(gridData);
   }

   @Override
   public void setContainer(ISearchPageContainer container) {
      ArtifactSearchPage.aContainer = container;
   }

   /**
    * @return Returns the aContainer.
    */
   public static ISearchPageContainer getContainer() {
      return aContainer;
   }

   @Override
   public boolean performAction() {
      NewSearchUI.activateSearchResultView();
      filterviewer.getFilterList().setAllSelected(allButton.getSelection());
      AbstractArtifactSearchQuery searchQuery =
         new FilterArtifactSearchQuery(filterviewer.getFilterList(), getSelectedBranch());
      NewSearchUI.runQueryInBackground(searchQuery);
      saveState();
      return true;
   }

   @Override
   public boolean performReplace() {
      filterviewer.getFilterList().setAllSelected(allButton.getSelection());
      AbstractArtifactSearchQuery searchQuery =
         new FilterArtifactSearchQuery(filterviewer.getFilterList(), getSelectedBranch());

      IStatus status = NewSearchUI.runQueryInForeground(getContainer().getRunnableContext(), searchQuery);
      if (status.matches(IStatus.CANCEL)) {
         return false;
      }

      ISearchResultViewPart view = NewSearchUI.activateSearchResultView();
      if (view != null) {
         final ISearchResultPage page = view.getActivePage();
         if (page instanceof AbstractArtifactSearchViewPage) {
            runAttributeFindReplaceDialog(page);
         }
      }
      return true;
   }

   private void runAttributeFindReplaceDialog(final ISearchResultPage page) {
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            if (page instanceof AbstractArtifactSearchViewPage) {
               AbstractArtifactSearchViewPage artifactPage = (AbstractArtifactSearchViewPage) page;
               List<Artifact> artifacts = artifactPage.getInput().getArtifactResults();
               new AttributeFindReplaceDialog(page.getSite().getShell(), artifacts).open();
            }
         }
      });
   }

   /*
    * Implements method from IDialogPage
    */
   @Override
   public void setVisible(boolean visible) {
      updateOKStatus();
      super.setVisible(visible);
   }

   public static void updateOKStatus() {
      if (filterviewer == null || filterviewer.getFilterList().getFilters().isEmpty()) {
         getContainer().setPerformActionEnabled(false);
      } else {
         getContainer().setPerformActionEnabled(true);
      }
   }

   private String asString(FilterModel model) {
      StringBuilder builder = new StringBuilder();
      builder.append(model.getSearch());
      builder.append(";");
      builder.append(model.getType());
      builder.append(";");
      builder.append(model.getValue());
      builder.append(";");
      builder.append(model.getSearchPrimitive().getStorageString());
      return builder.toString();
   }

   private void processStoredFilter(String entry) {
      storageStringMatcher.reset(entry);
      if (storageStringMatcher.find()) {
         String searchPrimitive = storageStringMatcher.group(1);
         String type = storageStringMatcher.group(2);
         String value = storageStringMatcher.group(3);
         String storageString = storageStringMatcher.group(4);
         boolean isNotEnabled = false;
         notSearchPrimitiveMatcher.reset(storageString);
         if (notSearchPrimitiveMatcher.find()) {
            isNotEnabled = true;
            storageString = notSearchPrimitiveMatcher.group(1);
         }
         SearchFilter searchFilter = (SearchFilter) searchTypeList.getData(searchPrimitive);
         searchFilter.loadFromStorageString(filterviewer, type, value, storageString, isNotEnabled);
         searchFilter.getFilterName();
      }
   }

   protected void saveState() {
      IDialogSettings dialogSettings = Activator.getInstance().getDialogSettings();
      if (dialogSettings != null) {

         List<String> filterString = new ArrayList<String>();
         FilterModelList filterList = filterviewer.getFilterList();
         for (FilterModel model : filterList.getFilters()) {
            filterString.add(asString(model));
         }
         dialogSettings.put(Activator.PLUGIN_ID + FILTERS_STORAGE_KEY,
            filterString.toArray(new String[filterString.size()]));
      }
   }

   protected void loadState() {
      IDialogSettings dialogSettings = Activator.getInstance().getDialogSettings();
      if (dialogSettings != null) {
         String[] filters = dialogSettings.getArray(Activator.PLUGIN_ID + FILTERS_STORAGE_KEY);
         if (filters != null) {
            for (String entry : filters) {
               processStoredFilter(entry);
            }
         }
      }
   }

   public class SearchLabelProvider implements ILabelProvider {

      @Override
      public Image getImage(Object arg0) {
         return null;
      }

      @Override
      public String getText(Object obj) {
         try {
            NamedIdentity<Long> identity = (NamedIdentity<Long>) obj;
            return identity.getName();
         } catch (Exception ex) {
            ;
            return "";
         }
      }

      @Override
      public void addListener(ILabelProviderListener arg0) {
         // do nothing
      }

      @Override
      public void dispose() {
         // do nothing
      }

      @Override
      public boolean isLabelProperty(Object arg0, String arg1) {
         return false;
      }

      @Override
      public void removeListener(ILabelProviderListener arg0) {
         // do nothing
      }
   }

   public class StringSearchLabelProvider implements ILabelProvider {

      @Override
      public Image getImage(Object arg0) {
         return null;
      }

      @Override
      public String getText(Object obj) {
         return (String) obj;
      }

      @Override
      public void addListener(ILabelProviderListener arg0) {
         // do nothing
      }

      @Override
      public void dispose() {
         // do nothing
      }

      @Override
      public boolean isLabelProperty(Object arg0, String arg1) {
         return false;
      }

      @Override
      public void removeListener(ILabelProviderListener arg0) {
         // do nothing
      }
   }

   public class SearchContentProvider implements IStructuredContentProvider {
      @Override
      public Object[] getElements(Object arg0) {
         return ((ArrayList<?>) arg0).toArray();
      }

      @Override
      public void dispose() {
         // do nothing
      }

      @Override
      public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
         // do nothing
      }
   }

   public class SearchSorter extends ViewerSorter {
      @SuppressWarnings("unchecked")
      @Override
      public int compare(Viewer viewer, Object e1, Object e2) {
         return getComparator().compare(e1.toString(), e2.toString());
      }
   }
}
