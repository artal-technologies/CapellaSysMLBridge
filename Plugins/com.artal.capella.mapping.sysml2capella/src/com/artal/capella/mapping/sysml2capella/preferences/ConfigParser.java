/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.sysml2capella.preferences;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * {@link ConfigParser} contains the specific SysML structural data.
 * 
 * @author YBI
 *
 */
public class ConfigParser {

	private String _configPath;

	/**
	 * Constructor.
	 * 
	 * @param configPath
	 *            the configuration file path.
	 */
	public ConfigParser(String configPath) {
		_configPath = configPath;
	}

	/**
	 * Read the xml file path.
	 * 
	 * @return the {@link SysMLConfiguration}.
	 */
	public SysMLConfiguration parse() {

		File xmlConfig = new File(_configPath);
		if (!xmlConfig.exists()) {
			return null;
		}

		SysMLConfiguration configuration = new SysMLConfiguration();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(xmlConfig);

			Element documentElement = document.getDocumentElement();
			NodeList childNodes = documentElement.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node item = childNodes.item(i);
				if (item.getNodeName().equals("partPath")) {
					String attribute = ((Element) item).getAttribute("path");
					configuration.setPartPath(attribute);
				}
				if (item.getNodeName().equals("productPath")) {
					String attribute = ((Element) item).getAttribute("path");
					configuration.setProductPath(attribute);
				}
				if (item.getNodeName().equals("useCasesPath")) {
					String attribute = ((Element) item).getAttribute("path");
					configuration.setUseCasesPath(attribute);
				}
				if (item.getNodeName().equals("activitiesPath")) {
					String attribute = ((Element) item).getAttribute("path");
					configuration.setActivitiesPath(attribute);
				}
				if (item.getNodeName().equals("parametricPath")) {
					String attribute = ((Element) item).getAttribute("path");
					configuration.setParametricPath(attribute);
				}
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return configuration;

	}

}
