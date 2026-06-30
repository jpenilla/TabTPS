/*
 * This file is part of TabTPS, licensed under the MIT License.
 *
 * Copyright (c) 2020-2024 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.jpenilla.tabtps.common.config;

import java.util.Objects;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import static net.kyori.adventure.text.Component.space;

@ConfigSerializable
@NullMarked
public final class DisplayConfig {
  private static final String SEPARATOR_COMMENT = "The text used to separate modules. Accepts MiniMessage (i.e. \" <gray>|</gray> \")";

  @Comment("The permission required to use this display config\nCan be an empty string (\"\") to require no permission")
  private String permission = "tabtps.defaultdisplay";

  private ActionBarSettings actionBarSettings = new ActionBarSettings();
  private BossBarSettings bossBarSettings = new BossBarSettings();
  private TabSettings tabSettings = new TabSettings();

  public String permission() {
    return this.permission;
  }

  public ActionBarSettings actionBarSettings() {
    return this.actionBarSettings;
  }

  public BossBarSettings bossBarSettings() {
    return this.bossBarSettings;
  }

  public TabSettings tabSettings() {
    return this.tabSettings;
  }

  @Override
  public boolean equals(final @Nullable Object o) {
    if (this == o) return true;
    if (!(o instanceof DisplayConfig)) return false;
    final DisplayConfig that = (DisplayConfig) o;
    return this.permission.equals(that.permission)
      && this.actionBarSettings.equals(that.actionBarSettings)
      && this.bossBarSettings.equals(that.bossBarSettings)
      && this.tabSettings.equals(that.tabSettings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.permission, this.actionBarSettings, this.bossBarSettings, this.tabSettings);
  }

  public interface DisplaySettings {
    boolean allow();

    boolean enableOnLogin();

    String theme();

    Component separator();
  }

  @ConfigSerializable
  public static final class ActionBarSettings implements DisplaySettings {
    private boolean allow = true;
    private boolean enableOnLogin = false;
    private String modules = "tps,mspt,ping";
    private String theme = "default";

    @Comment(SEPARATOR_COMMENT)
    private Component separator = space();

    @Override
    public Component separator() {
      return this.separator;
    }

    @Override
    public boolean enableOnLogin() {
      return this.enableOnLogin;
    }

    @Override
    public boolean allow() {
      return this.allow;
    }

    public String modules() {
      return this.modules;
    }

    @Override
    public String theme() {
      return this.theme;
    }
  }

  @ConfigSerializable
  public static final class BossBarSettings implements DisplaySettings {
    private boolean allow = true;
    private boolean enableOnLogin = false;
    private String modules = "tps,mspt,ping";
    private String theme = "default";

    @Comment("Available colors: [PINK, RED, GREEN, BLUE, YELLOW, PURPLE, WHITE]")
    private Colors colors = new Colors();

    @Comment("Set the mode for determining boss bar fill.\nPossible values: [MSPT, TPS, REVERSE_MSPT, REVERSE_TPS]")
    private FillMode fillMode = FillMode.MSPT;

    @Comment("What kind of overlay should be used for the boss bar?\nMust be one of: [PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20]")
    private BossBar.Overlay overlay = BossBar.Overlay.NOTCHED_20;

    @Comment(SEPARATOR_COMMENT)
    private Component separator = space();

    @Override
    public Component separator() {
      return this.separator;
    }

    @Override
    public boolean allow() {
      return this.allow;
    }

    @Override
    public boolean enableOnLogin() {
      return this.enableOnLogin;
    }

    public String modules() {
      return this.modules;
    }

    public FillMode fillMode() {
      return this.fillMode;
    }

    public BossBar.Overlay overlay() {
      return this.overlay;
    }

    public Colors colors() {
      return this.colors;
    }

    @Override
    public String theme() {
      return this.theme;
    }

    public enum FillMode {
      TPS, MSPT, REVERSE_TPS, REVERSE_MSPT
    }

    @ConfigSerializable
    public static final class Colors {
      private BossBar.Color lowPerformance = BossBar.Color.RED;
      private BossBar.Color mediumPerformance = BossBar.Color.YELLOW;
      private BossBar.Color goodPerformance = BossBar.Color.GREEN;

      public BossBar.Color lowPerformance() {
        return this.lowPerformance;
      }

      public BossBar.Color mediumPerformance() {
        return this.mediumPerformance;
      }

      public BossBar.Color goodPerformance() {
        return this.goodPerformance;
      }
    }
  }

  @ConfigSerializable
  public static final class TabSettings implements DisplaySettings {
    private boolean allow = true;
    private boolean enableOnLogin = false;
    private String headerModules = "";
    private String footerModules = "tps,mspt";
    private String theme = "default";

    @Comment(SEPARATOR_COMMENT)
    private Component separator = space();

    @Override
    public Component separator() {
      return this.separator;
    }

    @Override
    public boolean enableOnLogin() {
      return this.enableOnLogin;
    }

    @Override
    public boolean allow() {
      return this.allow;
    }

    public String headerModules() {
      return this.headerModules;
    }

    public String footerModules() {
      return this.footerModules;
    }

    @Override
    public String theme() {
      return this.theme;
    }
  }
}
