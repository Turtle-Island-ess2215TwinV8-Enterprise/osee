/**
 * <copyright>
 * </copyright>
 *

 */
package org.eclipse.osee.framework.core.dsl.oseeDsl.impl;

import java.util.Collection;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AccessContext;
import org.eclipse.osee.framework.core.dsl.oseeDsl.Import;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDsl;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDslPackage;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactRef;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XAttributeType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XBranchRef;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XOseeEnumOverride;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XOseeEnumType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XRelationType;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Osee Dsl</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.OseeDslImpl#getImports <em>Imports</em>}</li>
 * <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.OseeDslImpl#getArtifactTypes <em>Artifact Types</em>}</li>
 * <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.OseeDslImpl#getRelationTypes <em>Relation Types</em>}</li>
 * <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.OseeDslImpl#getAttributeTypes <em>Attribute Types</em>}</li>
 * <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.OseeDslImpl#getEnumTypes <em>Enum Types</em>}</li>
 * <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.OseeDslImpl#getEnumOverrides <em>Enum Overrides</em>}</li>
 * <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.OseeDslImpl#getBranchRefs <em>Branch Refs</em>}</li>
 * <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.OseeDslImpl#getArtifactRefs <em>Artifact Refs</em>}</li>
 * <li>{@link org.eclipse.osee.framework.core.dsl.oseeDsl.impl.OseeDslImpl#getAccessDeclarations <em>Access Declarations
 * </em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class OseeDslImpl extends MinimalEObjectImpl.Container implements OseeDsl {
   /**
    * The cached value of the '{@link #getImports() <em>Imports</em>}' containment reference list. <!-- begin-user-doc
    * --> <!-- end-user-doc -->
    * 
    * @see #getImports()
    * @generated
    * @ordered
    */
   protected EList<Import> imports;

   /**
    * The cached value of the '{@link #getArtifactTypes() <em>Artifact Types</em>}' containment reference list. <!--
    * begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #getArtifactTypes()
    * @generated
    * @ordered
    */
   protected EList<XArtifactType> artifactTypes;

   /**
    * The cached value of the '{@link #getRelationTypes() <em>Relation Types</em>}' containment reference list. <!--
    * begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #getRelationTypes()
    * @generated
    * @ordered
    */
   protected EList<XRelationType> relationTypes;

   /**
    * The cached value of the '{@link #getAttributeTypes() <em>Attribute Types</em>}' containment reference list. <!--
    * begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #getAttributeTypes()
    * @generated
    * @ordered
    */
   protected EList<XAttributeType> attributeTypes;

   /**
    * The cached value of the '{@link #getEnumTypes() <em>Enum Types</em>}' containment reference list. <!--
    * begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #getEnumTypes()
    * @generated
    * @ordered
    */
   protected EList<XOseeEnumType> enumTypes;

   /**
    * The cached value of the '{@link #getEnumOverrides() <em>Enum Overrides</em>}' containment reference list. <!--
    * begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #getEnumOverrides()
    * @generated
    * @ordered
    */
   protected EList<XOseeEnumOverride> enumOverrides;

   /**
    * The cached value of the '{@link #getBranchRefs() <em>Branch Refs</em>}' containment reference list. <!--
    * begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #getBranchRefs()
    * @generated
    * @ordered
    */
   protected EList<XBranchRef> branchRefs;

   /**
    * The cached value of the '{@link #getArtifactRefs() <em>Artifact Refs</em>}' containment reference list. <!--
    * begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #getArtifactRefs()
    * @generated
    * @ordered
    */
   protected EList<XArtifactRef> artifactRefs;

   /**
    * The cached value of the '{@link #getAccessDeclarations() <em>Access Declarations</em>}' containment reference
    * list. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #getAccessDeclarations()
    * @generated
    * @ordered
    */
   protected EList<AccessContext> accessDeclarations;

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   protected OseeDslImpl() {
      super();
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   protected EClass eStaticClass() {
      return OseeDslPackage.Literals.OSEE_DSL;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public EList<Import> getImports() {
      if (imports == null) {
         imports = new EObjectContainmentEList<Import>(Import.class, this, OseeDslPackage.OSEE_DSL__IMPORTS);
      }
      return imports;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public EList<XArtifactType> getArtifactTypes() {
      if (artifactTypes == null) {
         artifactTypes =
            new EObjectContainmentEList<XArtifactType>(XArtifactType.class, this,
               OseeDslPackage.OSEE_DSL__ARTIFACT_TYPES);
      }
      return artifactTypes;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public EList<XRelationType> getRelationTypes() {
      if (relationTypes == null) {
         relationTypes =
            new EObjectContainmentEList<XRelationType>(XRelationType.class, this,
               OseeDslPackage.OSEE_DSL__RELATION_TYPES);
      }
      return relationTypes;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public EList<XAttributeType> getAttributeTypes() {
      if (attributeTypes == null) {
         attributeTypes =
            new EObjectContainmentEList<XAttributeType>(XAttributeType.class, this,
               OseeDslPackage.OSEE_DSL__ATTRIBUTE_TYPES);
      }
      return attributeTypes;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public EList<XOseeEnumType> getEnumTypes() {
      if (enumTypes == null) {
         enumTypes =
            new EObjectContainmentEList<XOseeEnumType>(XOseeEnumType.class, this, OseeDslPackage.OSEE_DSL__ENUM_TYPES);
      }
      return enumTypes;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public EList<XOseeEnumOverride> getEnumOverrides() {
      if (enumOverrides == null) {
         enumOverrides =
            new EObjectContainmentEList<XOseeEnumOverride>(XOseeEnumOverride.class, this,
               OseeDslPackage.OSEE_DSL__ENUM_OVERRIDES);
      }
      return enumOverrides;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public EList<XBranchRef> getBranchRefs() {
      if (branchRefs == null) {
         branchRefs =
            new EObjectContainmentEList<XBranchRef>(XBranchRef.class, this, OseeDslPackage.OSEE_DSL__BRANCH_REFS);
      }
      return branchRefs;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public EList<XArtifactRef> getArtifactRefs() {
      if (artifactRefs == null) {
         artifactRefs =
            new EObjectContainmentEList<XArtifactRef>(XArtifactRef.class, this, OseeDslPackage.OSEE_DSL__ARTIFACT_REFS);
      }
      return artifactRefs;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public EList<AccessContext> getAccessDeclarations() {
      if (accessDeclarations == null) {
         accessDeclarations =
            new EObjectContainmentEList<AccessContext>(AccessContext.class, this,
               OseeDslPackage.OSEE_DSL__ACCESS_DECLARATIONS);
      }
      return accessDeclarations;
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
      switch (featureID) {
         case OseeDslPackage.OSEE_DSL__IMPORTS:
            return ((InternalEList<?>) getImports()).basicRemove(otherEnd, msgs);
         case OseeDslPackage.OSEE_DSL__ARTIFACT_TYPES:
            return ((InternalEList<?>) getArtifactTypes()).basicRemove(otherEnd, msgs);
         case OseeDslPackage.OSEE_DSL__RELATION_TYPES:
            return ((InternalEList<?>) getRelationTypes()).basicRemove(otherEnd, msgs);
         case OseeDslPackage.OSEE_DSL__ATTRIBUTE_TYPES:
            return ((InternalEList<?>) getAttributeTypes()).basicRemove(otherEnd, msgs);
         case OseeDslPackage.OSEE_DSL__ENUM_TYPES:
            return ((InternalEList<?>) getEnumTypes()).basicRemove(otherEnd, msgs);
         case OseeDslPackage.OSEE_DSL__ENUM_OVERRIDES:
            return ((InternalEList<?>) getEnumOverrides()).basicRemove(otherEnd, msgs);
         case OseeDslPackage.OSEE_DSL__BRANCH_REFS:
            return ((InternalEList<?>) getBranchRefs()).basicRemove(otherEnd, msgs);
         case OseeDslPackage.OSEE_DSL__ARTIFACT_REFS:
            return ((InternalEList<?>) getArtifactRefs()).basicRemove(otherEnd, msgs);
         case OseeDslPackage.OSEE_DSL__ACCESS_DECLARATIONS:
            return ((InternalEList<?>) getAccessDeclarations()).basicRemove(otherEnd, msgs);
      }
      return super.eInverseRemove(otherEnd, featureID, msgs);
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public Object eGet(int featureID, boolean resolve, boolean coreType) {
      switch (featureID) {
         case OseeDslPackage.OSEE_DSL__IMPORTS:
            return getImports();
         case OseeDslPackage.OSEE_DSL__ARTIFACT_TYPES:
            return getArtifactTypes();
         case OseeDslPackage.OSEE_DSL__RELATION_TYPES:
            return getRelationTypes();
         case OseeDslPackage.OSEE_DSL__ATTRIBUTE_TYPES:
            return getAttributeTypes();
         case OseeDslPackage.OSEE_DSL__ENUM_TYPES:
            return getEnumTypes();
         case OseeDslPackage.OSEE_DSL__ENUM_OVERRIDES:
            return getEnumOverrides();
         case OseeDslPackage.OSEE_DSL__BRANCH_REFS:
            return getBranchRefs();
         case OseeDslPackage.OSEE_DSL__ARTIFACT_REFS:
            return getArtifactRefs();
         case OseeDslPackage.OSEE_DSL__ACCESS_DECLARATIONS:
            return getAccessDeclarations();
      }
      return super.eGet(featureID, resolve, coreType);
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @SuppressWarnings("unchecked")
   @Override
   public void eSet(int featureID, Object newValue) {
      switch (featureID) {
         case OseeDslPackage.OSEE_DSL__IMPORTS:
            getImports().clear();
            getImports().addAll((Collection<? extends Import>) newValue);
            return;
         case OseeDslPackage.OSEE_DSL__ARTIFACT_TYPES:
            getArtifactTypes().clear();
            getArtifactTypes().addAll((Collection<? extends XArtifactType>) newValue);
            return;
         case OseeDslPackage.OSEE_DSL__RELATION_TYPES:
            getRelationTypes().clear();
            getRelationTypes().addAll((Collection<? extends XRelationType>) newValue);
            return;
         case OseeDslPackage.OSEE_DSL__ATTRIBUTE_TYPES:
            getAttributeTypes().clear();
            getAttributeTypes().addAll((Collection<? extends XAttributeType>) newValue);
            return;
         case OseeDslPackage.OSEE_DSL__ENUM_TYPES:
            getEnumTypes().clear();
            getEnumTypes().addAll((Collection<? extends XOseeEnumType>) newValue);
            return;
         case OseeDslPackage.OSEE_DSL__ENUM_OVERRIDES:
            getEnumOverrides().clear();
            getEnumOverrides().addAll((Collection<? extends XOseeEnumOverride>) newValue);
            return;
         case OseeDslPackage.OSEE_DSL__BRANCH_REFS:
            getBranchRefs().clear();
            getBranchRefs().addAll((Collection<? extends XBranchRef>) newValue);
            return;
         case OseeDslPackage.OSEE_DSL__ARTIFACT_REFS:
            getArtifactRefs().clear();
            getArtifactRefs().addAll((Collection<? extends XArtifactRef>) newValue);
            return;
         case OseeDslPackage.OSEE_DSL__ACCESS_DECLARATIONS:
            getAccessDeclarations().clear();
            getAccessDeclarations().addAll((Collection<? extends AccessContext>) newValue);
            return;
      }
      super.eSet(featureID, newValue);
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public void eUnset(int featureID) {
      switch (featureID) {
         case OseeDslPackage.OSEE_DSL__IMPORTS:
            getImports().clear();
            return;
         case OseeDslPackage.OSEE_DSL__ARTIFACT_TYPES:
            getArtifactTypes().clear();
            return;
         case OseeDslPackage.OSEE_DSL__RELATION_TYPES:
            getRelationTypes().clear();
            return;
         case OseeDslPackage.OSEE_DSL__ATTRIBUTE_TYPES:
            getAttributeTypes().clear();
            return;
         case OseeDslPackage.OSEE_DSL__ENUM_TYPES:
            getEnumTypes().clear();
            return;
         case OseeDslPackage.OSEE_DSL__ENUM_OVERRIDES:
            getEnumOverrides().clear();
            return;
         case OseeDslPackage.OSEE_DSL__BRANCH_REFS:
            getBranchRefs().clear();
            return;
         case OseeDslPackage.OSEE_DSL__ARTIFACT_REFS:
            getArtifactRefs().clear();
            return;
         case OseeDslPackage.OSEE_DSL__ACCESS_DECLARATIONS:
            getAccessDeclarations().clear();
            return;
      }
      super.eUnset(featureID);
   }

   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public boolean eIsSet(int featureID) {
      switch (featureID) {
         case OseeDslPackage.OSEE_DSL__IMPORTS:
            return imports != null && !imports.isEmpty();
         case OseeDslPackage.OSEE_DSL__ARTIFACT_TYPES:
            return artifactTypes != null && !artifactTypes.isEmpty();
         case OseeDslPackage.OSEE_DSL__RELATION_TYPES:
            return relationTypes != null && !relationTypes.isEmpty();
         case OseeDslPackage.OSEE_DSL__ATTRIBUTE_TYPES:
            return attributeTypes != null && !attributeTypes.isEmpty();
         case OseeDslPackage.OSEE_DSL__ENUM_TYPES:
            return enumTypes != null && !enumTypes.isEmpty();
         case OseeDslPackage.OSEE_DSL__ENUM_OVERRIDES:
            return enumOverrides != null && !enumOverrides.isEmpty();
         case OseeDslPackage.OSEE_DSL__BRANCH_REFS:
            return branchRefs != null && !branchRefs.isEmpty();
         case OseeDslPackage.OSEE_DSL__ARTIFACT_REFS:
            return artifactRefs != null && !artifactRefs.isEmpty();
         case OseeDslPackage.OSEE_DSL__ACCESS_DECLARATIONS:
            return accessDeclarations != null && !accessDeclarations.isEmpty();
      }
      return super.eIsSet(featureID);
   }

} //OseeDslImpl
