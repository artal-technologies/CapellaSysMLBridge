/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.capella2sysml.rules;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.diffmerge.impl.scopes.AbstractEditableModelScope;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.ProfileApplication;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellamodeller.Project;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.capella2sysml.Capella2SysmlAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class RootInitialMapping extends AbstractMapping {

	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;
	Project _source;
	CapellaBridgeAlgo<?> _algo;

	public RootInitialMapping(CapellaBridgeAlgo<?> algo, Project source, IMappingExecution mappingExecution) {
		super(algo);
		_mappingExecution = mappingExecution;
		_source = source;
		_algo = algo;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.artal.capella.mapping.rules.AbstractMapping#computeMapping()
	 */
	@Override
	public void computeMapping() {
		Object targetDataSet = _mappingExecution.getTargetDataSet();

		Model model = createModel(targetDataSet);

		org.eclipse.uml2.uml.Package requierments = createPackage(model, "01 Requirements", _source,
				"REQUIEREMENTS_PKG");
		createPackage(requierments, "01 Textual Requirements", _source, "TEXTUALREQUIREMENT");
		createPackage(requierments, "02 Formalised Requirements", _source, "FORMALISEDREQUIREMENT");
		org.eclipse.uml2.uml.Package behavior = createPackage(model, "02 Behavior", _source, "BEHAVIOR_PKG");
		createPackage(behavior, "02 Use Cases", _source, "USECASES");
		org.eclipse.uml2.uml.Package structure = createPackage(model, "03 Structure", _source, "STRUCTURE_PKG");
		createPackage(structure, "Parts", _source, "PARTS");
		createPackage(model, "04 Parametric", _source, "PARAMETRIC_PKG");
		// Apply to the model

		Sysml2CapellaUtils.trace(this, _source.eResource(), _source, model, "Model");
	}

	/**
	 * @return
	 */
	protected org.eclipse.uml2.uml.Package createPackage(org.eclipse.uml2.uml.Package parent, String name,
			CapellaElement source, String prefix) {
		org.eclipse.uml2.uml.Package pkg = UMLFactory.eINSTANCE.createPackage();
		pkg.setName(name);
		parent.getPackagedElements().add(pkg);
		Sysml2CapellaUtils.trace(this, _source.eResource(), source + prefix, pkg, prefix);
		return pkg;
	}

	/**
	 * @param targetDataSet
	 * @return
	 */
	protected Model createModel(Object targetDataSet) {
		Model model = UMLFactory.eINSTANCE.createModel();

		ResourceSet rset = null;
		if (targetDataSet instanceof AbstractEditableModelScope) {
			rset = Sysml2CapellaUtils.getTargetResourceSet((AbstractEditableModelScope) targetDataSet);
			UMLResourcesUtil.initLocalRegistries(rset);
			List<Profile> umlStdProfiles = SysML2CapellaUMLProfile.getProfiles(rset);
			for (Profile profile : umlStdProfiles) {
				model.applyProfile(profile);
			}
			EList<ProfileApplication> allProfileApplications = model.getAllProfileApplications();
			for (ProfileApplication profileApplication : allProfileApplications) {
				getAlgo().getProfileApplication().add(profileApplication);
				// EAnnotation eAnnotation =
				// profileApplication.getEAnnotation(UMLUtil.UML2_UML_PACKAGE_2_0_NS_URI);
				// Profile appliedProfile =
				// profileApplication.getAppliedProfile();
				// Sysml2CapellaUtils.trace(this, _source.eResource(),
				// appliedProfile.getURI() + appliedProfile.getName(),
				// eAnnotation, "Annotation");
				// Sysml2CapellaUtils.trace(this, _source.eResource(),
				// appliedProfile.getURI() + appliedProfile.getName(),
				// profileApplication, "ProfileAppl");
			}
			((AbstractEditableModelScope) targetDataSet).add(model);
		}
		return model;
	}

	@Override
	public Capella2SysmlAlgo getAlgo() {
		return (Capella2SysmlAlgo) super.getAlgo();
	}

}
