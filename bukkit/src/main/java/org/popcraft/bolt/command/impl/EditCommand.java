package org.popcraft.bolt.command.impl;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.popcraft.bolt.BoltPlugin;
import org.popcraft.bolt.command.Arguments;
import org.popcraft.bolt.command.BoltCommand;
import org.popcraft.bolt.util.Action;
import org.popcraft.bolt.util.BoltComponents;
import org.popcraft.bolt.util.BoltPlayer;
import org.popcraft.bolt.util.Source;
import org.popcraft.bolt.lang.Translation;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.popcraft.bolt.lang.Translator.translate;

public class EditCommand extends BoltCommand {
    public EditCommand(BoltPlugin plugin) {
        super(plugin);
    }

    @Override
    public void execute(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof final Player player)) {
            BoltComponents.sendMessage(sender, Translation.COMMAND_PLAYER_ONLY);
            return;
        }
        if (arguments.remaining() >= 3) {
            final BoltPlayer boltPlayer = plugin.player(player);
            boltPlayer.setAction(Action.EDIT);
            final String type = arguments.next();
            final String inputIdentifier = arguments.next();
            String identifier;
            if (Source.PLAYER.equals(type)) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(inputIdentifier);
                } catch (IllegalArgumentException ignored) {
                    uuid = null;
                }
                identifier = (uuid == null ? plugin.getUuidCache().getUniqueId(inputIdentifier) : uuid).toString();
            } else {
                identifier = inputIdentifier;
            }
            final String access = arguments.next();
            boltPlayer.getModifications().put(Source.from(type, identifier), access);
            BoltComponents.sendMessage(player, Translation.CLICK_ACTION, Placeholder.unparsed("action", translate(Translation.EDIT)));
        } else {
            BoltComponents.sendMessage(sender, Translation.COMMAND_NOT_ENOUGH_ARGS);
        }
    }

    @Override
    public List<String> suggestions(Arguments arguments) {
        if (arguments.remaining() == 0) {
            return Collections.emptyList();
        }
        arguments.next();
        if (arguments.remaining() == 0) {
            return List.of(Source.PLAYER);
        }
        arguments.next();
        if (arguments.remaining() == 0) {
            return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
        }
        arguments.next();
        if (arguments.remaining() == 0) {
            return plugin.getBolt().getAccessRegistry().types();
        }
        return Collections.emptyList();
    }
}
