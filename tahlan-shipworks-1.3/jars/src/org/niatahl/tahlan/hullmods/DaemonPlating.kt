package org.niatahl.tahlan.hullmods

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.DefenseUtils
import org.niatahl.tahlan.hullmods.DaemonArmor.Companion.DAMAGE_CAP
import org.niatahl.tahlan.hullmods.DaemonArmor.Companion.DAMAGE_CAP_REDUCTION
import org.niatahl.tahlan.hullmods.DaemonArmor.DaemonArmorListener
import org.niatahl.tahlan.utils.Utils
import org.niatahl.tahlan.utils.Utils.txt
import kotlin.math.roundToInt

class DaemonPlating : BaseHullMod() {
    override fun isApplicableToShip(ship: ShipAPI): Boolean {
        return !ship.variant.hasHullMod("tahlan_daemonarmor") && !ship.variant.hasHullMod("tahlan_heavyconduits")
    }

    override fun getCanNotBeInstalledNowReason(ship: ShipAPI, marketOrNull: MarketAPI, mode: CoreUITradeMode): String? {
        if (ship.variant.hasHullMod("tahlan_daemonarmor")) return "Already equipped with Hel Carapace"
        return if (ship.variant.hasHullMod("tahlan_heavyconduits")) "Incompatible with LosTech" else null
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String) {
        ship.addListener(DaemonArmorListener())
        ship.setNextHitHullDamageThresholdMult(DAMAGE_CAP, DAMAGE_CAP_REDUCTION)
    }

    override fun advanceInCombat(ship: ShipAPI, amount: Float) {
        if (!DefenseUtils.hasArmorDamage(ship)) {
            return
        }
        if (ship.isHulk) return
        if (ship.fluxTracker.isVenting || ship.isPhased) return
        ship.mutableStats.dynamic.getStat("tahlan_daemonarmor").modifyFlat("nuller", -1f)
        val timer = ship.mutableStats.dynamic.getStat("tahlan_daemonarmor").modifiedValue + amount
        ship.mutableStats.dynamic.getStat("tahlan_daemonarmor").modifyFlat("tracker", timer)
        if (timer < DISRUPTION_TIME) return
        val armorGrid = ship.armorGrid
        val grid = armorGrid.grid
        val max = armorGrid.maxArmorInCell
        val statusMult = if (ship.fluxTracker.isOverloaded) 0.5f else 1f
        var regenPercent = REGEN_PER_SEC_PERCENT
        if (isSMod(ship)) {
            regenPercent = REGEN_PER_SEC_PERCENT_SMOD
        }
        val baseCell = armorGrid.maxArmorInCell * ship.hullSpec.armorRating.coerceAtMost(ARMOR_CAP) / armorGrid.armorRating
        val repairAmount = baseCell * (regenPercent / 100f) * statusMult * amount

        // Iterate through all armor cells and find any that aren't at max
        for (x in grid.indices) {
            for (y in grid[0].indices) {
                if (grid[x][y] < max) {
                    val regen = (grid[x][y] + repairAmount).coerceAtMost(max)
                    armorGrid.setArmorValue(x, y, regen)
                }
            }
        }
        ship.syncWithArmorGridState()
    }

    override fun getDescriptionParam(index: Int, hullSize: HullSize, ship: ShipAPI?): String? {

        return when (index) {
            0 -> "${REGEN_PER_SEC_PERCENT.roundToInt()}${txt("%")}"
            1 -> "${(ARMOR_CAP / 100 * REGEN_PER_SEC_PERCENT).roundToInt()}/s"
            2 -> "${CALC_FLAT.roundToInt()}"
            3 -> "${DAMAGE_CAP.roundToInt()}"
            4 -> "${((1f - DAMAGE_CAP_REDUCTION) * 100f).roundToInt()}${txt("%")}"
            5 -> txt("halved")
            6 -> txt("disabled")
            7 -> "${DISRUPTION_TIME.roundToInt()} ${txt("seconds")}"
            8 -> "${((1f - ARMOR_MULT) * 100f).roundToInt()}${txt("%")}"
            9 -> txt("heavyarmor")
            else -> null
        }
    }

    override fun getSModDescriptionParam(index: Int, hullSize: HullSize?): String? {
        return when (index) {
            0 -> "${((1f - ARMOR_MULT_SMOD) * 100f).roundToInt()}${txt("%")}"
            1 -> "${REGEN_PER_SEC_PERCENT_SMOD.roundToInt()}${txt("%")}"
            else -> null
        }
    }

    override fun applyEffectsBeforeShipCreation(hullSize: HullSize, stats: MutableShipStatsAPI, id: String) {
        if (isSMod(stats)) {
            stats.armorBonus.modifyMult(id, ARMOR_MULT_SMOD)
        } else {
            stats.armorBonus.modifyMult(id, ARMOR_MULT)
        }
        stats.effectiveArmorBonus.modifyFlat(id, CALC_FLAT)
    }

    companion object {
        private const val ARMOR_MULT = 0.3967486f
        private const val ARMOR_MULT_SMOD = 0.4981574f
        private const val CALC_FLAT = 200f
        private const val ARMOR_CAP = 2000f
        private const val REGEN_PER_SEC_PERCENT = 6f
        private const val REGEN_PER_SEC_PERCENT_SMOD = 3f
        private const val DISRUPTION_TIME = 2f
    }
}