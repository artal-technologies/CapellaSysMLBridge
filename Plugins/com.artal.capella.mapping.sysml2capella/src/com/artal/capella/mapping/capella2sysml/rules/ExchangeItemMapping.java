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
import org.eclipse.emf.diffmerge.api.scopes.IModelScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Class;
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
public class ExchangeItemMapping extends AbstractMapping {

	private Project _source;
	private IMappingExecution _mappingExecution;
	MappingRulesManager _manager = new MappingRulesManager();

	public ExchangeItemMapping(CapellaBridgeAlgo<?> algo, Project source, IMappingExecution mappingExecution) {
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
		EList<ExchangeItem> ownedExchangeItems = dataPkgRoot.getOwnedExchangeItems();
		IModelScope targetDataSet = (IModelScope) _mappingExecution.getTargetDataSet();
		ResourceSet rset = Sysml2CapellaUtils.getTargetResourceSet(targetDataSet);
		Profile profile = SysML2CapellaUMLProfile.getProfile(rset, UMLProfile.SYSML_PROFILE);
		Stereotype ownedStereotype = profile.getNestedPackage("Ports&Flows").getOwnedStereotype("InterfaceBlock");
		for (ExchangeItem exchangeItem : ownedExchangeItems) {
			transformExchangeItem(exchangeItem, paramPkg, ownedStereotype);

			PropertiesMapping propertiesMapping = new PropertiesMapping(getAlgo(), exchangeItem, _mappingExecution);
			_manager.add(propertiesMapping.getClass().getName()
					+ Sysml2CapellaUtils.getSysMLID(_source.eResource(), exchangeItem), propertiesMapping);

		}

		_manager.executeRules();

	}

	private void transformExchangeItem(ExchangeItem exchangeItem, Package paramPkg, Stereotype ownedStereotype) {

		Class umlEI = paramPkg.createOwnedClass(exchangeItem.getName(), false);
		umlEI.applyStereotype(ownedStereotype);
		Sysml2CapellaUtils.trace(this, _source.eResource(), exchangeItem, umlEI, "EI_");

	}

}
