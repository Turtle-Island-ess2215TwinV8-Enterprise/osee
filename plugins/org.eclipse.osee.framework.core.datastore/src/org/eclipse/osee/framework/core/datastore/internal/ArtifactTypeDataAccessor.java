/*
 * Created on Aug 17, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.core.datastore.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import org.eclipse.osee.framework.core.message.OseeImportModelRequest;
import org.eclipse.osee.framework.core.message.OseeImportModelResponse;
import org.eclipse.osee.framework.core.model.AbstractOseeType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.cache.BranchCache;
import org.eclipse.osee.framework.core.model.cache.IOseeCache;
import org.eclipse.osee.framework.core.model.cache.IOseeDataAccessor;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.RemoteIdManager;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.resource.management.IResource;
import org.eclipse.osee.framework.resource.management.IResourceLocator;
import org.eclipse.osee.framework.resource.management.IResourceLocatorManager;
import org.eclipse.osee.framework.resource.management.IResourceManager;
import org.eclipse.osee.framework.resource.management.StandardOptions;

public class ArtifactTypeDataAccessor<T extends AbstractOseeType<Long>> implements IOseeDataAccessor<Long, T> {

   private static final String LOAD_OSEE_TYPE_DEF_URIS =
      "select attr.uri from osee_txs txs1, osee_artifact art, osee_attribute attr, osee_txs txs2 where txs1.branch_id = ? and txs1.tx_current = ? and txs1.gamma_id = art.gamma_id and txs2.branch_id = ? and txs2.tx_current = ? and txs2.gamma_id = attr.gamma_id and art.art_type_id = ? and art.art_id = attr.art_id and attr.attr_type_id = ?";

   private static volatile boolean wasLoaded;

   private final ModelingServiceProvider modelService;
   private final IOseeDatabaseService databaseService;
   private final IResourceLocatorManager locatorManager;
   private final IResourceManager resourceManager;
   private final BranchCache branchCache;

   public ArtifactTypeDataAccessor(ModelingServiceProvider modelService, IOseeDatabaseService databaseService, IResourceLocatorManager locatorManager, IResourceManager resourceManager, BranchCache branchCache) {
      this.modelService = modelService;
      this.databaseService = databaseService;
      this.locatorManager = locatorManager;
      this.resourceManager = resourceManager;
      this.branchCache = branchCache;
   }

   @Override
   public synchronized void load(IOseeCache<Long, T> cache) throws OseeCoreException {
      if (!wasLoaded) {
         wasLoaded = true;
         Collection<String> uriPaths = findOseeTypeData();
         if (!uriPaths.isEmpty()) {
            List<IResource> resources = getTypeData(uriPaths);
            String modelData = createCombinedFile(resources);
            String modelName = String.format("osee.types.%s.osee", Lib.getDateTimeString());
            OseeImportModelRequest request = new OseeImportModelRequest(modelName, modelData, false, false, true);
            OseeImportModelResponse response = new OseeImportModelResponse();
            modelService.getIOseeModelingService().importOseeTypes(new NullProgressMonitor(), true, request, response);
         }
      }
   }

   @Override
   public void store(Collection<T> types) throws OseeCoreException {
      RemoteIdManager manager = databaseService.getRemoteIdManager();
      Collection<Long> remoteIds = new ArrayList<Long>();
      for (T type : types) {
         remoteIds.add(type.getGuid());
      }
      manager.store(remoteIds);
      for (T type : types) {
         type.setId(manager.getLocalId(type.getGuid()));
         type.clearDirty();
      }
   }

   private Collection<String> findOseeTypeData() throws OseeCoreException {
      Collection<String> paths = new ArrayList<String>();

      RemoteIdManager manager = databaseService.getRemoteIdManager();

      // john: Which attribute type store type def binary data
      Integer artifactTypeId = manager.getLocalId(CoreArtifactTypes.OseeTypeDefinition.getGuid());
      Integer attributeTypeId = manager.getLocalId(CoreAttributeTypes.UriGeneralStringData.getGuid());

      Branch commonBranch = branchCache.get(CoreBranches.COMMON);

      if (commonBranch != null) {
         IOseeStatement chStmt = null;
         try {
            chStmt = databaseService.getStatement();
            chStmt.runPreparedQuery(LOAD_OSEE_TYPE_DEF_URIS, commonBranch.getId(), TxChange.CURRENT.getValue(),
               commonBranch.getId(), TxChange.CURRENT.getValue(), artifactTypeId, attributeTypeId);
            while (chStmt.next()) {
               String uri = chStmt.getString("uri");
               paths.add(uri);
            }
         } finally {
            Lib.close(chStmt);
         }
      }
      return paths;
   }

   private List<IResource> getTypeData(Collection<String> paths) throws OseeCoreException {
      List<IResource> toReturn = new ArrayList<IResource>();

      PropertyStore options = new PropertyStore();
      options.put(StandardOptions.DecompressOnAquire.name(), "true");
      for (String path : paths) {
         IResourceLocator locator = locatorManager.getResourceLocator(path);
         IResource resource = resourceManager.acquire(locator, options);
         toReturn.add(resource);
      }
      return toReturn;
   }

   private String createCombinedFile(List<IResource> resources) throws OseeCoreException {
      StringWriter writer = new StringWriter();
      for (IResource resource : resources) {
         InputStream inputStream = null;
         try {
            inputStream = resource.getContent();
            String oseeTypeFragment = Lib.inputStreamToString(inputStream);
            oseeTypeFragment = oseeTypeFragment.replaceAll("import\\s+\"", "// import \"");
            writer.write("\n");
            writer.write("//////////////     ");
            writer.write(resource.getName());
            writer.write("\n");
            writer.write("\n");
            writer.write(oseeTypeFragment);
         } catch (IOException ex) {
            OseeExceptions.wrapAndThrow(ex);
         } finally {
            Lib.close(inputStream);
         }
      }
      return writer.toString();
   }
}