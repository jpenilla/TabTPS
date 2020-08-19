package xyz.jpenilla.tabtps.module;

import xyz.jpenilla.tabtps.TabTPS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ModuleRenderer {
    private final TabTPS tabTPS;
    private final List<String> modules = new ArrayList<>();
    private Function<Module, String> renderModule;
    private String separator;

    public ModuleRenderer(TabTPS tabTPS) {
        this.tabTPS = tabTPS;
        modules.addAll(Arrays.asList(TabTPS.getInstance().getPluginSettings().getModuleOrder().split(",")));
        if (!tabTPS.getPluginSettings().getModules().isTps()) {
            modules.remove("tps");
        }
        if (!tabTPS.getPluginSettings().getModules().isMemory()) {
            modules.remove("memory");
        }
        if (!tabTPS.getPluginSettings().getModules().isMspt()) {
            modules.remove("mspt");
        }
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
        this.modules.clear();
        this.modules.add(module);
        return render();
    }

    public String render(List<String> modules) {
        this.modules.clear();
        this.modules.addAll(modules);
        return render();
    }

    public String render() {
        StringBuilder builder = new StringBuilder();

        for (String moduleName : modules) {
            builder.append(renderModule.apply(Module.of(moduleName)));
            if (modules.indexOf(moduleName) != modules.size() - 1) {
                builder.append(separator);
            }
        }

        return builder.toString();
    }
}
