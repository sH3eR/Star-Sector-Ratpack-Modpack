package assortment_of_things.campaign.scripts.render

import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.misc.levelBetween
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import org.apache.log4j.Level
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.log

class RATCampaignFlashRenderer {


    data class CampaignFlash(var location: Vector2f, var containingLocation: LocationAPI, val color: Color, var secondaryColor: Color, var minSize: Float, var additionalSize: Float,
                             var inDuration: Float, var activeDuration: Float, var outDuration: Float,
                             var maxInDuration: Float = inDuration, var maxActiveDuration: Float = activeDuration, var maxOutDuration: Float = outDuration,
                             var level: Float = 0f, var isOut: Boolean = false, var angle1: Float = MathUtils.getRandomNumberInRange(0f, 360f), var angle2: Float = MathUtils.getRandomNumberInRange(0f, 360f))

    var flashes = ArrayList<CampaignFlash>()
    @Transient var sprite1: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/fx/explosion5.png")
    @Transient var sprite2: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/fx/explosion5.png")
    @Transient var sprite3: SpriteAPI? = Global.getSettings().getAndLoadSprite("graphics/fx/explosion5.png")

    fun spawnFlash(location: Vector2f, containingLocation: LocationAPI, color: Color, secondaryColor: Color, minSize: Float, additionalSize: Float, inDuration: Float, activeDuration: Float, outDuration: Float) {

        var lensflare = CampaignFlash(location, containingLocation, color, secondaryColor, minSize, additionalSize, inDuration, activeDuration, outDuration)
        flashes.add(lensflare)
    }

    fun advance(amount: Float) {
        if (Global.getSector().isPaused) return
        for (flare in ArrayList(flashes)) {
            if (flare.inDuration >= 0f) {
                flare.inDuration -= 1 * amount
                flare.level = 1 - flare.inDuration.levelBetween(0f, flare.maxInDuration)
            }
            else if (flare.activeDuration >= 0f) {
                flare.activeDuration -= 1 * amount
                flare.level = 1f
            }
            else if (flare.outDuration >= 0f) {
                flare.outDuration -= 1 * amount
                flare.level = flare.outDuration.levelBetween(0f, flare.maxOutDuration)
                flare.isOut = true
            }
            else {
                flashes.remove(flare)
            }

            flare.angle1 += 3f * amount
            flare.angle2 += -2f * amount

            flare.level = easeInOutSine(flare.level)
        }
    }

    fun easeInOutSine(x: Float): Float {
        return (-(Math.cos(Math.PI * x) - 1) / 2).toFloat();
    }


    fun render(layer: CampaignEngineLayers, view: ViewportAPI) {


        if (sprite1 == null) {
            sprite1 = Global.getSettings().getAndLoadSprite("graphics/fx/explosion5.png")
            sprite2 = Global.getSettings().getAndLoadSprite("graphics/fx/explosion5.png")
            sprite3 = Global.getSettings().getAndLoadSprite("graphics/fx/explosion5.png")
        }

        for (flare in ArrayList(flashes)) {

            var logger = Global.getLogger(this::class.java)
            logger.level = Level.ALL

            if (flare.containingLocation != Global.getSector().playerFleet.containingLocation) continue

            var level = flare.level
            var size = flare.additionalSize
            if (!flare.isOut) {
                size *= flare.level
            }
            size += flare.minSize
            var alpha = 1f * flare.level


            sprite1!!.color = flare.secondaryColor
            sprite1!!.setAdditiveBlend()
            sprite1!!.alphaMult = alpha  * 0.1f
            sprite1!!.setSize(size * 4, size * 4)
            sprite1!!.angle = 60f
            sprite1!!.renderAtCenter(flare.location.x, flare.location.y)

            sprite1!!.color = flare.secondaryColor
            sprite1!!.setAdditiveBlend()
            sprite1!!.alphaMult = alpha  * 0.2f
            sprite1!!.setSize(size * 2, size * 2)
            sprite1!!.angle = 60f
            sprite1!!.renderAtCenter(flare.location.x, flare.location.y)

            sprite1!!.color = flare.color
            sprite1!!.setAdditiveBlend()
            sprite1!!.alphaMult = alpha  * 0.5f
            sprite1!!.setSize(size, size)
            sprite1!!.angle = 90f
            sprite1!!.renderAtCenter(flare.location.x, flare.location.y)

            sprite2!!.color = flare.color
            sprite2!!.setAdditiveBlend()
            sprite2!!.alphaMult = alpha * 0.3f
            sprite2!!.setSize(size * 0.5f, size * 0.5f)
            sprite2!!.angle = 90f + flare.angle1
            sprite2!!.renderAtCenter(flare.location.x, flare.location.y)

            sprite3!!.color = flare.color
            sprite3!!.setAdditiveBlend()
            sprite3!!.alphaMult = alpha
            sprite3!!.setSize(size * 0.1f, size * 0.1f)
            sprite3!!.angle = 40f + flare.angle2
            sprite3!!.renderAtCenter(flare.location.x, flare.location.y)
        }
    }

}