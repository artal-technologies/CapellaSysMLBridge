/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.capella2sysml;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope;
import org.eclipse.emf.diffmerge.gmf.GMFDiffPolicy;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.uml2.uml.Profile;
import org.polarsys.capella.core.data.capellamodeller.Project;

import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile;
import com.artal.capella.mapping.uml.UMLBridge;
import com.artal.capella.mapping.uml.UMLBridgeJob;

/**
 * @author YBI
 *
 */
public class Capella2SysmlBridgeJob extends UMLBridgeJob<Project> {

	public Capella2SysmlBridgeJob(String jobName_p, Project sourceDataSet_p, URI targetURI_p) {
		super(sourceDataSet_p, targetURI_p, new Capella2SysmlAlgo());
	}

	@Override
	protected void setupLogger() {
	}

	public void setTargetParentFolder(String folder) {
		((Capella2SysmlAlgo) getAlgo()).setTargetParentFolder(folder);

	}

	@Override
	protected Profile loadSysMLProfileForBridge() {
		Profile sysMLProfile = SysML2CapellaUMLProfile.getProfile(getTargetResourceSet(),
				SysML2CapellaUMLProfile.UMLProfile.SYSML_PROFILE);

		return sysMLProfile;

	}

	@Override
	public UMLBridge<Project, IEditableModelScope> createMappingBridge() {
		UMLBridge<Project, IEditableModelScope> mappingBridge = super.createMappingBridge();
		((Capella2SysmlAlgo) getAlgo()).setBridge(mappingBridge);
		return mappingBridge;
	}

	@Override
	protected GMFDiffPolicy createDiffPolicy() {
		GMFDiffPolicy diffPolicy = new GMFDiffPolicy() {
			public boolean coverOutOfScopeValue(EObject element_p, org.eclipse.emf.ecore.EReference reference_p) {
				return false;
			};

			@Override
			public boolean coverValue(Object value_p, EAttribute attribute_p) {
				return super.coverValue(value_p, attribute_p);
			}

			@Override
			public boolean coverFeature(EStructuralFeature feature_p) {
				if (hasParentProfile(feature_p)) {
					return false;
				}
				return super.coverFeature(feature_p);
			}

			private boolean hasParentProfile(EObject feature_p) {
				if (feature_p == null) {
					return false;
				}
				if (feature_p instanceof Profile) {
					return true;
				} else {
					return hasParentProfile(feature_p.eContainer());
				}

			}

			@Override
			public boolean considerEqual(Object value1_p, Object value2_p, EAttribute attribute_p) {
				return super.considerEqual(value1_p, value2_p, attribute_p);
			}
		};
		return diffPolicy;
	}
}
