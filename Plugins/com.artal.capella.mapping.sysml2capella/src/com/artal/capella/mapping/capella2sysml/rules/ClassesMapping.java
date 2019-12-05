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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.diffmerge.api.scopes.IModelScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLFactory;
import org.polarsys.capella.common.helpers.EObjectExt;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.information.Class;
import org.polarsys.capella.core.data.information.DataPkg;
import org.polarsys.capella.core.data.information.InformationPackage;
import org.polarsys.capella.core.data.information.datatype.DatatypePackage;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.capella2sysml.Capella2SysmlAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile.UMLProfile;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class ClassesMapping extends AbstractMapping {

	/**
	 * The capella source element.
	 */
	private Project _source;

	/**
	 * The {@link IMappingExecution} allows to get the mapping data.
	 */
	private IMappingExecution _mappingExecution;

	/**
	 * A {@link MappingRulesManager} allowing to manage the sub rules.
	 */
	MappingRulesManager _manager = new MappingRulesManager();

	/**
	 * Constructor.
	 * 
	 * @param algo
	 *            the {@link Capella2SysmlAlgo} algo.
	 * @param source
	 *            the source Capella {@link Project}.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public ClassesMapping(CapellaBridgeAlgo<?> algo, Project source, IMappingExecution mappingExecution) {
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

		// the package containing the parametric data.
		Package paramPkg = (Package) MappingRulesManager.getCapellaObjectFromAllRules(_source + "PARAMETRIC_PKG");
		// the capella data package root containing the classes to transform.
		DataPkg dataPkgRoot = Sysml2CapellaUtils.getDataPkgRoot(_source);
		// transform.
		transformClasses(paramPkg, dataPkgRoot);

		// execute the sub rules.
		_manager.executeRules();

	}

	/**
	 * Transform {@link Class} to {@link org.eclipse.uml2.uml.Class}
	 * 
	 * @param paramPkg
	 *            the uml package containing the parametric data to fill with
	 *            transformed classes
	 * @param dataPkgRoot
	 *            the source package containing all the {@link Class} to
	 *            transform.
	 */
	private void transformClasses(Package paramPkg, DataPkg dataPkgRoot) {
		IModelScope targetDataSet = (IModelScope) _mappingExecution.getTargetDataSet();
		ResourceSet rset = Sysml2CapellaUtils.getTargetResourceSet(targetDataSet);
		Profile profile = SysML2CapellaUMLProfile.getProfile(rset, UMLProfile.SYSML_PROFILE);

		Stereotype ownedStereotype = profile.getNestedPackage("Blocks").getOwnedStereotype("Block");
		Stereotype ownedStereotypeValueType = profile.getNestedPackage("Blocks").getOwnedStereotype("ValueType");

		// get all classes and classes in sub packages
		Set<EObject> all = EObjectExt.getAll(dataPkgRoot, InformationPackage.eINSTANCE.getClass_());
		List<Class> classes = all.stream().filter(cl -> cl instanceof Class).map(Class.class::cast)
				.collect(Collectors.toList());

		for (org.polarsys.capella.core.data.information.Class clazz : classes) {
			if (!clazz.isIsPrimitive()) {
				transformClass(clazz, paramPkg, ownedStereotype);
			} else {
				transformDataType(clazz, paramPkg, ownedStereotypeValueType);
			}
			PropertiesMapping propertiesMapping = new PropertiesMapping(getAlgo(), clazz, _mappingExecution);
			_manager.add(
					propertiesMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(_source.eResource(), clazz),
					propertiesMapping);
		}
		Set<EObject> allNT = EObjectExt.getAll(dataPkgRoot, DatatypePackage.eINSTANCE.getDataType());
		List<org.polarsys.capella.core.data.information.datatype.DataType> dataTypes = allNT.stream()
				.filter(cl -> cl instanceof org.polarsys.capella.core.data.information.datatype.DataType)
				.map(org.polarsys.capella.core.data.information.datatype.DataType.class::cast)
				.collect(Collectors.toList());

		for (org.polarsys.capella.core.data.information.datatype.DataType clazz : dataTypes) {
			transformDataType(clazz, paramPkg, ownedStereotypeValueType);
			PropertiesMapping propertiesMapping = new PropertiesMapping(getAlgo(), clazz, _mappingExecution);
			_manager.add(
					propertiesMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(_source.eResource(), clazz),
					propertiesMapping);
		}
	}

	/**
	 * Transform {@link Class} to {@link org.eclipse.uml2.uml.Class}.
	 * 
	 * @param clazz
	 *            the Capella {@link Class} to transfom
	 * @param paramPkg
	 *            the uml package to fill with transformed classes.
	 * @param ownedStereotype
	 *            the stereotype to apply at transformed class.
	 */
	private void transformClass(org.polarsys.capella.core.data.information.Class clazz, Package paramPkg,
			Stereotype ownedStereotype) {
		org.eclipse.uml2.uml.Class umlClass = paramPkg.createOwnedClass(clazz.getName(), false);
		EObject applyStereotype = umlClass.applyStereotype(ownedStereotype);
		getAlgo().getStereoApplications().add(applyStereotype);
		Sysml2CapellaUtils.trace(this, _source.eResource(), clazz, umlClass, "CLASS_");

	}

	private void transformDataType(org.polarsys.capella.core.data.information.Class clazz, Package paramPkg,
			Stereotype ownedStereotype) {
		DataType dataType = UMLFactory.eINSTANCE.createDataType();
		dataType.setName(clazz.getName());
		paramPkg.getPackagedElements().add(dataType);

		EObject applyStereotype = dataType.applyStereotype(ownedStereotype);
		getAlgo().getStereoApplications().add(applyStereotype);
		Sysml2CapellaUtils.trace(this, _source.eResource(), clazz, dataType, "DATATYPE_");

	}

	private void transformDataType(org.polarsys.capella.core.data.information.datatype.DataType clazz, Package paramPkg,
			Stereotype ownedStereotype) {
		DataType dataType = UMLFactory.eINSTANCE.createDataType();
		dataType.setName(clazz.getName());
		paramPkg.getPackagedElements().add(dataType);

		EObject applyStereotype = dataType.applyStereotype(ownedStereotype);
		getAlgo().getStereoApplications().add(applyStereotype);
		Sysml2CapellaUtils.trace(this, _source.eResource(), clazz, dataType, "DATATYPE_");

	}

	@Override
	public Capella2SysmlAlgo getAlgo() {
		// TODO Auto-generated method stub
		return (Capella2SysmlAlgo) super.getAlgo();
	}

}
