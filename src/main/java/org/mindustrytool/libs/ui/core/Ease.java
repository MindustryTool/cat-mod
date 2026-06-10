package org.mindustrytool.libs.ui.animation;

/**
 * Easing functions for animation. Each constant implements {@link #apply(float)}
 * to transform normalised time {@code t} (0→1) into an eased value.
 */
public enum Ease {
    linear {
        public float apply(float t) {
            return t;
        }
    },
    quadIn {
        public float apply(float t) {
            return t * t;
        }
    },
    quadOut {
        public float apply(float t) {
            return t * (2f - t);
        }
    },
    quadInOut {
        public float apply(float t) {
            return t < 0.5f ? 2f * t * t : -1f + (4f - 2f * t) * t;
        }
    },
    cubicIn {
        public float apply(float t) {
            return t * t * t;
        }
    },
    cubicOut {
        public float apply(float t) {
            float f = t - 1f;
            return f * f * f + 1f;
        }
    },
    cubicInOut {
        public float apply(float t) {
            return t < 0.5f ? 4f * t * t * t : (t - 1f) * (2f * t - 2f) * (2f * t - 2f) + 1f;
        }
    },
    expoOut {
        public float apply(float t) {
            return t >= 1f ? 1f : 1f - (float) Math.pow(2f, -10f * t);
        }
    },
    bounceOut {
        public float apply(float t) {
            if (t < 1f / 2.75f) {
                return 7.5625f * t * t;
            } else if (t < 2f / 2.75f) {
                float f = t - 1.5f / 2.75f;
                return 7.5625f * f * f + 0.75f;
            } else if (t < 2.5f / 2.75f) {
                float f = t - 2.25f / 2.75f;
                return 7.5625f * f * f + 0.9375f;
            } else {
                float f = t - 2.625f / 2.75f;
                return 7.5625f * f * f + 0.984375f;
            }
        }
    },
    elasticOut {
        public float apply(float t) {
            if (t >= 1f) return 1f;
            float f = t - 1f;
            return (float) (Math.pow(2f, -10f * t) * Math.sin(f * 6f * Math.PI / 0.3f) + 1f);
        }
    };

    public abstract float apply(float t);
}
