package org.dark.shaders.distortion;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.io.IOException;
import org.apache.log4j.Level;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

/**
 * A 60-frame animated ripple-shaped distortion object. This type of distortion is suitable for situations where a
 * spreading relatively-even ripple effect of varying relative thickness is desired. This distortion is a radial
 * "pop-out" that does not skew textures. You may also opt not to animate this distortion, instead selecting the
 * thickness desired by loading a particular frame.
 * <p>
 * @author DarkRevenant
 * @since Alpha 1.1
 */
public class RippleDistortion implements DistortionAPI {

    public static final int FRAMES = 60;

    private static final String SETTINGS_FILE = "GRAPHICS_OPTIONS.ini";
    protected static final SpriteAPI[] animation = new SpriteAPI[60];
    protected static boolean useLargeRipple = false;

    static boolean pathsSet = false;

    static {
        Global.getLogger(RippleDistortion.class).setLevel(Level.ERROR);

        try {
            loadSettings();
        } catch (IOException | JSONException e) {
            Global.getLogger(RippleDistortion.class).log(Level.ERROR, "Failed to load shader settings: "
                    + e.getMessage());
        }
    }

    private static void loadSettings() throws IOException, JSONException {
        final JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE);

        useLargeRipple = settings.getBoolean("useLargeRipple");
    }

    private static void setPaths() {
        String path;
        for (int i = 1; i <= FRAMES; i++) {
            if (!useLargeRipple) {
                if (i < 10) {
                    path = "graphics/shaders/distortions/smallripple/000" + i + ".PNG";
                } else {
                    path = "graphics/shaders/distortions/smallripple/00" + i + ".PNG";
                }
                animation[i - 1] = Global.getSettings().getSprite(path);
            } else {
                if (i < 10) {
                    path = "graphics/shaders/distortions/ripple/000" + i + ".PNG";
                } else {
                    path = "graphics/shaders/distortions/ripple/00" + i + ".PNG";
                }
                animation[i - 1] = Global.getSettings().getSprite(path);
            }
        }
    }

    protected float arcAttenWidth = 0f;
    protected float arcEnd = 0f;
    protected float arcStart = 0f;
    protected float autoAnimateFrameRate = 0f;
    protected float autoAnimateFrameStart = 0f;
    protected float autoFadeIntensityTime = 0f;
    protected float autoFadeSizeTime = 0f;
    protected float deltaIntensity = 0f;
    protected float deltaSize = 0f;
    protected boolean flipped = false;
    protected float frame = 0f;
    protected float frameRate = 0f;
    protected float intensity = 20f;
    protected float lifetime = -1f;
    protected final Vector2f location;
    protected float maxIntensity = 20f;
    protected float maxSize = 100f;
    protected float size = 100f;
    protected final Vector2f velocity;

    public RippleDistortion() {
        if (!pathsSet) {
            setPaths();
            pathsSet = true;
        }
        this.location = new Vector2f();
        this.velocity = new Vector2f();
    }

    public RippleDistortion(Vector2f location, Vector2f velocity) {
        if (!pathsSet) {
            setPaths();
            pathsSet = true;
        }
        this.location = new Vector2f(location);
        this.velocity = new Vector2f(velocity);
    }

    /**
     * Runs once per frame.
     * <p>
     * @param amount Seconds since last frame.
     * <p>
     * @return True if the distortion object should be destroyed this frame, false if it should not be destroyed this
     * frame.
     * <p>
     * @since Alpha 1.1
     */
    @Override
    public boolean advance(float amount) {
        size += deltaSize * amount;
        intensity += deltaIntensity * amount;
        frame += frameRate * amount;
        if ((size >= maxSize) && (deltaSize > 0f)) {
            size = maxSize;
            deltaSize = 0f;
        }
        if ((intensity >= maxIntensity) && (deltaIntensity > 0f)) {
            intensity = maxIntensity;
            deltaIntensity = 0f;
        }
        if ((size <= 0f) || (intensity <= 0f) || (frame < 0) || (frame >= FRAMES)) {
            return true;
        }

        location.translate(velocity.x * amount, velocity.y * amount);

        if ((Float.compare(deltaSize, 0f) == 0) && (Float.compare(deltaIntensity, 0f) == 0)) {
            if (lifetime >= 0f) {
                lifetime -= amount;

                if (lifetime <= 0f) {
                    if ((Float.compare(autoFadeSizeTime, 0f) == 0) || (Float.compare(autoFadeIntensityTime, 0f) == 0)) {
                        return true;
                    }

                    if (Float.compare(autoFadeSizeTime, 0f) != 0) {
                        fadeOutSize(autoFadeSizeTime);
                    }
                    if (Float.compare(autoFadeIntensityTime, 0f) != 0) {
                        fadeOutIntensity(autoFadeIntensityTime);
                    }
                    setCurrentFrame(autoAnimateFrameStart);
                    setFrameRate(autoAnimateFrameRate);
                }
            }
        }

        return false;
    }

    /**
     * Sets the distortion's intensity to zero and fades in the distortion's intensity to its original value over a
     * period of time.
     * <p>
     * @param time The time over which to fade in the distortion's intensity.
     * <p>
     * @since Alpha 1.1
     */
    public void fadeInIntensity(float time) {
        deltaIntensity = intensity / time;
        maxIntensity = intensity;
        intensity = 0f;
    }

    /**
     * Sets the distortion's size to zero and fades in the distortion's size to its original value over a period of
     * time.
     * <p>
     * @param time The time over which to fade in the distortion's size.
     * <p>
     * @since Alpha 1.1
     */
    public void fadeInSize(float time) {
        deltaSize = size / time;
        maxSize = size;
        size = 0f;
    }

    /**
     * Fades out the distortion's intensity over a period of time. If the distortion hits zero intensity, it is
     * destroyed. Negative time values will cause the intensity to increase forever.
     * <p>
     * @param time The time over which to fade out the distortion's intensity.
     * <p>
     * @since Alpha 1.1
     */
    public void fadeOutIntensity(float time) {
        deltaIntensity = -intensity / time;
        maxIntensity = Float.MAX_VALUE;
    }

    /**
     * Fades out the distortion's size over a period of time. If the distortion hits zero size, it is destroyed.
     * Negative time values will cause the size to increase forever.
     * <p>
     * @param time The time over which to fade out the distortion's size.
     * <p>
     * @since Alpha 1.1
     */
    public void fadeOutSize(float time) {
        deltaSize = -size / time;
        maxSize = Float.MAX_VALUE;
    }

    /**
     * Sets whether the distortion should be flipped in magnitude. This simulates the effect of performing a
     * horizontal-vertical inversion of the red and green color channels. For example, if the distortion normally looks
     * like a sphere, it will instead look like a spherical hole.
     * <p>
     * @param flipped Whether the magnitude should be flipped.
     * <p>
     * @since Alpha 1.1
     */
    public void flip(boolean flipped) {
        this.flipped = flipped;
    }

    /**
     * The width of the visible arc's edge attenuation, in degrees. Wider attenuation will make the transition smoother.
     * <p>
     * @return The width of the visible arc's edge attenuation, in degrees.
     * <p>
     * @since Beta 1.0
     */
    @Override
    public float getArcAttenuationWidth() {
        return arcAttenWidth;
    }

    /**
     * Sets the width of the visible arc's edge attenuation, in degrees. Wider attenuation will make the transition
     * smoother.
     * <p>
     * @param width The desired width of the visible arc's edge attenuation, in degrees.
     * <p>
     * @since Beta 1.0
     */
    public void setArcAttenuationWidth(float width) {
        arcAttenWidth = width;
    }

    /**
     * The end of the current visible arc of the distortion, in degrees. Note: the entire shader is visible if the arc
     * spans 0 degrees.
     * <p>
     * @return The current facing direction of the distortion.
     * <p>
     * @since Alpha 1.11
     */
    @Override
    public float getArcEnd() {
        return arcEnd;
    }

    /**
     * The start of the current visible arc of the distortion, in degrees. Note: the entire shader is visible if the arc
     * spans 0 degrees.
     * <p>
     * @return The start of the current visible arc of the distortion, in degrees.
     * <p>
     * @since Alpha 1.11
     */
    @Override
    public float getArcStart() {
        return arcStart;
    }

    /**
     * Returns the frame rate the animation will play at after its lifetime expires.
     * <p>
     * @return The frame rate the animation will play at after its lifetime expires.
     * <p>
     * @since Alpha 1.1
     */
    public float getAutoAnimateFrameRate() {
        return autoAnimateFrameRate;
    }

    /**
     * Returns the starting frame the animation will play from after its lifetime expires.
     * <p>
     * @return The starting frame the animation will play from after its lifetime expires.
     * <p>
     * @since Alpha 1.1
     */
    public float getAutoAnimateFrameStart() {
        return autoAnimateFrameStart;
    }

    /**
     * Returns the amount of time the distortion will take to fade out in intensity after its lifetime expires.
     * <p>
     * @return The amount of time the distortion will take to fade out in intensity after its lifetime expires.
     * <p>
     * @since Alpha 1.1
     */
    public float getAutoFadeIntensityTime() {
        return autoFadeIntensityTime;
    }

    /**
     * Sets the amount of time the distortion will take to fade out in intensity after its lifetime expires.
     * <p>
     * @param time The amount of time the distortion should take to fade out in intensity after its lifetime expires.
     * <p>
     * @since Alpha 1.1
     */
    public void setAutoFadeIntensityTime(float time) {
        this.autoFadeIntensityTime = time;
    }

    /**
     * Returns the amount of time the distortion will take to fade out in size after its lifetime expires.
     * <p>
     * @return The amount of time the distortion will take to fade out in size after its lifetime expires.
     * <p>
     * @since Alpha 1.1
     */
    public float getAutoFadeSizeTime() {
        return autoFadeSizeTime;
    }

    /**
     * Sets the amount of time the distortion will take to fade out in size after its lifetime expires.
     * <p>
     * @param time The amount of time the distortion should take to fade out in size after its lifetime expires.
     * <p>
     * @since Alpha 1.1
     */
    public void setAutoFadeSizeTime(float time) {
        this.autoFadeSizeTime = time;
    }

    /**
     * Returns the current frame of the animation.
     * <p>
     * @return The animation's current frame.
     * <p>
     * @since Alpha 1.1
     */
    public float getCurrentFrame() {
        return frame;
    }

    /**
     * Sets the current animation frame to the given value.
     * <p>
     * @param frame The animation frame to set the object to.
     */
    public void setCurrentFrame(float frame) {
        this.frame = frame;
    }

    /**
     * The current facing direction of the distortion, in degrees. Matches sprite's rotation.
     * <p>
     * @return The current facing direction of the distortion.
     * <p>
     * @since Alpha 1.1
     */
    @Override
    public float getFacing() {
        // Normally, you set the sprite to match the facing
        // In this case, the facing angle doesn't matter because it's radial
        if (frame >= FRAMES || frame < 0) {
            return animation[0].getAngle();
        }
        return animation[(int) frame].getAngle();
    }

    /**
     * Returns the current frame rate of the animation.
     * <p>
     * @return The animation's current frame rate.
     * <p>
     * @since Alpha 1.1
     */
    public float getFrameRate() {
        return frameRate;
    }

    /**
     * Plays the animation at the desired frame rate. May be negative.
     * <p>
     * @param frameRate The desired frame rate.
     * <p>
     * @since Alpha 1.1
     */
    public void setFrameRate(float frameRate) {
        this.frameRate = frameRate;
    }

    /**
     * Returns the world-space quantity, in units, to distort by at the maximum distortion level (blue channel at 255).
     * The distortion engine will handle the transformation for you. Note that this function refers to the intensity
     * scale of the distortion texture, not its size. Modify the texture's sprite directly to change its size directly.
     * <p>
     * @return The scaling factor to transform the blue channel by, in world space units.
     * <p>
     * @since Alpha 1.1
     */
    @Override
    public float getIntensity() {
        return intensity;
    }

    /**
     * Sets the world-space quantity, in units, to distort by at the maximum distortion level (blue channel at 255). The
     * distortion engine will handle the transformation for you. Note that this function refers to the intensity scale
     * of the distortion texture, not its size. Modify the texture's sprite directly to change its size directly.
     * <p>
     * @param intensity The scaling factor to transform the blue channel by, in world space units.
     * <p>
     * @since Alpha 1.1
     */
    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    /**
     * Sets the time the distortion has before it expires. Lifetime does not count down while the distortion is fading
     * (in or out). After reaching the end of its lifetime, the distortion may fade out before disappearing if it has
     * AutoFade set.
     * <p>
     * @param lifetime The time the distortion has before it expires.
     * <p>
     * @since Alpha 1.1
     */
    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    /**
     * Returns the current location of the distortion.
     * <p>
     * @return The current location of the distortion.
     * <p>
     * @since Alpha 1.1
     */
    @Override
    public Vector2f getLocation() {
        return location;
    }

    /**
     * Sets the desired location of the distortion.
     * <p>
     * @param location The desired location of the distortion.
     * <p>
     * @since Alpha 1.1
     */
    public void setLocation(Vector2f location) {
        this.location.set(location);
    }

    /**
     * Returns the remaining time the distortion has left before it expires. Lifetime does not count down while the
     * distortion is fading (in or out). After reaching the end of its lifetime, the distortion may fade out before
     * disappearing if it has AutoFade set.
     * <p>
     * @return The remaining time the distortion has left before it expires.
     * <p>
     * @since Alpha 1.1
     */
    public float getRemainingLifetime() {
        return lifetime;
    }

    /**
     * Returns the apparent size of the distortion, in world-space units.
     * <p>
     * @return The apparent size of the distortion, in world-space units.
     * <p>
     * @since Alpha 1.1
     */
    public float getSize() {
        return size;
    }

    /**
     * Sets the apparent size of the distortion, in world-space units.
     * <p>
     * @param size The desired size of the distortion, in world-space units.
     * <p>
     * @since Alpha 1.1
     */
    public void setSize(float size) {
        this.size = size;
    }

    /**
     * The sprite used to draw the distortion texture. The red channel corresponds to horizontal distortion vector,
     * while the green channel corresponds to vertical distortion vector. The engine will normalize these values for
     * you. The blue channel corresponds to distortion magnitude. For example, a pure white square as a distortion
     * texture will copy a square of pixels somewhere to the top-right of the screen.
     * <p>
     * @return The sprite used to draw the distortion texture.
     * <p>
     * @since Alpha 1.1
     */
    @Override
    public SpriteAPI getSprite() {
        if (frame >= FRAMES || frame < 0) {
            animation[0].setSize(2f * size, 2f * size);
            return animation[0];
        }

        animation[(int) frame].setSize(2f * size, 2f * size);
        return animation[(int) frame];
    }

    /**
     * Gets the current velocity of the distortion.
     * <p>
     * @return The current velocity of the distortion.
     * <p>
     * @since Alpha 1.2
     */
    public Vector2f getVelocity() {
        return velocity;
    }

    /**
     * Sets the velocity for the distortion.
     * <p>
     * @param velocity The velocity to apply to the distortion.
     * <p>
     * @since Alpha 1.2
     */
    public void setVelocity(Vector2f velocity) {
        this.velocity.set(velocity);
    }

    /**
     * Returns whether the distortion is currently fading or not, regardless of how it is fading.
     * <p>
     * @return Whether the distortion is currently fading or not.
     * <p>
     * @since Alpha 1.1
     */
    public boolean isFading() {
        return Float.compare(deltaSize, 0f) != 0 || Float.compare(deltaIntensity, 0f) != 0;
    }

    /**
     * Whether the distortion should be flipped in magnitude. This simulates the effect of performing a
     * horizontal-vertical inversion of the red and green color channels. For example, if the distortion normally looks
     * like a sphere, it will instead look like a spherical hole.
     * <p>
     * @return Whether the magnitude should be flipped.
     * <p>
     * @since Alpha 1.1
     */
    @Override
    public boolean isFlipped() {
        return flipped;
    }

    /**
     * Sets the visible arc of the distortion, in degrees. Note: the entire shader is visible if the arc spans 0
     * degrees.
     * <p>
     * @param start The start of the distortion's visible arc, in degrees.
     * @param end The end of the distortion's visible arc, in degrees.
     * <p>
     * @since Alpha 1.11
     */
    public void setArc(float start, float end) {
        arcStart = start;
        arcEnd = end;
        if ((arcStart < -360f) || (arcStart > 360f)) {
            arcStart %= 360f;
        }
        if (arcStart < 0f) {
            arcStart += 360f;
        }
        if ((arcEnd < -360f) || (arcEnd > 360f)) {
            arcEnd %= 360f;
        }
        if (arcEnd < 0f) {
            arcEnd += 360f;
        }
    }

    /**
     * Sets the frame rate and the starting frame the animation will play at after its lifetime expires.
     * <p>
     * @param frameRate The frame rate the animation should play at when the object's lifetime expires.
     * @param frameStart The frame the animation should start at when the object's lifetime expires.
     * <p>
     * @since Alpha 1.1
     */
    public void setAutoAnimateFrameRate(float frameRate, float frameStart) {
        this.autoAnimateFrameRate = frameRate;
        this.autoAnimateFrameStart = frameStart;
    }

    /**
     * Gets the change in size over time.
     * <p>
     * @return The rate of change of the size of the distortion.
     * <p>
     * @since 1.8.0
     */
    public float getDeltaSize() {
        return deltaSize;
    }

    /**
     * Sets the change in size over time.
     * <p>
     * @param deltaSize The rate of change of size to apply to the distortion.
     * <p>
     * @since 1.8.0
     */
    public void setDeltaSize(float deltaSize) {
        this.deltaSize = deltaSize;
    }

    /**
     * Gets the maximum size to reach.
     * <p>
     * @return The maximum size that the distortion can reach.
     * <p>
     * @since 1.8.0
     */
    public float getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the maximum size to reach.
     * <p>
     * @param maxSize The maximum size that the distortion should reach.
     * <p>
     * @since 1.8.0
     */
    public void setMaxSize(float maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Gets the change in intensity over time.
     * <p>
     * @return The rate of change of the intensity of the distortion.
     * <p>
     * @since 1.8.0
     */
    public float getDeltaIntensity() {
        return deltaIntensity;
    }

    /**
     * Sets the change in intensity over time.
     * <p>
     * @param deltaIntensity The rate of change of intensity to apply to the distortion.
     * <p>
     * @since 1.8.0
     */
    public void setDeltaIntensity(float deltaIntensity) {
        this.deltaIntensity = deltaIntensity;
    }

    /**
     * Gets the maximum intensity to reach.
     * <p>
     * @return The maximum intensity that the distortion can reach.
     * <p>
     * @since 1.8.0
     */
    public float getMaxIntensity() {
        return maxIntensity;
    }

    /**
     * Sets the maximum intensity to reach.
     * <p>
     * @param maxIntensity The maximum intensity that the distortion should reach.
     * <p>
     * @since 1.8.0
     */
    public void setMaxIntensity(float maxIntensity) {
        this.maxIntensity = maxIntensity;
    }
}
