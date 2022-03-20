import bot.CommandCenterBot
import bot.ScvBot
import draw.DrawVisible
import listener.CustomListener

fun main() {
    CustomListener(
        DrawVisible(),
        CommandCenterBot(),
        ScvBot()
    ).start()
}
