import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

public class Tetris extends JFrame {

    private final static int BOARD_WIDTH = 10;
    private final static int BOARD_HEIGHT = 20;
    private final static int BLOCK_SIZE = 30;

    private Board board;

    public Tetris() {
        setTitle("Tetris");
        setSize(BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        board = new Board(BOARD_WIDTH, BOARD_HEIGHT, BLOCK_SIZE);
        add(board);
        board.start();

        setVisible(true);
    }

    public static void main(String[] args) {
        new Tetris();
    }

    private static class Board extends JPanel implements ActionListener, KeyListener {

        private final int width;
        private final int height;
        private final int blockSize;
        private Timer timer;
        private boolean[][] grid;
        private Tetromino currentPiece;
        private int currentX, currentY;

        public Board(int width, int height, int blockSize) {
            this.width = width;
            this.height = height;
            this.blockSize = blockSize;
            grid = new boolean[height][width];
            setPreferredSize(new Dimension(width * blockSize, height * blockSize));
            setBackground(Color.BLACK);
            setFocusable(true);
            addKeyListener(this);
        }

        public void start() {
            spawnPiece();
            timer = new Timer(500, this);
            timer.start();
        }

        private void spawnPiece() {
            currentPiece = Tetromino.randomPiece();
            currentX = width / 2 - 1;
            currentY = 0;
            if (!canMove(currentPiece, currentX, currentY)) {
                gameOver();
            }
        }

        private boolean canMove(Tetromino piece, int x, int y) {
            for (int row = 0; row < piece.getShape().length; row++) {
                for (int col = 0; col < piece.getShape()[row].length; col++) {
                    if (piece.getShape()[row][col]) {
                        int newX = x + col;
                        int newY = y + row;
                        if (newX < 0 || newX >= width || newY >= height || (newY >= 0 && grid[newY][newX])) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        private void placePiece() {
            for (int row = 0; row < currentPiece.getShape().length; row++) {
                for (int col = 0; col < currentPiece.getShape()[row].length; col++) {
                    if (currentPiece.getShape()[row][col]) {
                        grid[currentY + row][currentX + col] = true;
                    }
                }
            }
            clearLines();
            spawnPiece();
        }

        private void clearLines() {
            for (int row = height - 1; row >= 0; row--) {
                boolean full = true;
                for (int col = 0; col < width; col++) {
                    if (!grid[row][col]) {
                        full = false;
                        break;
                    }
                }
                if (full) {
                    for (int r = row; r > 0; r--) {
                        grid[r] = grid[r - 1].clone();
                    }
                    grid[0] = new boolean[width];
                    row++;
                }
            }
        }

        private void gameOver() {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Game Over!");
            System.exit(0);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (canMove(currentPiece, currentX, currentY + 1)) {
                currentY++;
            } else {
                placePiece();
            }
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    if (grid[row][col]) {
                        g.setColor(Color.BLUE);
                        g.fillRect(col * blockSize, row * blockSize, blockSize, blockSize);
                    }
                }
            }
            for (int row = 0; row < currentPiece.getShape().length; row++) {
                for (int col = 0; col < currentPiece.getShape()[row].length; col++) {
                    if (currentPiece.getShape()[row][col]) {
                        g.setColor(Color.RED);
                        g.fillRect((currentX + col) * blockSize, (currentY + row) * blockSize, blockSize, blockSize);
                    }
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (canMove(currentPiece, currentX - 1, currentY)) {
                        currentX--;
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (canMove(currentPiece, currentX + 1, currentY)) {
                        currentX++;
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (canMove(currentPiece, currentX, currentY + 1)) {
                        currentY++;
                    }
                    break;
                case KeyEvent.VK_UP:
                    Tetromino rotated = currentPiece.rotate();
                    if (canMove(rotated, currentX, currentY)) {
                        currentPiece = rotated;
                    }
                    break;
            }
            repaint();
        }

        @Override
        public void keyReleased(KeyEvent e) {}

        @Override
        public void keyTyped(KeyEvent e) {}
    }

    private static class Tetromino {

        private final boolean[][] shape;

        private Tetromino(boolean[][] shape) {
            this.shape = shape;
        }

        public boolean[][] getShape() {
            return shape;
        }

        public Tetromino rotate() {
            int rows = shape.length;
            int cols = shape[0].length;
            boolean[][] rotated = new boolean[cols][rows];
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    rotated[col][rows - row - 1] = shape[row][col];
                }
            }
            return new Tetromino(rotated);
        }

        public static Tetromino randomPiece() {
            Random random = new Random();
            int index = random.nextInt(7);
            switch (index) {
                case 0: return new Tetromino(new boolean[][] {
                    {true, true, true, true}
                });
                case 1: return new Tetromino(new boolean[][] {
                    {true, true},
                    {true, true}
                });
                case 2: return new Tetromino(new boolean[][] {
                    {false, true, false},
                    {true, true, true}
                });
                case 3: return new Tetromino(new boolean[][] {
                    {true, true, false},
                    {false, true, true}
                });
                case 4: return new Tetromino(new boolean[][] {
                    {false, true, true},
                    {true, true, false}
                });
                case 5: return new Tetromino(new boolean[][] {
                    {true, false, false},
                    {true, true, true}
                });
                case 6: return new Tetromino(new boolean[][] {
                    {false, false, true},
                    {true, true, true}
                });
                default: return null;
            }
        }
    }
}