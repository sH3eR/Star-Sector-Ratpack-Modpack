Better Combat v.0.3.8
This mod is safe to add and remove from saves.
Most modifications of this mod can be turned off in the settings file at data/config/bcom/bcomSettings.json or via lunalib
exceptions are
1.vanila ship systems that slow you down at the end of a speed boost no longer do.

How LunaLib integration works: Nothing changes if you are using the settings file. If you want to use LunaLib, you need to enable using those settings in the LunaLib settings for Better Combat.

Compatable with secrets of the frontier.

One more thing. With arbitrary settings feature, settings.json values can be overwriden from better combat. This was done to give better combat presets access to vanilla combat balance tools.

change the settings in data/config/bcomSettings.json

Thanks to Liral for the map plugin.
Thanks to Crablobab for the asteroid resize code.
Thanks to Maelstrom & Ciruno for suggesting and testing features.

changelog:
	0.1.3:
	public release
	0.1.4:
	fixed dev mode f8
	made mod a utility mod
	0.1.5:
	added range threshold feature.
	added asteroid splitting feature.
	added weapon range threshold modification(for SO) feature.
	added weapon range addition.
	added projectile speed feature.
	added recoil feature.
	added missile speed feature.
	added instant beams feature.
	fixed issue with projectile coast enable effecting unintended features.
	fixed issue with weapon range incorrectly calculating range of projectiles.
	0.2.0:
	implemented integration with lunalib
	updated settings
	changed asteroid HP to be more regular
	added asteroidHpExponentFactor
	0.3.0
	added sightFeature
	map feature toggleable
	added deployment feature
	bug fixes with bounds feature, missile speed
	secrets of the frontier integration
	added CR reduction on damage taken feature
	added autofire accuracy feature
	added acceleration addition
	0.3.1
	added submunition feature
	added max zoom feature
	added arbitrary settings feature, which allows you to override settings in settings.json
	fixed deployment bug where ships from mods that deploy other ships were being moved to the deployment zone 
	changed location of the settings file to be more convenient
	changed several setting defaults
	0.3.2
	fixed crash wile loading the game
	0.3.3
	added range exponent
	fixed arbitrary settings toggle logic
	added ship defense feature
	added ship shield and phase feature
	added ship flux feature
	rearranged the settings to make more sense
	modified settings
	0.3.4
	added opFeature
	added beamDamageDropoffFeature
	added phaseConstantsFeature
	added shipExplosionFeature
	0.3.5
	added mapSizeFactorNoObjectives
	added fighterTargetSpeed
	added shipTargetSpeed
	moved useAdditionalAsteroidSprites to be a setting instead of hard coded
	added retreatAtLowCrFeature
	added shipFragmentSpeedFeature
	0.3.6
	added weaponDamageFeature
	added pptFeature
	added customValuesFeature, allowing ships and weapons to be modified using a query
	added weaponDamageMult
	added turn acceleration and rate to shipAccelerationFeature
	changed cr damage feature to respect PPT. Added a PptPerHullAmount value
	0.3.7
	modified default settings
	fixed bug where CR per damage did not work
	added noFluxBurnSystemsFeature, replacing a built in feature with one that is configurable. Now there is no difference between a vanilla game and one running better combat with all the settings off
	fixed bug where asteroids removed by ships deploying on top of them were creating fragments
	added afterDeploymentFeature with slowdownAfterDeployment and deploymentImmunity
	0.3.8
	fixed issue where asteriods would explode at the start of combat under some conditions
	0.3.9
	modified logic for spawn protection. Hopefully fixing issues where sometimes ships will become invunerable.
	added ship break feature, which modifies the number of pieces a ship breaks into
	0.3.10
	added melee weapons from some other mods to weapon exclusions.
