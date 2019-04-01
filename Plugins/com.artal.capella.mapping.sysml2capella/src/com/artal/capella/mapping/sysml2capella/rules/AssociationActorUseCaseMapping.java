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

import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.polarsys.capella.core.data.cs.ActorCapabilityRealizationInvolvement;
import org.polarsys.capella.core.data.cs.CsFactory;
import org.polarsys.capella.core.data.la.CapabilityRealization;
import org.polarsys.capella.core.data.la.LogicalActor;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class AssociationActorUseCaseMapping extends AbstractMapping {

	/**
	 * The sysml root {@link Model}.
	 */
	Model _source;
	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;

	/**
	 * Constructor.
	 * 
	 * @param algo
	 *            the {@link Sysml2CapellaAlgo} algo.
	 * @param source
	 *            the {@link Model} sysml model.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public AssociationActorUseCaseMapping(Sysml2CapellaAlgo algo, Model source, IMappingExecution mappingExecution) {
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
		List<Association> associations = Sysml2CapellaUtils.getAssociations(_source,
				getAlgo().getConfiguration().getUseCasesPath());
		Resource eResource = _source.eResource();

		for (Association association : associations) {
			ActorCapabilityRealizationInvolvement acri = CsFactory.eINSTANCE
					.createActorCapabilityRealizationInvolvement();
			EList<Property> ownedEnds = association.getOwnedEnds();
			if (ownedEnds.size() == 2) {// TODO Is it possible more ?
				Property source = ownedEnds.get(0);
				Property target = ownedEnds.get(1);

				Type sourceType = source.getType();
				Type targetType = target.getType();

				ActorMapping actorRule = (ActorMapping) MappingRulesManager.getRule(ActorMapping.class.getName());
				Map<Object, Object> mapSToTActor = actorRule.getMapSourceToTarget();
				Map<Object, Object> mapSToTUseCase = MappingRulesManager.getRule(UseCaseMapping.class.getName())
						.getMapSourceToTarget();
				Object object = mapSToTActor.get(sourceType);

				CapabilityRealization cr = null;
				if (object == null) {
					object = mapSToTActor.get(targetType);
					cr = (CapabilityRealization) mapSToTUseCase.get(sourceType);
				} else {
					cr = (CapabilityRealization) mapSToTUseCase.get(targetType);
				}

				LogicalActor partActor = null;
				if (object instanceof List) {
					List<?> list = (List<?>) object;
					for (Object object2 : list) {
						if (object2 instanceof LogicalActor) {
							partActor = (LogicalActor) object2;
						}
					}
				}

				acri.setInvolved(partActor);
				cr.getOwnedActorCapabilityRealizations().add(acri);

				Sysml2CapellaUtils.trace(this, eResource, association, acri, "AssociationActorUseCase_");
			}
		}

	}

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		// TODO Auto-generated method stub
		return (Sysml2CapellaAlgo) super.getAlgo();
	}

}
