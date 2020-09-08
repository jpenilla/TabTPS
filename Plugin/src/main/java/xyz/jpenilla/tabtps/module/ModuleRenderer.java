package xyz.jpenilla.tabtps.module;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModuleRenderer {
    private final Player player;
    private final List<Module> modules = new ArrayList<>();
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

    public ModuleRenderer modules(List<Module> modules) {
        this.modules.clear();
        this.modules.addAll(modules);
        return this;
    }

    public ModuleRenderer modules(Module... modules) {
        return modules(Arrays.asList(modules));
    }

    public ModuleRenderer modules(String modules) {
        return modules(
                Arrays.stream(modules.replace(" ", "").split(","))
                        .filter(s -> s != null && !s.equals(""))
                        .map(moduleName -> Module.from(player, moduleName))
                        .filter(module -> !module.needsPlayer() || player != null)
                        .collect(Collectors.toList())
        );
    }

    public String render() {
        final StringBuilder builder = new StringBuilder();
        modules.forEach(module -> {
            builder.append(renderModule.apply(module));
            if (modules.indexOf(module) != modules.size() - 1) {
                builder.append(separator);
            }
        });
        return builder.toString();
    }
}
