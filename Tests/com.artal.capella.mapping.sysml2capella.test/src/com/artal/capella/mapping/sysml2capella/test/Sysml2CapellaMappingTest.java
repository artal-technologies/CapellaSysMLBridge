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

import org.eclipse.uml2.uml.Model;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.polarsys.capella.core.data.capellamodeller.Project;

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
		deleteLib(fileTemp);
		fileTemp.deleteOnExit();
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
		String pathReferenceModel = "resources/capellaResources/referenceProject/CapellaProjectReference.melodymodeller";
		String pathRefBridgeTrace = "resources/capellaResources/referenceProject/test.bridgetraces";
		String bridgeTraceName = "test";
		String pathEmptyProject = "resources/capellaResources/emptyproject/CapellaProjectEmpty.melodymodeller";
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

}
