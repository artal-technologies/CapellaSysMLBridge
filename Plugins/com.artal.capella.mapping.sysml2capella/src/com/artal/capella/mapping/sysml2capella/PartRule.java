/**
 * 
 */
package com.artal.capella.mapping.sysml2capella;

import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IQueryExecution;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IQueryHolder;
import org.eclipse.emf.diffmerge.bridge.mapping.impl.QueryAndRule;
import org.eclipse.emf.diffmerge.bridge.mapping.impl.QueryAndRule.QueryAndRuleIdentifier;
import org.eclipse.emf.diffmerge.bridge.util.structures.Tuple2;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.la.LogicalComponent;

/**
 * @author binot
 *
 */
public class PartRule extends QueryAndRule<Model, Class, Part> {

	
	public static QueryAndRuleIdentifier<org.eclipse.uml2.uml.Class, Part> ID = new QueryAndRuleIdentifier<>(
			"PartRule");

	
	public PartRule(IQueryHolder<? extends Model> parent_p,
			org.eclipse.emf.diffmerge.bridge.mapping.impl.QueryAndRule.QueryAndRuleIdentifier<Class, Part> id_p) {
		super(parent_p, id_p);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.emf.diffmerge.bridge.mapping.api.IQuery#evaluate(java.lang.Object, org.eclipse.emf.diffmerge.bridge.mapping.api.IQueryExecution)
	 */
	@Override
	public Iterable<Class> evaluate(Model input_p, IQueryExecution queryExecution_p) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.emf.diffmerge.bridge.mapping.impl.QueryAndRule#createTarget(java.lang.Object, org.eclipse.emf.diffmerge.bridge.mapping.api.IQueryExecution)
	 */
	@Override
	public Part createTarget(Class source_p, IQueryExecution queryExecution_p) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.emf.diffmerge.bridge.mapping.impl.QueryAndRule#defineTarget(java.lang.Object, java.lang.Object, org.eclipse.emf.diffmerge.bridge.mapping.api.IQueryExecution, org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution)
	 */
	@Override
	public void defineTarget(Class source_p, Part target_p, IQueryExecution queryExecution_p,
			IMappingExecution mappingExecution_p) {
		// TODO Auto-generated method stub

	}

}
