/*
 * Created on Sep 20, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.ui.skynet.accessProviders;

import java.util.Collection;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.RelationTypeSide;
import org.eclipse.osee.framework.core.model.access.PermissionStatus;
import org.eclipse.osee.framework.ui.skynet.artifact.IAccessPolicyHandlerService;

/**
 * @author Jeff C. Phillips
 */
public class RelationTypeAccessProvider {

   public boolean relationTypeHasPermission(IAccessPolicyHandlerService accessService, Collection<RelationTypeSide> relationTypeSides) throws OseeCoreException {
      PermissionStatus permissionStatus =
         accessService.hasRelationSidePermission(relationTypeSides, PermissionEnum.WRITE, Level.WARNING);
      return permissionStatus.matched();
   }
}