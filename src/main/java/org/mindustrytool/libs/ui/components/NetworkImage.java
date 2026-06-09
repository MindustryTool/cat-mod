package org.mindustrytool.libs.ui.components;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.scene.Element;
import arc.scene.Scene;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Scl;
import arc.util.Http;
import arc.util.Http.HttpStatus;
import arc.util.Http.HttpStatusException;
import arc.util.Log;
import arc.util.Scaling;

import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;

import java.util.concurrent.ConcurrentHashMap;

import org.mindustrytool.libs.signal.Effect;
import org.mindustrytool.libs.ui.component.AbstractComponent;
import org.mindustrytool.libs.ui.component.ComponentStyle;
import org.mindustrytool.libs.ui.layout.NodeSizing;

import arc.func.Cons;

/**
 * NetworkImage is a UI image component that downloads and caches images from a URL asynchronously.
 * It caches downloaded files locally and handles scaling, borders, loading states, and automatic cleanup.
 */
public class NetworkImage extends AbstractComponent {
    public static final Fi imageCacheDirectory = Vars.dataDirectory.child("neko-content-caches");

    /**
     * Style builder for NetworkImage, supporting custom border color, border thickness, image scaling, and sizing.
     */
    public class Style extends ComponentStyle<Style> {
        Style() {
        }

        @Override
        protected NodeSizing sizing() {
            return sizing;
        }

        @Override
        protected Element styledElement() {
            return element;
        }

        /**
         * Sets the border color of the image container.
         *
         * @param value the border color
         * @return this style builder instance
         */
        public Style borderColor(Color value) {
            NetworkImage.this.borderColor = value;
            return this;
        }

        /**
         * Sets the thickness of the border outline.
         *
         * @param value the border thickness in pixels
         * @return this style builder instance
         */
        public Style borderThickness(float value) {
            NetworkImage.this.borderThickness = value;
            return this;
        }

        /**
         * Sets the Scaling option for the image content.
         *
         * @param value the scaling mode (e.g. Scaling.fit)
         * @return this style builder instance
         */
        public Style scaling(Scaling value) {
            element.setScaling(value);
            return this;
        }

        /**
         * Configures layout sizing.
         *
         * @param configurator the node sizing configurator callback
         * @return this style builder instance
         */
        public Style size(Cons<NodeSizing> configurator) {
            configurator.get(sizing);
            return this;
        }
    }

    public Color borderColor = Pal.gray;
    public float scalingFactor = 16.0f;
    public float borderThickness = 1.0f;

    private boolean isError = false;
    private final String imageUrl;
    private TextureRegion lastTexture;

    private static final ConcurrentHashMap<String, TextureRegion> cache = new ConcurrentHashMap<>();

    private final Image element;
    public final Style style;

    private Effect styleEffect;
    private Effect sizeEffect;

    /**
     * Constructs a new NetworkImage with a source URL.
     *
     * @param imageUrl the remote URL of the image to load
     */
    public NetworkImage(String imageUrl) {
        this.imageUrl = imageUrl;
        this.element = new Image(Tex.clear) {
            @Override
            protected void setScene(Scene sceneInstance) {
                super.setScene(sceneInstance);
                if (sceneInstance == null) {
                    NetworkImage.this.dispose();
                }
            }

            @Override
            public void draw() {
                super.draw();

                TextureRegion nextRegion = cache.get(imageUrl);

                if (lastTexture != nextRegion) {
                    lastTexture = nextRegion;
                    setDrawable(nextRegion);

                    Draw.color(borderColor);
                    Lines.stroke(Scl.scl(borderThickness));
                    Lines.rect(x, y, width, height);
                    Draw.reset();
                }

                if (isError) {
                    return;
                }

                try {
                    if (!cache.containsKey(imageUrl)) {
                        cache.put(imageUrl, Icon.refresh.getRegion());

                        if (!imageUrl.endsWith("png") && !imageUrl.endsWith("jpg") && !imageUrl.endsWith("jpeg")) {
                            return;
                        }

                        imageCacheDirectory.mkdirs();

                        Fi cachedFile = imageCacheDirectory.child(imageUrl
                            .replace(":", "-")
                            .replace("/", "-")
                            .replace("?", "-")
                            .replace("&", "-"));

                        if (cachedFile.exists()) {
                            try {
                                byte[] resultBytes = cachedFile.readBytes();
                                Pixmap pixmap = new Pixmap(resultBytes);
                                Core.app.post(() -> {
                                    try {
                                        Texture texture = new Texture(pixmap);
                                        texture.setFilter(TextureFilter.linear);
                                        cache.put(imageUrl, new TextureRegion(texture));
                                        pixmap.dispose();
                                    } catch (Exception exception) {
                                        Log.err(imageUrl, exception);
                                        isError = true;
                                    }
                                });
                            } catch (Exception exception) {
                                isError = true;
                                cachedFile.delete();
                                Log.err(imageUrl, exception);
                            }
                        } else {
                            Http.get(imageUrl + "?format=jpeg")
                                .timeout(10000)
                                .error(error -> {
                                    isError = true;
                                    if (!(error instanceof HttpStatusException requestError)
                                        || requestError.status != HttpStatus.NOT_FOUND) {
                                        Log.err(imageUrl, error);
                                    }
                                })
                                .submit(response -> {
                                    byte[] responseBytes = response.getResult();
                                    if (responseBytes.length == 0) {
                                        return;
                                    }

                                    try {
                                        cachedFile.writeBytes(responseBytes);
                                    } catch (Exception exception) {
                                        Log.err(imageUrl, exception);
                                        isError = true;
                                    }

                                    Core.app.post(() -> {
                                        try {
                                            Pixmap pixmap = new Pixmap(responseBytes);
                                            Texture texture = new Texture(pixmap);
                                            texture.setFilter(TextureFilter.linear);
                                            cache.put(imageUrl, new TextureRegion(texture));
                                            pixmap.dispose();
                                        } catch (Exception exception) {
                                            Log.err(imageUrl, exception);
                                            isError = true;
                                        }
                                    });
                                });
                        }
                    }
                } catch (Exception error) {
                    Log.err(imageUrl, error);
                    isError = true;
                }
            }
        };
        element.setScaling(Scaling.fit);
        element.userObject = this;
        this.style = new Style();
    }

    /**
     * Checks if the given URL is a valid web image link.
     *
     * @param url the image URL to validate
     * @return true if it is a valid web image link, false otherwise
     */
    public static boolean isValidImageLink(String url) {
        return url != null && url.matches("^https?://[^?\\s]+\\.(png|jpg|jpeg)(\\?.*)?$");
    }

    /**
     * Configures the image style properties reactively.
     *
     * @param configurator the style configurator callback
     * @return this image instance for chaining
     */
    public NetworkImage style(Cons<Style> configurator) {
        if (styleEffect != null) {
            styleEffect.dispose();
            subscriptions.remove(styleEffect);
        }
        styleEffect = new Effect(() -> {
            configurator.get(style);
            element.invalidateHierarchy();
        });
        subscriptions.add(styleEffect);
        return this;
    }

    /**
     * Configures the image sizing constraints reactively.
     *
     * @param configurator the sizing configurator callback
     * @return this image instance for chaining
     */
    public NetworkImage size(Cons<NodeSizing> configurator) {
        if (sizeEffect != null) {
            sizeEffect.dispose();
            subscriptions.remove(sizeEffect);
        }
        sizeEffect = new Effect(() -> {
            configurator.get(sizing);
            element.invalidateHierarchy();
        });
        subscriptions.add(sizeEffect);
        return this;
    }

    @Override
    public Element element() {
        return element;
    }
}
