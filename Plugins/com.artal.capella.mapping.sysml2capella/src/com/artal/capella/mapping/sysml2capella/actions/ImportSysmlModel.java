/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package com.artal.capella.mapping.sysml2capella.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.uml2.uml.Model;
import org.polarsys.capella.common.ef.ExecutionManager;
import org.polarsys.capella.common.ef.ExecutionManagerRegistry;
import org.polarsys.capella.core.data.la.LogicalArchitecture;

import com.artal.capella.mapping.CapellaExtensionBridgeJob;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.preferences.ConfigParser;
import com.artal.capella.mapping.sysml2capella.preferences.SysMLConfiguration;

/**
 * @author YBI
 *
 */
public class ImportSysmlModel extends AbstractHandler {

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

//		try {
//			LicenseUtils.runWithPrivileges(new Runnable() {
//
//				@Override
//				public void run() {
					LogicalArchitecture firstElement = (LogicalArchitecture) currentSelection.getFirstElement();
					Resource capellaResource = firstElement.eResource();
					URI targetUri = capellaResource.getURI();

					// launch dialog allowing to configure sysml/uml input and
					// xml
					// configuration
					SysMLCapellaLaunchDialog dialog = new SysMLCapellaLaunchDialog(
							Display.getCurrent().getActiveShell());
					int status = dialog.open();

					String filePath = null;
					String configPath = null;
					boolean eventOption = true;
					if (status == IStatus.OK) {
						filePath = dialog.getUmlPath();
						configPath = dialog.getConfigPath();
						eventOption = dialog.isEventOption();
					}

					// if no model input, stop the mapping.
					if (filePath == null || filePath.isEmpty()) {
						return null;
					}

					// Create the configuration from configuration file.
					SysMLConfiguration configuration = null;
					if (configPath != null && !configPath.isEmpty()) {
						ConfigParser configParser = new ConfigParser(configPath);
						configuration = configParser.parse();
					}

					URI fileURI = URI.createFileURI(filePath);

					// load source resource
					ExecutionManager manager = ExecutionManagerRegistry.getInstance().addNewManager();
					TransactionalEditingDomain domain = manager.getEditingDomain();
					ResourceSet resourceSet = domain.getResourceSet();
					Resource resource = resourceSet.getResource(fileURI, true);

					Model context = (Model) resource.getContents().get(0);
					if (context != null) {
						if (targetUri != null) {
							CapellaExtensionBridgeJob<Model> cameo2CapellaBridgeJob = new CapellaExtensionBridgeJob<Model>(
									context, targetUri, new Sysml2CapellaAlgo(configuration, eventOption));
							ProgressMonitorDialog pmd = new ProgressMonitorDialog(
									Display.getCurrent().getActiveShell());
							try {
								pmd.run(false, false, new IRunnableWithProgress() {

									@Override
									public void run(IProgressMonitor monitor)
											throws InvocationTargetException, InterruptedException {
										cameo2CapellaBridgeJob.run(monitor);

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

//				}
//			}, new ArtalFeature() {
//
//				@Override
//				public int getId() {
//					return 4;
//				}
//			});
//		} catch (InvalidPrivilegeException e1) {
//			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Invalid Privilege", e1.getMessage());
//		}

		return null;
	}

}
