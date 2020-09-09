package xyz.jpenilla.tabtps.module;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ModuleRenderer {
    private final Player player;
    private final List<Module> modules;
    private final Function<Module, String> renderModule;
    private final String separator;

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Player player = null;
        private final List<Module> modules = new ArrayList<>();
        private Function<Module, String> renderModule;
        private String separator = null;

        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        public Builder moduleRenderFunction(Function<Module, String> function) {
            this.renderModule = function;
            return this;
        }

        public Builder separator(String separator) {
            this.separator = separator;
            return this;
        }

        public Builder modules(List<Module> modules) {
            this.modules.clear();
            this.modules.addAll(modules);
            return this;
        }

        public Builder modules(Module... modules) {
            return modules(Arrays.asList(modules));
        }

        public Builder modules(String modules) {
            return modules(
                    Arrays.stream(modules.replace(" ", "").split(","))
                            .filter(s -> s != null && !s.equals(""))
                            .map(moduleName -> Module.from(player, moduleName))
                            .filter(module -> !module.needsPlayer() || player != null)
                            .collect(Collectors.toList())
            );
        }

        /**
         * @return The built {@link ModuleRenderer}
         * @throws IllegalArgumentException When a needed parameter has not been provided
         */
        public ModuleRenderer build() throws IllegalArgumentException {
            if (separator == null && modules.size() > 1) {
                throw new IllegalArgumentException("separator is null but there is more than one module");
            }
            if (renderModule == null) {
                throw new IllegalArgumentException("must provide a module render function");
            }
            return new ModuleRenderer(player, modules, renderModule, separator);
        }
    }
}
