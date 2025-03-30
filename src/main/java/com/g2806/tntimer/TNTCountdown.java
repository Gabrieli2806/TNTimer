package com.g2806.tntimer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.TntEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TNTCountdown implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TNTCountdown.class);
    private final List<TntEntity> tntEntities = new CopyOnWriteArrayList<>();
    public static Config config; // Objeto de configuración (hacerlo público para ConfigScreen)
    private static KeyBinding configKey;

    // Enum para las posiciones posibles (público)
    public enum Position {
        TOP_LEFT("tntimer.position.top_left"),
        TOP_RIGHT("tntimer.position.top_right"),
        BOTTOM_LEFT("tntimer.position.bottom_left"),
        BOTTOM_RIGHT("tntimer.position.bottom_right"),
        TOP_CENTER("tntimer.position.top_center"),
        BOTTOM_CENTER("tntimer.position.bottom_center"),
        UNDER_CURSOR("tntimer.position.under_cursor");

        private final String translationKey;

        Position(String translationKey) {
            this.translationKey = translationKey;
        }

        public Text getDisplayName() {
            return Text.translatable(translationKey);
        }

        public Position next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    // Enum para los tamaños de texto (público)
    public enum TextSize {
        SMALL("tntimer.text_size.small", 0.5f),
        MEDIUM("tntimer.text_size.medium", 1.0f),
        LARGE("tntimer.text_size.large", 1.5f);

        private final String translationKey;
        private final float scale;

        TextSize(String translationKey, float scale) {
            this.translationKey = translationKey;
            this.scale = scale;
        }

        public Text getDisplayName() {
            return Text.translatable(translationKey);
        }

        public float getScale() {
            return scale;
        }

        public TextSize next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    // Clase para manejar la configuración
    public static class Config {
        private Position cornerPosition = Position.TOP_LEFT; // Posición por defecto
        private int maxTntDisplay = 5; // Cantidad máxima por defecto
        private boolean enabled = true; // Contador activado por defecto
        private TextSize textSize = TextSize.MEDIUM; // Tamaño de texto por defecto
        private boolean showOnlySeconds = false; // Mostrar solo segundos (por defecto: false)
        private boolean showBackground = false; // Mostrar fondo semitransparente (por defecto: false)
        private boolean showDonateButton = true; // Mostrar el botón "Donar" (por defecto: true)

        // Getters y setters
        public Position getCornerPosition() {
            return cornerPosition;
        }

        public void setCornerPosition(Position cornerPosition) {
            this.cornerPosition = cornerPosition;
        }

        public int getMaxTntDisplay() {
            return maxTntDisplay;
        }

        public void setMaxTntDisplay(int maxTntDisplay) {
            this.maxTntDisplay = maxTntDisplay;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public TextSize getTextSize() {
            return textSize;
        }

        public void setTextSize(TextSize textSize) {
            this.textSize = textSize;
        }

        public boolean isShowOnlySeconds() {
            return showOnlySeconds;
        }

        public void setShowOnlySeconds(boolean showOnlySeconds) {
            this.showOnlySeconds = showOnlySeconds;
        }

        public boolean isShowBackground() {
            return showBackground;
        }

        public void setShowBackground(boolean showBackground) {
            this.showBackground = showBackground;
        }

        public boolean isShowDonateButton() {
            return showDonateButton;
        }

        public void setShowDonateButton(boolean showDonateButton) {
            this.showDonateButton = showDonateButton;
        }

        // Métodos para cargar y guardar la configuración
        public static Config load() {
            File configFile = new File(MinecraftClient.getInstance().runDirectory, "config/tntimer.json");
            Gson gson = new Gson();
            Config config = new Config();

            if (configFile.exists()) {
                try (FileReader reader = new FileReader(configFile)) {
                    config = gson.fromJson(reader, Config.class);
                } catch (IOException e) {
                    LOGGER.error("Failed to load config file: {}", configFile.getAbsolutePath(), e);
                }
            }

            // Si no se pudo cargar, devolver la configuración por defecto
            return config != null ? config : new Config();
        }

        public void save() {
            File configDir = new File(MinecraftClient.getInstance().runDirectory, "config");
            File configFile = new File(configDir, "tntimer.json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            if (!configDir.exists()) {
                if (!configDir.mkdirs()) {
                    LOGGER.error("Failed to create config directory: {}", configDir.getAbsolutePath());
                    return;
                }
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(this, writer);
            } catch (IOException e) {
                LOGGER.error("Failed to save config file: {}", configFile.getAbsolutePath(), e);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onInitializeClient() {
        // Cargar la configuración al iniciar
        config = Config.load();

        // Registrar el renderizado HUD
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> render(drawContext));

        // Configurar tecla para abrir el menú (por defecto: K)
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tntimer.config",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_K,
                "category.tntimer"
        ));
    }

    private void render(DrawContext drawContext) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Verificar si se presionó la tecla de configuración
        if (configKey.wasPressed() && client != null) {
            // Asegurarse de que client no sea null antes de llamar a setScreen
            client.setScreen(new ConfigScreen(null));
        }

        if (client != null && client.world != null && config.isEnabled()) { // Solo renderizar si está activado
            tntEntities.clear();
            client.world.getEntities().forEach(entity -> {
                if (entity instanceof TntEntity tnt) tntEntities.add(tnt);
            });

            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();
            int padding = 10;

            // Usar la configuración cargada
            int displayCount = Math.min(tntEntities.size(), config.getMaxTntDisplay());
            float scale = config.getTextSize().getScale(); // Obtener la escala del texto

            for (int i = 0; i < displayCount; i++) {
                TntEntity tnt = tntEntities.get(i);
                int fuse = tnt.getFuse();
                double seconds = fuse / 20.0;
                String formattedSeconds = String.format("%.1f", seconds).replace(',', '.');
                String timeLeft;
                if (config.isShowOnlySeconds()) {
                    timeLeft = formattedSeconds + "s"; // Mostrar solo los segundos (ejemplo: "2.5s")
                } else {
                    timeLeft = Text.translatable("tntimer.countdown", formattedSeconds).getString(); // Texto completo
                }
                int color = 0xFFFFFF; // Blanco por defecto
                if (fuse < 20) {
                    color = 0xFF0000; // Rojo para menos de 1 segundo
                } else if (fuse < 40) {
                    color = 0xFF8000; // Naranja para menos de 2 segundos
                }

                int x, y;
                int textWidth = client.textRenderer.getWidth(timeLeft);
                int textHeight = client.textRenderer.fontHeight;

                // Ajustar la posición según la escala
                textWidth = (int)(textWidth * scale);
                textHeight = (int)(textHeight * scale);

                // Usar la posición de la configuración
                int bottomY = screenHeight - (textHeight + padding) - i * (textHeight + 5);
                switch (config.getCornerPosition()) {
                    case TOP_LEFT:
                        x = padding;
                        y = padding + i * (textHeight + 5);
                        break;
                    case TOP_RIGHT:
                        x = screenWidth - textWidth - padding;
                        y = padding + i * (textHeight + 5);
                        break;
                    case BOTTOM_LEFT:
                        x = padding;
                        y = bottomY;
                        break;
                    case BOTTOM_RIGHT:
                        x = screenWidth - textWidth - padding;
                        y = bottomY;
                        break;
                    case TOP_CENTER:
                        x = screenWidth / 2 - textWidth / 2;
                        y = padding + i * (textHeight + 5);
                        break;
                    case BOTTOM_CENTER:
                        x = screenWidth / 2 - textWidth / 2;
                        y = screenHeight - 60 - i * (textHeight + 5);
                        break;
                    case UNDER_CURSOR:
                        // Posicionar el texto debajo del cursor
                        x = screenWidth / 2 - textWidth / 2; // Centrar horizontalmente respecto al cursor
                        y = screenHeight / 2 + 15 + i * (textHeight + 5); // 15 píxeles debajo del cursor
                        // Asegurarse de que el texto no se salga de la pantalla
                        y = Math.min(y, screenHeight - textHeight - 5); // No sobrepasar el borde inferior
                        y = Math.max(y, 5); // No sobrepasar el borde superior
                        break;
                    default:
                        x = padding; // Valor por defecto (igual que TOP_LEFT)
                        y = padding + i * (textHeight + 5);
                        break;
                }

                // Escalar y dibujar el texto
                drawContext.getMatrices().push();
                drawContext.getMatrices().translate(x, y, 0);

                // Calcular las dimensiones del fondo antes de aplicar la escala
                int backgroundPadding = 2; // Espacio adicional alrededor del texto
                int backgroundX = -backgroundPadding;
                int backgroundY = -backgroundPadding;
                int backgroundWidth = client.textRenderer.getWidth(timeLeft) + backgroundPadding * 2;
                int backgroundHeight = client.textRenderer.fontHeight + backgroundPadding * 2;
                int backgroundColor = 0x80000000; // Negro con 50% de opacidad (0x80 en el canal alpha)

                // Aplicar la escala después de calcular las dimensiones
                drawContext.getMatrices().scale(scale, scale, 1.0f);

                // Dibujar fondo semitransparente si está activado
                if (config.isShowBackground()) {
                    drawContext.fill(backgroundX, backgroundY, backgroundX + backgroundWidth, backgroundY + backgroundHeight, backgroundColor);
                }

                drawContext.drawText(client.textRenderer, Text.of(timeLeft), 0, 0, color, false);
                drawContext.getMatrices().pop();
            }
        }
    }
}