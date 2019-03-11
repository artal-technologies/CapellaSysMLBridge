/**
 * 
 */
package com.artal.capella.mapping.sysml2capella.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.uml2.uml.Model;
import org.polarsys.capella.common.ef.ExecutionManager;
import org.polarsys.capella.common.ef.ExecutionManagerRegistry;
import org.polarsys.capella.core.data.la.LogicalArchitecture;

import com.artal.capella.mapping.CapellaExtensionBridgeJob;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaBridgeJob;

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

		LogicalArchitecture firstElement = (LogicalArchitecture) currentSelection.getFirstElement();
		Resource capellaResource = firstElement.eResource();
		URI targetUri = capellaResource.getURI();
		FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell());

		String filePath = dialog.open();

		URI fileURI = URI.createFileURI(filePath);

		// load source resource
		ExecutionManager manager = ExecutionManagerRegistry.getInstance().addNewManager();
		TransactionalEditingDomain domain = manager.getEditingDomain();
		ResourceSet resourceSet = domain.getResourceSet();
		Resource resource = resourceSet.getResource(fileURI, true);

		Model context = (Model) resource.getContents().get(0);
		if (context != null) {
			if (targetUri != null) {
				CapellaExtensionBridgeJob<Model> cameo2CapellaBridgeJob = new CapellaExtensionBridgeJob<Model>(context,
						targetUri, new Sysml2CapellaAlgo());
				cameo2CapellaBridgeJob.run(new NullProgressMonitor());
			} else {
				Display display = Display.getCurrent();
				if (display != null) {
					Shell shell = display.getActiveShell();
					MessageDialog.openError(shell, "Mapping error", "file " + filePath + " not found");
				}
			}
		} else {
			throw new ExecutionException("Execution context not found");
		}

		return null;
	}

}
