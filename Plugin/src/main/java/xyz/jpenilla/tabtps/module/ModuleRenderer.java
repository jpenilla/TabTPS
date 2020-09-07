package xyz.jpenilla.tabtps.module;

import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Function;

public class ModuleRenderer {
    private final Player player;
    private Function<Module, String> renderModule;
    private String separator;

    public ModuleRenderer(Player player) {
        this.player = player;
    }

    public ModuleRenderer moduleRenderFunction(Function<Module, String> function) {
        renderModule = function;
        return this;
    }

    public ModuleRenderer separator(String separator) {
        this.separator = separator;
        return this;
    }

    public String render(String module) {
        return module != null ? render(ImmutableList.of(module)) : render(ImmutableList.of());
    }

    public String render(List<String> modules) {
        StringBuilder builder = new StringBuilder();
        modules.forEach(moduleName -> {
            if (moduleName.equals("")) return;
            final Module module = Module.from(player, moduleName);
            if (module.needsPlayer() && player == null) return;
            builder.append(renderModule.apply(module));
            if (modules.indexOf(moduleName) != modules.size() - 1) {
                builder.append(separator);
            }
        });
        return builder.toString();
    }
}
