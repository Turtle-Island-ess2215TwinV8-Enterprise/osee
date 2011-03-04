/**
 * <copyright>
 * </copyright>
 *

 */
package org.eclipse.osee.ats.dsl.atsDsl;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Team Def</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getTeamDefOption <em>Team Def Option</em>}</li>
 *   <li>{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getActive <em>Active</em>}</li>
 *   <li>{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getUsesVersions <em>Uses Versions</em>}</li>
 *   <li>{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getStaticId <em>Static Id</em>}</li>
 *   <li>{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getLead <em>Lead</em>}</li>
 *   <li>{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getMember <em>Member</em>}</li>
 *   <li>{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getPriviledged <em>Priviledged</em>}</li>
 *   <li>{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getWorkDefinition <em>Work Definition</em>}</li>
 *   <li>{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getChildren <em>Children</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getTeamDef()
 * @model
 * @generated
 */
public interface TeamDef extends EObject
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
   * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getTeamDef_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

  /**
   * Returns the value of the '<em><b>Team Def Option</b></em>' attribute list.
   * The list contents are of type {@link java.lang.String}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Team Def Option</em>' attribute list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Team Def Option</em>' attribute list.
   * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getTeamDef_TeamDefOption()
   * @model unique="false"
   * @generated
   */
  EList<String> getTeamDefOption();

  /**
   * Returns the value of the '<em><b>Active</b></em>' attribute.
   * The literals are from the enumeration {@link org.eclipse.osee.ats.dsl.atsDsl.BooleanDef}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Active</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Active</em>' attribute.
   * @see org.eclipse.osee.ats.dsl.atsDsl.BooleanDef
   * @see #setActive(BooleanDef)
   * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getTeamDef_Active()
   * @model
   * @generated
   */
  BooleanDef getActive();

  /**
   * Sets the value of the '{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getActive <em>Active</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Active</em>' attribute.
   * @see org.eclipse.osee.ats.dsl.atsDsl.BooleanDef
   * @see #getActive()
   * @generated
   */
  void setActive(BooleanDef value);

  /**
   * Returns the value of the '<em><b>Uses Versions</b></em>' attribute.
   * The literals are from the enumeration {@link org.eclipse.osee.ats.dsl.atsDsl.BooleanDef}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Uses Versions</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Uses Versions</em>' attribute.
   * @see org.eclipse.osee.ats.dsl.atsDsl.BooleanDef
   * @see #setUsesVersions(BooleanDef)
   * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getTeamDef_UsesVersions()
   * @model
   * @generated
   */
  BooleanDef getUsesVersions();

  /**
   * Sets the value of the '{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getUsesVersions <em>Uses Versions</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Uses Versions</em>' attribute.
   * @see org.eclipse.osee.ats.dsl.atsDsl.BooleanDef
   * @see #getUsesVersions()
   * @generated
   */
  void setUsesVersions(BooleanDef value);

  /**
   * Returns the value of the '<em><b>Static Id</b></em>' attribute list.
   * The list contents are of type {@link java.lang.String}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Static Id</em>' attribute list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Static Id</em>' attribute list.
   * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getTeamDef_StaticId()
   * @model unique="false"
   * @generated
   */
  EList<String> getStaticId();

  /**
   * Returns the value of the '<em><b>Lead</b></em>' containment reference list.
   * The list contents are of type {@link org.eclipse.osee.ats.dsl.atsDsl.UserRef}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Lead</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Lead</em>' containment reference list.
   * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getTeamDef_Lead()
   * @model containment="true"
   * @generated
   */
  EList<UserRef> getLead();

  /**
   * Returns the value of the '<em><b>Member</b></em>' containment reference list.
   * The list contents are of type {@link org.eclipse.osee.ats.dsl.atsDsl.UserRef}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Member</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Member</em>' containment reference list.
   * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getTeamDef_Member()
   * @model containment="true"
   * @generated
   */
  EList<UserRef> getMember();

  /**
   * Returns the value of the '<em><b>Priviledged</b></em>' containment reference list.
   * The list contents are of type {@link org.eclipse.osee.ats.dsl.atsDsl.UserRef}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Priviledged</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Priviledged</em>' containment reference list.
   * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getTeamDef_Priviledged()
   * @model containment="true"
   * @generated
   */
  EList<UserRef> getPriviledged();

  /**
   * Returns the value of the '<em><b>Work Definition</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Work Definition</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Work Definition</em>' attribute.
   * @see #setWorkDefinition(String)
   * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getTeamDef_WorkDefinition()
   * @model
   * @generated
   */
  String getWorkDefinition();

  /**
   * Sets the value of the '{@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef#getWorkDefinition <em>Work Definition</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Work Definition</em>' attribute.
   * @see #getWorkDefinition()
   * @generated
   */
  void setWorkDefinition(String value);

  /**
   * Returns the value of the '<em><b>Version</b></em>' containment reference list.
   * The list contents are of type {@link org.eclipse.osee.ats.dsl.atsDsl.VersionDef}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Version</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Version</em>' containment reference list.
   * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getTeamDef_Version()
   * @model containment="true"
   * @generated
   */
  EList<VersionDef> getVersion();

  /**
   * Returns the value of the '<em><b>Children</b></em>' containment reference list.
   * The list contents are of type {@link org.eclipse.osee.ats.dsl.atsDsl.TeamDef}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Children</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Children</em>' containment reference list.
   * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage#getTeamDef_Children()
   * @model containment="true"
   * @generated
   */
  EList<TeamDef> getChildren();

} // TeamDef