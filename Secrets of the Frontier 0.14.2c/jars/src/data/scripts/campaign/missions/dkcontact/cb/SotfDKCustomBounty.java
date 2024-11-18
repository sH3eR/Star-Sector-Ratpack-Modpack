package data.scripts.campaign.missions.dkcontact.cb;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.missions.cb.*;

import java.util.ArrayList;
import java.util.List;

/**
 *	DUSTKEEPER FLEET BOUNTIES (by them, and usually not of them)
 */

public class SotfDKCustomBounty extends BaseCustomBounty {

	public static List<CustomBountyCreator> CREATORS = new ArrayList<CustomBountyCreator>();
	static {
		CREATORS.add(new SotfDKCBPirate());
		CREATORS.add(new SotfDKCBPather());
		CREATORS.add(new SotfDKCBBurnout());
		CREATORS.add(new SotfDKCBDerelict());
		CREATORS.add(new SotfDKCBDeserter());
		CREATORS.add(new SotfDKCBRemnant());
		CREATORS.add(new SotfDKCBRemnantPlus());
		CREATORS.add(new SotfDKCBProjectSiren());
	}
	
	@Override
	public List<CustomBountyCreator> getCreators() {
		return CREATORS;
	}

	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		return super.create(createdAt, barEvent);
	}
	
}











