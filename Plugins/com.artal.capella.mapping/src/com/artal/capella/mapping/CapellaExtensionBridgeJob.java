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
package com.artal.capella.mapping;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.diffmerge.api.IMatchPolicy;
import org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope;
import org.eclipse.emf.diffmerge.bridge.api.IBridge;
import org.eclipse.emf.diffmerge.bridge.api.IBridgeTrace;
import org.eclipse.emf.diffmerge.bridge.api.IBridgeTrace.Editable;
import org.eclipse.emf.diffmerge.bridge.capella.integration.CapellaBridgeJob;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.interactive.EMFInteractiveBridge;
import org.eclipse.emf.diffmerge.bridge.traces.gen.bridgetraces.impl.TraceImpl;
import org.eclipse.emf.diffmerge.diffdata.EComparison;
import org.eclipse.emf.diffmerge.diffdata.impl.EComparisonImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.progress.IProgressConstants;
import org.polarsys.capella.core.data.capellamodeller.Project;

import com.artal.capella.mapping.cheat.TraceCheat;

/**
 * @author binot
 *
 */
public class CapellaExtensionBridgeJob<SD> extends CapellaBridgeJob<SD> {

	CapellaBridgeAlgo _algo;

	public CapellaExtensionBridgeJob(SD context_p, URI targetURI_p, CapellaBridgeAlgo algo) {
		super(context_p, targetURI_p);
		setProperty(IProgressConstants.PROPERTY_IN_DIALOG, true);
		_algo = algo;

	}

	@Override
	protected IBridge<SD, IEditableModelScope> createMappingBridge() {
		CapellaBridge<SD, Object> capellaBridge = new CapellaBridge<>(_algo);
		capellaBridge.registerRules();
		return capellaBridge;
	}

	/**
	 * @see org.eclipse.emf.diffmerge.bridge.interactive.BridgeJob#getBridge()
	 */
	@Override
	protected EMFInteractiveBridge<SD, IEditableModelScope> getBridge() {
		EMFInteractiveBridge<SD, IEditableModelScope> result = new EMFInteractiveBridge<SD, IEditableModelScope>(
				createMappingBridge(), createDiffPolicy(), createMergePolicy(), createMergeSelector()) {
			/**
			 * @see org.eclipse.emf.diffmerge.bridge.incremental.EMFIncrementalBridge#createIntermediateDataSet(java.lang.Object,
			 *      org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope)
			 */
			@Override
			public IEditableModelScope createIntermediateDataSet(SD sourceDataSet_p,
					IEditableModelScope targetDataSet_p) {
				if (targetDataSet_p instanceof CapellaUpdateScope) {
					// make an in-memory copy of the existing model
					final Project emptyProject = createEmptyProject(targetDataSet_p);
					CapellaUpdateScope scope = new CapellaUpdateScope(emptyProject);
					return createIntermediateCapellaScope(sourceDataSet_p, scope);
				}
				return super.createIntermediateDataSet(sourceDataSet_p, targetDataSet_p);
			}

			/**
			 * @see org.eclipse.emf.diffmerge.bridge.incremental.EMFIncrementalBridge#compare(org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope,
			 *      org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope,
			 *      org.eclipse.emf.diffmerge.bridge.api.IBridgeTrace,
			 *      org.eclipse.emf.diffmerge.bridge.api.IBridgeTrace,
			 *      org.eclipse.core.runtime.IProgressMonitor)
			 */
			@Override
			protected EComparison compare(IEditableModelScope created_p, IEditableModelScope existing_p,
					IBridgeTrace createdTrace_p, IBridgeTrace existingTrace_p, IProgressMonitor monitor_p) {
				EComparison comparison = new EComparisonImpl(existing_p, created_p);
				IMatchPolicy delegate = createMatchPolicyDelegate();
				IMatchPolicy matchPolicy = createDelegatingMatchPolicy(created_p, createdTrace_p, existingTrace_p,
						delegate);
				comparison.compute(matchPolicy, getDiffPolicy(), getMergePolicy(), monitor_p);
				return comparison;
			}

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

	@Override
	protected URI getTraceURI() {
		return super.getTraceURI()/* buildTraceURI(_sourceDataSet, context) */;
	}

	// public URI buildTraceURI(SD source, Project context) {
	// String key = null;
	// if (source instanceof CapellaElement) {
	// CapellaElement capellaEltSource = (CapellaElement) source;
	// // Associate a global id to the source item if needed
	// if (capellaEltSource.getId() == null) {
	// // CitrusPersistence TODO in capella context
	// // .execute(new EmfCitrusAction<Object>("Set the Global ID",
	// // capellaEltSource)
	// // {
	// // @Override
	// // public Object run()
	// // {
	// capellaEltSource.setId((UUID.randomUUID().toString()));
	// // return null;
	// // }
	// // });
	// }
	// key = capellaEltSource.getId().toString();
	// } else if (source instanceof EObject) {
	// EObject eObjectSource = (EObject) source;
	// Resource eObjectSourceRes = eObjectSource.eResource();
	// if (eObjectSourceRes != null) {
	// key = eObjectSourceRes.getURIFragment(eObjectSource);
	// }
	// } else if (source instanceof File) {
	// key = ((File) source).getName();
	// }
	//
	// if (key == null) {
	// key = source.toString();
	// }
	//
	// // Build the URI used by the bridge job (uri of the TRACE file)
	// String uriPath = context.getName() + "/" + TRACE_FOLDER
	// + "/"/* + getId() */ + "_ON_" + key;
	//
	// URI uri = URI.createPlatformResourceURI(uriPath, true);
	//
	// return uri.appendFileExtension(BridgetracesPackage.eNAME);
	// }

	// @Override
	// protected Resource getCreateTargetResource(URI uri_p) {
	// // TODO Auto-generated method stub
	// return super.getCreateTargetResource(uri_p);
	// }
	//
	// @Override
	// protected Resource getCreateTraceResource(URI traceURI_p) {
	//
	// Resource traceResource = new XMIResourceImpl(traceURI_p);
	//
	// return super.getCreateTraceResource(traceURI_p);
	// }

	protected CapellaBridgeAlgo<SD> getAlgo() {
		return _algo;
	}

	@Override
	protected void setupLogger() {
		// TODO Auto-generated method stub
	}
}
