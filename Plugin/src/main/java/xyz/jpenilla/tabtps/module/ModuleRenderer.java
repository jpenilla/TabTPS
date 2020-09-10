package xyz.jpenilla.tabtps.module;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.TabTPS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ModuleRenderer {
    private final List<Module> modules;
    private final Function<Module, String> moduleRenderFunction;
    private final String separator;

    public String render() {
        final StringBuilder builder = new StringBuilder();
        modules.forEach(module -> {
            builder.append(moduleRenderFunction.apply(module));
            if (modules.indexOf(module) != modules.size() - 1) {
                builder.append(separator);
            }
        });
        return builder.toString();
    }

    /**
     * @return A new {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Module> modules = new ArrayList<>();
        private Function<Module, String> moduleRenderFunction;
        private String separator = null;

        private Builder() {
        }

        public Builder moduleRenderFunction(Function<Module, String> function) {
            this.moduleRenderFunction = function;
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

        /**
         * Sets the list of {@link Module}s to use from a comma separated {@link String}.
         * <p>
         * If Modules which need a {@link Player} are used, {@link Builder#modules(TabTPS, Player, String)} should be used instead.
         *
         * @param tabTPS  The TabTPS instance
         * @param modules The list of Modules to use in the builder, separated by commas.
         * @return The {@link Builder}
         */
        public Builder modules(TabTPS tabTPS, String modules) {
            return modules(tabTPS, null, modules);
        }

        /**
         * Sets the list of {@link Module}s to use from a comma separated {@link String}.
         *
         * @param tabTPS  The TabTPS instance
         * @param player  The Player to use
         * @param modules The list of Modules to use in the builder, separated by commas.
         * @return The {@link Builder}
         */
        public Builder modules(TabTPS tabTPS, Player player, String modules) {
            return modules(
                    Arrays.stream(modules.replace(" ", "").split(","))
                            .filter(s -> s != null && !s.equals(""))
                            .map(moduleName -> Module.from(tabTPS, player, moduleName))
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
            if (moduleRenderFunction == null) {
                throw new IllegalArgumentException("must provide a module render function");
            }
            return new ModuleRenderer(modules, moduleRenderFunction, separator);
        }
    }
}
