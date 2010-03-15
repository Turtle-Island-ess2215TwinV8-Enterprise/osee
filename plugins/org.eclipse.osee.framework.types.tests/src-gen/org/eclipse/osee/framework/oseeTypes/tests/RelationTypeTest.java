/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.osee.framework.oseeTypes.tests;

import junit.textui.TestRunner;
import org.eclipse.osee.framework.oseeTypes.OseeTypesFactory;
import org.eclipse.osee.framework.oseeTypes.XRelationType;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Relation Type</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class RelationTypeTest extends OseeTypeTest {

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public static void main(String[] args) {
      TestRunner.run(RelationTypeTest.class);
   }

   /**
    * Constructs a new Relation Type test case with the given name.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public RelationTypeTest(String name) {
      super(name);
   }

   /**
    * Returns the fixture for this Relation Type test case.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   @Override
   protected XRelationType getFixture() {
      return (XRelationType) fixture;
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see junit.framework.TestCase#setUp()
    * @generated
    */
   @Override
   protected void setUp() throws Exception {
      setFixture(OseeTypesFactory.eINSTANCE.createXRelationType());
   }

   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see junit.framework.TestCase#tearDown()
    * @generated
    */
   @Override
   protected void tearDown() throws Exception {
      setFixture(null);
   }

} //RelationTypeTest
