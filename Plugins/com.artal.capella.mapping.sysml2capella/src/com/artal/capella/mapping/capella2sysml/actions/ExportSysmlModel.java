/*******************************************************************************
 * Copyright (c) 2019 - 2022 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.capella2sysml.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.la.LogicalArchitecture;

import com.artal.capella.mapping.capella2sysml.Capella2SysmlBridgeJob;

/**
 * @author YBI
 *
 */
public class ExportSysmlModel extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection currentSelection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);

		CapellaSysmlLaunchDialog dialog = new CapellaSysmlLaunchDialog(Display.getCurrent().getActiveShell());
		int status = dialog.open();

		String filePath = null;
		if (status == IStatus.OK) {
			filePath = dialog.getUmlPath();
		}

		// if no model input, stop the mapping.
		if (filePath == null || filePath.isEmpty()) {
			return null;
		}
		String folder = filePath.substring(0, filePath.lastIndexOf(File.separator));
		URI targetUri = URI.createFileURI(filePath);

		LogicalArchitecture firstElement = (LogicalArchitecture) currentSelection.getFirstElement();
		Resource capellaResource = firstElement.eResource();
		ResourceSet resourceSet = capellaResource.getResourceSet();
		URI semanticResourceURI = capellaResource.getURI().trimFileExtension().appendFileExtension("capella");
		Resource semanticResource = resourceSet.getResource(semanticResourceURI, false);
		Project context = null;
		if (semanticResource != null) {
			EObject root = semanticResource.getContents().get(0);
			if (root instanceof Project) {
				context = (Project) root;
			}
		}

		if (context != null) {
			if (targetUri != null) {
				Capella2SysmlBridgeJob job = new Capella2SysmlBridgeJob("", context, targetUri);
				job.setTargetParentFolder(folder);
				ProgressMonitorDialog pmd = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
				try {
					pmd.run(false, false, new IRunnableWithProgress() {

						@Override
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException, InterruptedException {
							job.run(monitor);

						}
					});
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				Display display = Display.getCurrent();
				if (display != null) {
					Shell shell = display.getActiveShell();
					MessageDialog.openError(shell, "Mapping error", "file " + filePath + " not found");
				}
			}
		} else {
			return null;
		}

		return null;
	}

}
