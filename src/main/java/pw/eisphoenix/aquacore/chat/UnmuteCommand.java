package pw.eisphoenix.aquacore.chat;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pw.eisphoenix.aquacore.cmd.CCommand;
import pw.eisphoenix.aquacore.cmd.CommandInfo;
import pw.eisphoenix.aquacore.dependency.Inject;
import pw.eisphoenix.aquacore.service.MessageService;
import pw.eisphoenix.aquacore.service.MuteService;
import pw.eisphoenix.aquacore.service.PlayerInfoService;

import java.util.List;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@CommandInfo(
        name = "unmute",
        description = "Unmutes a Player",
        usage = "/%CMD% <player>",
        permission = "core.unmute"
)
public final class UnmuteCommand extends CCommand {
    @Inject
    private PlayerInfoService playerInfoService;
    @Inject
    private MuteService muteService;
    @Inject
    private MessageService messageService;

    public void onCommand(final CommandSender sender, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return;
        }

        playerInfoService.getPlayer(args[0]).thenAccept(cPlayer -> {
            if (cPlayer == null) {
                messageService.getMessage("cmd.error.player", MessageService.MessageType.WARNING).thenAccept(
                        message -> sender.sendMessage(message.replaceAll("%NAME%", args[0]))
                );
                return;
            }
            if (!muteService.isMuted(cPlayer.getUuid()).join()) {
                messageService.getMessage("mute.notmuted", MessageService.MessageType.WARNING).thenAccept(
                        message -> sender.sendMessage(message.replaceAll("%NAME%", cPlayer.getActualUsername()))
                );
                return;
            }
            if (!muteService.unmutePlayer(cPlayer.getUuid(),
                    sender instanceof Player ? ((Player) sender).getUniqueId() : null).join()) {
                messageService.getMessage("mute.error.nounmute", MessageService.MessageType.ERROR).thenAccept(
                        message -> sender.sendMessage(message.replaceAll("%NAME%", cPlayer.getActualUsername()))
                );
                return;
            }
            messageService.getMessage("mute.success.unmute", MessageService.MessageType.INFO).thenAccept(
                    message -> sender.sendMessage(message.replaceAll("%NAME%", cPlayer.getActualUsername()))
            );
        });
    }

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");

        if (args.length == 1) {
            return super.onTabComplete(sender, args);
        }
        return ImmutableList.of();
    }
}
