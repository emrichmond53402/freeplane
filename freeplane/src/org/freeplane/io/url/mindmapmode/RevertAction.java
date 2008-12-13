/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.io.url.mindmapmode;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import javax.swing.JOptionPane;

import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.main.Tools;
import org.freeplane.map.tree.MapController;
import org.freeplane.map.tree.MapModel;
import org.freeplane.modes.ModeController;

/**
 * Reverts the map to the saved version. In Xml, the old map is stored as xml
 * and as an undo action, the new map is stored, too. Moreover, the filename of
 * the doAction is set to the appropriate map file's name. The undo action has
 * no file name associated. The action goes like this: close the actual map and
 * open the given Xml/File. If only a Xml string is given, a temporary file name
 * is created, the xml stored into and this map is opened instead of the actual.
 *
 * @author foltin
 */
class RevertAction extends FreeplaneAction {
	private static class RevertActionInstance {
		private String filePrefix;
		private String localFileName;
		private String map;

		public void act() {
			final MapController mapController = Controller.getController()
			    .getModeController().getMapController();
			try {
				Controller.getController().close(true);
				if (this.getLocalFileName() != null) {
					mapController.newMap(Tools.fileToUrl(new File(this
					    .getLocalFileName())));
				}
				else {
					String filePrefix = Controller.getText("freemind_reverted");
					if (this.getFilePrefix() != null) {
						filePrefix = this.getFilePrefix();
					}
					final File tempFile = File
					    .createTempFile(
					        filePrefix,
					        org.freeplane.io.url.mindmapmode.FileManager.FREEMIND_FILE_EXTENSION,
					        new File(Controller.getResourceController()
					            .getFreemindUserDirectory()));
					final FileWriter fw = new FileWriter(tempFile);
					fw.write(this.getMap());
					fw.close();
					mapController.newMap(Tools.fileToUrl(tempFile));
				}
			}
			catch (final Exception e) {
				org.freeplane.main.Tools.logException(e);
			}
		}

		public String getFilePrefix() {
			return filePrefix;
		}

		public String getLocalFileName() {
			return localFileName;
		}

		public String getMap() {
			return map;
		}

		public void setFilePrefix(final String filePrefix) {
			this.filePrefix = filePrefix;
		}

		public void setLocalFileName(final String localFileName) {
			this.localFileName = localFileName;
		}

		public void setMap(final String map) {
			this.map = map;
		}
	}

	/**
	 */
	public RevertAction() {
		super("RevertAction", (String) null);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent arg0) {
		try {
			final File file = Controller.getController().getMap().getFile();
			if (file == null) {
				final ModeController controller = getModeController();
				JOptionPane.showMessageDialog(controller.getMapView(),
				    controller.getText("map_not_saved"), "FreeMind",
				    JOptionPane.ERROR_MESSAGE);
				return;
			}
			final RevertActionInstance doAction = createRevertXmlAction(file);
			doAction.act();
		}
		catch (final IOException e) {
			org.freeplane.main.Tools.logException(e);
		}
	}

	public RevertActionInstance createRevertXmlAction(final File file)
	        throws IOException {
		final String fileName = file.getAbsolutePath();
		final FileReader f = new FileReader(file);
		final StringBuffer buffer = new StringBuffer();
		for (int c; (c = f.read()) != -1;) {
			buffer.append((char) c);
		}
		f.close();
		return createRevertXmlAction(buffer.toString(), fileName, null);
	}

	public RevertActionInstance createRevertXmlAction(final MapModel map,
	                                                  final String fileName,
	                                                  final String filePrefix)
	        throws IOException {
		final StringWriter writer = new StringWriter();
		map.getModeController().getMapController().writeMapAsXml(map, writer,
		    true);
		return createRevertXmlAction(writer.getBuffer().toString(), fileName,
		    filePrefix);
	}

	/**
	 * @param filePrefix
	 *            is used to generate the name of the reverted map in case that
	 *            fileName is null.
	 */
	public RevertActionInstance createRevertXmlAction(
	                                                  final String xmlPackedFile,
	                                                  final String fileName,
	                                                  final String filePrefix) {
		final RevertActionInstance revertXmlAction = new RevertActionInstance();
		revertXmlAction.setLocalFileName(fileName);
		revertXmlAction.setMap(xmlPackedFile);
		revertXmlAction.setFilePrefix(filePrefix);
		return revertXmlAction;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.controller.actions.ActorXml#getDoActionClass()
	 */
	public void openXmlInsteadOfMap(final String xmlFileContent) {
		final RevertActionInstance doAction = createRevertXmlAction(
		    xmlFileContent, null, null);
		doAction.act();
	}
}
