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
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.uml2.uml.UMLFactory;
import org.polarsys.capella.core.data.capellacommon.Region;
import org.polarsys.capella.core.data.capellacommon.State;
import org.polarsys.capella.core.data.capellacommon.StateMachine;
import org.polarsys.capella.core.data.capellacore.CapellaElement;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class RegionsMapping extends AbstractMapping {

	private CapellaElement _source;
	private IMappingExecution _mappingExecution;
	private MappingRulesManager _manager = new MappingRulesManager();

	public RegionsMapping(CapellaBridgeAlgo<?> algo, CapellaElement source, IMappingExecution mappingExecution) {
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
		List<Region> regions = getRegions();
		Object umlSource = MappingRulesManager.getCapellaObjectFromAllRules(_source);

		for (Region region : regions) {
			transformRegion(region, umlSource);
		}
		_manager.executeRules();

	}

	private void transformRegion(Region region, Object umlSource) {
		org.eclipse.uml2.uml.Region umlRegion = UMLFactory.eINSTANCE.createRegion();
		umlRegion.setName(region.getName());
		if (umlSource instanceof org.eclipse.uml2.uml.StateMachine) {
			((org.eclipse.uml2.uml.StateMachine) umlSource).getRegions().add(umlRegion);
		} else if (umlSource instanceof org.eclipse.uml2.uml.State) {
			((org.eclipse.uml2.uml.State) umlSource).getRegions().add(umlRegion);
		}
		Sysml2CapellaUtils.trace(this, _source.eResource(), region, umlRegion, "REGIONS_");

		// Mode
		ModesMapping modesMapping = new ModesMapping(getAlgo(), region, _mappingExecution);
		_manager.add(modesMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(_source.eResource(), region),
				modesMapping);
	}

	/**
	 * Get the {@link Region} list under a StateMachine or State source.
	 * 
	 * @return {@link List}
	 */
	private List<Region> getRegions() {
		if (_source instanceof StateMachine) {
			return new ArrayList<>(((StateMachine) _source).getOwnedRegions());
		} else if (_source instanceof State) {
			return new ArrayList<>(((State) _source).getOwnedRegions());
		}

		return Collections.emptyList();
	}

}
