package snownee.jade.api.config;

import net.minecraft.resource.Identifier;
import snownee.jade.api.IToggleableProvider;

import java.util.Set;

/**
 * Read-only interface for Waila internal config storage.<br>
 * An instance of this interface is passed to most of Waila callbacks as a way to change the behavior depending on client settings.
 *
 * @author ProfMobius
 */
public interface IPluginConfig {

	/**
	 * Gets a collection of all the keys for a given namespace.
	 *
	 * @param namespace The namespace to get keys from
	 * @return all the keys for a given namespace.
	 */
	Set<Identifier> getKeys(String namespace);

	/**
	 * Gets a collection of all keys.
	 *
	 * @return all registered keys.
	 */
	Set<Identifier> getKeys();

	boolean get(IToggleableProvider provider);

	boolean get(Identifier key);

	<T extends Enum<T>> T getEnum(Identifier key);

	int getInt(Identifier key);

	float getFloat(Identifier key);

	String getString(Identifier key);

}
