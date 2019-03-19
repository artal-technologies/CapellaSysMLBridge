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
		Model model = SysmlToCapellaTestUtils.loadUMLModel("resources/cameoResources/CameoToCapella.uml");

		// load the reference capella project
		File referenceFile = new File(
				"resources/capellaResources/referenceProject/CapellaProjectReference.melodymodeller");

		// load the empty capella project to fill.
		// and copy them in the the os temp repository.
		File targetFile = new File("resources/capellaResources/emptyproject/CapellaProjectEmpty.melodymodeller");
		String absolutePath = fileTemp.getAbsolutePath();
		absolutePath = absolutePath + "/" + targetFile.getName();
		File tmpFile = new File(absolutePath);
		Files.copy(targetFile.toPath(), tmpFile.toPath());

		// create the trace file
		File createdTrace = new File(fileTemp.getAbsolutePath() + "/test.bridgetraces");

		// launch the sysml to capella mapping.
		SysmlToCapellaTestUtils.launchSysml2Capella(model, tmpFile, createdTrace.getAbsolutePath());
		// load the created and filled capella project.
		Project targetCapellaModel = SysmlToCapellaTestUtils.loadCapellaModel(tmpFile.getAbsolutePath());

		// load the reference capella project.
		Project refereceCapellaModel = SysmlToCapellaTestUtils.loadCapellaModel(referenceFile.getAbsolutePath());
		// load the reference bridge trace.
		File existingTrace = new File("resources/capellaResources/referenceProject/test.bridgetraces");

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
