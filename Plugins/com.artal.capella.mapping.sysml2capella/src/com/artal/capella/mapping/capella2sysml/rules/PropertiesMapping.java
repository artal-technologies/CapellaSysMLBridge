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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.diffmerge.api.scopes.IModelScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.polarsys.capella.common.data.modellingcore.AbstractType;
import org.polarsys.capella.common.data.modellingcore.AbstractTypedElement;
import org.polarsys.capella.common.data.modellingcore.FinalizableElement;
import org.polarsys.capella.core.data.capellacore.Feature;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.information.Class;
import org.polarsys.capella.core.data.information.ExchangeItem;
import org.polarsys.capella.core.data.information.ExchangeItemElement;
import org.polarsys.capella.core.data.information.MultiplicityElement;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;
import org.polarsys.capella.core.data.information.datavalue.NumericValue;

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
public class PropertiesMapping extends AbstractMapping {

	private FinalizableElement _source;
	private IMappingExecution _mappingExecution;

	public PropertiesMapping(CapellaBridgeAlgo<?> algo, FinalizableElement source, IMappingExecution mappingExecution) {
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
		org.eclipse.uml2.uml.Class classParent = (org.eclipse.uml2.uml.Class) MappingRulesManager
				.getCapellaObjectFromAllRules(_source);

		List<NamedElement> attributes = getAllAttributes();

		for (NamedElement namedElement : attributes) {
			if (namedElement instanceof ExchangeItemElement || namedElement instanceof Feature) {
				transformProperty(namedElement, classParent);
			}
		}

	}

	/**
	 * @return
	 */
	private List<NamedElement> getAllAttributes() {
		List<NamedElement> attributes = new ArrayList<>();
		if (_source instanceof Class) {
			attributes.addAll(((Class) _source).getOwnedFeatures());

		} else if (_source instanceof ExchangeItem) {
			attributes.addAll(((ExchangeItem) _source).getOwnedElements());

		}
		return attributes;
	}

	private void transformProperty(NamedElement namedElement, org.eclipse.uml2.uml.Class classParent) {
		Property property = UMLFactory.eINSTANCE.createProperty();
		property.setName(namedElement.getName());
		classParent.getOwnedAttributes().add(property);
		Sysml2CapellaUtils.trace(this, _source.eResource(), namedElement, property, "PROPERTY_");

		IModelScope targetDataSet = (IModelScope) _mappingExecution.getTargetDataSet();
		ResourceSet rset = Sysml2CapellaUtils.getTargetResourceSet(targetDataSet);
		Profile profile = SysML2CapellaUMLProfile.getProfile(rset, UMLProfile.MD_CUST_SYSML_ADD_STEREO_PROFILE);
		Stereotype ownedStereotype = profile.getOwnedStereotype("ValueProperty");
		EObject applyStereotype = property.applyStereotype(ownedStereotype);
		Sysml2CapellaUtils.trace(this, _source.eResource(), namedElement + "VALUEPARAM_STEREO", applyStereotype,
				"VALUEPARAM_STEREO_");
		getAlgo().getTransientItems().add(applyStereotype);

		if (namedElement instanceof MultiplicityElement) {
			transformMultiplicity(namedElement, property);
		}
		if (namedElement instanceof AbstractTypedElement) {
			transformType(namedElement, property);
		}

	}

	private void transformType(NamedElement namedElement, Property property) {
		AbstractType abstractType = ((AbstractTypedElement) namedElement).getAbstractType();
		ResourceSet targetResourceSet = Sysml2CapellaUtils.getTargetResourceSet(_mappingExecution.getTargetDataSet());

		// profile.getty
		if (abstractType != null && Sysml2CapellaUtils.isPrimiriveType(abstractType)) {
			Type ptype = Sysml2CapellaUtils.getPrimitiveType(abstractType, targetResourceSet);
			property.setType(ptype);
			
		} else {
			Object capellaObjectFromAllRules = MappingRulesManager.getCapellaObjectFromAllRules(abstractType);
			if (capellaObjectFromAllRules instanceof Type) {
				property.setType((Type) capellaObjectFromAllRules);
			}
		}
	}

	private void transformMultiplicity(NamedElement namedElement, Property property) {
		NumericValue ownedMinCard = ((MultiplicityElement) namedElement).getOwnedMinCard();
		if (ownedMinCard instanceof LiteralNumericValue) {
			String value = ((LiteralNumericValue) ownedMinCard).getValue();
			int parseInt = Integer.parseInt(value);
			property.setLower(parseInt);
		}

		NumericValue ownedMaxCard = ((MultiplicityElement) namedElement).getOwnedMaxCard();
		if (ownedMaxCard instanceof LiteralNumericValue) {
			String value = ((LiteralNumericValue) ownedMaxCard).getValue();
			if (value.equals("*")) {
				property.setUpper(-1);
			} else {
				int parseInt = Integer.parseInt(value);
				property.setUpper(parseInt);
			}
		}
	}

}
