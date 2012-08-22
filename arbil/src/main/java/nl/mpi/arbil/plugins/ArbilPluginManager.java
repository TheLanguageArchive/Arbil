package nl.mpi.arbil.plugins;

import java.util.HashSet;
import nl.mpi.arbil.plugin.ActivatablePlugin;
import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.kinnate.plugin.BasePlugin;
import nl.mpi.pluginloader.PluginManager;

/**
 * Document : ArbilPluginManager <br> Created on Aug 22, 2012, 5:59:39 PM <br>
 *
 * @author Peter Withers <br>
 */
public class ArbilPluginManager implements PluginManager {

    final private SessionStorage arbilSessionStorage;
    final private ArbilWindowManager dialogHandler;
    final private HashSet<BasePlugin> hashSet = new HashSet<BasePlugin>();

    public ArbilPluginManager(SessionStorage arbilSessionStorage, ArbilWindowManager dialogHandler) {
        this.arbilSessionStorage = arbilSessionStorage;
        this.dialogHandler = dialogHandler;
    }

    public void activatePlugin(BasePlugin kinOathPlugin) {
        if (kinOathPlugin instanceof ActivatablePlugin) {
            try {
                ((ActivatablePlugin) kinOathPlugin).activatePlugin(dialogHandler, arbilSessionStorage);
                hashSet.add(kinOathPlugin);
            } catch (PluginException exception) {
                dialogHandler.addMessageDialogToQueue("Failed to activate the requested plugin.\n" + exception.getMessage() + "\n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription(), "Enable Plugin Error");
            }
        } else {
            dialogHandler.addMessageDialogToQueue("No method to activate this type of plugin yet.\n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription(), "Enable Plugin");
        }
    }

    public void deactivatePlugin(BasePlugin kinOathPlugin) {
        if (kinOathPlugin instanceof ActivatablePlugin) {
            try {
                ((ActivatablePlugin) kinOathPlugin).deactivatePlugin(dialogHandler, arbilSessionStorage);
                hashSet.remove(kinOathPlugin);
            } catch (PluginException exception) {
                dialogHandler.addMessageDialogToQueue("Failed to deactivate the requested plugin.\n" + exception.getMessage() + "\n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription(), "Enable Plugin Error");
            }
        } else {
            dialogHandler.addMessageDialogToQueue("No method to deactivate this type of plugin yet.\n" + kinOathPlugin.getName() + "\n" + kinOathPlugin.getMajorVersionNumber() + "." + kinOathPlugin.getMinorVersionNumber() + "." + kinOathPlugin.getBuildVersionNumber() + "\n" + kinOathPlugin.getDescription(), "Enable Plugin");
        }
    }

    public boolean isActivated(BasePlugin kinOathPlugin) {
        return hashSet.contains(kinOathPlugin);
    }
}
