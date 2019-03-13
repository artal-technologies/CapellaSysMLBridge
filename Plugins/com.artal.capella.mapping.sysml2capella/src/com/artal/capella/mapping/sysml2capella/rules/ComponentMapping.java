/**
 * 
 */
package com.artal.capella.mapping.sysml2capella.rules;

import java.util.Date;
import java.util.List;

import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.capella.integration.util.CapellaUtil;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.polarsys.capella.core.data.la.LaFactory;
import org.polarsys.capella.core.data.la.LogicalComponent;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * 
 * The sysml classes contained in the "03 Structure/Parts" uml package from a
 * Cameo project are transposed as Component Capella.
 * 
 * @author YBI
 *
 * 
 *
 */
public class ComponentMapping extends AbstractMapping {

	/**
	 * The sysml root {@link Model}.
	 */
	Model _source;
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
	 *            the {@link Model} sysml model.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public ComponentMapping(Sysml2CapellaAlgo algo, Model source, IMappingExecution mappingExecution) {
		super(algo);
		_source = source;
		_mappingExecution = mappingExecution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.artal.capella.mapping.rules.AbstractMapping#computeMapping()
	 */
	public void computeMapping() {

		List<Class> classes = Sysml2CapellaUtils.getClasses(_source, "03 Structure/Parts");
		Resource eResource = _source.eResource();
		CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
		LogicalComponent rootPhysicalSystem = CapellaUtil.getLogicalSystemRoot(targetScope.getProject());
		
		for (Class class1 : classes) {
			LogicalComponent lComponent = LaFactory.eINSTANCE.createLogicalComponent();
			lComponent.setName(class1.getName());
			String date = new Date().toString();
			lComponent.setDescription(date);
			
			rootPhysicalSystem.getOwnedLogicalComponents().add(lComponent);

			Sysml2CapellaUtils.trace(this, eResource, class1, lComponent, "LogicalComponent_");
			// transpose the port
			ComponentPortMapping componentPortMapping = new ComponentPortMapping(getAlgo(), class1, _mappingExecution);
			_manager.add(ComponentPortMapping.class.getName() + Sysml2CapellaUtils.getSysMLID(eResource, class1),
					componentPortMapping);

		}

		_manager.executeRules();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.artal.capella.mapping.rules.AbstractMapping#getAlgo()
	 */
	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}
}
