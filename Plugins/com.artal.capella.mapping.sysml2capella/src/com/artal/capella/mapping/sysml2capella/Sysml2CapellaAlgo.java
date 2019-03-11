/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package com.artal.capella.mapping.sysml2capella;

import java.util.Date;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.capella.integration.util.CapellaUtil;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.polarsys.capella.core.data.la.LaFactory;
import org.polarsys.capella.core.data.la.LogicalComponent;

import com.artal.capella.mapping.CapellaBridgeAlgo;

/**
 * @author binot
 *
 */
public class Sysml2CapellaAlgo extends CapellaBridgeAlgo<Model> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.artal.capella.mapping.CapellaBridgeAlgo#launch()
	 */
	@Override
	public void launch(Model source, IMappingExecution mappingExecution_p) {
		EList<PackageableElement> packagedElements = source.getPackagedElements();
		for (PackageableElement structurePkg : packagedElements) {
			if (structurePkg instanceof Package && structurePkg.getName().equals("03 Structure")) {
				EList<PackageableElement> packagedElements2 = ((Package) structurePkg).getPackagedElements();

				for (PackageableElement partPkg : packagedElements2) {
					if (partPkg instanceof Package && partPkg.getName().equals("Parts")) {
						EList<PackageableElement> packagedElements3 = ((Package) partPkg).getPackagedElements();
						for (PackageableElement elementClass : packagedElements3) {
							if (elementClass instanceof Class) {

								LogicalComponent lComponent = LaFactory.eINSTANCE.createLogicalComponent();
								lComponent.setName(elementClass.getName());
								String date = new Date().toString();
								lComponent.setDescription(date);
								CapellaUpdateScope targetScope = mappingExecution_p.getTargetDataSet();
								LogicalComponent rootPhysicalSystem = CapellaUtil
										.getLogicalSystemRoot(targetScope.getProject());
								rootPhysicalSystem.getOwnedLogicalComponents().add(lComponent);
								Resource eResource = elementClass.eResource();
								String id = "LogicalComponent_";
								if (eResource instanceof XMIResource) {
									id += ((XMIResource) eResource).getID(elementClass);
								}
								add(id, lComponent);
								
							}
						}

					}
				}
			}
		}
	}

}
