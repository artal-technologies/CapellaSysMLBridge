/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.sysml2capella.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.pde.core.target.TargetFeature;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.polarsys.capella.common.data.modellingcore.AbstractExchangeItem;
import org.polarsys.capella.common.data.modellingcore.AbstractType;
import org.polarsys.capella.common.data.modellingcore.TraceableElement;
import org.polarsys.capella.common.helpers.EObjectExt;
import org.polarsys.capella.core.data.capellacommon.AbstractCapabilityPkg;
import org.polarsys.capella.core.data.capellacore.CapellacorePackage;
import org.polarsys.capella.core.data.capellacore.Constraint;
import org.polarsys.capella.core.data.capellacore.EnumerationPropertyLiteral;
import org.polarsys.capella.core.data.capellacore.Feature;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.cs.ActorCapabilityRealizationInvolvement;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.fa.AbstractFunction;
import org.polarsys.capella.core.data.fa.ComponentExchange;
import org.polarsys.capella.core.data.fa.ComponentFunctionalAllocation;
import org.polarsys.capella.core.data.fa.FunctionPkg;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.data.information.Port;
import org.polarsys.capella.core.data.information.datavalue.OpaqueExpression;
import org.polarsys.capella.core.data.interaction.AbstractCapabilityInclude;
import org.polarsys.capella.core.data.la.CapabilityRealization;
import org.polarsys.capella.core.data.la.CapabilityRealizationPkg;
import org.polarsys.capella.core.data.la.LaFactory;
import org.polarsys.capella.core.data.la.LaPackage;
import org.polarsys.capella.core.data.la.LogicalActor;
import org.polarsys.capella.core.data.la.LogicalActorPkg;
import org.polarsys.capella.core.data.la.LogicalArchitecture;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.la.LogicalFunction;
import org.polarsys.capella.core.data.la.LogicalFunctionPkg;

/**
 * @author YBI
 *
 */
public class Sysml2CapellaMappingTest {
	static private File fileTemp;

	@BeforeClass
	static public void setUp() throws IOException {
		Path createTempDirectory = Files.createTempDirectory("cameoToCapella");
		fileTemp = createTempDirectory.toFile();
	}

	static private void deleteLib(File dossierLib) {
		if (dossierLib.listFiles().length != 0) {
			deleteDirectoryContent(dossierLib.listFiles());
		} else {
			dossierLib.delete();
		}
	}

	@AfterClass
	static public void setDown() {
		// deleteLib(fileTemp);
		// fileTemp.deleteOnExit();
	}

	static private void deleteDirectoryContent(File[] files) {
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory() && files[i].listFiles().length != 0) {
				deleteDirectoryContent(files[i].listFiles());
			}
			files[i].delete();
		}
	}

	@Test
	public void classesToComponentsTest() throws IOException {
		// load uml source project.
		String pathUmlModel = "resources/cameoResources/CameoToCapella.uml";
		String pathReferenceModel = "resources/capellaResources/coffeedispense/CoffeeDispense.melodymodeller";
		String pathRefBridgeTrace = "resources/capellaResources/coffeedispense/CoffeeDispense.melodymodeller.bridgetraces";
		String bridgeTraceName = "CoffeeDispense.melodymodeller";
		String pathEmptyProject = "resources/capellaResources/coffeedispense/CoffeeDispense_empty.melodymodeller";
		launchTest(pathUmlModel, pathReferenceModel, pathRefBridgeTrace, bridgeTraceName, "", pathEmptyProject);
	}

	@Test
	public void comp01Test() throws IOException {
		String pathUmlModel = "resources/capellaResources/Components/Comp_01/ComponentCameo01.uml";
		String pathReferenceModel = "resources/capellaResources/Components/Comp_01/Comp_1.melodymodeller";
		String pathRefBridgeTrace = "resources/capellaResources/Components/Comp_01/Comp_1.melodymodeller.bridgetraces";
		String bridgeTraceName = "Comp_1.melodymodeller";
		String pathEmptyProject = "resources/capellaResources/Components/Comp_01/Comp_1_empty.melodymodeller";
		launchTest(pathUmlModel, pathReferenceModel, pathRefBridgeTrace, bridgeTraceName, "comp01Test",
				pathEmptyProject);
	}

	@Test
	public void comp02Test() throws IOException {
		String pathUmlModel = "resources/capellaResources/Components/Comp_02/ComponentCameo02.uml";
		String pathReferenceModel = "resources/capellaResources/Components/Comp_02/Comp_2.melodymodeller";
		String pathRefBridgeTrace = "resources/capellaResources/Components/Comp_02/Comp_2.melodymodeller.bridgetraces";
		String bridgeTraceName = "Comp_2.melodymodeller";
		String pathEmptyProject = "resources/capellaResources/Components/Comp_02/Comp_2_empty.melodymodeller";
		launchTest(pathUmlModel, pathReferenceModel, pathRefBridgeTrace, bridgeTraceName, "comp02Test",
				pathEmptyProject);
	}

	@Test
	public void comp03Test() throws IOException {
		String pathUmlModel = "resources/capellaResources/Components/Comp_03/ComponentCameo03.uml";
		String pathReferenceModel = "resources/capellaResources/Components/Comp_03/Comp_03.melodymodeller";
		String pathRefBridgeTrace = "resources/capellaResources/Components/Comp_03/Comp_03.melodymodeller.bridgetraces";
		String bridgeTraceName = "Comp_03.melodymodeller";
		String pathEmptyProject = "resources/capellaResources/Components/Comp_03/Comp_03_empty.melodymodeller";
		launchTest(pathUmlModel, pathReferenceModel, pathRefBridgeTrace, bridgeTraceName, "comp03Test",
				pathEmptyProject);
	}

	@Test
	public void capa04Test() throws IOException {
		String pathUmlModel = "resources/capellaResources/Capabilities/Capa_04/CapabilitiesCameo04.uml";
		String pathReferenceModel = "resources/capellaResources/Capabilities/Capa_04/Capa_4.melodymodeller";
		String pathRefBridgeTrace = "resources/capellaResources/Capabilities/Capa_04/Capa_4.melodymodeller.bridgetraces";
		String bridgeTraceName = "Capa_4.melodymodeller";
		String pathEmptyProject = "resources/capellaResources/Capabilities/Capa_04/Capa_4_empty.melodymodeller";
		launchTest(pathUmlModel, pathReferenceModel, pathRefBridgeTrace, bridgeTraceName, "capa04Test",
				pathEmptyProject);
	}

	@Test
	public void funct05Test() throws IOException {
		String pathUmlModel = "resources/capellaResources/Functions/Funct_05/FunctionCameo05.uml";
		String pathReferenceModel = "resources/capellaResources/Functions/Funct_05/Funct_5.melodymodeller";
		String pathRefBridgeTrace = "resources/capellaResources/Functions/Funct_05/Funct_5.melodymodeller.bridgetraces";
		String bridgeTraceName = "Funct_5.melodymodeller";
		String pathEmptyProject = "resources/capellaResources/Functions/Funct_05/Funct_5_empty.melodymodeller";
		launchTest(pathUmlModel, pathReferenceModel, pathRefBridgeTrace, bridgeTraceName, "funct05Test",
				pathEmptyProject);
	}

	@Test
	public void actorTest() throws IOException {
		Project actorProject = launchTest2("resources/SysML2Capella/Actor/UML_Export/SysML2Capella_Actor.uml",
				"actorTest", "actor.bridgetraces",
				"resources/SysML2Capella/Actor/empty_project/ActorTest.melodymodeller");

		List<LogicalArchitecture> listLa = EObjectExt.getAll(actorProject, LaPackage.Literals.LOGICAL_ARCHITECTURE)
				.stream().filter(la -> la instanceof LogicalArchitecture).map(LogicalArchitecture.class::cast)
				.collect(Collectors.toList());
		if (listLa != null && !listLa.isEmpty()) {
			LogicalArchitecture logicalArchitecture = listLa.get(0);

			LogicalActorPkg ownedLogicalActorPkg = logicalArchitecture.getOwnedLogicalActorPkg();
			EList<LogicalActor> ownedLogicalActors = ownedLogicalActorPkg.getOwnedLogicalActors();

			// nb of actors = 2
			if (ownedLogicalActors.size() != 2) {
				Assert.fail("The number of actors is wrong. expected number 2. nb actors" + ownedLogicalActors.size());
			}

			// Actor 1
			boolean hasActor1 = false;
			boolean hasActor2 = false;
			for (LogicalActor logicalActor : ownedLogicalActors) {
				if (logicalActor.getName().equals("Actor 1")) {
					hasActor1 = true;
				}
				if (logicalActor.getName().equals("Actor 2")) {
					hasActor2 = true;
				}
			}
			if (!hasActor1) {
				Assert.fail("Missing Actor 1");
			}
			if (!hasActor2) {
				Assert.fail("Missing Actor 2");
			}

		} else {
			Assert.fail("No Logical Architecture in Project " + actorProject.getName());
		}

	}

	@Test
	public void activitiesTest() throws IOException {

		Project activityProject = launchTest2("resources/SysML2Capella/Activities/UML_Export/Activities.uml",
				"activitiesTest", "activities.bridgetraces",
				"resources/SysML2Capella/Activities/empty_project/ActivityTest.melodymodeller");
		List<LogicalArchitecture> listLa = EObjectExt.getAll(activityProject, LaPackage.Literals.LOGICAL_ARCHITECTURE)
				.stream().filter(la -> la instanceof LogicalArchitecture).map(LogicalArchitecture.class::cast)
				.collect(Collectors.toList());
		if (listLa != null && !listLa.isEmpty()) {
			LogicalArchitecture logicalArchitecture = listLa.get(0);

			// check Logical functions
			FunctionPkg ownedFunctionPkg = logicalArchitecture.getOwnedFunctionPkg();
			if (ownedFunctionPkg == null && !(ownedFunctionPkg instanceof LogicalFunctionPkg)) {
				Assert.fail("Missing root functional package");
			}

			String[] listExpectedName = new String[] { "Environment", "Activity 1", "Activity 2", "Activity 3",
					"Activity 2_2" };
			LogicalFunctionPkg lfp = (LogicalFunctionPkg) ownedFunctionPkg;
			EList<LogicalFunction> ownedRootFunctions = lfp.getOwnedLogicalFunctions();
			LogicalFunction rootFucntions = ownedRootFunctions.get(0);
			List<LogicalFunction> ownedLogicalFunctions = rootFucntions.getOwnedFunctions().stream()
					.filter(fc -> fc instanceof LogicalFunction).map(LogicalFunction.class::cast)
					.collect(Collectors.toList());
			if (ownedLogicalFunctions.size() != 4) {
				Assert.fail("Missing function");
			}

			for (LogicalFunction logicalFunction : ownedLogicalFunctions) {
				if (!Arrays.asList(listExpectedName).contains(logicalFunction.getName())) {
					Assert.fail("Bad name" + logicalFunction.getName());
				}
				if (logicalFunction.getName().equals("Activity 2_2")) {
					EList<AbstractFunction> ownedFunctions = logicalFunction.getOwnedFunctions();
					if (ownedFunctions.isEmpty()) {
						Assert.fail("Missing function Activity 2_2");
					}
				}
			}

			// check Component allocations
			LogicalComponent logicalSystem = logicalArchitecture.getOwnedLogicalComponent();

			EList<Feature> ownedFeatures = logicalSystem.getOwnedFeatures();

			List<String> expectedFeatures = new ArrayList<String>();

			expectedFeatures.add("Component 1");
			expectedFeatures.add("Component 2");
			expectedFeatures.add("Component 3");

			for (Feature feature : ownedFeatures) {
				if (!expectedFeatures.contains(feature.getName())) {
					Assert.fail("Bad feature " + feature.getName());
				}

				checkAllocation(feature, "Activity 1", "Component 1");
				checkAllocation(feature, "Activity 2", "Component 2");
				checkAllocation(feature, "Activity 3", "Component 3");

			}

		} else {
			Assert.fail("No Logical Architecture in Project " + activityProject.getName());
		}

	}

	@Test
	public void actorCapabalityRealizationTest() throws IOException {
		Project activityProject = launchTest2(
				"resources/SysML2Capella/ActorCapabilityRealizationAssociation/UML_Export/ActorCapabilityRealizationAssociation.uml",
				"actorCapRealTest", "actorCapabilityRealization.bridgetraces",
				"resources/SysML2Capella/ActorCapabilityRealizationAssociation/empty_project/ActorCapabilitiesRealizationTest.melodymodeller");
		List<LogicalArchitecture> listLa = EObjectExt.getAll(activityProject, LaPackage.Literals.LOGICAL_ARCHITECTURE)
				.stream().filter(la -> la instanceof LogicalArchitecture).map(LogicalArchitecture.class::cast)
				.collect(Collectors.toList());
		if (listLa != null && !listLa.isEmpty()) {
			LogicalArchitecture logicalArchitecture = listLa.get(0);
			LogicalActorPkg ownedLogicalActorPkg = logicalArchitecture.getOwnedLogicalActorPkg();
			EList<LogicalActor> ownedLogicalActors = ownedLogicalActorPkg.getOwnedLogicalActors();

			// nb of actors = 1
			if (ownedLogicalActors.size() != 1) {
				Assert.fail("The number of actors is wrong. expected number 1. nb actors" + ownedLogicalActors.size());
			}

			// Actor 1
			boolean hasActor1 = false;
			for (LogicalActor logicalActor : ownedLogicalActors) {
				if (logicalActor.getName().equals("Actor 1")) {
					hasActor1 = true;
				}
			}
			if (!hasActor1) {
				Assert.fail("Missing Actor 1");
			}

			// check capabilities
			AbstractCapabilityPkg ownedAbstractCapabilityPkg = logicalArchitecture.getOwnedAbstractCapabilityPkg();
			EList<CapabilityRealization> ownedCapabilityRealizations = ((CapabilityRealizationPkg) ownedAbstractCapabilityPkg)
					.getOwnedCapabilityRealizations();
			if (ownedCapabilityRealizations.size() != 2) {
				Assert.fail("Bad capability count (2)");
			}

			List<String> expectedCapR = new ArrayList<>();
			expectedCapR.add("UseCase 1");
			expectedCapR.add("Use case 2");

			for (CapabilityRealization capabilityRealization : ownedCapabilityRealizations) {
				if (!expectedCapR.contains(capabilityRealization.getName())) {
					Assert.fail("Bad Capabilties realization");
				}
				if (capabilityRealization.getName().equals("Use case 2")) {
					EList<AbstractCapabilityInclude> includes = capabilityRealization.getIncludes();
					if (includes.size() != 1) {
						Assert.fail("Bad includes count");
					}
					for (AbstractCapabilityInclude abstractCapabilityInclude : includes) {
						if (!abstractCapabilityInclude.getIncluded().getName().equals("UseCase 1")) {
							Assert.fail("Bad included name");
						}
					}
				}
				if (capabilityRealization.getName().equals("UseCase 1")) {
					EList<ActorCapabilityRealizationInvolvement> ownedActorCapabilityRealizations = capabilityRealization
							.getOwnedActorCapabilityRealizations();
					if (ownedActorCapabilityRealizations.size() != 1) {
						Assert.fail("bad actor realization size");
					}
					for (ActorCapabilityRealizationInvolvement actorCapabilityRealizationInvolvement : ownedActorCapabilityRealizations) {
						if (!((LogicalActor) actorCapabilityRealizationInvolvement.getInvolved()).getName()
								.equals("Actor 1")) {
							Assert.fail("Bad actor realization");
						}
					}
				}
			}

		} else {
			Assert.fail("No Logical Architecture in Project " + activityProject.getName());
		}

	}

	@Test
	public void capabilityRealizationTest() throws IOException {
		Project activityProject = launchTest2(
				"resources/SysML2Capella/CapabilityRealization/UML_Export/CapabilityRealization.uml", "capRealTest",
				"capabilityRealization.bridgetraces",
				"resources/SysML2Capella/CapabilityRealization/empty_project/CapabilityRealizationTest.melodymodeller");
		List<LogicalArchitecture> listLa = EObjectExt.getAll(activityProject, LaPackage.Literals.LOGICAL_ARCHITECTURE)
				.stream().filter(la -> la instanceof LogicalArchitecture).map(LogicalArchitecture.class::cast)
				.collect(Collectors.toList());
		if (listLa != null && !listLa.isEmpty()) {
			LogicalArchitecture logicalArchitecture = listLa.get(0);

			// check capabilities
			AbstractCapabilityPkg ownedAbstractCapabilityPkg = logicalArchitecture.getOwnedAbstractCapabilityPkg();
			EList<CapabilityRealization> ownedCapabilityRealizations = ((CapabilityRealizationPkg) ownedAbstractCapabilityPkg)
					.getOwnedCapabilityRealizations();
			if (ownedCapabilityRealizations.size() != 2) {
				Assert.fail("Bad capability count (2)");
			}

			List<String> expectedCapR = new ArrayList<>();
			expectedCapR.add("UseCase 1");
			expectedCapR.add("UseCase 2");

			for (CapabilityRealization capabilityRealization : ownedCapabilityRealizations) {
				if (!expectedCapR.contains(capabilityRealization.getName())) {
					Assert.fail("Bad Capabilties realization");
				}
				if (capabilityRealization.getName().equals("UseCase 2")) {
					EList<AbstractCapabilityInclude> includes = capabilityRealization.getIncludes();
					if (includes.size() != 1) {
						Assert.fail("Bad includes count");
					}
					for (AbstractCapabilityInclude abstractCapabilityInclude : includes) {
						if (!abstractCapabilityInclude.getIncluded().getName().equals("UseCase 1")) {
							Assert.fail("Bad included name");
						}
					}
				}
			}

		} else {
			Assert.fail("No Logical Architecture in Project " + activityProject.getName());
		}

	}

	@Test
	public void componentsTest() throws IOException {
		Project componentProject = launchTest2("resources/SysML2Capella/Components/UML_Export/Components.uml",
				"componentTest", "component.bridgetraces",
				"resources/SysML2Capella/Components/empty_project/ComponentsTest.melodymodeller");
		List<LogicalArchitecture> listLa = EObjectExt.getAll(componentProject, LaPackage.Literals.LOGICAL_ARCHITECTURE)
				.stream().filter(la -> la instanceof LogicalArchitecture).map(LogicalArchitecture.class::cast)
				.collect(Collectors.toList());
		if (listLa != null && !listLa.isEmpty()) {
			LogicalArchitecture logicalArchitecture = listLa.get(0);

			LogicalComponent logicalSystem = logicalArchitecture.getOwnedLogicalComponent();

			List<String> expectedComponents = new ArrayList<>();
			expectedComponents.add("Component 1");
			expectedComponents.add("Component 2");
			expectedComponents.add("Component 3");
			expectedComponents.add("Component 2_2");

			List<LogicalComponent> ownedLogicalComponents = logicalSystem.getOwnedLogicalComponents();
			for (LogicalComponent logicalComponent : ownedLogicalComponents) {
				if (!expectedComponents.contains(logicalComponent.getName())) {
					Assert.fail("Wrong logicalcomponent count (4)");
				}
				expectedComponents.remove(logicalComponent.getName());
			}
			if (expectedComponents.size() > 0) {
				Assert.fail("Missing components");
			}

			// Part
			expectedComponents.add("Component 1");
			expectedComponents.add("Component 2");
			expectedComponents.add("Component 3");
			expectedComponents.add("Component 2_2");

			List<Feature> features = logicalSystem.getOwnedFeatures();
			for (Feature feature : features) {
				if (!expectedComponents.contains(feature.getName())) {
					Assert.fail("Wrong parts");
				}
				expectedComponents.remove(feature.getName());
				if (feature.getName().equals("Component 2")) {
					AbstractType abstractType = ((Part) feature).getAbstractType();
					if (!((LogicalComponent) abstractType).getName().equals("Component 2")) {
						Assert.fail("Wrong part");
					}
					List<Part> ownedFeatures = ((LogicalComponent) abstractType).getOwnedFeatures().stream()
							.filter(p -> p instanceof Part).map(Part.class::cast).collect(Collectors.toList());
					if (ownedFeatures.size() != 1) {
						Assert.fail("Bad feature");
					}
					Part part = (Part) ownedFeatures.get(0);
					LogicalComponent lc = (LogicalComponent) part.getAbstractType();
					if (!lc.getName().equals("Component 2_2")) {
						Assert.fail("Bad sub component");
					}
				} else {
					AbstractType abstractType = ((Part) feature).getAbstractType();
					List<Part> ownedFeatures = ((LogicalComponent) abstractType).getOwnedFeatures().stream()
							.filter(p -> p instanceof Part).map(Part.class::cast).collect(Collectors.toList());
					if (ownedFeatures.size() != 0) {
						Assert.fail("Bad feature");
					}
				}
			}
			EList<ComponentExchange> ownedComponentExchanges = logicalSystem.getOwnedComponentExchanges();
			if (ownedComponentExchanges.size() != 2) {
				Assert.fail("Missing component exchanges");
			}
			for (ComponentExchange componentExchange : ownedComponentExchanges) {
				Port sourcePort = componentExchange.getSourcePort();
				LogicalComponent sourceLc = (LogicalComponent) sourcePort.eContainer();
				Port targetPort = componentExchange.getTargetPort();
				LogicalComponent targetLc = (LogicalComponent) targetPort.eContainer();

				if (sourceLc.getName().equals("Component 1")) {
					if (!targetLc.getName().equals("Component 2_2")) {
						Assert.fail("Bad component exchange");
					}
				} else if (sourceLc.getName().equals("Component 3")) {
					if (!targetLc.getName().equals("Component 2_2")) {
						Assert.fail("Bad component exchange");
					}
				} else {
					Assert.fail("Bad component exchange");
				}

			}

		} else {
			Assert.fail("No Logical Architecture in Project " + componentProject.getName());
		}

	}

	@Test
	public void constraintsTest() throws IOException {

		Project constraintProject = launchTest2("resources/SysML2Capella/Constraints/UML_Export/Constraints.uml",
				"constraintTest", "constraint.bridgetraces",
				"resources/SysML2Capella/Constraints/empty_project/ConstraintTest.melodymodeller");
		List<LogicalArchitecture> listLa = EObjectExt.getAll(constraintProject, LaPackage.Literals.LOGICAL_ARCHITECTURE)
				.stream().filter(la -> la instanceof LogicalArchitecture).map(LogicalArchitecture.class::cast)
				.collect(Collectors.toList());
		if (listLa != null && !listLa.isEmpty()) {
			LogicalArchitecture logicalArchitecture = listLa.get(0);

			Set<EObject> all = EObjectExt.getAll(logicalArchitecture, CapellacorePackage.Literals.CONSTRAINT);
			List<Constraint> constraints = all.stream().map(Constraint.class::cast).collect(Collectors.toList());

			if (!searchConstraint("constraint 1", "Interface 1", "Interface constraint", constraints)) {
				Assert.fail("Wrong constraint");
			}
			if (!searchConstraint("constraint 2", "Use Case 1", "UseCase constraint", constraints)) {
				Assert.fail("Wrong constraint");
			}
			if (!searchConstraint("constraint 3", "Actor 1", "actor constraint", constraints)) {
				Assert.fail("Wrong constraint");
			}
			if (!searchConstraint("constraint 4", "Activity 1", "acttivity constraint", constraints)) {
				Assert.fail("Wrong constraint");
			}
			if (!searchConstraint("", "Class 1", "constraint in ConstraintBlock", constraints)) {
				Assert.fail("Wrong constraint");
			}
			if (!searchConstraint("", "Component 1", "constraint in ConstraintBlock", constraints)) {
				Assert.fail("Wrong constraint");
			}

		} else {
			Assert.fail("No Logical Architecture in Project " + constraintProject.getName());
		}

	}

	private boolean searchConstraint(String name, String containerName, String expression,
			List<Constraint> constraints) {

		for (Constraint constraint : constraints) {
			if (constraint.getName().equals(name)) {
				// if (((NamedElement)
				// constraint.eContainer()).getName().equals(containerName)) {
				if (((OpaqueExpression) constraint.getOwnedSpecification()).getBodies().get(0).equals(expression)) {
					return true;
				}
				// }
			}
		}

		return false;
	}

	@Test
	public void interfacesTest() throws IOException {

	}

	@Test
	public void requirementsTest() throws IOException {

	}

	@Test
	public void stateModesTest() throws IOException {

	}

	private Project launchTest2(String pathUmlModel, String nameTest, String bridgeTraceName, String pathEmptyProject)
			throws IOException {
		Model model = SysmlToCapellaTestUtils.loadUMLModel(pathUmlModel);

		File targetFile = new File(pathEmptyProject);
		String absolutePath = fileTemp.getAbsolutePath() + "/" + nameTest;
		File d = new File(absolutePath);
		absolutePath = absolutePath + "/" + targetFile.getName();
		File tmpFile = new File(absolutePath);
		if (!tmpFile.getParentFile().exists()) {
			tmpFile.getParentFile().mkdirs();
		}

		Files.copy(targetFile.toPath(), tmpFile.toPath());
		File createdTrace = new File(fileTemp.getAbsolutePath() + "/" + bridgeTraceName + ".bridgetraces");

		// launch the sysml to capella mapping.
		SysmlToCapellaTestUtils.launchSysml2Capella(model, tmpFile, createdTrace.getAbsolutePath());

		Project targetCapellaModel = SysmlToCapellaTestUtils.loadCapellaModel(tmpFile.getAbsolutePath());

		return targetCapellaModel;

	}

	private void launchTest(String pathUmlModel, String pathReferenceModel, String pathRefBridgeTrace,
			String bridgeTraceName, String nameTest, String pathEmptyProject) throws IOException {
		Model model = SysmlToCapellaTestUtils.loadUMLModel(pathUmlModel);

		// load the reference capella project

		File referenceFile = new File(pathReferenceModel);

		// load the empty capella project to fill.
		// and copy them in the the os temp repository.

		File targetFile = new File(pathEmptyProject);
		String absolutePath = fileTemp.getAbsolutePath() + "/" + nameTest;
		File d = new File(absolutePath);
		absolutePath = absolutePath + "/" + targetFile.getName();
		File tmpFile = new File(absolutePath);
		if (!tmpFile.getParentFile().exists()) {
			tmpFile.getParentFile().mkdirs();
		}

		Files.copy(targetFile.toPath(), tmpFile.toPath());

		// create the trace file

		File createdTrace = new File(fileTemp.getAbsolutePath() + "/" + bridgeTraceName + ".bridgetraces");
		// launch the sysml to capella mapping.
		SysmlToCapellaTestUtils.launchSysml2Capella(model, tmpFile, createdTrace.getAbsolutePath());
		// load the created and filled capella project.
		Project targetCapellaModel = SysmlToCapellaTestUtils.loadCapellaModel(tmpFile.getAbsolutePath());

		// load the reference capella project.
		Project refereceCapellaModel = SysmlToCapellaTestUtils.loadCapellaModel(referenceFile.getAbsolutePath());
		// load the reference bridge trace.

		File existingTrace = new File(pathRefBridgeTrace);

		// compare the created capella project and the reference capella
		// project.
		String message = SysmlToCapellaTestUtils.compareResources(targetCapellaModel, refereceCapellaModel,
				createdTrace, existingTrace);

		// check if there is differences.
		if (!message.isEmpty()) {
			Assert.fail(message);
		}
	}

	private void checkAllocation(Feature feature, String anObject, String compName) {
		boolean hasComp1Alloc = false;
		if (feature.getName().equals(compName)) {
			AbstractType abstractType = ((Part) feature).getAbstractType();
			if (abstractType instanceof LogicalComponent) {
				EList<ComponentFunctionalAllocation> ownedFunctionalAllocation = ((LogicalComponent) abstractType)
						.getOwnedFunctionalAllocation();
				for (ComponentFunctionalAllocation componentFunctionalAllocation : ownedFunctionalAllocation) {
					TraceableElement targetElement = componentFunctionalAllocation.getTargetElement();
					if (targetElement instanceof LogicalFunction
							&& ((LogicalFunction) targetElement).getName().equals(anObject)) {
						hasComp1Alloc = true;
					}
				}

			}
			if (!hasComp1Alloc) {
				Assert.fail("Allocation " + anObject + " to " + compName);
			}
		}

	}

}
