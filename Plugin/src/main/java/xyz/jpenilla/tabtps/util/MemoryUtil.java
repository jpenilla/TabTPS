package xyz.jpenilla.tabtps.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.fancy.Gradient;
import xyz.jpenilla.tabtps.TabTPS;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.stream.IntStream;

public class MemoryUtil {
    public static int getUsedMemory() {
        return Math.round(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / 1048576f);
    }

    public static int getCommittedMemory() {
        return Math.round(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted() / 1048576f);
    }

    public static int getMaxMemory() {
        return Math.round(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() / 1048576f);
    }

    public static Component renderBar(String name, MemoryUsage usage, int barLength) {
        final long max = usage.getMax() == -1 ? usage.getCommitted() : usage.getMax();
        final long init = usage.getInit() == -1 ? 0 : usage.getInit();
        final float initPercent = (float) init / max;
        final float usedPercent = (float) usage.getUsed() / max;
        final float committedPercent = (float) usage.getCommitted() / max;
        final Gradient usedGradient = new Gradient(0, NamedTextColor.GREEN, NamedTextColor.DARK_GREEN);
        final Gradient committedGradient = new Gradient(0, NamedTextColor.AQUA, NamedTextColor.BLUE);
        final Gradient unallocatedGradient = new Gradient(0, NamedTextColor.GRAY, NamedTextColor.DARK_GRAY);
        final int usedLength = Math.round(barLength * usedPercent);
        final int committedLength = Math.round(barLength * (committedPercent - usedPercent));
        final int unallocatedLength = barLength - usedLength - committedLength;
        final int initPointer = Math.min(barLength, Math.max(1, Math.round(barLength * initPercent)));
        usedGradient.init(usedLength);
        committedGradient.init(committedLength);
        unallocatedGradient.init(unallocatedLength);

        final TextComponent.Builder builder = TextComponent.builder();

        final StringBuilder hover = new StringBuilder();
        hover.append(humanReadableByteCountBin(usage.getUsed())).append(" <white>Used</white>/").append(humanReadableByteCountBin(usage.getCommitted())).append(" <white>Committed</white>\n");
        if (usage.getMax() != -1) {
            hover.append(humanReadableByteCountBin(max)).append(" <white>Max</white><gray>,</gray> ");
        }
        hover.append(humanReadableByteCountBin(init)).append(" <white>Init</white>");
        builder.hoverEvent(HoverEvent.showText(TabTPS.getInstance().getMiniMessage().parse(hover.toString())));

        builder.append("[", NamedTextColor.GRAY);
        IntStream.rangeClosed(1, barLength).forEach(i -> {
            if (i == initPointer) {
                builder.append("|", TextColor.of(0xFF48A8));
            } else if (i <= usedLength) {
                builder.append(usedGradient.apply(TextComponent.of("|")));
            } else if (i <= usedLength + committedLength) {
                builder.append(committedGradient.apply(TextComponent.of("|")));
            } else {
                builder.append(unallocatedGradient.apply(TextComponent.of("|")));
            }
        });
        builder.append("]", NamedTextColor.GRAY);
        if (name != null && !name.equals("")) {
            builder.append(" ");
            builder.append(name, NamedTextColor.WHITE, TextDecoration.ITALIC);
        }

        return builder.build();
    }

    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return "<gradient:blue:aqua>" + bytes + "</gradient></gray>B</gray>";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("<gradient:blue:aqua>%.1f</gradient><gray>%ciB<gray>", value / 1024.0, ci.current());
    }
}
