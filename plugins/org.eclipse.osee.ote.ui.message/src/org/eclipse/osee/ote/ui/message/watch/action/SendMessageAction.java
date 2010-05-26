/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ote.ui.message.watch.action;

import java.util.logging.Level;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.ote.message.tool.MessageMode;
import org.eclipse.osee.ote.ui.message.tree.WatchedMessageNode;
import org.eclipse.swt.widgets.Display;

/**
 * @author Ken J. Aguilar
 *
 */
public class SendMessageAction extends Action {
	private final WatchedMessageNode msgNode;

	public SendMessageAction(WatchedMessageNode msgNode) {
		super("Send");
		this.msgNode = msgNode;
		setEnabled(msgNode.isEnabled() && msgNode.getSubscription().getMessageMode() == MessageMode.WRITER && msgNode.getSubscription().isActive());

	}
	
	@Override
	public void run() {
		try {
			msgNode.getSubscription().send();
		} catch (Exception e) {
			String message = "could not send the message " + msgNode.getMessageClassName();
			OseeLog.log(SendMessageAction.class, Level.SEVERE, message, e);
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Send Message", message + ". Check error log for trace");
		}
	}

	
}