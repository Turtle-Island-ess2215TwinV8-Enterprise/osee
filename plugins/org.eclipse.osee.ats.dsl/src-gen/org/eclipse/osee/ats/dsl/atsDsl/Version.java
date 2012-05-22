/**
 * <copyright>
 * </copyright>
 *

 */
package org.eclipse.osee.ats.dsl.atsDsl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Version</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.osee.ats.dsl.atsDsl.Version#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.osee.ats.dsl.atsDsl.Version#getConfigurationId <em>Configuration Id</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getVersion()
 * @model
 * @generated
 */
public interface Version extends EObject
{
  /**
   * Returns the value of the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Name</em>' attribute.
   * @see #setName(String)
   * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getVersion_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link org.eclipse.osee.ats.dsl.atsDsl.Version#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

  /**
   * Returns the value of the '<em><b>Configuration Id</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Configuration Id</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Configuration Id</em>' containment reference.
   * @see #setConfigurationId(ProgramParallelConfiguration)
   * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getVersion_ConfigurationId()
   * @model containment="true"
   * @generated
   */
  ProgramParallelConfiguration getConfigurationId();

  /**
   * Sets the value of the '{@link org.eclipse.osee.ats.dsl.atsDsl.Version#getConfigurationId <em>Configuration Id</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Configuration Id</em>' containment reference.
   * @see #getConfigurationId()
   * @generated
   */
  void setConfigurationId(ProgramParallelConfiguration value);

} // Version
