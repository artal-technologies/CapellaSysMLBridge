/**
 * 
 */
package com.artal.capella.mapping.sysml2capella;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.capella.integration.util.CapellaUtil;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IQueryExecution;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IQueryHolder;
import org.eclipse.emf.diffmerge.bridge.mapping.impl.QueryAndRule;
import org.eclipse.emf.diffmerge.bridge.util.structures.Tuple2;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.polarsys.capella.core.data.cs.CsFactory;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.la.LaFactory;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponent;

/**
 * @author binot
 *
 */
public class ComponentRule extends QueryAndRule<Model, org.eclipse.uml2.uml.Class, Tuple2<LogicalComponent, Part>> {

	public static QueryAndRuleIdentifier<org.eclipse.uml2.uml.Class, Tuple2<LogicalComponent, Part>> ID = new QueryAndRuleIdentifier<>(
			"LogicalComponentRule");

	public ComponentRule(IQueryHolder<? extends Model> parent_p) {
		super(parent_p, ID);
	}

	@Override
	public Iterable<org.eclipse.uml2.uml.Class> evaluate(Model input_p, IQueryExecution queryExecution_p) {
		List<org.eclipse.uml2.uml.Class> results = new ArrayList<>();
		// results.add(input_p);
		EList<PackageableElement> packagedElements = input_p.getPackagedElements();
		for (PackageableElement structurePkg : packagedElements) {
			if (structurePkg instanceof Package && structurePkg.getName().equals("03 Structure")) {
				EList<PackageableElement> packagedElements2 = ((Package) structurePkg).getPackagedElements();

				for (PackageableElement partPkg : packagedElements2) {
					if (partPkg instanceof Package && partPkg.getName().equals("Parts")) {
						EList<PackageableElement> packagedElements3 = ((Package) partPkg).getPackagedElements();
						for (PackageableElement elementClass : packagedElements3) {
							if (elementClass instanceof Class) {
								results.add((Class) elementClass);
							}
						}

					}
				}
			}
		}
		return results;
	}

	@Override
	public Tuple2<LogicalComponent, Part> createTarget(org.eclipse.uml2.uml.Class source_p,
			IQueryExecution queryExecution_p) {

		LogicalComponent createLogicalComponent = LaFactory.eINSTANCE.createLogicalComponent();
		Part createPart = CsFactory.eINSTANCE.createPart();

		createPart.setAbstractType(createLogicalComponent);
		return new Tuple2<LogicalComponent, Part>(createLogicalComponent, createPart);
	}

	@Override
	public void defineTarget(org.eclipse.uml2.uml.Class source_p, Tuple2<LogicalComponent, Part> target_p,
			IQueryExecution queryExecution_p, IMappingExecution mappingExecution_p) {
		LogicalComponent lc = target_p.get1();
		Part part = target_p.get2();
		part.setName(source_p.getName());
		lc.setName(source_p.getName());

		String date = new Date().toString();
		lc.setDescription(date);
		part.setDescription(date);

		CapellaUpdateScope targetScope = mappingExecution_p.getTargetDataSet();
		LogicalComponent rootPhysicalSystem = CapellaUtil.getLogicalSystemRoot(targetScope.getProject());
		rootPhysicalSystem.getOwnedLogicalComponents().add(lc);
		rootPhysicalSystem.getOwnedFeatures().add(part);
	}

}
