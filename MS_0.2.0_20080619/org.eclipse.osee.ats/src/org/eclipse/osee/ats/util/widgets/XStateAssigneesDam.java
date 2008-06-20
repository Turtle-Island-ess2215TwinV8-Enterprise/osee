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
package org.eclipse.osee.ats.util.widgets;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.ats.AtsPlugin;
import org.eclipse.osee.ats.artifact.StateMachineArtifact;
import org.eclipse.osee.framework.skynet.core.attribute.Attribute;
import org.eclipse.osee.framework.skynet.core.exception.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.widgets.XTextDam;

/**
 * @author Donald G. Dunne
 */
public abstract class XStateAssigneesDam extends XTextDam {

   protected final String attributeTypeName;
   protected final StateMachineArtifact sma;

   public XStateAssigneesDam(StateMachineArtifact sma, String attributeName) {
      super(attributeName);
      this.sma = sma;
      this.attributeTypeName = attributeName;
   }

   public SMAState getState(String stateName, boolean create) {
      try {
         for (String stateXml : sma.getAttributesToStringList(attributeTypeName)) {
            if (stateXml.startsWith(stateName + ";")) {
               SMAState state = new SMAState();
               state.setFromXml(stateXml);
               return state;
            }
         }
         if (create) return new SMAState(stateName);
      } catch (Exception ex) {
         OSEELog.logException(AtsPlugin.class, "Error parsing state data for " + sma.getHumanReadableId(), ex, false);
      }
      return null;
   }

   public Set<SMAState> getStates() {
      Set<SMAState> states = new HashSet<SMAState>();
      try {
         for (String stateXml : sma.getAttributesToStringList(attributeTypeName)) {
            SMAState state = new SMAState();
            state.setFromXml(stateXml);
            states.add(state);
         }
      } catch (Exception ex) {
         OSEELog.logException(AtsPlugin.class, "Error parsing state data for " + sma.getHumanReadableId(), ex, false);
      }
      return states;
   }

   public void setState(SMAState state) throws OseeCoreException, SQLException {
      // Update attribute if it already exists
      try {
         Collection<Attribute<String>> attrs = sma.getAttributes(attributeTypeName);
         for (Attribute<String> attr : attrs) {
            SMAState storedState = new SMAState();
            storedState.setFromXml(attr.getValue());
            if (state.getName().equals(storedState.getName())) {
               attr.setValue(state.toXml());
               return;
            }
         }
         // Else, doesn't exist yet, create
         sma.addAttribute(attributeTypeName, state.toXml());
      } catch (Exception ex) {
         OSEELog.logException(AtsPlugin.class, "Error setting state data for " + sma.getHumanReadableId(), ex, false);
      }
   }
}
