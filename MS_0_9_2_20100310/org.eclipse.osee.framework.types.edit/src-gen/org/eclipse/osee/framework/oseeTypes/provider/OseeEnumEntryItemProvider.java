/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.osee.framework.oseeTypes.provider;


import java.util.Collection;
import java.util.List;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;
import org.eclipse.osee.framework.oseeTypes.OseeTypesPackage;
import org.eclipse.osee.framework.oseeTypes.XOseeEnumEntry;

/**
 * This is the item provider adapter for a {@link org.eclipse.osee.framework.oseeTypes.OseeEnumEntry} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class OseeEnumEntryItemProvider
   extends ItemProviderAdapter
   implements
      IEditingDomainItemProvider,
      IStructuredItemContentProvider,
      ITreeItemContentProvider,
      IItemLabelProvider,
      IItemPropertySource {
   /**
    * This constructs an instance from a factory and a notifier.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public OseeEnumEntryItemProvider(AdapterFactory adapterFactory) {
      super(adapterFactory);
   }

   /**
    * This returns the property descriptors for the adapted class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   @Override
   public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
      if (itemPropertyDescriptors == null) {
         super.getPropertyDescriptors(object);

         addNamePropertyDescriptor(object);
         addOrdinalPropertyDescriptor(object);
         addEntryGuidPropertyDescriptor(object);
      }
      return itemPropertyDescriptors;
   }

   /**
    * This adds a property descriptor for the Name feature.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   protected void addNamePropertyDescriptor(Object object) {
      itemPropertyDescriptors.add
         (createItemPropertyDescriptor
            (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
             getResourceLocator(),
             getString("_UI_OseeEnumEntry_name_feature"),
             getString("_UI_PropertyDescriptor_description", "_UI_OseeEnumEntry_name_feature", "_UI_OseeEnumEntry_type"),
            OseeTypesPackage.Literals.XOSEE_ENUM_ENTRY__NAME,
             true,
             false,
             false,
             ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
             null,
             null));
   }

   /**
    * This adds a property descriptor for the Ordinal feature.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   protected void addOrdinalPropertyDescriptor(Object object) {
      itemPropertyDescriptors.add
         (createItemPropertyDescriptor
            (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
             getResourceLocator(),
             getString("_UI_OseeEnumEntry_ordinal_feature"),
             getString("_UI_PropertyDescriptor_description", "_UI_OseeEnumEntry_ordinal_feature", "_UI_OseeEnumEntry_type"),
            OseeTypesPackage.Literals.XOSEE_ENUM_ENTRY__ORDINAL,
             true,
             false,
             false,
             ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
             null,
             null));
   }

   /**
    * This adds a property descriptor for the Entry Guid feature.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   protected void addEntryGuidPropertyDescriptor(Object object) {
      itemPropertyDescriptors.add
         (createItemPropertyDescriptor
            (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
             getResourceLocator(),
             getString("_UI_OseeEnumEntry_entryGuid_feature"),
             getString("_UI_PropertyDescriptor_description", "_UI_OseeEnumEntry_entryGuid_feature", "_UI_OseeEnumEntry_type"),
            OseeTypesPackage.Literals.XOSEE_ENUM_ENTRY__ENTRY_GUID,
             true,
             false,
             false,
             ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
             null,
             null));
   }

   /**
    * This returns OseeEnumEntry.gif.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   @Override
   public Object getImage(Object object) {
      return overlayImage(object, getResourceLocator().getImage("full/obj16/OseeEnumEntry"));
   }

   /**
    * This returns the label text for the adapted class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   @Override
   public String getText(Object object) {
      String label = ((XOseeEnumEntry) object).getName();
      return label == null || label.length() == 0 ?
         getString("_UI_OseeEnumEntry_type") :
         getString("_UI_OseeEnumEntry_type") + " " + label;
   }

   /**
    * This handles model notifications by calling {@link #updateChildren} to update any cached
    * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   @Override
   public void notifyChanged(Notification notification) {
      updateChildren(notification);

      switch (notification.getFeatureID(XOseeEnumEntry.class)) {
         case OseeTypesPackage.XOSEE_ENUM_ENTRY__NAME:
         case OseeTypesPackage.XOSEE_ENUM_ENTRY__ORDINAL:
         case OseeTypesPackage.XOSEE_ENUM_ENTRY__ENTRY_GUID:
            fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
            return;
      }
      super.notifyChanged(notification);
   }

   /**
    * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
    * that can be created under this object.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   @Override
   protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
      super.collectNewChildDescriptors(newChildDescriptors, object);
   }

   /**
    * Return the resource locator for this item provider's resources.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   @Override
   public ResourceLocator getResourceLocator() {
      return OseeTypesEditPlugin.INSTANCE;
   }

}
