/**
 */
package org.eclipse.osee.ats.dsl.atsDsl.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import org.eclipse.osee.ats.dsl.atsDsl.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.eclipse.osee.ats.dsl.atsDsl.AtsDslPackage
 * @generated
 */
public class AtsDslSwitch<T> extends Switch<T>
{
  /**
   * The cached model package
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected static AtsDslPackage modelPackage;

  /**
   * Creates an instance of the switch.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public AtsDslSwitch()
  {
    if (modelPackage == null)
    {
      modelPackage = AtsDslPackage.eINSTANCE;
    }
  }

  /**
   * Checks whether this is a switch for the given package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @parameter ePackage the package in question.
   * @return whether this is a switch for the given package.
   * @generated
   */
  @Override
  protected boolean isSwitchFor(EPackage ePackage)
  {
    return ePackage == modelPackage;
  }

  /**
   * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the first non-null result returned by a <code>caseXXX</code> call.
   * @generated
   */
  @Override
  protected T doSwitch(int classifierID, EObject theEObject)
  {
    switch (classifierID)
    {
      case AtsDslPackage.ATS_DSL:
      {
        AtsDsl atsDsl = (AtsDsl)theEObject;
        T result = caseAtsDsl(atsDsl);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.USER_DEF:
      {
        UserDef userDef = (UserDef)theEObject;
        T result = caseUserDef(userDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.TEAM_DEF:
      {
        TeamDef teamDef = (TeamDef)theEObject;
        T result = caseTeamDef(teamDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.ACTIONABLE_ITEM_DEF:
      {
        ActionableItemDef actionableItemDef = (ActionableItemDef)theEObject;
        T result = caseActionableItemDef(actionableItemDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.VERSION_DEF:
      {
        VersionDef versionDef = (VersionDef)theEObject;
        T result = caseVersionDef(versionDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.WORK_DEF:
      {
        WorkDef workDef = (WorkDef)theEObject;
        T result = caseWorkDef(workDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.WIDGET_DEF:
      {
        WidgetDef widgetDef = (WidgetDef)theEObject;
        T result = caseWidgetDef(widgetDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.WIDGET_REF:
      {
        WidgetRef widgetRef = (WidgetRef)theEObject;
        T result = caseWidgetRef(widgetRef);
        if (result == null) result = caseLayoutItem(widgetRef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.ATTR_WIDGET:
      {
        AttrWidget attrWidget = (AttrWidget)theEObject;
        T result = caseAttrWidget(attrWidget);
        if (result == null) result = caseLayoutItem(attrWidget);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.STATE_DEF:
      {
        StateDef stateDef = (StateDef)theEObject;
        T result = caseStateDef(stateDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.DECISION_REVIEW_REF:
      {
        DecisionReviewRef decisionReviewRef = (DecisionReviewRef)theEObject;
        T result = caseDecisionReviewRef(decisionReviewRef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.DECISION_REVIEW_DEF:
      {
        DecisionReviewDef decisionReviewDef = (DecisionReviewDef)theEObject;
        T result = caseDecisionReviewDef(decisionReviewDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.DECISION_REVIEW_OPT:
      {
        DecisionReviewOpt decisionReviewOpt = (DecisionReviewOpt)theEObject;
        T result = caseDecisionReviewOpt(decisionReviewOpt);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.PEER_REVIEW_REF:
      {
        PeerReviewRef peerReviewRef = (PeerReviewRef)theEObject;
        T result = casePeerReviewRef(peerReviewRef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.PEER_REVIEW_DEF:
      {
        PeerReviewDef peerReviewDef = (PeerReviewDef)theEObject;
        T result = casePeerReviewDef(peerReviewDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.FOLLOWUP_REF:
      {
        FollowupRef followupRef = (FollowupRef)theEObject;
        T result = caseFollowupRef(followupRef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.USER_REF:
      {
        UserRef userRef = (UserRef)theEObject;
        T result = caseUserRef(userRef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.USER_BY_USER_ID:
      {
        UserByUserId userByUserId = (UserByUserId)theEObject;
        T result = caseUserByUserId(userByUserId);
        if (result == null) result = caseUserRef(userByUserId);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.USER_BY_NAME:
      {
        UserByName userByName = (UserByName)theEObject;
        T result = caseUserByName(userByName);
        if (result == null) result = caseUserRef(userByName);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.TO_STATE:
      {
        ToState toState = (ToState)theEObject;
        T result = caseToState(toState);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.LAYOUT_TYPE:
      {
        LayoutType layoutType = (LayoutType)theEObject;
        T result = caseLayoutType(layoutType);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.LAYOUT_DEF:
      {
        LayoutDef layoutDef = (LayoutDef)theEObject;
        T result = caseLayoutDef(layoutDef);
        if (result == null) result = caseLayoutType(layoutDef);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.LAYOUT_COPY:
      {
        LayoutCopy layoutCopy = (LayoutCopy)theEObject;
        T result = caseLayoutCopy(layoutCopy);
        if (result == null) result = caseLayoutType(layoutCopy);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.LAYOUT_ITEM:
      {
        LayoutItem layoutItem = (LayoutItem)theEObject;
        T result = caseLayoutItem(layoutItem);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      case AtsDslPackage.COMPOSITE:
      {
        Composite composite = (Composite)theEObject;
        T result = caseComposite(composite);
        if (result == null) result = caseLayoutItem(composite);
        if (result == null) result = defaultCase(theEObject);
        return result;
      }
      default: return defaultCase(theEObject);
    }
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Ats Dsl</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Ats Dsl</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseAtsDsl(AtsDsl object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>User Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>User Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseUserDef(UserDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Team Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Team Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseTeamDef(TeamDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Actionable Item Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Actionable Item Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseActionableItemDef(ActionableItemDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Version Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Version Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseVersionDef(VersionDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Work Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Work Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseWorkDef(WorkDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Widget Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Widget Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseWidgetDef(WidgetDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Widget Ref</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Widget Ref</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseWidgetRef(WidgetRef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Attr Widget</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Attr Widget</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseAttrWidget(AttrWidget object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>State Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>State Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseStateDef(StateDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Decision Review Ref</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Decision Review Ref</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseDecisionReviewRef(DecisionReviewRef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Decision Review Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Decision Review Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseDecisionReviewDef(DecisionReviewDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Decision Review Opt</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Decision Review Opt</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseDecisionReviewOpt(DecisionReviewOpt object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Peer Review Ref</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Peer Review Ref</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T casePeerReviewRef(PeerReviewRef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Peer Review Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Peer Review Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T casePeerReviewDef(PeerReviewDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Followup Ref</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Followup Ref</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseFollowupRef(FollowupRef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>User Ref</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>User Ref</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseUserRef(UserRef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>User By User Id</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>User By User Id</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseUserByUserId(UserByUserId object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>User By Name</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>User By Name</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseUserByName(UserByName object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>To State</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>To State</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseToState(ToState object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Layout Type</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Layout Type</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseLayoutType(LayoutType object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Layout Def</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Layout Def</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseLayoutDef(LayoutDef object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Layout Copy</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Layout Copy</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseLayoutCopy(LayoutCopy object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Layout Item</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Layout Item</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseLayoutItem(LayoutItem object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>Composite</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>Composite</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
   * @generated
   */
  public T caseComposite(Composite object)
  {
    return null;
  }

  /**
   * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
   * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch, but this is the last case anyway.
   * <!-- end-user-doc -->
   * @param object the target of the switch.
   * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
   * @see #doSwitch(org.eclipse.emf.ecore.EObject)
   * @generated
   */
  @Override
  public T defaultCase(EObject object)
  {
    return null;
  }

} //AtsDslSwitch
