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

import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.polarsys.capella.core.data.capellacommon.CapellacommonFactory;
import org.polarsys.capella.core.data.capellacommon.Mode;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * Transform UML/SysML {@link Region} to Capella
 * {@link org.polarsys.capella.core.data.capellacommon.Region}
 * 
 * @author YBI
 *
 */
public class RegionsMapping extends AbstractMapping {
	/**
	 * The sysml root {@link Element}.
	 */
	Element _source;
	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;
	/**
	 * A {@link MappingRulesManager} allowing to manage the sub rules.
	 */
	MappingRulesManager _manager = new MappingRulesManager();

	/**
	 * Constructor.
	 * 
	 * @param algo
	 *            the {@link Sysml2CapellaAlgo} algo.
	 * @param source
	 *            the {@link Element} sysml element. (shall be StateMachine or
	 *            State)
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public RegionsMapping(Sysml2CapellaAlgo algo, Element source, IMappingExecution mappingExecution) {
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
		List<Region> regions = getRegions();
		Collections.sort(regions, new Comparator<Region>() {
			@Override
			public int compare(Region o1, Region o2) {
				String sysMLID = Sysml2CapellaUtils.getSysMLID(eResource, o1);
				String sysMLID2 = Sysml2CapellaUtils.getSysMLID(eResource, o2);
				return sysMLID.compareTo(sysMLID2);
			}
		});
		transformRegions(eResource, regions);

		_manager.executeRules();

	}

	/**
	 * Transform {@link Region} to
	 * {@link org.polarsys.capella.core.data.capellacommon.Region}
	 * 
	 * @param eResource
	 *            the sysml model
	 * @param regions
	 *            the {@link Region} to transform
	 */
	private void transformRegions(Resource eResource, List<Region> regions) {
		for (Region sysMLRegion : regions) {
			org.polarsys.capella.core.data.capellacommon.Region capellaRegion = CapellacommonFactory.eINSTANCE
					.createRegion();
			capellaRegion.setName(sysMLRegion.getName());
			Sysml2CapellaUtils.trace(this, eResource, sysMLRegion, capellaRegion, "Region_");
			Object sm = MappingRulesManager.getCapellaObjectFromAllRules(_source);
			if (sm instanceof org.polarsys.capella.core.data.capellacommon.StateMachine) {
				((org.polarsys.capella.core.data.capellacommon.StateMachine) sm).getOwnedRegions().add(capellaRegion);
			} else if (sm instanceof Mode) {
				((Mode) sm).getOwnedRegions().add(capellaRegion);
			} else {
				continue;
			}
			VerticesMapping verticesMapping = new VerticesMapping(getAlgo(), sysMLRegion, _mappingExecution);
			_manager.add(verticesMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(eResource, sysMLRegion),
					verticesMapping);
		}
	}

	/**
	 * Get the {@link Region} list under a StateMachine or State source.
	 * 
	 * @return {@link List}
	 */
	private List<Region> getRegions() {
		if (_source instanceof StateMachine) {
			return new ArrayList<>(((StateMachine) _source).getRegions());
		} else if (_source instanceof State) {
			return new ArrayList<>(((State) _source).getRegions());
		}

		return Collections.emptyList();
	}

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}

}
