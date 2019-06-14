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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.diffmerge.impl.scopes.FragmentedModelScope;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.information.DataPkg;
import org.polarsys.capella.core.data.information.ExchangeItem;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile.UMLProfile;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class ClassesMapping extends AbstractMapping {

	private Project _source;
	private IMappingExecution _mappingExecution;
	MappingRulesManager _manager = new MappingRulesManager();

	public ClassesMapping(CapellaBridgeAlgo<?> algo, Project source, IMappingExecution mappingExecution) {
		super(algo);
		_source = source;
		_mappingExecution = mappingExecution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.artal.capella.mapping.rules.AbstractMapping#computeMapping()
	 */
	@Override
	public void computeMapping() {
		DataPkg dataPkgRoot = Sysml2CapellaUtils.getDataPkgRoot(_source);
		Package paramPkg = (Package) MappingRulesManager.getCapellaObjectFromAllRules(_source + "PARAMETRIC_PKG");
		EList<org.polarsys.capella.core.data.information.Class> ownedClasses = dataPkgRoot.getOwnedClasses();
		Object targetDataSet = _mappingExecution.getTargetDataSet();
		ResourceSet rset = null;
		if (targetDataSet instanceof FragmentedModelScope) {
			rset = ((FragmentedModelScope) targetDataSet).getResources().get(0).getResourceSet();
		}
		Profile profile = SysML2CapellaUMLProfile.getProfile(rset, UMLProfile.SYSML_PROFILE);
		Stereotype ownedStereotype = profile.getNestedPackage("Blocks").getOwnedStereotype("Block");
		for (org.polarsys.capella.core.data.information.Class clazz : ownedClasses) {
			transformClass(clazz, paramPkg, ownedStereotype);
			PropertiesMapping propertiesMapping = new PropertiesMapping(getAlgo(), clazz, _mappingExecution);
			_manager.add(
					propertiesMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(_source.eResource(), clazz),
					propertiesMapping);
		}
		_manager.executeRules();

	}

	private void transformClass(org.polarsys.capella.core.data.information.Class clazz, Package paramPkg,
			Stereotype ownedStereotype) {

		org.eclipse.uml2.uml.Class umlClass = paramPkg.createOwnedClass(clazz.getName(), false);
		umlClass.applyStereotype(ownedStereotype);
		Sysml2CapellaUtils.trace(this, _source.eResource(), clazz, umlClass, "CLASS_");

	}

}
