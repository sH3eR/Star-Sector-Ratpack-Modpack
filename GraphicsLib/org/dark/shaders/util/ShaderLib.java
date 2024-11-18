package org.dark.shaders.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.CombatAsteroidAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MissileRenderDataAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.apache.log4j.Level;
import org.dark.graphics.util.Tessellate;
import org.dark.shaders.ShaderModPlugin;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.util.TextureData.ObjectType;
import org.dark.shaders.util.TextureData.TextureDataType;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.KHRDebugCallback;
import org.lwjgl.opengl.KHRDebugCallback.Handler;
import org.lwjgl.util.vector.Vector2f;

/**
 * This class performs a variety of functions designed to assist in shader-writing.
 * <p>
 * @author DarkRevenant
 */
public final class ShaderLib {

    public static final Comparator<ShipAPI> SHIP_DRAW_ORDER = new Comparator<ShipAPI>() {
        @Override
        public int compare(ShipAPI ship1, ShipAPI ship2) {
            if (ship1.getParentStation() == ship2) {
                if (ship1.getHullSpec().getHints().contains(ShipTypeHints.UNDER_PARENT)) {
                    return -1; // ship1 first
                } else {
                    return 1; // ship2 first
                }
            } else if (ship2.getParentStation() == ship1) {
                if (ship2.getHullSpec().getHints().contains(ShipTypeHints.UNDER_PARENT)) {
                    return 1; // ship2 first
                } else {
                    return -1; // ship1 first
                }
            }
            if ((ship1.getParentStation() != null) && (ship1.getParentStation() == ship2.getParentStation())) {
                if (ship1.getHullSpec().getHints().contains(ShipTypeHints.UNDER_PARENT)
                        && !ship2.getHullSpec().getHints().contains(ShipTypeHints.UNDER_PARENT)) {
                    return -1; // ship1 first
                } else if (!ship1.getHullSpec().getHints().contains(ShipTypeHints.UNDER_PARENT)
                        && ship2.getHullSpec().getHints().contains(ShipTypeHints.UNDER_PARENT)) {
                    return 1; // ship2 first
                } else {
                    final List<ShipAPI> children = ship1.getParentStation().getChildModulesCopy();
                    return Integer.compare(children.indexOf(ship1), children.indexOf(ship2));
                }
            }
            if (ship1.getLayer().ordinal() > ship2.getLayer().ordinal()) {
                return 1; // ship2 first
            } else if (ship1.getLayer().ordinal() < ship2.getLayer().ordinal()) {
                return -1; // ship1 first
            }
            return ship1.getHullSpec().getHullId().compareTo(ship2.getHullSpec().getHullId()); // alphabetical order
        }
    };

    private static final Comparator<ShaderAPI> LOAD_ORDER = new Comparator<ShaderAPI>() {
        @Override
        public int compare(ShaderAPI shader1, ShaderAPI shader2) {
            final int ro1, ro2;
            if (null == shader1.getRenderOrder()) {
                ro1 = 3;
            } else {
                switch (shader1.getRenderOrder()) {
                    case OBJECT_SPACE:
                        ro1 = 0;
                        break;
                    case WORLD_SPACE:
                        ro1 = 1;
                        break;
                    case DISTORTED_SPACE:
                        ro1 = 2;
                        break;
                    default:
                        ro1 = 3;
                        break;
                }
            }
            if (null == shader2.getRenderOrder()) {
                ro2 = 3;
            } else {
                switch (shader2.getRenderOrder()) {
                    case OBJECT_SPACE:
                        ro2 = 0;
                        break;
                    case WORLD_SPACE:
                        ro2 = 1;
                        break;
                    case DISTORTED_SPACE:
                        ro2 = 2;
                        break;
                    default:
                        ro2 = 3;
                        break;
                }
            }
            if (ro1 < ro2) {
                return -1; // shader1 first
            } else if (ro2 < ro1) {
                return 1; // shader2 first
            } else {
                return 0;
            }
        }
    };

    public static final boolean VALIDATE_EVERY_FRAME = false;
    public static final boolean DEBUG_CALLBACK = false;

    private static int RTTSizeX = 2048;
    private static int RTTSizeY = 2048;
    private static final String SETTINGS_FILE = "GRAPHICS_OPTIONS.ini";

    private static boolean auxiliaryBuffer64Bit = false;
    private static int auxiliaryBufferId;
    private static int auxiliaryBufferTex;
    private static boolean buffersAllowed = false;
    private static int displayHeight = 1080;
    private static int displayWidth = 1920;
    private static int foregroundBufferId;
    private static int foregroundBufferTex;
    private static boolean isFirstFrame = true;
    private static boolean isForegroundEmpty = true;
    private static boolean isForegroundRendered = false;
    private static int screenTex;

    private static final List<ShaderAPI> shaders = new ArrayList<>(10);

    private static boolean shadersAllowed = false;
    private static float squareTrans = 1f;
    private static double texCXRatio = 0.0625d;
    private static double texCYRatio = 0.47265625d;
    private static boolean useFramebufferARB = false;
    private static boolean useFramebufferCore = false;
    private static boolean useFramebufferEXT = false;
    private static boolean aaCompatMode = false;
    static boolean enabled = false;
    static boolean extraClear = false;
    static boolean initialized = false;
    static int reloadKey;
    static int toggleKey;

    /**
     * Adds the given instance of ShaderAPI to the rendering queue. Duplicates of the same shader or same type of shader
     * are invalid. The shader will be added even if ShaderLib is not enabled.
     * <p>
     * @param shader The shader to add.
     */
    public static void addShaderAPI(ShaderAPI shader) {
        for (ShaderAPI sdr : shaders) {
            if (sdr.getClass().equals(shader.getClass())) {
                return;
            }
        }
        shaders.add(shader);
    }

    /**
     * Do not use FBOs if this returns false! Otherwise the game will crash!
     * <p>
     * @return Whether the user can use framebuffer objects.
     */
    public static boolean areBuffersAllowed() {
        return buffersAllowed;
    }

    /**
     * Do not use shader code if this returns false! Otherwise the game will crash!
     * <p>
     * @return Whether the user can use shaders (has OpenGL 2.0 support).
     */
    public static boolean areShadersAllowed() {
        return shadersAllowed;
    }

    /**
     * Returns the state of the antialiasing compatibility mode option.
     * <p>
     * @return The current "aaCompatMode" setting.
     * <p>
     * @since v1.5.1
     */
    public static boolean isAACompatMode() {
        return aaCompatMode;
    }

    /**
     * This function is meant to be used at the start of the shader's rendering stage. This sets also sets glOrtho to a
     * screen-space value.
     * <p>
     * This function pushes GL_PROJECTION, GL_TEXTURE, and GL_MODELVIEW (in that order) onto the stack, and also pushes
     * GL_ALL_ATTRIB_BITS to the stack. {@link ShaderLib#exitDraw()} can be used to undo the changes caused by this
     * function; if you do not do so, errors may occur.
     * <p>
     * @param shader The shader program ID to bind the renderer to.
     */
    public static void beginDraw(int shader) {
        GL20.glUseProgram(shader);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        GL11.glViewport(0, 0, (int) (Global.getSettings().getScreenWidthPixels() * Display.getPixelScaleFactor()),
                (int) (Global.getSettings().getScreenHeightPixels() * Display.getPixelScaleFactor()));

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, displayWidth, 0, displayHeight, -1, 1);

        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPushMatrix();

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * Clamps an angle to the range -180 to 180 degrees.
     * <p>
     * @param degrees The angle.
     * <p>
     * @return An angle, clamped to -180 to 180 degrees.
     */
    public static float clampAngle(float degrees) {
        float degreesPredicate = degrees % 360f;
        if (degreesPredicate > 180f) {
            degreesPredicate -= 360f;
        } else if (degreesPredicate < -180f) {
            degreesPredicate += 360f;
        }

        return degreesPredicate;
    }

    /**
     * This function copies the current screen to the given texture and binds that texture to the given texture unit.
     * This function is meant to be used when the uniform parameters have been bound during the shader's rendering
     * stage.
     * <p>
     * @param texture The texture to copy to.
     * @param textureUnit The texture unit to bind the given texture to.
     */
    public static void copyScreen(int texture, int textureUnit) {
        GL13.glActiveTexture(textureUnit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        if (isFirstFrame) {
            GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 0, 0, RTTSizeX, RTTSizeY, 0);
            isFirstFrame = false;
        } else {
            GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, RTTSizeX, RTTSizeY);
        }
    }

    /**
     * This function draws a quad formatted to fit the whole screen, with texture coordinates mapped such that the
     * entire screen texture (which is usually a size like 2048x2048) is represented by the range [0,0] to [1,1]. Not
     * intended for world coordinates.
     * <p>
     * @param scale How much to scale the quad by (usually for subsampling purposes).
     * <p>
     * @since Alpha 1.2
     */
    public static void drawScreenQuad(float scale) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2d(0.0, 1.0 - texCYRatio);
        GL11.glVertex2d(0.0, displayHeight * scale);
        GL11.glTexCoord2d(1.0 - texCXRatio, 1.0 - texCYRatio);
        GL11.glVertex2d(displayWidth * scale, displayHeight * scale);
        GL11.glTexCoord2d(1.0 - texCXRatio, 0.0);
        GL11.glVertex2d(displayWidth * scale, 0.0);
        GL11.glTexCoord2d(0.0, 0.0);
        GL11.glVertex2d(0.0, 0.0);
        GL11.glEnd();
    }

    /**
     * This function is meant to be used at the end of the shader's rendering stage.
     * <p>
     * This function pops GL_MODELVIEW, GL_TEXTURE, and GL_PROJECTION (in that order) from the stack, and also pops
     * state variables from the stack. See {@link ShaderLib#beginDraw(int shader)}.
     */
    public static void exitDraw() {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTex);

        GL20.glUseProgram(0);
    }

    /**
     * Get the cosine of an angle. From LazyLib's FastTrig library, designed by JeffK.
     * <p>
     * @param radians The angle.
     * <p>
     * @return The cosine of the angle.
     */
    public static double fastCos(double radians) {
        return fastSin(radians + Math.PI / 2);
    }

    /**
     * Get the sine of an angle. From LazyLib's FastTrig library, designed by JeffK.
     * <p>
     * @param radians The angle.
     * <p>
     * @return The sine of the angle.
     */
    public static double fastSin(double radians) {
        double radiansPredicate = reduceSinAngle(radians); // limits angle to between -PI/2 and +PI/2
        if (Math.abs(radiansPredicate) <= Math.PI / 4) {
            return Math.sin(radiansPredicate);
        } else {
            return Math.cos(Math.PI / 2 - radiansPredicate);
        }
    }

    /**
     * Returns the framebuffer object ID of the auxiliary buffer. The auxiliary buffer may be used for any drawing you
     * might need as input for another shader. The auxiliary buffer is not preserved between frames.
     * <p>
     * The auxiliary buffer texture is a 32-bit or 64-bit RGBA texture with the smallest power-of-two size that fits the
     * visible screen. It has mipmaps and a floating-point internal type.
     * <p>
     * @return The framebuffer object ID of the auxiliary buffer. Returns 0 if framebuffer objects are not supported.
     * <p>
     * @since Alpha v1.6
     */
    public static int getAuxiliaryBufferId() {
        if (buffersAllowed) {
            return auxiliaryBufferId;
        } else {
            return 0;
        }
    }

    /**
     * Returns the texture ID of the texture pointed to by the auxiliary buffer. The auxiliary buffer may be used for
     * any drawing you might need as input for another shader. The auxiliary buffer is not preserved between frames.
     * <p>
     * The auxiliary buffer texture is a 32-bit or 64-bit RGBA texture with the smallest power-of-two size that fits the
     * visible screen. It has mipmaps and a floating-point internal type.
     * <p>
     * @return The texture ID of the texture pointed to by the auxiliary buffer. Returns 0 if framebuffer objects are
     * not supported.
     * <p>
     * @since Alpha v1.6
     */
    public static int getAuxiliaryBufferTexture() {
        if (buffersAllowed) {
            return auxiliaryBufferTex;
        } else {
            return 0;
        }
    }

    /**
     * Returns the texture ID of the framebuffer texture containing every foreground object. Useful for object-space
     * shaders. Returns 0 if there is no combat engine. Do not use this if the user does not have framebuffer object
     * support!
     * <p>
     * Warning: This function may reset your framebuffer binding to 0! You should save your framebuffer parameters (if
     * any) prior to calling this function! Note that this function uses any Material maps associated with those
     * foreground objects.
     * <p>
     * @param viewport The viewport in use by the renderer.
     * <p>
     * @return The texture ID of the framebuffer texture containing every foreground object. Includes any Material maps
     * associated with those foreground objects.
     */
    public static int getForegroundTexture(ViewportAPI viewport) {
        if (Global.getCombatEngine() == null) {
            return 0;
        }

        if (!isForegroundRendered) {
            renderForeground(viewport);
        }

        return foregroundBufferTex;
    }

    /**
     * Gets the height of ShaderLib's screen texture.
     * <p>
     * @return The height of ShaderLib's screen texture.
     */
    public static int getInternalHeight() {
        return RTTSizeY;
    }

    /**
     * Gets the width of ShaderLib's screen texture.
     * <p>
     * @return The width of ShaderLib's screen texture.
     */
    public static int getInternalWidth() {
        return RTTSizeX;
    }

    public static String getProgramLogInfo(int obj) {
        return GL20.glGetProgramInfoLog(obj, GL20.glGetProgrami(obj, GL20.GL_INFO_LOG_LENGTH));
    }

    /**
     * Returns the texture ID of the texture allocated for the purpose of storing the current rendered screen.
     * <p>
     * @return The texture ID of the texture allocated for the purpose of storing the currently-rendered screen.
     * <p>
     * @since Alpha v1.03
     */
    public static int getScreenTexture() {
        return screenTex;
    }

    /**
     * Returns the loaded instance of the given class that implements ShaderAPI. Returns null if there is no instance of
     * the given shader.
     * <p>
     * @param shaderAPI The class to search for. Must implement ShaderAPI.
     * <p>
     * @return The instance of the given shader. Returns null if there is no instance of the given shader.
     * <p>
     * @since Alpha 1.02
     */
    public static ShaderAPI getShaderAPI(Class<? extends ShaderAPI> shaderAPI) {
        for (ShaderAPI sdr : shaders) {
            if (sdr.getClass().equals(shaderAPI)) {
                return sdr;
            }
        }
        return null;
    }

    /**
     * The returned list is read-only and sorted in render order.
     * <p>
     * @return A read-only copy of the loaded ShaderAPI objects.
     */
    public static List<ShaderAPI> getShaderAPIsCopy() {
        Collections.sort(shaders, LOAD_ORDER);
        return Collections.unmodifiableList(shaders);
    }

    public static String getShaderLogInfo(int obj) {
        return GL20.glGetShaderInfoLog(obj, GL20.glGetShaderi(obj, GL20.GL_INFO_LOG_LENGTH));
    }

    /**
     * Gets a transformation scalar for the x axis to pass to the shader to grant it a proper aspect ratio.
     * <p>
     * @return The transformation scalar for the x axis to pass to a shader to grant it a proper aspect ratio.
     */
    public static float getSquareTransform() {
        return squareTrans;
    }

    /**
     * Gets a scalar (x) and offset (y) to transform any range of floats to a texture's standard [0,1] clamp. Sent float
     * (s): s = (data - y) / x. Decoded float (d): d = s * x + y
     * <p>
     * @param minValue The lower bound of the float range.
     * @param maxValue The upper bound of the float range.
     * <p>
     * @return A scalar (x) and offset (y) that can be used to transform the given range of floats to a texture's
     * standard [0,1] clamp.
     */
    public static Vector2f getTextureDataNormalization(float minValue, float maxValue) {
        final Vector2f tempVec = new Vector2f(0.01f, 0f);
        if (Math.abs(maxValue - minValue) >= 0.01f) {
            tempVec.x = maxValue - minValue;
        }
        tempVec.y = minValue;
        return tempVec;
    }

    /**
     * Gets the fraction of the U component of ShaderLib's screen texture that is visible on screen.
     * <p>
     * @return The fraction of the U component of ShaderLib's screen texture that is visible on screen.
     */
    public static float getVisibleU() {
        return 1f - (float) texCXRatio;
    }

    /**
     * Gets the fraction of the V component of ShaderLib's screen texture that is visible on screen.
     * <p>
     * @return The fraction of the V component of ShaderLib's screen texture that is visible on screen.
     */
    public static float getVisibleV() {
        return 1f - (float) texCYRatio;
    }

    /**
     * Starts up ShaderLib; many mods may call this method but only the first will actually do anything.
     */
    public static void init() {
        if (initialized) {
            return;
        }

        if (DEBUG_CALLBACK) {
            GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
            GL43.glDebugMessageCallback(new KHRDebugCallback(new QuickHandler()));
        }

        Global.getLogger(ShaderLib.class).setLevel(Level.INFO);

        displayWidth = (int) Global.getSettings().getScreenWidthPixels();
        if (displayWidth <= 1024) {
            RTTSizeX = 1024;
        } else if (displayWidth <= 2048) {
            RTTSizeX = 2048;
        } else if (displayWidth <= 4096) {
            RTTSizeX = 4096;
        } else if (displayWidth <= 8192) {
            RTTSizeX = 8192;
        } else if (displayWidth <= 16384) {
            RTTSizeX = 16384;
        } else if (displayWidth <= 32768) {
            RTTSizeX = 32768;
        }
        texCXRatio = (RTTSizeX - displayWidth) / (double) RTTSizeX;

        displayHeight = (int) Global.getSettings().getScreenHeightPixels();
        if (displayHeight <= 1024) {
            RTTSizeY = 1024;
        } else if (displayHeight <= 2048) {
            RTTSizeY = 2048;
        } else if (displayHeight <= 4096) {
            RTTSizeY = 4096;
        } else if (displayHeight <= 8192) {
            RTTSizeY = 8192;
        } else if (displayHeight <= 16384) {
            RTTSizeY = 16384;
        } else if (displayHeight <= 32768) {
            RTTSizeY = 32768;
        }
        texCYRatio = (RTTSizeY - displayHeight) / (double) RTTSizeY;

        squareTrans = RTTSizeX / (float) RTTSizeY;

        if (GLContext.getCapabilities().GL_EXT_framebuffer_object || GLContext.getCapabilities().OpenGL30
                || GLContext.getCapabilities().GL_ARB_framebuffer_object) {
            if (GLContext.getCapabilities().OpenGL30) {
                useFramebufferCore = true;
                Global.getLogger(ShaderLib.class).log(Level.INFO, "Using Core framebuffer.");
            } else if (GLContext.getCapabilities().GL_ARB_framebuffer_object) {
                useFramebufferARB = true;
                Global.getLogger(ShaderLib.class).log(Level.INFO, "Using ARB framebuffer.");
            } else {
                useFramebufferEXT = true;
                Global.getLogger(ShaderLib.class).log(Level.INFO, "Using Extension framebuffer.");
            }
            buffersAllowed = true;
        } else {
            buffersAllowed = false;
            Global.getLogger(ShaderLib.class).log(Level.ERROR,
                    "GPU does not support Framebuffer Objects! Some shaders disabled!");
        }

        if (GLContext.getCapabilities().OpenGL20) {
            shadersAllowed = true;
        } else {
            shadersAllowed = false;
            Global.getLogger(ShaderLib.class).log(Level.ERROR, "GPU does not support OpenGL 2.0! Shaders disabled!");
        }

        try {
            loadSettings();
        } catch (IOException | JSONException e) {
            Global.getLogger(ShaderLib.class).log(Level.ERROR, "Failed to load shader settings: " + e.getMessage());
            enabled = false;
            return;
        }

        if (shadersAllowed) {
            screenTex = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTex);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB8, RTTSizeX, RTTSizeY, 0, GL11.GL_RGB,
                    GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            if (useFramebufferEXT) {
                EXTFramebufferObject.glGenerateMipmapEXT(GL11.GL_TEXTURE_2D);
            } else if (useFramebufferARB) {
                ARBFramebufferObject.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            } else {
                GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            }
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        }

        if (buffersAllowed && shadersAllowed) {
            foregroundBufferTex = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, foregroundBufferTex);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, RTTSizeX, RTTSizeY, 0, GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            if (useFramebufferEXT) {
                EXTFramebufferObject.glGenerateMipmapEXT(GL11.GL_TEXTURE_2D);
            } else if (useFramebufferARB) {
                ARBFramebufferObject.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            } else {
                GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            }
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
            if (useFramebufferCore) {
                foregroundBufferId = makeFramebuffer(GL30.GL_COLOR_ATTACHMENT0, foregroundBufferTex,
                        ShaderLib.getInternalWidth(), ShaderLib.getInternalHeight(), 0);
            } else if (useFramebufferARB) {
                foregroundBufferId = makeFramebuffer(ARBFramebufferObject.GL_COLOR_ATTACHMENT0, foregroundBufferTex,
                        ShaderLib.getInternalWidth(), ShaderLib.getInternalHeight(), 0);
            } else {
                foregroundBufferId = makeFramebuffer(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, foregroundBufferTex,
                        ShaderLib.getInternalWidth(), ShaderLib.getInternalHeight(), 0);
            }

            if (foregroundBufferId == 0) {
                buffersAllowed = false;
                Global.getLogger(ShaderLib.class).log(Level.ERROR,
                        "Foreground framebuffer object error!  ShaderLib features disabled!");
            }

            auxiliaryBufferTex = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, auxiliaryBufferTex);
            if (auxiliaryBuffer64Bit) {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA16, ShaderLib.getInternalWidth(),
                        ShaderLib.getInternalHeight(), 0, GL11.GL_RGBA,
                        GL11.GL_UNSIGNED_SHORT, (ByteBuffer) null);
            } else {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, ShaderLib.getInternalWidth(),
                        ShaderLib.getInternalHeight(), 0, GL11.GL_RGBA,
                        GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            }
            if (useFramebufferEXT) {
                EXTFramebufferObject.glGenerateMipmapEXT(GL11.GL_TEXTURE_2D);
            } else if (useFramebufferARB) {
                ARBFramebufferObject.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            } else {
                GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            }
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
            if (useFramebufferCore) {
                auxiliaryBufferId = makeFramebuffer(GL30.GL_COLOR_ATTACHMENT0, auxiliaryBufferTex,
                        ShaderLib.getInternalWidth(), ShaderLib.getInternalHeight(), 0);
            } else if (useFramebufferARB) {
                auxiliaryBufferId = makeFramebuffer(ARBFramebufferObject.GL_COLOR_ATTACHMENT0, auxiliaryBufferTex,
                        ShaderLib.getInternalWidth(), ShaderLib.getInternalHeight(), 0);
            } else {
                auxiliaryBufferId
                        = makeFramebuffer(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, auxiliaryBufferTex,
                                ShaderLib.getInternalWidth(), ShaderLib.getInternalHeight(), 0);
            }

            if (auxiliaryBufferId == 0) {
                buffersAllowed = false;
                Global.getLogger(ShaderLib.class).log(Level.ERROR,
                        "Auxiliary framebuffer object error!  ShaderLib features disabled!");
            }
        }

        if (DEBUG_CALLBACK) {
            GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
        }

        initialized = true;
    }

    // TODO: more error handling like parsing the i, i1, i3...
    public static class QuickHandler implements Handler {

        @Override
        public void handleMessage(int i, int i1, int i2, int i3, String string) {
            String trace = "\n";
            StackTraceElement stes[] = new Throwable().getStackTrace();
            for (StackTraceElement ste : stes) {
                trace += ste + "\n";
            }
            Global.getLogger(ShaderLib.class).log(Level.ERROR, "QuickHandler: " + i + ", " + i1 + ", " + i2 + ", " + i3 + ", " + string + trace);
        }
    }

    /**
     * Returns true if there is no combat engine. Do not use this if the user does not have framebuffer object support!
     * Warning: This function may reset your framebuffer binding to 0! You should save your framebuffer parameters (if
     * any) prior to calling this function!
     * <p>
     * @param viewport The viewport in use by the renderer.
     * <p>
     * @return Whether the foreground framebuffer texture is empty or not.
     */
    public static boolean isForegroundEmpty(ViewportAPI viewport) {
        if (Global.getCombatEngine() == null) {
            return true;
        }

        if (!isForegroundRendered) {
            renderForeground(viewport);
        }

        return isForegroundEmpty;
    }

    /**
     * Checks if a point, bounded by a certain radius, is on screen. Do note that this is only absolutely true if it
     * returns false; if it returns true, the point may still not actually be on screen.
     * <p>
     * @param worldCoords The absolute world coordinates of the point.
     * @param radius How far away from the screen the point can be.
     * <p>
     * @return Whether the point is within the given radius of the viewport.
     */
    public static boolean isOnScreen(Vector2f worldCoords, float radius) {
        return Global.getCombatEngine().getViewport().isNearViewport(worldCoords, radius);
    }

    /**
     * Checks if a line, bounded by a certain radius, is on screen. Do note that this is only absolutely true if it
     * returns false; if it returns true, the line may still not actually be on screen.
     * <p>
     * @param worldCoords1 The absolute world coordinates of one endpoint of the line.
     * @param worldCoords2 The absolute world coordinates of the other endpoint of the line.
     * @param radius How far away from the screen the line can be.
     * <p>
     * @return Whether the line is within the given radius of the viewport.
     */
    public static boolean isOnScreen(Vector2f worldCoords1, Vector2f worldCoords2, float radius) {
        if (Global.getCombatEngine().getViewport().isNearViewport(worldCoords1, radius)) {
            return true;
        }
        if (Global.getCombatEngine().getViewport().isNearViewport(worldCoords2, radius)) {
            return true;
        }

        final ViewportAPI viewport = Global.getCombatEngine().getViewport();
        final float sideL = viewport.getLLX() - radius;
        final float sideR = viewport.getLLX() + viewport.getVisibleWidth() + radius;
        final float sideB = viewport.getLLY() - radius;
        final float sideU = viewport.getLLY() + viewport.getVisibleHeight() + radius;

        if (lineIntersect(worldCoords1, worldCoords2, sideL, sideB, sideL, sideU)) {
            return true;
        } else if (lineIntersect(worldCoords1, worldCoords2, sideL, sideU, sideR, sideU)) {
            return true;
        } else if (lineIntersect(worldCoords1, worldCoords2, sideR, sideU, sideR, sideB)) {
            return true;
        } else {
            return lineIntersect(worldCoords1, worldCoords2, sideR, sideB, sideL, sideB);
        }
    }

    /**
     * Compiles the given shader. Does NOT validate the shader; you must call that function yourself. See the example
     * shaders for how to do this.
     * <p>
     * @param vert The vertex shader, given as a String.
     * @param frag The fragment shader, given as a String.
     * <p>
     * @return The shader program ID corresponding to the newly-compiled shader. Returns 0 on error.
     */
    public static int loadShader(String vert, String frag) {
        final int vertShader, fragShader;

        try {
            vertShader = createShader(vert, GL20.GL_VERTEX_SHADER);
            fragShader = createShader(frag, GL20.GL_FRAGMENT_SHADER);
        } catch (RuntimeException exc) {
            Global.getLogger(ShaderLib.class).log(Level.ERROR, exc.getMessage());
            return 0;
        }

        if (vertShader == 0 || fragShader == 0) {
            return 0;
        }

        final int prog = GL20.glCreateProgram();

        if (prog == 0) {
            return 0;
        }

        GL20.glAttachShader(prog, vertShader);
        GL20.glAttachShader(prog, fragShader);

        GL20.glLinkProgram(prog);
        if (GL20.glGetProgrami(prog, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            Global.getLogger(ShaderLib.class).log(Level.ERROR, getProgramLogInfo(prog));
            GL20.glDeleteProgram(prog);
            GL20.glDeleteShader(vertShader);
            GL20.glDeleteShader(fragShader);
            return 0;
        }

        /*
         glValidateProgram(prog); if (glGetProgrami(prog, GL_VALIDATE_STATUS) == GL_FALSE) { Global.getLogger(ShaderLib.class).log(Level.ERROR,
         getProgramLogInfo(prog)); return 0; }
         */
        return prog;
    }

    /**
     * Creates a framebuffer object for the given texture.
     * <p>
     * @param attachment Can be EXTFramebufferObject.GL_COLOR_ATTACHMENTi_EXT (i is typically 0),
     * EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, or EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, depending on
     * what kind of texture you use.
     * @param texture Assumed to be a GL_TEXTURE_2D texture.
     * @param texWidth Width of texture
     * @param texHeight Height of texture
     * @param mipLevel The mipmapping level of the texture to bind to.
     * <p>
     * @return The created FBO ID.
     */
    public static int makeFramebuffer(int attachment, int texture, int texWidth, int texHeight, int mipLevel) {
        if (useFramebufferCore) {
            final int rbStencilId = GL30.glGenRenderbuffers();
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rbStencilId);
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_STENCIL_INDEX8, texWidth, texHeight);
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);

            final int bufferId = GL30.glGenFramebuffers();
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, bufferId);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachment, GL11.GL_TEXTURE_2D, texture, mipLevel);
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER,
                    rbStencilId);

            final int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
            if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
                Global.getLogger(ShaderLib.class).log(Level.ERROR, "Couldn't make framebuffer! Error: " + status);
                return 0;
            }
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

            return bufferId;
        } else if (useFramebufferARB) {
            final int rbStencilId = ARBFramebufferObject.glGenRenderbuffers();
            ARBFramebufferObject.glBindRenderbuffer(ARBFramebufferObject.GL_RENDERBUFFER, rbStencilId);
            ARBFramebufferObject.glRenderbufferStorage(ARBFramebufferObject.GL_RENDERBUFFER,
                    ARBFramebufferObject.GL_STENCIL_INDEX8, texWidth, texHeight);
            ARBFramebufferObject.glBindRenderbuffer(ARBFramebufferObject.GL_RENDERBUFFER, 0);

            final int bufferId = ARBFramebufferObject.glGenFramebuffers();
            ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, bufferId);
            ARBFramebufferObject.glFramebufferTexture2D(ARBFramebufferObject.GL_FRAMEBUFFER, attachment,
                    GL11.GL_TEXTURE_2D, texture, mipLevel);
            ARBFramebufferObject.glFramebufferRenderbuffer(ARBFramebufferObject.GL_FRAMEBUFFER,
                    ARBFramebufferObject.GL_STENCIL_ATTACHMENT,
                    ARBFramebufferObject.GL_RENDERBUFFER, rbStencilId);

            final int status = ARBFramebufferObject.glCheckFramebufferStatus(ARBFramebufferObject.GL_FRAMEBUFFER);
            if (status != ARBFramebufferObject.GL_FRAMEBUFFER_COMPLETE) {
                Global.getLogger(ShaderLib.class).log(Level.ERROR, "Couldn't make framebuffer! Error: " + status);
                return 0;
            }
            ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, 0);

            return bufferId;
        } else {
            final int rbStencilId = EXTFramebufferObject.glGenRenderbuffersEXT();
            EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, rbStencilId);
            EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT,
                    EXTFramebufferObject.GL_STENCIL_INDEX8_EXT, texWidth,
                    texHeight);
            EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, 0);

            final int bufferId = EXTFramebufferObject.glGenFramebuffersEXT();
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, bufferId);
            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, attachment,
                    GL11.GL_TEXTURE_2D, texture, mipLevel);
            EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                    EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT,
                    EXTFramebufferObject.GL_RENDERBUFFER_EXT, rbStencilId);

            final int status = EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT);
            if (status != EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT) {
                Global.getLogger(ShaderLib.class).log(Level.ERROR, "Couldn't make framebuffer! Error: " + status);
                return 0;
            }
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);

            return bufferId;
        }
    }

    /**
     * This function combines the functionalities of copyScreen and drawScreenQuad. This function is meant to be used
     * when the uniform parameters have been bound during the shader's rendering stage.
     * <p>
     * @param texture The texture to copy to.
     * @param textureUnit The texture unit to bind the given texture to.
     */
    public static void screenDraw(int texture, int textureUnit) {
        copyScreen(texture, textureUnit);
        if (extraClear) {
            GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        }
        drawScreenQuad(1f);
    }

    /**
     * Transforms relative screen coordinates to UV texture coordinates.
     * <p>
     * @param screenCoords The relative unscaled screen coordinates to transform.
     * <p>
     * @return The UV texture coordinates corresponding to the given relative screen coordinates.
     */
    public static Vector2f transformScreenToUV(Vector2f screenCoords) {
        final Vector2f tempVec = new Vector2f();
        tempVec.x = screenCoords.x / RTTSizeX;
        tempVec.y = screenCoords.y / RTTSizeY;
        return tempVec;
    }

    /**
     * Transforms absolute world coordinates to relative screen coordinates, unscaled.
     * <p>
     * @param worldCoords The absolute world coordinates to transform.
     * <p>
     * @return The relative screen coordinates corresponding to the given absolute world coordinates.
     */
    public static Vector2f transformWorldToScreen(Vector2f worldCoords) {
        final Vector2f tempVec = new Vector2f();
        tempVec.x = Global.getCombatEngine().getViewport().convertWorldXtoScreenX(worldCoords.x) * Global.getSettings().getScreenScaleMult();
        tempVec.y = Global.getCombatEngine().getViewport().convertWorldYtoScreenY(worldCoords.y) * Global.getSettings().getScreenScaleMult();
        return tempVec;
    }

    /**
     * Transforms a distance given in absolute world space units to UV distance. Normalized to square coordinates.
     * <p>
     * @param units The absolute world-space distance to transform.
     * <p>
     * @return UV distance representing the given absolute world space units.
     */
    public static float unitsToUV(float units) {
        return units / (RTTSizeY * Global.getCombatEngine().getViewport().getViewMult());
    }

    /**
     * Whether to use the ARB_framebuffer_object implementation of FBOs.
     * <p>
     * @return Whether to use the ARB_framebuffer_object implementation of FBOs.
     * <p>
     * @since Beta 1.01
     */
    public static boolean useBufferARB() {
        return useFramebufferARB;
    }

    /**
     * Whether to use the core OpenGL 3.0 implementation of FBOs.
     * <p>
     * @return Whether to use the core OpenGL 3.0 implementation of FBOs.
     * <p>
     * @since Beta 1.01
     */
    public static boolean useBufferCore() {
        return useFramebufferCore;
    }

    /**
     * Whether to use the EXT_framebuffer_object implementation of FBOs.
     * <p>
     * @return Whether to use the EXT_framebuffer_object implementation of FBOs.
     * <p>
     * @since Beta 1.01
     */
    public static boolean useBufferEXT() {
        return useFramebufferEXT;
    }

    private static int createShader(String source, int shaderType) throws RuntimeException {
        int shader = 0;
        try {
            shader = GL20.glCreateShader(shaderType);

            if (shader == 0) {
                return 0;
            }

            GL20.glShaderSource(shader, source);
            GL20.glCompileShader(shader);

            if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                throw new RuntimeException("Error creating shader: " + getShaderLogInfo(shader));
            }

            return shader;
        } catch (RuntimeException exc) {
            GL20.glDeleteShader(shader);
            throw exc;
        }
    }

    // From LazyWizard's LazyLib
    private static boolean equals(float a, float b) {
        return ((a == b) || (a >= b * (.9999999f) && a <= b * (1.0000001f)));
    }

    // Modified from LazyWizard's LazyLib
    private static boolean lineIntersect(Vector2f start1, Vector2f end1, float start2x, float start2y, float end2x,
            float end2y) {
        if (Line2D.Float.ptSegDistSq(start2x, start2y, end2x, end2y, end1.x, end1.y) <= 0.11111f) {
            return true;
        }

        if (Line2D.Float.ptSegDistSq(start2x, start2y, end2x, end2y, start1.x, start1.y) <= 0.11111f) {
            return true;
        }

        final float denom = ((end1.x - start1.x) * (end2y - start2y)) - ((end1.y - start1.y) * (end2x - start2x));

        //  AB & CD are parallel
        if (equals(0f, denom)) {
            return false;
        }

        final float numer = ((start1.y - start2y) * (end2x - start2x)) - ((start1.x - start2x) * (end2y - start2y));
        final float r = numer / denom;
        final float numer2 = ((start1.y - start2y) * (end1.x - start1.x)) - ((start1.x - start2x) * (end1.y - start1.y));
        final float s = numer2 / denom;

        return !((r < 0 || r > 1) || (s < 0 || s > 1));
    }

    private static void loadSettings() throws IOException, JSONException {
        final JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE);

        enabled = settings.getBoolean("enableShaders");
        toggleKey = settings.getInt("toggleKey");
        reloadKey = settings.getInt("reloadKey");
        auxiliaryBuffer64Bit = settings.getBoolean("use64BitBuffer");
        extraClear = settings.getBoolean("extraScreenClear");
        aaCompatMode = settings.getBoolean("aaCompatMode");

        if (!enabled) {
            shadersAllowed = false;
            buffersAllowed = false;
        }
    }

    // JeffK's FastTrig functionality, as seen in LazyLib
    private static double reduceSinAngle(double radians) {
        radians %= Math.PI * 2.0; // put us in -2PI to +2PI space
        if (Math.abs(radians) > Math.PI) { // put us in -PI to +PI space
            radians -= (Math.PI * 2.0);
        }
        if (Math.abs(radians) > Math.PI / 2.0) {// put us in -PI/2 to +PI/2 space
            radians = Math.PI - radians;
        }
        return radians;
    }

    /**
     * Retrieves the most appropriate TextureEntry corresponding to the given ship and texture type.
     * <p>
     * @param ship Ship to find TextureEntry for.
     * @param type Texture type to find TextureEntry for.
     * <p>
     * @return TextureEntry corresponding to the given ship/type, or null if not found.
     * <p>
     * @since 1.4.0
     */
    public static TextureEntry getShipTexture(ShipAPI ship, TextureDataType type) {
        TextureEntry entry = null;

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine != null) {
            Map<String, Object> customData = engine.getCustomData();
            if (customData != null) {
                Map<ShipAPI, String> shipTexOvd = (Map<ShipAPI, String>) customData.get("SL_shipTexOvd");
                if (shipTexOvd != null) {
                    String ovdId = shipTexOvd.get(ship);
                    if (ovdId != null) {
                        entry = TextureData.getTextureData(ovdId, type, ObjectType.SHIP, 0);
                    }
                }

                if ((entry == null) && ship.isFighter() && (ship.getWing() != null) && ship.getWing().getSpec().isSupport()) {
                    if ((ship.getCustomData() != null) && !ship.getCustomData().containsKey("SL_fighterCheck")) {
                        ship.setCustomData("SL_fighterCheck", 1);
                        ShipAPI sourceShip = ship.getWing().getSourceShip();
                        if (sourceShip != null) {
                            String fighterSkin = getFighterSkin(ship, sourceShip);
                            if (fighterSkin != null) {
                                overrideShipTexture(ship, fighterSkin);
                                entry = TextureData.getTextureData(fighterSkin, type, ObjectType.SHIP, 0);
                            }
                        }
                    }
                }
            }
        }

        if (entry == null) {
            entry = TextureData.getTextureData(ship.getHullSpec().getHullId(), type, ObjectType.SHIP, 0);

            if (entry == null) {
                entry = TextureData.getTextureData(ship.getHullSpec().getDParentHullId(), type, ObjectType.SHIP, 0);

                if (entry == null) {
                    entry = TextureData.getTextureData(ship.getHullSpec().getBaseHullId(), type, ObjectType.SHIP, 0);
                }
            }
        }
        return entry;
    }

    private static String getFighterSkin(ShipAPI fighter, ShipAPI carrier) {
        if (carrier.getHullStyleId().equals(fighter.getHullStyleId())) {
            return null;
        }

        String cat;
        String skin = null;
        if ((carrier.getOwner() == 0) || (carrier.getOriginalOwner() == 0)) {
            cat = "fighterSkinsPlayerOnly";
            skin = getFighterSkin(cat, fighter, carrier);
        }
        if (skin != null) {
            return skin;
        }

        cat = "fighterSkinsPlayerAndNPC";
        skin = getFighterSkin(cat, fighter, carrier);

        return skin;
    }

    private static String getFighterSkin(String cat, ShipAPI fighter, ShipAPI carrier) {
        String exclude = "fighterSkinsExcludeFromSharing";
        String id = fighter.getHullSpec().getHullId();
        String style = carrier.getHullStyleId();

        List<String> skins = Global.getSettings().getSpriteKeys(cat);
        Set<String> noSharing = new LinkedHashSet<>(Global.getSettings().getSpriteKeys(exclude));

        List<String[]> matching = new ArrayList<>();
        for (String key : skins) {
            if (key.equals(id + "_" + style)) {
                return key;
            }
            if (key.startsWith(id) && !noSharing.contains(key)) {
                String[] skin = {cat, key};
                matching.add(skin);
            }
        }

        if (!matching.isEmpty()) {
            String best = null;
            float minDist = Float.MAX_VALUE;

            for (String[] curr : matching) {
                SpriteAPI sprite = Global.getSettings().getSprite(curr[0], curr[1]);
                float dist = Misc.getColorDist(carrier.getSpriteAPI().getAverageBrightColor(), sprite.getAverageBrightColor());
                if (dist < minDist) {
                    best = curr[1];
                    minDist = dist;
                }
            }
            return best;
        }

        return null;
    }

    /**
     * Overrides a ship's ID for the purposes of determining which TextureEntry to use within shaders (such as
     * lighting). Normally, the ship's Hull ID, D-Parent Hull ID, and then the Base Hull ID are used (in that order).
     * <p>
     * @param ship Ship to override ID for.
     * @param id New ID to use.
     * <p>
     * @since 1.4.0
     */
    public static void overrideShipTexture(ShipAPI ship, String id) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine != null) {
            Map<String, Object> customData = engine.getCustomData();
            if (customData != null) {
                Map<ShipAPI, String> shipTexOvd = (Map<ShipAPI, String>) customData.get("SL_shipTexOvd");
                if (shipTexOvd == null) {
                    shipTexOvd = new WeakHashMap<>();
                    customData.put("SL_shipTexOvd", shipTexOvd);
                }
                shipTexOvd.put(ship, id);
            }
        }
    }

    private static void renderForeground(ViewportAPI viewport) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        if (useFramebufferCore) {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, foregroundBufferId);
        } else if (useFramebufferARB) {
            ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, foregroundBufferId);
        } else {
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, foregroundBufferId);
        }

        GL11.glViewport(0, 0, (int) (Global.getSettings().getScreenWidthPixels() * Display.getPixelScaleFactor()),
                (int) (Global.getSettings().getScreenHeightPixels() * Display.getPixelScaleFactor()));

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(viewport.getLLX(), viewport.getLLX() + viewport.getVisibleWidth(), viewport.getLLY(),
                viewport.getLLY() + viewport.getVisibleHeight(), -2000, 2000);

        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPushMatrix();

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glColorMask(true, true, true, true);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        int objectCount = 0;
        final List<CombatEntityAPI> asteroids = Global.getCombatEngine().getAsteroids();
        int size = asteroids.size();
        for (int i = 0; i < size; i++) {
            final CombatEntityAPI asteroid = asteroids.get(i);
            if (asteroid.getCustomData().containsKey(LightShader.DO_NOT_RENDER)) {
                continue;
            }

            final Vector2f asteroidLocation = asteroid.getLocation();
            if (!isOnScreen(asteroidLocation, 100f)) { // You can't trust asteroid collision radius.
                continue;
            }

            final CombatAsteroidAPI assteroid = (CombatAsteroidAPI) asteroid;

            final SpriteAPI asteroidSprite = assteroid.getSpriteAPI();
            String asteroidType = ShaderModPlugin.ASTEROID_MAP.get(asteroidSprite.getTextureId());
            if (asteroidType == null) {
                asteroidType = "nil";
            }

            final TextureEntry entry = TextureData.getTextureData(asteroidType, TextureDataType.MATERIAL_MAP,
                    ObjectType.ASTEROID, 0);
            final SpriteAPI sprite;
            if (entry != null) {
                sprite = entry.sprite;
                sprite.setAngle(asteroidSprite.getAngle());
                sprite.setSize(asteroidSprite.getWidth(), asteroidSprite.getHeight());
                sprite.setCenter(asteroidSprite.getCenterX(), asteroidSprite.getCenterY());
                sprite.setAlphaMult(asteroidSprite.getAlphaMult());
            } else {
                sprite = asteroidSprite;
            }

            sprite.renderAtCenter(asteroidLocation.x, asteroidLocation.y);

            objectCount++;
        }

        final List<ShipAPI> ships = Global.getCombatEngine().getShips();
        Collections.sort(ships, SHIP_DRAW_ORDER);
        size = ships.size();
        for (int i = 0; i < size; i++) {
            final ShipAPI ship = ships.get(i);
            if (ship.getCustomData().containsKey(LightShader.DO_NOT_RENDER)) {
                continue;
            }

            final Vector2f shipLocation = ship.getLocation();
            if (!isOnScreen(shipLocation, 1.25f * ship.getCollisionRadius())) {
                continue;
            }

            TextureEntry entry = getShipTexture(ship, TextureDataType.MATERIAL_MAP);
            SpriteAPI originalSprite = ship.getSpriteAPI();
            SpriteAPI sprite;
            if (entry != null) {
                sprite = entry.sprite;
                sprite.setAngle(originalSprite.getAngle());
                sprite.setSize(originalSprite.getWidth(), originalSprite.getHeight());
                sprite.setCenter(originalSprite.getCenterX(), originalSprite.getCenterY());
                sprite.setAlphaMult(ship.getCombinedAlphaMult());
                sprite.setColor(originalSprite.getColor());
            } else {
                sprite = originalSprite;
            }

            BoundsAPI bounds = ship.getVisualBounds();
            if (bounds != null) {
                GL11.glEnable(GL11.GL_STENCIL_TEST);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glColorMask(false, false, false, false);
                GL11.glStencilFunc(GL11.GL_ALWAYS, 16, 0xFF); // Set stencil to 16
                GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
                GL11.glStencilMask(0xFF); // Write to stencil buffer
                GL11.glClearStencil(0);
                GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT); // Clear stencil buffer

                Tessellate.render(bounds, 1f, 1f, 1f, ship);

                GL11.glColorMask(true, true, true, true);
                GL11.glStencilFunc(GL11.GL_EQUAL, 16, 0xFF); // Pass test if stencil value is 16
                GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
                GL11.glStencilMask(0x00); // Don't write anything to stencil buffer

                sprite.setBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                sprite.renderAtCenter(shipLocation.x, shipLocation.y);

                GL11.glDisable(GL11.GL_STENCIL_TEST);
                GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 0xFF); // Pass test always
            } else {
                sprite.renderAtCenter(shipLocation.x, shipLocation.y);
            }

            final Vector2f renderOffset = VectorUtils.rotate(ship.getRenderOffset(), ship.getFacing(), new Vector2f());

            final List<WeaponAPI> weapons = ship.getAllWeapons();
            final List<WeaponSlotAPI> emptySlots = ship.getHullSpec().getAllWeaponSlotsCopy();
            final int weaponSize = weapons.size();
            for (int j = 0; j < weaponSize; j++) {
                final WeaponAPI weapon = weapons.get(j);
                if (!emptySlots.remove(weapon.getSlot())) {
                    for (Iterator<WeaponSlotAPI> iter = emptySlots.iterator(); iter.hasNext();) {
                        final WeaponSlotAPI slot = iter.next();
                        final Vector2f slotLocation = Vector2f.add(slot.computePosition(ship), renderOffset, new Vector2f());
                        final Vector2f weaponLocation = Vector2f.add(weapon.getLocation(), renderOffset, new Vector2f());
                        if (MathUtils.getDistance(slotLocation, weaponLocation) <= 1f) {
                            iter.remove();
                            break;
                        }
                    }
                }
            }

            if (bounds == null) {
                final int slotSize = emptySlots.size();
                for (int j = 0; j < slotSize; j++) {
                    final WeaponSlotAPI slot = emptySlots.get(j);
                    if (slot.isDecorative() || slot.isHidden() || slot.isSystemSlot() || (slot.getWeaponType() == WeaponType.LAUNCH_BAY)
                            || slot.isStationModule() || slot.isBuiltIn()) {
                        continue;
                    }
                    final Vector2f slotLocation = Vector2f.add(slot.computePosition(ship), renderOffset, new Vector2f());
                    switch (slot.getSlotSize()) {
                        default:
                        case SMALL:
                            if (slot.isHardpoint()) {
                                entry = TextureData.getTextureData(ship.getHullStyleId(), TextureDataType.MATERIAL_MAP, ObjectType.HARDPOINT_COVER_SMALL, 0);
                                originalSprite = ship.getSmallHardpointCover();
                            } else {
                                entry = TextureData.getTextureData(ship.getHullStyleId(), TextureDataType.MATERIAL_MAP, ObjectType.TURRET_COVER_SMALL, 0);
                                originalSprite = ship.getSmallTurretCover();
                            }
                            break;
                        case MEDIUM:
                            if (slot.isHardpoint()) {
                                entry = TextureData.getTextureData(ship.getHullStyleId(), TextureDataType.MATERIAL_MAP, ObjectType.HARDPOINT_COVER_MEDIUM, 0);
                                originalSprite = ship.getMediumHardpointCover();
                            } else {
                                entry = TextureData.getTextureData(ship.getHullStyleId(), TextureDataType.MATERIAL_MAP, ObjectType.TURRET_COVER_MEDIUM, 0);
                                originalSprite = ship.getMediumTurretCover();
                            }
                            break;
                        case LARGE:
                            if (slot.isHardpoint()) {
                                entry = TextureData.getTextureData(ship.getHullStyleId(), TextureDataType.MATERIAL_MAP, ObjectType.HARDPOINT_COVER_LARGE, 0);
                                originalSprite = ship.getLargeHardpointCover();
                            } else {
                                entry = TextureData.getTextureData(ship.getHullStyleId(), TextureDataType.MATERIAL_MAP, ObjectType.TURRET_COVER_LARGE, 0);
                                originalSprite = ship.getLargeTurretCover();
                            }
                            break;
                    }
                    if (originalSprite == null || originalSprite.getTextureId() == 0) {
                        continue;
                    }

                    if (entry != null) {
                        sprite = entry.sprite;
                        sprite.setAngle(slot.getAngle() + ship.getFacing() - 90f);
                        sprite.setSize(originalSprite.getWidth(), originalSprite.getHeight());
                        sprite.setCenter(originalSprite.getCenterX(), originalSprite.getCenterY());
                        sprite.setAlphaMult(Math.min(ship.getCombinedAlphaMult(), originalSprite.getAlphaMult()));
                        sprite.setColor(originalSprite.getColor());
                    } else {
                        sprite = originalSprite;
                        sprite.setAngle(slot.getAngle() + ship.getFacing() - 90f);
                    }

                    sprite.renderAtCenter(slotLocation.x, slotLocation.y);
                }
            }

            for (int j = 0; j < weaponSize; j++) {
                final WeaponAPI weapon = weapons.get(j);
                if (!weapon.getSlot().isHidden()) {
                    Vector2f weaponLocation = Vector2f.add(weapon.getLocation(), renderOffset, new Vector2f());
                    if (weapon.isDecorative() && weapon.isBeam() && (weapon.getRenderOffsetForDecorativeBeamWeaponsOnly() != null)) {
                        final Vector2f additionalOffset = VectorUtils.rotate(weapon.getRenderOffsetForDecorativeBeamWeaponsOnly(), ship.getFacing(), new Vector2f());
                        weaponLocation = Vector2f.add(weaponLocation, additionalOffset, new Vector2f());
                    }

                    if (weapon.getUnderSpriteAPI() != null) {
                        if (weapon.getSlot().isHardpoint()) {
                            if (weapon.getAnimation() != null) {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.HARDPOINT_UNDER, weapon.getAnimation().getFrame());
                            } else {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.HARDPOINT_UNDER, 0);
                            }
                        } else {
                            if (weapon.getAnimation() != null) {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.TURRET_UNDER, weapon.getAnimation().getFrame());
                            } else {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.TURRET_UNDER, 0);
                            }
                        }
                        originalSprite = weapon.getUnderSpriteAPI();
                        if (entry != null) {
                            sprite = entry.sprite;
                            sprite.setAngle(originalSprite.getAngle());
                            sprite.setSize(originalSprite.getWidth(), originalSprite.getHeight());
                            sprite.setCenter(originalSprite.getCenterX(), originalSprite.getCenterY());
                            sprite.setAlphaMult(Math.min(ship.getCombinedAlphaMult(), originalSprite.getAlphaMult()));
                            sprite.setColor(originalSprite.getColor());
                        } else {
                            sprite = originalSprite;
                        }

                        sprite.renderAtCenter(weaponLocation.x, weaponLocation.y);
                    }

                    if (weapon.getBarrelSpriteAPI() != null && weapon.isRenderBarrelBelow()) {
                        if (weapon.getSlot().isHardpoint()) {
                            if (weapon.getAnimation() != null) {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.HARDPOINT_BARREL, weapon.getAnimation().getFrame());
                            } else {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.HARDPOINT_BARREL, 0);
                            }
                        } else {
                            if (weapon.getAnimation() != null) {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.TURRET_BARREL, weapon.getAnimation().getFrame());
                            } else {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.TURRET_BARREL, 0);
                            }
                        }
                        originalSprite = weapon.getBarrelSpriteAPI();
                        if (entry != null) {
                            sprite = entry.sprite;
                            sprite.setSize(originalSprite.getWidth(), originalSprite.getHeight());
                            sprite.setCenter(originalSprite.getCenterX(), originalSprite.getCenterY());
                            sprite.setColor(originalSprite.getColor());
                        } else {
                            sprite = originalSprite;
                        }

                        weapon.renderBarrel(sprite, weaponLocation, Math.min(ship.getCombinedAlphaMult(), originalSprite.getAlphaMult()));
                    }

                    if (weapon.getSprite() != null) {
                        if (weapon.getSlot().isHardpoint()) {
                            if (weapon.getAnimation() != null) {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.HARDPOINT, weapon.getAnimation().getFrame());
                            } else {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.HARDPOINT, 0);
                            }
                        } else {
                            if (weapon.getAnimation() != null) {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.TURRET, weapon.getAnimation().getFrame());
                            } else {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.TURRET, 0);
                            }
                        }
                        originalSprite = weapon.getSprite();
                        if (entry != null) {
                            sprite = entry.sprite;
                            sprite.setAngle(originalSprite.getAngle());
                            sprite.setSize(originalSprite.getWidth(), originalSprite.getHeight());
                            sprite.setCenter(originalSprite.getCenterX(), originalSprite.getCenterY());
                            sprite.setAlphaMult(Math.min(ship.getCombinedAlphaMult(), originalSprite.getAlphaMult()));
                            sprite.setColor(originalSprite.getColor());
                        } else {
                            sprite = originalSprite;
                        }

                        sprite.renderAtCenter(weaponLocation.x, weaponLocation.y);
                    }

                    if (weapon.getBarrelSpriteAPI() != null && !weapon.isRenderBarrelBelow()) {
                        if (weapon.getSlot().isHardpoint()) {
                            if (weapon.getAnimation() != null) {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.HARDPOINT_BARREL, weapon.getAnimation().getFrame());
                            } else {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.HARDPOINT_BARREL, 0);
                            }
                        } else {
                            if (weapon.getAnimation() != null) {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.TURRET_BARREL, weapon.getAnimation().getFrame());
                            } else {
                                entry = TextureData.getTextureData(weapon.getId(), TextureDataType.MATERIAL_MAP, ObjectType.TURRET_BARREL, 0);
                            }
                        }
                        originalSprite = weapon.getBarrelSpriteAPI();
                        if (entry != null) {
                            sprite = entry.sprite;
                            sprite.setSize(originalSprite.getWidth(), originalSprite.getHeight());
                            sprite.setCenter(originalSprite.getCenterX(), originalSprite.getCenterY());
                            sprite.setColor(originalSprite.getColor());
                        } else {
                            sprite = originalSprite;
                        }

                        weapon.renderBarrel(sprite, weaponLocation, Math.min(ship.getCombinedAlphaMult(), originalSprite.getAlphaMult()));
                    }

                    if (weapon.getMissileRenderData() != null && !weapon.getMissileRenderData().isEmpty() && (!weapon.usesAmmo() || weapon.getAmmo() > 0)) {
                        final List<MissileRenderDataAPI> msls = weapon.getMissileRenderData();
                        final int mslSize = msls.size();
                        for (int k = 0; k < mslSize; k++) {
                            final MissileRenderDataAPI msl = msls.get(k);
                            if (msl.getMissileSpecId() == null) {
                                continue;
                            }

                            final Vector2f missileLocation = msl.getMissileCenterLocation();

                            entry = TextureData.getTextureData(msl.getMissileSpecId(), TextureDataType.MATERIAL_MAP, ObjectType.MISSILE, 0);
                            originalSprite = msl.getSprite();
                            if (entry != null) {
                                sprite = entry.sprite;
                                sprite.setAngle(msl.getMissileFacing() - 90f);
                                sprite.setSize(originalSprite.getWidth(), originalSprite.getHeight());
                                sprite.setCenter(originalSprite.getCenterX(), originalSprite.getCenterY());
                                sprite.setAlphaMult(Math.min(ship.getCombinedAlphaMult(), originalSprite.getAlphaMult()) * msl.getBrightness());
                                sprite.setColor(originalSprite.getColor());
                            } else {
                                sprite = originalSprite;
                            }

                            sprite.renderAtCenter(missileLocation.x + renderOffset.x, missileLocation.y + renderOffset.y);
                        }
                    }
                }
            }

            objectCount++;
        }

        final List<MissileAPI> missiles = Global.getCombatEngine().getMissiles();
        size = missiles.size();
        for (int i = 0; i < size; i++) {
            final MissileAPI missile = missiles.get(i);
            if (missile.getCustomData().containsKey(LightShader.DO_NOT_RENDER)) {
                continue;
            }

            final Vector2f shipLocation = missile.getLocation();
            if (!isOnScreen(shipLocation, 1.25f * missile.getCollisionRadius())) {
                continue;
            }

            if (missile.getProjectileSpecId() == null) {
                continue;
            }

            final TextureEntry entry = TextureData.getTextureData(missile.getProjectileSpecId(),
                    TextureDataType.MATERIAL_MAP, ObjectType.MISSILE, 0);
            final SpriteAPI originalSprite = missile.getSpriteAPI();
            final SpriteAPI sprite;
            if (entry != null) {
                sprite = entry.sprite;
                sprite.setAngle(originalSprite.getAngle());
                sprite.setSize(originalSprite.getWidth(), originalSprite.getHeight());
                sprite.setCenter(originalSprite.getCenterX(), originalSprite.getCenterY());
                sprite.setAlphaMult(originalSprite.getAlphaMult());
                sprite.setColor(originalSprite.getColor());
            } else {
                sprite = originalSprite;
            }

            sprite.renderAtCenter(shipLocation.x, shipLocation.y);

            objectCount++;
        }

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        if (useFramebufferCore) {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        } else if (useFramebufferARB) {
            ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, 0);
        } else {
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
        }
        GL11.glPopAttrib();

        GL11.glViewport(0, 0, (int) (Global.getSettings().getScreenWidthPixels() * Display.getPixelScaleFactor()),
                (int) (Global.getSettings().getScreenHeightPixels() * Display.getPixelScaleFactor()));

        isForegroundEmpty = objectCount <= 0;
        isForegroundRendered = true;
    }

    // Also sorts the list
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    static List<ShaderAPI> getShaderAPIs() {
        Collections.sort(shaders, LOAD_ORDER);
        return shaders;
    }

    static void unsetForegroundRendered() {
        isForegroundRendered = false;
        isForegroundEmpty = true;
    }
}
