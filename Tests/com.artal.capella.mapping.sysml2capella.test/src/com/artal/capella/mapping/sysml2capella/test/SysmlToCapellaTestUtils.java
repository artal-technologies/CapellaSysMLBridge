/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.sysml2capella.test;

import static org.eclipse.emf.diffmerge.impl.policies.ConfigurableMatchPolicy.CRITERION_SEMANTICS_DEFAULTCONTENTS;
import static org.eclipse.emf.diffmerge.impl.policies.ConfigurableMatchPolicy.CRITERION_STRUCTURE_ROOTS;
import static org.eclipse.emf.diffmerge.impl.policies.ConfigurableMatchPolicy.MatchCriterionKind.SEMANTICS;
import static org.eclipse.emf.diffmerge.impl.policies.ConfigurableMatchPolicy.MatchCriterionKind.STRUCTURE;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.diffmerge.api.IComparison;
import org.eclipse.emf.diffmerge.api.IMatchPolicy;
import org.eclipse.emf.diffmerge.api.diff.IAttributeValuePresence;
import org.eclipse.emf.diffmerge.api.diff.IDifference;
import org.eclipse.emf.diffmerge.api.diff.IElementPresence;
import org.eclipse.emf.diffmerge.api.diff.IReferenceValuePresence;
import org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope;
import org.eclipse.emf.diffmerge.bridge.api.IBridgeTrace;
import org.eclipse.emf.diffmerge.bridge.capella.integration.policies.DelegatingTraceBasedMatchPolicy;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.interactive.util.ResourceUtil;
import org.eclipse.emf.diffmerge.bridge.traces.gen.bridgetraces.BridgetracesPackage;
import org.eclipse.emf.diffmerge.diffdata.impl.EComparisonImpl;
import org.eclipse.emf.diffmerge.impl.policies.ConfigurableDiffPolicy;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.util.UMLUtil;
import org.polarsys.capella.common.libraries.LibrariesPackage;
import org.polarsys.capella.core.compare.CapellaMatchPolicy;
import org.polarsys.capella.core.data.capellacommon.CapellacommonPackage;
import org.polarsys.capella.core.data.capellacore.CapellacorePackage;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.capellamodeller.CapellamodellerPackage;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.capellamodeller.util.CapellamodellerResourceFactoryImpl;
import org.polarsys.capella.core.data.cs.CsPackage;
import org.polarsys.capella.core.data.ctx.CtxPackage;
import org.polarsys.capella.core.data.fa.FaPackage;
import org.polarsys.capella.core.data.information.InformationPackage;
import org.polarsys.capella.core.data.information.datatype.DatatypePackage;
import org.polarsys.capella.core.data.information.datavalue.DatavaluePackage;
import org.polarsys.capella.core.data.la.LaPackage;
import org.polarsys.capella.core.data.oa.OaPackage;
import org.polarsys.capella.core.data.pa.PaPackage;
import org.polarsys.capella.core.data.requirement.RequirementPackage;
import org.polarsys.kitalpha.emde.model.EmdePackage;

import com.artal.capella.mapping.CapellaExtensionBridgeJob;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;

/**
 * 
 * {@link SysmlToCapellaTestUtils} provides methods to use for the sysml to
 * capella tests.
 * 
 * @author YBI
 *
 */
public class SysmlToCapellaTestUtils {

	/**
	 * Load the uml {@link Model}
	 * 
	 * @param path
	 *            the path of the uml file.
	 * @return the uml {@link Model}
	 */
	static public Model loadUMLModel(String path) {
		ResourceSet resourceSet = createResourceSet();
		Model load = (Model) UMLUtil.load(resourceSet, URI.createFileURI(path), UMLPackage.Literals.MODEL);
		return load;
	}

	/**
	 * Create a uml {@link ResourceSet}.
	 * 
	 * @return the {@link ResourceSet}
	 */
	public static ResourceSet createResourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION,
				UMLResource.Factory.INSTANCE);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION,
				UMLResource.Factory.INSTANCE);
		return resourceSet;
	}

	/**
	 * Launch the Sysml 2 capella mapping.
	 * 
	 * @param model
	 *            the source UML {@link Model}.
	 * @param targetFile
	 *            the Capella file containing the capella {@link Project} to
	 *            fill.
	 * @param traceFile
	 *            the created bridge trace file.
	 */
	static public void launchSysml2Capella(Model model, File targetFile, String traceFile) {
		URI targetUri = URI.createFileURI(targetFile.getAbsolutePath());
		CapellaExtensionBridgeJob<Model> cameo2CapellaBridgeJob = new CapellaExtensionBridgeJob<Model>(model, targetUri,
				new Sysml2CapellaAlgo()) {
			protected ResourceSet initializeTargetResourceSet() {
				return createResourceSet();
			};

			@Override
			protected Resource getCreateTargetResource(URI uri_p) {
				return getCapellaResource(uri_p);
			}

			@Override
			protected Resource getCreateTraceResource(URI traceURI_p) {
				return getTraceResource(URI.createFileURI(traceFile));
			}
		};
		cameo2CapellaBridgeJob.run(new NullProgressMonitor());
	}

	/**
	 * Load the capella {@link Project}.
	 * 
	 * @param path
	 *            the path of the Capella melodymodeller
	 * @return {@link Project}
	 */
	static public Project loadCapellaModel(String path) {
		URI melodyModeller = URI.createFileURI(path);

		Resource resource = getCapellaResource(melodyModeller);

		Project projectCapella = (org.polarsys.capella.core.data.capellamodeller.Project) resource.getContents().get(0);
		return projectCapella;
	}

	/**
	 * Get or create the BridgeTrace {@link Resource}
	 * 
	 * @param bridgeTrace
	 *            the uri of the bridgeTrace file.
	 * @return {@link Resource}
	 */
	private static Resource getTraceResource(URI bridgeTrace) {
		ResourceSetImpl rs = new ResourceSetImpl();
		
		
		
		
		Resource traceResource = ResourceUtil.getCreateResourceForUri(bridgeTrace, rs);
		ResourceUtil.ensureLoaded(traceResource);
		return traceResource;
	}

	/**
	 * Create Capella ResourceSet and get Resource for the capella uri.
	 * 
	 * @param melodyModeller
	 *            the Capella uri.
	 * @return {@link Resource}
	 */
	private static Resource getCapellaResource(URI melodyModeller) {
		ResourceSet res = new ResourceSetImpl();

		res.getResourceFactoryRegistry().getExtensionToFactoryMap().put(CapellamodellerPackage.eNS_PREFIX,
				new CapellamodellerResourceFactoryImpl());
		res.getResourceFactoryRegistry().getExtensionToFactoryMap().put("melodymodeller",
				new CapellamodellerResourceFactoryImpl());

		res.getPackageRegistry().put("http://www.polarsys.org/capella/common/libraries/1.2.0",
				LibrariesPackage.eINSTANCE);
		res.getPackageRegistry().put("http://www.polarsys.org/capella/core/common/1.2.0",
				CapellacommonPackage.eINSTANCE);
		res.getPackageRegistry().put("http://www.polarsys.org/capella/core/core/1.2.0", CapellacorePackage.eINSTANCE);
		res.getPackageRegistry().put("http://www.polarsys.org/capella/core/modeller/1.2.0",
				CapellamodellerPackage.eINSTANCE);
		res.getPackageRegistry().put("http://www.polarsys.org/capella/core/cs/1.2.0", CsPackage.eINSTANCE);
		res.getPackageRegistry().put("http://www.polarsys.org/capella/core/ctx/1.2.0", CtxPackage.eINSTANCE);
		res.getPackageRegistry().put("http://www.polarsys.org/capella/core/fa/1.2.0", FaPackage.eINSTANCE);
		res.getPackageRegistry().put("http://www.polarsys.org/capella/core/information/1.2.0",
				InformationPackage.eINSTANCE);
		res.getPackageRegistry().put("http://www.polarsys.org/capella/core/information/datatype/1.2.0",
				DatatypePackage.eINSTANCE);
		res.getPackageRegistry().put("http://www.polarsys.org/capella/core/information/datavalue/1.2.0",
				DatavaluePackage.eINSTANCE);
		res.getPackageRegistry().put("http://www.polarsys.org/capella/core/la/1.2.0", LaPackage.eINSTANCE);
		res.getPackageRegistry().put("http://www.polarsys.org/capella/core/oa/1.2.0", OaPackage.eINSTANCE);
		res.getPackageRegistry().put("http://www.polarsys.org/capella/core/pa/1.2.0", PaPackage.eINSTANCE);
		res.getPackageRegistry().put("http://www.polarsys.org/capella/core/requirement/1.2.0",
				RequirementPackage.eINSTANCE);
		res.getPackageRegistry().put("http://www.polarsys.org/kitalpha/patterns/emde/1.2.0", EmdePackage.eINSTANCE);
		Resource resource = res.getResource(melodyModeller, true);
		return resource;
	}

	/**
	 * Create {@link CapellaMatchPolicy}.
	 * 
	 * @return {@link CapellaMatchPolicy}
	 */
	static private IMatchPolicy createMatchPolicyDelegate() {
		CapellaMatchPolicy delegate = new CapellaMatchPolicy();
		delegate.setUseCriterion(STRUCTURE, true);
		delegate.setUseCriterion(SEMANTICS, true);
		delegate.setUseFineGrainedCriterion(CRITERION_STRUCTURE_ROOTS, true);
		delegate.setUseFineGrainedCriterion(CRITERION_SEMANTICS_DEFAULTCONTENTS, true);
		return delegate;
	}

	/**
	 * Return the trace contained in the given trace resource, if any
	 * 
	 * @param traceResource_p
	 *            a non-null resource
	 * @return a potentially null trace
	 */
	static protected IBridgeTrace getTrace(Resource traceResource_p) {
		IBridgeTrace result = null;
		if (!traceResource_p.getContents().isEmpty()) {
			EObject root = traceResource_p.getAllContents().next();
			if (root instanceof IBridgeTrace)
				result = (IBridgeTrace) root;
		}
		return result;
	}

	/**
	 * Compare two capella model.
	 * 
	 * @param targetModel
	 *            the created model
	 * @param sourceModel
	 *            the reference model
	 * @param targetTraceFile
	 *            the create trace
	 * @param sourceTraceFile
	 *            the reference trace
	 * @return all message if differences
	 */
	static public String compareResources(Project targetModel, Project sourceModel, File targetTraceFile,
			File sourceTraceFile) {
		// create scopes
		IEditableModelScope targetScope = new CapellaUpdateScope(targetModel);
		IEditableModelScope sourceScope = new CapellaUpdateScope(sourceModel);

		// instanciate the comparison tool.
		IComparison comparison = new EComparisonImpl(sourceScope, targetScope);

		// get the trace resources.
		Resource targetTraceResource = getTraceResource(URI.createFileURI(targetTraceFile.getAbsolutePath()));
		Resource sourceTraceResource = getTraceResource(URI.createFileURI(sourceTraceFile.getAbsolutePath()));

		// get the bridge trace.
		IBridgeTrace targetTrace = getTrace(targetTraceResource);
		IBridgeTrace sourceTrace = getTrace(sourceTraceResource);

		// compare the both source and target models.
		comparison.compute(
				new DelegatingTraceBasedMatchPolicy(targetScope, targetTrace, sourceTrace, createMatchPolicyDelegate()),
				new ConfigurableDiffPolicy() {
					@Override
					public boolean coverFeature(EStructuralFeature feature_p) {

						EAttribute capellaElementDescription = CapellacorePackage.Literals.CAPELLA_ELEMENT__DESCRIPTION;
						if (feature_p.equals(capellaElementDescription)) {
							return false;
						}
						return super.coverFeature(feature_p);
					}
				}, null, null);

		// build message if there is differences.
		String message = "";
		Collection<IDifference> remainingDifferences = comparison.getRemainingDifferences();
		for (IDifference iDifference : remainingDifferences) {
			if (iDifference instanceof IElementPresence) {
				EObject element = ((IElementPresence) iDifference).getElement();
				if (element instanceof NamedElement) {
					message += ("Element " + ((NamedElement) element).getName()
							+ " in a scope has no match in the reference model.\n");
				}
			}
			if (iDifference instanceof IReferenceValuePresence) {
				EObject value = ((IReferenceValuePresence) iDifference).getValue();
				if (value instanceof NamedElement) {
					message += ("Element " + ((NamedElement) value).getName()
							+ " references another element in the reference model.\n");
				}
			}
			if (iDifference instanceof IAttributeValuePresence) {
				Object value = ((IAttributeValuePresence) iDifference).getValue();
				if (value instanceof String) {
					message += ("Element " + value + " owns a certain attribute value in the reference model.\n");
				}
			}
		}
		return message;
	}
	
	
}
