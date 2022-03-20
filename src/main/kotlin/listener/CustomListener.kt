package listener

import bot.CommandCenterBot
import bot.ScvBot
import bwapi.*
import bwapi.Unit as Unit
import draw.DrawVisible
import tile.CustomAnalyseTile

class CustomListener(
    private val drawVisible: DrawVisible,
    private val commandCenterBot: CommandCenterBot,
    private val scvBot: ScvBot
): DefaultBWListener() {
    private val bwClient = BWClient(this)
    private lateinit var game: Game
    private lateinit var customAnalyseTile: CustomAnalyseTile
    private var buildingScv: Unit? = null

    fun start() {
        bwClient.startGame()
    }

    override fun onStart() {
        game = bwClient.game
        game.setLocalSpeed(35)//게임 속도 30이 기본, 토너먼트에선 20
        customAnalyseTile = CustomAnalyseTile(game)
    }

    override fun onFrame() {
        val self: Player = game.self()
        game.drawTextScreen(10, 10, "Playing as ${self.name} - ${self.race}")
        game.drawTextScreen(10, 20, "supplyTotal : ${self.supplyTotal()}, supplyUsed : ${self.supplyUsed()}")

        game.allUnits
            .filter { it.isCompleted }
            .forEach { myUnit ->
                drawVisible.worker(myUnit, game)
                drawVisible.supplyDepot(myUnit, game)
                commandCenterBot.makeScv(myUnit, self)
                scvBot.gatherMineral(myUnit, game)
                if (buildingScv == null) {
                    buildingScv = scvBot.selectBuildSupplyDepotScv(myUnit, self)
                } else {
                    scvBot.buildSupplyDepot(buildingScv!!, game)
                }
        }
    }


    override fun onUnitComplete(unit: Unit?) {
        unit?.let {
            if (it.type == UnitType.Terran_Supply_Depot && it.isCompleted) {
                buildingScv = null
            }
        }

    }
}
