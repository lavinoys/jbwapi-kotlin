import bot.BarracksBot
import bot.CommandCenterBot
import bot.MarinBot
import bot.ScvBot
import draw.DrawVisible
import listener.CustomListener

fun main() {
    CustomListener(
        DrawVisible(),
        CommandCenterBot(),
        ScvBot(),
        BarracksBot(),
        MarinBot()
    ).start()
}
