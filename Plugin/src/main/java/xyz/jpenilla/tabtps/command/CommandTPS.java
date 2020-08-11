package xyz.jpenilla.tabtps.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.util.TPSUtil;

@CommandAlias("tickinfo|mspt")
public class CommandTPS extends BaseCommand {
    @Dependency
    private TabTPS tabTPS;

    @Default
    @CommandPermission("tabtps.tps")
    @Description("Displays the current TPS and MSPT of the server.")
    public void onTPS(CommandSender sender) {
        final StringBuilder builder = new StringBuilder();

        final double[] tps = tabTPS.getTpsUtil().getTps();

        builder.append("<gray><hover:show_text:'Ticks per second<gray>.</gray> <green>20</green> is optimal<gray>.</gray>'>TPS<white>:</white> ");
        builder.append(TPSUtil.getColoredTps(tps[0]));
        builder.append(" <italic>(1m)</italic><white>,</white> ");
        builder.append(TPSUtil.getColoredTps(tps[1]));
        builder.append(" <italic>(5m)</italic><white>,</white> ");
        builder.append(TPSUtil.getColoredTps(tps[2]));
        builder.append(" <italic>(15m)</italic></hover>");

        tabTPS.getChat().send(sender, "<gradient:blue:aqua><strikethrough>-----------</strikethrough></gradient><aqua>[</aqua> <bold><gradient:red:gold>TabTPS</gradient></bold> <gradient:aqua:blue>]<strikethrough>-----------</strikethrough>");
        tabTPS.getChat().send(sender, builder.toString());
        tabTPS.getChat().send(sender, "<gray><hover:show_text:'Milliseconds per tick<gray>.</gray> MSPT <gray><</gray> 50 <gray>-></gray> <green>20 TPS</green>'>MSPT<white>:</white> " + TPSUtil.getColoredMspt(tabTPS.getTpsUtil().getMspt()) + "</hover>");
    }
}
