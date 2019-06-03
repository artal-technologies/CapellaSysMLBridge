/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.sysml2capella.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage;
import org.polarsys.capella.common.helpers.EObjectExt;
import org.polarsys.capella.core.data.information.DataPkg;
import org.polarsys.capella.core.data.information.ExchangeItem;
import org.polarsys.capella.core.data.information.ExchangeMechanism;
import org.polarsys.capella.core.data.information.InformationFactory;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class SignalMapping extends AbstractMapping {

	/**
	 * The sysml root {@link Model}.
	 */
	Model _source;
	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;

	public SignalMapping(Sysml2CapellaAlgo algo, Model source, IMappingExecution mappingExecution) {
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
		Resource eResource = _source.eResource();
		List<Signal> sysMLSignals = getSysMLSignals(_source);

		CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
		DataPkg dataPkgRoot = Sysml2CapellaUtils.getDataPkgRoot(targetScope.getProject());

		// sort the sysml constraint to be sure the order doesn't change at the
		// coevolution.
		Collections.sort(sysMLSignals, new Comparator<org.eclipse.uml2.uml.Signal>() {
			@Override
			public int compare(org.eclipse.uml2.uml.Signal o1, org.eclipse.uml2.uml.Signal o2) {
				String sysMLID = Sysml2CapellaUtils.getSysMLID(eResource, o1);
				String sysMLID2 = Sysml2CapellaUtils.getSysMLID(eResource, o2);
				return sysMLID.compareTo(sysMLID2);
			}
		});

		for (Signal signal : sysMLSignals) {

			ExchangeItem ei = InformationFactory.eINSTANCE.createExchangeItem();
			ei.setName(signal.getName());
			ei.setExchangeMechanism(ExchangeMechanism.EVENT);
			dataPkgRoot.getOwnedExchangeItems().add(ei);

			Sysml2CapellaUtils.trace(this, eResource, signal, ei, "_ExchangeItemEvent");
		}
	}

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}

	/**
	 * Get all the Sysml {@link Class} requirement. The Class with
	 * "SysML::Requirements::Requirement" stereotype
	 * 
	 * @param source
	 *            the source Package.
	 * @return {@link List}
	 */
	private List<Signal> getSysMLSignals(Package source) {
		EList<PackageableElement> packagedElements = source.getPackagedElements();
		List<Signal> signals = new ArrayList<Signal>();
		for (PackageableElement eObject : packagedElements) {
			if (eObject instanceof Package) {
				Stereotype appliedStereotype = ((Package) eObject)
						.getAppliedStereotype("MagicDraw Profile::auxiliaryResource");
				if (appliedStereotype == null) {
					Set<EObject> all = EObjectExt.getAll(eObject, UMLPackage.Literals.SIGNAL);
					for (EObject eObject2 : all) {
						if (eObject2 instanceof org.eclipse.uml2.uml.Signal) {
							signals.add((Signal) eObject2);
						}
					}
				}
			}
		}
		return signals;
	}

}
