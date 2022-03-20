package listener

import bot.BarracksBot
import bot.CommandCenterBot
import bot.ScvBot
import bwapi.*
import bwapi.Unit as Unit
import draw.DrawVisible

class CustomListener(
    private val drawVisible: DrawVisible,
    private val commandCenter: CommandCenterBot,
    private val scvBot: ScvBot,
    private val barracks: BarracksBot
): DefaultBWListener() {
    private val bwClient = BWClient(this)
    private lateinit var game: Game
    private var buildingScv: Unit? = null
    private var buildingBarracksScv: Unit? = null
    private var isTwiceReq: Boolean = false

    fun start() {
        bwClient.startGame()
    }

    override fun onStart() {
        game = bwClient.game
        game.setLocalSpeed(35)//게임 속도 30이 기본, 토너먼트에선 20
    }

    override fun onFrame() {
        if (isTwiceReq) {
            isTwiceReq = false
            return
        } else {
            isTwiceReq = true
        }
        val self: Player = game.self()
        game.drawTextScreen(10, 10, "Playing as ${self.name} - ${self.race}")
        game.drawTextScreen(10, 20, "supplyTotal : ${self.supplyTotal()}, supplyUsed : ${self.supplyUsed()}")

        game.allUnits
            .filter { it.isCompleted && it.lastCommandFrame < 3 }
            .forEach { myUnit ->
                println("lastCommandFrame : ${myUnit.lastCommandFrame}")
                drawVisible.worker(myUnit, game)
                drawVisible.supplyDepot(myUnit, game)
                commandCenter.train(myUnit, self, game)
                scvBot.gatherMineral(myUnit, game)
                if (buildingScv == null) {
                    buildingScv = scvBot.selectBuildSupplyDepotScv(myUnit, self)
                } else {
                    scvBot.build(buildingBarracksScv!!, UnitType.Terran_Supply_Depot, game)
                }
                if (buildingBarracksScv == null) {
                    buildingBarracksScv = scvBot.selectBuildBarracksScv(myUnit, self)
                } else {
                    scvBot.build(buildingBarracksScv!!, UnitType.Terran_Barracks, game)
                }
                barracks.train(myUnit, UnitType.Terran_Marine, self, game)
        }
    }


    override fun onUnitComplete(unit: Unit?) {
        unit?.let {
            if (it.type == UnitType.Terran_Supply_Depot && it.isCompleted) {
                buildingScv = null
            }
            if (it.type == UnitType.Terran_Barracks && it.isCompleted) {
                buildingBarracksScv = null
            }
        }

    }
}
