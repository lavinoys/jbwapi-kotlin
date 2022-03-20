package tile

import bwapi.*
import bwapi.Unit
import kotlin.math.abs

class CustomAnalyseTile(
    private val game: Game
) {
    // Returns a suitable TilePosition to build a given building type near
    // specified TilePosition aroundTile, or null if not found. (builder parameter is our worker)
    fun getBuildTile(builder: Unit, buildingType: UnitType, aroundTile: TilePosition): TilePosition? {
        var maxDist = 6
        val stopDist = 40

        // Refinery, Assimilator, Extractor
        if (buildingType.isRefinery) {
            game.neutralUnits
                .filter { it.type == UnitType.Resource_Vespene_Geyser }
                .filter { abs(it.tilePosition.getX() - aroundTile.getX()) < stopDist
                        && abs(it.tilePosition.getY() - aroundTile.getY()) < stopDist}
                .forEach { neutral ->
                    return neutral.tilePosition
                }
        }
        while (maxDist < stopDist) {
            for (i in aroundTile.getX() - maxDist..aroundTile.getX() + maxDist) {
                for (j in aroundTile.getY() - maxDist..aroundTile.getY() + maxDist) {
                    if (game.canBuildHere(TilePosition(i, j), buildingType, builder, false)) {
                        // units that are blocking the tile
                        var unitsInWay = false
                        game.allUnits.filter { it.id == builder.id }.forEach {
                            if (abs(it.tilePosition.getX() - i) < 4
                                && abs(it.tilePosition.getY() - j) < 4) {
                                unitsInWay = true
                            }
                        }
                        if (!unitsInWay) {
                            return TilePosition(i, j)
                        }
                        // creep for Zerg
                        if (buildingType.requiresCreep()) {
                            var creepMissing = false
                            for (k in i..i + buildingType.tileWidth()) {
                                for (l in j..j + buildingType.tileHeight()) {
                                    if (!game.hasCreep(k, l)) creepMissing = true
                                    break
                                }
                            }
                            if (creepMissing) continue
                        }
                    }
                }
            }
            maxDist += 2
        }
        game.printf("Unable to find suitable build position for $buildingType")
        return null
    }
}
