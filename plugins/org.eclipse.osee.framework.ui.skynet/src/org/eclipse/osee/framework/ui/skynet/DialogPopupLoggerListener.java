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
package org.eclipse.osee.framework.ui.skynet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osee.framework.logging.ILoggerListener;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Andrew M. Finkbeiner
 */
public class DialogPopupLoggerListener implements ILoggerListener {

	@Override
	public void log(String loggerName, Level level, String message, Throwable th) {
		if (level == OseeLevel.SEVERE_POPUP) {
			String title = "OSEE Error";
			String messageText = message;
			String reasonMessage = "";
			if (th != null) {
				reasonMessage = th.getMessage();
			}
			final IStatus status;
			if (th != null) {
				List<IStatus> exc = new ArrayList<IStatus>();
				exceptionToString(true, loggerName, th, exc);
				status = new MultiStatus(loggerName, Status.ERROR, exc.toArray(new IStatus[exc.size()]), reasonMessage, th);
			} else {
				status = new Status(Status.ERROR, loggerName, -20, reasonMessage, th);
			}
			final String realTitle = title;
			final String realMessageText = messageText;
			Displays.pendInDisplayThread(new Runnable() {
				@Override
				public void run() {
					ErrorDialog.openError(Displays.getActiveShell(), realTitle, realMessageText, status);
				}
			});
		}
	}

	private static void exceptionToString(boolean firstTime, String loggerName, Throwable ex, List<IStatus> exc) {
		if (ex == null) {
			return;
		}
		if (!firstTime) {
			exc.add(new Status(Status.ERROR, loggerName, ex.getMessage()));
		}
		StackTraceElement st[] = ex.getStackTrace();
		for (int i = 0; i < st.length; i++) {
			StackTraceElement ste = st[i];
			exc.add(new Status(Status.ERROR, loggerName, ste.toString()));
		}
		Throwable cause = ex.getCause();
		if (cause != null) {
			exc.add(new Status(Status.ERROR, loggerName, "   caused by "));
			exceptionToString(false, loggerName, cause, exc);
		}
	}

}
