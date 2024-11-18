package org.niatahl.tahlan.hullmods.barcodes

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import org.niatahl.tahlan.utils.Utils.txt

class Gluttony : BaseHullMod() {
    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String) {
        val cnt = ship.variant.sMods.count()
        ship.mutableStats.apply {
            fluxDissipation.modifyMult(id, AMOUNT * cnt + 1f)
            fluxCapacity.modifyMult(id, AMOUNT * cnt + 1f)
        }
    }

    override fun advanceInCampaign(member: FleetMemberAPI, amount: Float) {
        if (member.fleetCommander == null || member.fleetData == null || member.fleetData.fleet == null) return
        if (member.fleetCommander == Global.getSector().playerPerson || member.fleetData.fleet.isPlayerFleet) {
            member.variant.removePermaMod("tahlan_gluttony")
            member.variant.removeMod("tahlan_gluttony")
        }
    }

    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? {
        return when (index) {
            0 -> "${AMOUNT * 100}${txt("%")}"
            else -> null
        }
    }

    companion object {
        const val AMOUNT = 0.03f
    }
}