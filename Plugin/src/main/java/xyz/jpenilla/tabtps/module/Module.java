package xyz.jpenilla.tabtps.module;

public abstract class Module {
    public abstract String getLabel();
    public abstract String getData();

    public static Module of(String name) {
        switch (name.toLowerCase()) {
            case "tps":
                return new TPS();
            case "mspt":
                return new MSPT();
            case "memory":
                return new Memory();
            default:
                throw new IllegalArgumentException("No such module: " + name.toLowerCase());
        }
    }
}
