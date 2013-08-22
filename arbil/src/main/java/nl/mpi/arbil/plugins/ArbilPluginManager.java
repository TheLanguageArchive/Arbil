/**
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.plugins;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbilcommons.journal.ArbilJournal;
import nl.mpi.flap.module.BaseModule;
import nl.mpi.flap.plugin.ActivatablePlugin;
import nl.mpi.flap.plugin.ArbilWindowPlugin;
import nl.mpi.flap.plugin.JournalWatcherPlugin;
import nl.mpi.flap.plugin.PluginArbilDataNodeLoader;
import nl.mpi.flap.plugin.PluginException;
import nl.mpi.pluginloader.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ArbilPluginManager <br> Created on Aug 22, 2012, 5:59:39 PM <br>
 *
 * @author Peter Withers <br>
 */
public class ArbilPluginManager implements PluginManager {

    private final static Logger logger = LoggerFactory.getLogger(ArbilPluginManager.class);
    public static final String ARCHIVING_PLUGINS_DIR_NAME = "ArchivingPlugins";
    final private SessionStorage arbilSessionStorage;
    final private ArbilWindowManager dialogHandler;
    final private BugCatcher bugCatcher;
    final private PluginArbilDataNodeLoader arbilDataNodeLoader;
    final private HashSet<BaseModule> hashSet = new HashSet<BaseModule>();
    private final ResourceBundle services = java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Services");

    public ArbilPluginManager(SessionStorage arbilSessionStorage, ArbilWindowManager dialogHandler, PluginArbilDataNodeLoader arbilDataNodeLoader, BugCatcher bugCatcher) {
	this.arbilSessionStorage = arbilSessionStorage;
	this.dialogHandler = dialogHandler;
	this.arbilDataNodeLoader = arbilDataNodeLoader;
	this.bugCatcher = bugCatcher;
    }

    public void activatePlugin(BaseModule kinOathPlugin) {
	boolean pluginActivated = false;
	if (kinOathPlugin instanceof JournalWatcherPlugin) {
	    try {
		final ArbilJournal arbilJournal = ArbilJournal.getSingleInstance();
		arbilJournal.addJounalWatcher((JournalWatcherPlugin) kinOathPlugin);
		((JournalWatcherPlugin) kinOathPlugin).initJournalWatcher(arbilJournal);
	    } catch (PluginException exception) {
		dialogHandler.addMessageDialogToQueue(MessageFormat.format(services.getString("FAILED TO PASS THE JOURNAL TO THE REQUESTED PLUGIN") + "\n{0}\n{1}\n{2}.{3}.{4}\n{5}", new Object[]{exception.getMessage(), kinOathPlugin.getName(), kinOathPlugin.getMajorVersionNumber(), kinOathPlugin.getMinorVersionNumber(), kinOathPlugin.getBuildVersionNumber(), kinOathPlugin.getDescription()}), "Journal Plugin Error");
	    }
	}
	if (kinOathPlugin instanceof ActivatablePlugin) {
	    try {
		((ActivatablePlugin) kinOathPlugin).activatePlugin(dialogHandler, arbilSessionStorage);
		hashSet.add(kinOathPlugin);
		pluginActivated = true;
	    } catch (PluginException exception) {
		dialogHandler.addMessageDialogToQueue(MessageFormat.format(services.getString("FAILED TO ACTIVATE THE REQUESTED PLUGIN") + "\n{0}\n{1}\n{2}.{3}.{4}\n{5}", new Object[]{exception.getMessage(), kinOathPlugin.getName(), kinOathPlugin.getMajorVersionNumber(), kinOathPlugin.getMinorVersionNumber(), kinOathPlugin.getBuildVersionNumber(), kinOathPlugin.getDescription()}), "Enable Plugin Error");
	    }
	}
	if (kinOathPlugin instanceof ArbilWindowPlugin) {
	    try {
		dialogHandler.createWindow(kinOathPlugin.getName(), ((ArbilWindowPlugin) kinOathPlugin).getUiPanel(dialogHandler, arbilSessionStorage, bugCatcher, arbilDataNodeLoader, dialogHandler));
		pluginActivated = true;
	    } catch (PluginException exception) {
		dialogHandler.addMessageDialogToQueue(MessageFormat.format(services.getString("FAILED TO SHOW THE REQUESTED PLUGIN") + "\n{0}\n{1}\n{2}.{3}.{4}\n{5}", new Object[]{exception.getMessage(), kinOathPlugin.getName(), kinOathPlugin.getMajorVersionNumber(), kinOathPlugin.getMinorVersionNumber(), kinOathPlugin.getBuildVersionNumber(), kinOathPlugin.getDescription()}), "Enable Plugin Error");
	    }
	}
	if (!pluginActivated) {
	    dialogHandler.addMessageDialogToQueue(MessageFormat.format(services.getString("NO METHOD TO ACTIVATE THIS TYPE OF PLUGIN YET") + "\n{0}\n{1}.{2}.{3}\n{4}", new Object[]{kinOathPlugin.getName(), kinOathPlugin.getMajorVersionNumber(), kinOathPlugin.getMinorVersionNumber(), kinOathPlugin.getBuildVersionNumber(), kinOathPlugin.getDescription()}), "Enable Plugin");
	}
    }

    public void deactivatePlugin(BaseModule kinOathPlugin) {
	if (kinOathPlugin instanceof ActivatablePlugin) {
	    try {
		((ActivatablePlugin) kinOathPlugin).deactivatePlugin(dialogHandler, arbilSessionStorage);
		hashSet.remove(kinOathPlugin);
	    } catch (PluginException exception) {
		dialogHandler.addMessageDialogToQueue(MessageFormat.format(services.getString("FAILED TO DEACTIVATE THE REQUESTED PLUGIN") + "\n{0}\n{1}\n{2}.{3}.{4}\n{5}", new Object[]{exception.getMessage(), kinOathPlugin.getName(), kinOathPlugin.getMajorVersionNumber(), kinOathPlugin.getMinorVersionNumber(), kinOathPlugin.getBuildVersionNumber(), kinOathPlugin.getDescription()}), "Enable Plugin Error");
	    }
	} else {
	    dialogHandler.addMessageDialogToQueue(MessageFormat.format(services.getString("NO METHOD TO DEACTIVATE THIS TYPE OF PLUGIN YET") + "\n{0}\n{1}.{2}.{3}\n{4}", new Object[]{kinOathPlugin.getName(), kinOathPlugin.getMajorVersionNumber(), kinOathPlugin.getMinorVersionNumber(), kinOathPlugin.getBuildVersionNumber(), kinOathPlugin.getDescription()}), "Enable Plugin");
	}
    }

    public boolean isActivated(BaseModule kinOathPlugin) {
	return hashSet.contains(kinOathPlugin);
    }

    public List<URL> getPluginsFromDirectoriesAndPluginsList() {
	// Using a set to strip out duplicates
	final Collection<URL> pluginsSet = new HashSet<URL>();
	pluginsSet.addAll(getPluginsFromDirectories());
	pluginsSet.addAll(getPluginsFromPluginList());
	return new ArrayList<URL>(pluginsSet);
    }

    public Collection<URL> getPluginsFromDirectories() {
	final Collection<URL> pluginURLs = new HashSet<URL>();

	final FileFilter jarFileFilter = new FileFilter() {
	    public boolean accept(File pathname) {
		return pathname.toString().toLowerCase().endsWith(".jar");
	    }
	};

	final String[] locationOptions = new String[]{
	    //System.getProperty("user.dir") is unreliable in the case of Vista and possibly others
	    //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6519127
	    System.getProperty("user.home") + File.separatorChar + "Local Settings" + File.separatorChar + "Application Data" + File.separatorChar + ARCHIVING_PLUGINS_DIR_NAME + File.separatorChar,
	    System.getenv("APPDATA") + File.separatorChar + ARCHIVING_PLUGINS_DIR_NAME + File.separatorChar,
	    System.getProperty("user.home") + File.separatorChar + ARCHIVING_PLUGINS_DIR_NAME + File.separatorChar,
	    System.getenv("USERPROFILE") + File.separatorChar + ARCHIVING_PLUGINS_DIR_NAME + File.separatorChar,
	    System.getProperty("user.dir") + File.separatorChar + ARCHIVING_PLUGINS_DIR_NAME + File.separatorChar,
	    new File(arbilSessionStorage.getProjectDirectory(), ARCHIVING_PLUGINS_DIR_NAME).getAbsolutePath(),
	    new File(arbilSessionStorage.getProjectWorkingDirectory(), ARCHIVING_PLUGINS_DIR_NAME).getAbsolutePath()
	};

	for (String locationOption : locationOptions) {
	    File pluginDir = new File(locationOption);
	    if (pluginDir.isDirectory()) {
		File[] jarFiles = pluginDir.listFiles(jarFileFilter);
		for (File jarFile : jarFiles) {
		    try {
			pluginURLs.add(jarFile.toURI().toURL());
		    } catch (MalformedURLException ex) {
			// Should not happen, URI comes from file location
			bugCatcher.logError(ex);
		    }
		}
	    }
	}
	return pluginURLs;
    }

    public Collection<URL> getPluginsFromPluginList() {
	final Collection<URL> pluginUlrs = new HashSet<URL>();
	String errorMessages = "";
	try {
	    final String[] pluginStringArray = arbilSessionStorage.loadStringArray("PluginList");
	    if (pluginStringArray != null) {
		for (String pluginString : pluginStringArray) {
		    try {
			pluginUlrs.add(new URL(pluginString));
		    } catch (MalformedURLException exception) {
			logger.warn(exception.getMessage());
			errorMessages = errorMessages + MessageFormat.format(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("COULD NOT LOAD PLUGIN: {0}"), new Object[]{pluginString});
		    }
		}
//            } else {
//                sessionStorage.saveStringArray("PluginList", new String[]{"file:///<path to plugin>.jar", "file:///<path to plugin>.jar"});
	    }
	    if (!"".equals(errorMessages)) {
		dialogHandler.addMessageDialogToQueue(errorMessages, "Plugin Error");
	    }
	} catch (IOException ex) {
	    // if the list is not found then we need not worry at this point.
	    logger.debug("PluginList not found");
	}
	return pluginUlrs;
    }
}
