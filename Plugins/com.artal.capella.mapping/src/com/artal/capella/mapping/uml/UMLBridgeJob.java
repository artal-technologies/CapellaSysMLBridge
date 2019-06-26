/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.uml;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope;
import org.eclipse.emf.diffmerge.bridge.api.IBridgeTrace.Editable;
import org.eclipse.emf.diffmerge.bridge.interactive.BridgeJob;
import org.eclipse.emf.diffmerge.bridge.interactive.EMFInteractiveBridge;
import org.eclipse.emf.diffmerge.bridge.traces.gen.bridgetraces.impl.TraceImpl;
import org.eclipse.emf.diffmerge.bridge.uml.incremental.UMLMergePolicy;
import org.eclipse.emf.diffmerge.gmf.GMFDiffPolicy;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.cheat.TraceCheat;

/**
 * @author YBI
 *
 */
public class UMLBridgeJob<SD> extends BridgeJob<SD> {

	CapellaBridgeAlgo<SD> _algo;

	/**
	 * Constructor
	 * 
	 * @param context_p
	 *            a non-null physical architecture
	 */
	public UMLBridgeJob(SD context_p, URI targetURI_p, CapellaBridgeAlgo<SD> algo) {
		super("UML Bridge Job", context_p, targetURI_p);
		setProperty(IProgressConstants.PROPERTY_IN_DIALOG, true);
		_algo = algo;
	}

	@Override
	protected ResourceSet initializeTargetResourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION,
				UMLResource.Factory.INSTANCE);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION,
				UMLResource.Factory.INSTANCE);
		return resourceSet;
	}

	protected Profile loadSysMLProfileForBridge() {
		return null;
	}

	/**
	 * @see org.eclipse.emf.diffmerge.bridge.interactive.BridgeJob#getBridge()
	 */
	@Override
	protected EMFInteractiveBridge<SD, IEditableModelScope> getBridge() {
		UMLBridge<SD, IEditableModelScope> mappingBridge = new UMLBridge<SD, IEditableModelScope>(_algo) {
			@Override
			public Profile loadSysMLProfile() throws Exception {
				// TODO Auto-generated method stub
				return loadSysMLProfileForBridge();
			}
		};
		mappingBridge.registerRules();
		// Make the mapping bridge incremental
		GMFDiffPolicy diffPolicy = new GMFDiffPolicy();
		diffPolicy.setIgnoreOrders(true);
		EMFInteractiveBridge<SD, IEditableModelScope> result = new EMFInteractiveBridge<SD, IEditableModelScope>(
				mappingBridge, diffPolicy, new UMLMergePolicy(), null) {

			/**
			 * @see org.eclipse.emf.diffmerge.bridge.incremental.EMFIncrementalBridge#createTrace()
			 */
			@Override
			protected Editable createTrace() {
				TraceImpl trace = new TraceImpl() {
					/** {@inheritDoc} */
					@Override
					public String putCause(Object cause_p, Object target_p) {
						if (target_p == null) {
							return null;
						}

						if (target_p instanceof TraceCheat) {
							TraceCheat<?> cheat = (TraceCheat<?>) target_p;
							String stringCause = cheat.getCause();
							EObject targetItem = cheat.getTarget();
							return super.putCause(stringCause, targetItem);
						} else {
							return super.putCause(cause_p, target_p);
						}

					}
				};
				return trace;
			}

		};
		return result;
	}

	public CapellaBridgeAlgo<SD> getAlgo() {
		return _algo;
	}

}
