import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Image
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer

class GamePanel : JPanel(), ActionListener {
    private val playerImage: Image
    private val ballImage: Image
    private val playerSizeFactor = 1 / 2.0
    private val ballSizeFactor = 1 / 5.0
    private var playerX = 200
    private var playerY = 600
    private var ballX = 200
    private var ballY = 0
    private var ballSpeed = 5
    private var score = 0
    private var strikes = 0
    private var foulCount = 0 // ファールカウント
    private var isGameOver = false

    private val timer: Timer

    init {
        playerImage = loadImage("asset/player.png")
        ballImage = loadImage("asset/ball.png")

        preferredSize = Dimension(400, 800)
        background = Color.BLACK

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (isGameOver) return
                swingBat()
                repaint()
            }
        })

        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                if (isGameOver) return
                playerX = e.x - (playerImage.getWidth(this@GamePanel) * playerSizeFactor).toInt() / 2
                repaint()
            }
        })

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_F5) {
                    if (isGameOver) {
                        resetGame()
                        repaint()
                    }
                }
            }
        })

        timer = Timer(16, this)
        timer.start()
        isFocusable = true
        requestFocusInWindow()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        // Draw the player with the resized image
        val playerWidth = (playerImage.getWidth(this) * playerSizeFactor).toInt()
        val playerHeight = (playerImage.getHeight(this) * playerSizeFactor).toInt()
        g.drawImage(playerImage, playerX, playerY, playerWidth, playerHeight, this)

        // Draw the ball with the resized image
        val ballWidth = (ballImage.getWidth(this) * ballSizeFactor).toInt()
        val ballHeight = (ballImage.getHeight(this) * ballSizeFactor).toInt()
        g.drawImage(ballImage, ballX, ballY, ballWidth, ballHeight, this)

        // Draw the hit zone as a light blue rectangle
        g.color = Color.CYAN
        g.drawRect(playerX, playerY, playerWidth, playerHeight)

        // Draw text information
        g.color = Color.WHITE
        g.drawString("Score: $score", 10, 20)
        g.drawString("Strikes: $strikes", 10, 40)
        g.drawString("Fouls: $foulCount", 10, 60)

        if (isGameOver) {
            g.color = Color.RED
            g.drawString("Game Over", width / 2 - 50, height / 2)
            g.drawString("Press F5 to Restart", width / 2 - 80, height / 2 + 20)
        }
    }

    override fun actionPerformed(e: ActionEvent) {
        if (isGameOver) return
        ballY += ballSpeed
        if (ballY > height) {
            if (!isBallInHittingZone()) {
                foulCount++
                if (foulCount >= 13) {
                    gameOver()
                }
            }
            resetBall()
        }
        repaint()
    }

    private fun swingBat() {
        if (isBallInHittingZone()) {
            // 50%の確率でファールにする
            if (Math.random() < 0.5) {
                println("ファール！")
                foulCount++
                if (foulCount >= 13) {
                    gameOver()
                }
            } else {
                println("ホームラン！")
                score += 1
                resetBall()
            }
        } else {
            println("ファール！")
            foulCount++
            if (foulCount >= 13) {
                gameOver()
            }
        }
    }

    private fun isBallInHittingZone(): Boolean {
        val playerWidth = (playerImage.getWidth(this) * playerSizeFactor).toInt()
        val playerHeight = (playerImage.getHeight(this) * playerSizeFactor).toInt()
        val batX = playerX + playerWidth
        val batY = playerY
        val ballWidth = (ballImage.getWidth(this) * ballSizeFactor).toInt()
        val ballHeight = (ballImage.getHeight(this) * ballSizeFactor).toInt()

        return ballX in (playerX..batX) && ballY in (batY..(batY + playerHeight)) &&
                (ballX + ballWidth) in (playerX..batX) && (ballY + ballHeight) in (batY..(batY + playerHeight))
    }

    private fun resetBall() {
        ballX = (Math.random() * (width - (ballImage.getWidth(this) * ballSizeFactor).toInt())).toInt()
        ballY = 0
        ballSpeed = (5..15).random()
    }

    private fun gameOver() {
        isGameOver = true
    }

    private fun resetGame() {
        playerX = 200
        playerY = 600
        ballX = 200
        ballY = 0
        ballSpeed = 5
        score = 0
        strikes = 0
        foulCount = 0 // ファールカウントをリセット
        isGameOver = false
        requestFocusInWindow() // Re-focus to ensure key events are captured
    }

    private fun loadImage(path: String): Image {
        val url = javaClass.getResource(path)
            ?: throw IllegalArgumentException("画像ファイルが見つかりません: $path")
        return ImageIcon(url).image
    }
}

fun main() {
    val frame = JFrame("野獣先輩のホームランダービー")
    val gamePanel = GamePanel()
    frame.add(gamePanel)
    frame.pack()
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
}
