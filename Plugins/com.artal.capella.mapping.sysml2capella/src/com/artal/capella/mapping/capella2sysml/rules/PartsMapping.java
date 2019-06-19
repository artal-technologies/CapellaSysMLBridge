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
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.polarsys.capella.common.data.modellingcore.AbstractType;
import org.polarsys.capella.core.data.capellacore.Feature;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.la.LogicalComponent;

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
public class PartsMapping extends AbstractMapping {

	private Project _source;
	private IMappingExecution _mappingExecution;

	public PartsMapping(CapellaBridgeAlgo<?> algo, Project source, IMappingExecution mappingExecution) {
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
		LogicalComponent logicalSystemRoot = Sysml2CapellaUtils.getLogicalSystemRoot(_source);

		ResourceSet rset = null;
		Object targetDataSet = _mappingExecution.getTargetDataSet();
		if (targetDataSet instanceof FragmentedModelScope) {
			rset = ((FragmentedModelScope) targetDataSet).getResources().get(0).getResourceSet();
		}
		Profile profile = SysML2CapellaUMLProfile.getProfile(rset, UMLProfile.MD_CUST_SYSML_ADD_STEREO_PROFILE);
		Stereotype ownedStereotype = profile.getOwnedStereotype("PartProperty");

		transformParts(logicalSystemRoot, ownedStereotype);

	}

	/**
	 * @param lc
	 * @param ownedStereotype
	 */
	private void transformParts(LogicalComponent lc, Stereotype ownedStereotype) {
		Class umlParent = (Class) MappingRulesManager.getCapellaObjectFromAllRules(lc);
		EList<Feature> ownedFeatures = lc.getOwnedFeatures();
		for (Feature feature : ownedFeatures) {
			if (feature instanceof Part) {
				Part part = (Part) feature;
				AbstractType abstractType = part.getAbstractType();

				Class type = (Class) MappingRulesManager.getCapellaObjectFromAllRules(abstractType);
				Property partProperty = umlParent.createOwnedAttribute(part.getName(), type);
				partProperty.applyStereotype(ownedStereotype);
				Sysml2CapellaUtils.trace(this, _source.eResource(), feature, partProperty, "PART_PROP_");
				
			}
		}
		EList<LogicalComponent> ownedLogicalComponents = lc.getOwnedLogicalComponents();
		for (LogicalComponent logicalComponent : ownedLogicalComponents) {
			transformParts(logicalComponent, ownedStereotype);
		}

	}

}
