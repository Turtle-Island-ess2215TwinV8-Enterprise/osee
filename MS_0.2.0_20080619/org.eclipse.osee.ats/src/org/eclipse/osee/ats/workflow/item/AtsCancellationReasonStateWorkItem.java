/*
 * Created on May 28, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.workflow.item;

import org.eclipse.osee.ats.artifact.LogItem;
import org.eclipse.osee.ats.artifact.ATSLog.LogType;
import org.eclipse.osee.ats.editor.SMAManager;
import org.eclipse.osee.framework.ui.skynet.widgets.XOption;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayoutData;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.WorkWidgetDefinition;

/**
 * @author Donald G. Dunne
 */
public class AtsCancellationReasonStateWorkItem extends WorkWidgetDefinition {

   protected AtsCancellationReasonStateWorkItem(SMAManager smaMgr) {
      super("Cancellation Reason", "ats.CancellationReason");
      DynamicXWidgetLayoutData data = new DynamicXWidgetLayoutData(null);
      data.setName(getName());
      LogItem item = smaMgr.getSma().getLog().getStateEvent(LogType.StateCancelled);
      data.setDefaultValue(item.getMsg());
      data.setStorageName(getId());
      data.setXWidgetName("XText");
      data.getXOptionHandler().add(XOption.NOT_EDITABLE);
      data.getXOptionHandler().add(XOption.FILL_HORIZONTALLY);
      setData(data);
   }
}
