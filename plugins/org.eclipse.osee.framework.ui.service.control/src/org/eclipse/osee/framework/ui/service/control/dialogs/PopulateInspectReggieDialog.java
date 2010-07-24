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
package org.eclipse.osee.framework.ui.service.control.dialogs;

import java.rmi.RemoteException;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.plugin.core.util.ExportClassLoader;
import org.eclipse.osee.framework.ui.service.control.ControlPlugin;
import org.eclipse.osee.framework.ui.service.control.managers.ServiceTreeBuilder;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.ui.PlatformUI;

/**
 * @author Roberto E. Escobar
 */
public class PopulateInspectReggieDialog extends Job {

	private final ServiceRegistrar reggie;
	private final ServiceTreeBuilder serviceTreeBuilder;

	public PopulateInspectReggieDialog(String title, ServiceTreeBuilder serviceTreeBuilder, ServiceRegistrar reggie) {
		super(title);
		this.serviceTreeBuilder = serviceTreeBuilder;
		this.reggie = reggie;

	}

	public static void scheduleJob(Job job) {
		job.setUser(true);
		job.setPriority(Job.SHORT);
		job.schedule();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus status = Status.CANCEL_STATUS;
		ClassLoader loader = this.getThread().getContextClassLoader();
		try {
			this.getThread().setContextClassLoader(ExportClassLoader.getInstance());
			ServiceMatches serviceMatches = reggie.lookup(new ServiceTemplate(null, null, null), Integer.MAX_VALUE);
			final ServiceItem[] serviceItemArray = serviceMatches.items;
			Displays.ensureInDisplayThread(new Runnable() {
				@Override
				public void run() {
					for (ServiceItem item : serviceItemArray) {
						serviceTreeBuilder.serviceAdded(item);
					}
				}
			});
			status = Status.OK_STATUS;
		} catch (RemoteException ex) {
			try {
				displayMessage("Reggie Lookup Error", String.format("Error searching for services in [%s:%s] reggie.\n%s",
							reggie.getLocator().getHost(), reggie.getLocator().getPort(), ControlPlugin.getStackMessages(ex)));
			} catch (RemoteException ex1) {
				displayMessage(
							"Reggie Lookup Error",
							String.format("Unable to access selected the selected reggie.\n%s",
										ControlPlugin.getStackMessages(ex)));
			}
		} finally {
			this.getThread().setContextClassLoader(loader);
		}
		return status;
	}

	private void displayMessage(final String title, final String message) {
		displayMessage(title, message, true);
	}

	private void displayMessage(final String title, final String message, final boolean isError) {
		Displays.pendInDisplayThread(new Runnable() {
			@Override
			public void run() {
				if (isError) {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, message);
				} else {
					MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title,
								message);
				}
			}
		});
	}

}
