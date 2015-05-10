package com.indysoft.amazingtetris;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class GameActivity extends Activity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    final int NUM_ROWS = 26;
    final int NUM_COLUMNS = 16;
    final int BOARD_HEIGHT = 800;
    final int BOARD_WIDTH = 400;
    final Handler handler = new Handler();
    final Shape[] shapes = new Shape[7];
    final int UP_DIRECTION = 0;
    final int RIGHT_DIRECTION = 1;
    final int DOWN_DIRECTION = 2;
    final int LEFT_DIRECTION = 3;
    int score;
    boolean gameInProgress, gamePaused;

    final int dx[] = {-1, 0, 1, 0};
    final int dy[] = {0, 1, 0, -1};

    private GestureDetectorCompat gestureDetector;

    Random random = new Random();

    BoardCell[][] gameMatrix;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    LinearLayout linearLayout;

    Shape currentShape;

    Timer timer;
    TimerTask timerTask;
    int fastSpeedState;

    private void ShapesInit() {
        int[][] a = new int[5][5];

        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < 5; ++j) {
                a[i][j] = 0;
            }
        }

        // L
        a[1][2] = a[1][3] = a[2][3] = a[3][3] = 1;
        shapes[0] = new Shape(a, Color.rgb(255, 165, 0));
        a[1][2] = a[1][3] = a[2][3] = a[3][3] = 0;

        // Z
        a[2][1] = a[2][2] = a[3][2] = a[3][3] = 1;
        shapes[1] = new Shape(a, Color.RED);
        a[2][1] = a[2][2] = a[3][2] = a[3][3] = 0;

        // I
        a[1][2] = a[2][2] = a[3][2] = a[4][2] = 1;
        shapes[2] = new Shape(a, Color.CYAN);
        a[1][2] = a[2][2] = a[3][2] = a[4][2] = 0;

        // O
        a[2][2] = a[2][3] = a[3][2] = a[3][3] = 1;
        shapes[3] = new Shape(a, Color.YELLOW);
        a[2][2] = a[2][3] = a[3][2] = a[3][3] = 0;

        // T
        a[1][2] = a[2][2] = a[2][3] = a[3][2] = 1;
        shapes[4] = new Shape(a, Color.rgb(139, 0, 139));
        a[1][2] = a[2][2] = a[2][3] = a[3][2] = 0;

        // S
        a[1][2] = a[2][2] = a[2][3] = a[3][3] = 1;
        shapes[5] = new Shape(a, Color.rgb(0, 255, 0));
        a[1][2] = a[2][2] = a[2][3] = a[3][3] = 0;

        // J
        a[1][3] = a[2][3] = a[3][2] = a[3][3] = 1;
        shapes[6] = new Shape(a, Color.BLUE);
        a[1][3] = a[2][3] = a[3][2] = a[3][3] = 0;

    }

    private void CopyMatrix(BoardCell[][] A, BoardCell[][] B) {
        for (int i = 0; i < NUM_ROWS; ++i) {
            for (int j = 0; j < NUM_COLUMNS; ++j) {
                B[i][j] = new BoardCell(A[i][j].getState(), A[i][j].getColor());
            }
        }
    }

    private boolean MoveShape(final int direction, Shape nowShape) {
        // copy the gameMatrix in aux
        BoardCell[][] aux = new BoardCell[NUM_ROWS][];
        for (int i = 0; i < NUM_ROWS; ++i)
            aux[i] = new BoardCell[NUM_COLUMNS];
        CopyMatrix(gameMatrix, aux);
        int i, ii, j, jj;
        // eliminate the shape from the table
        for (ii = nowShape.x, i = 1; i <= 4; ++i, ++ii) {
            for (jj = nowShape.y, j = 1; j <= 4; ++j, ++jj) {
                if (nowShape.mat[i][j].getState() == 1) {
                    gameMatrix[ii][jj] = new BoardCell();
                }
            }
        }

        // try to move the shape to the specified direction
        for (ii = nowShape.x + dx[direction], i = 1; i <= 4; ++i, ++ii) {
            for (jj = nowShape.y + dy[direction], j = 1; j <= 4; ++j, ++jj) {
                gameMatrix[ii][jj].setState(gameMatrix[ii][jj].getState() + nowShape.mat[i][j].getState());
                if (nowShape.mat[i][j].getState() == 1) {
                    gameMatrix[ii][jj].setColor(nowShape.mat[i][j].getColor());
                }
                if (gameMatrix[ii][jj].getState() > 1) {
                    CopyMatrix(aux, gameMatrix);
                    return false;
                }
            }
        }
        nowShape.x += dx[direction];
        nowShape.y += dy[direction];
        return true;
    }

    private boolean RotateLeft(Shape nowShape) {
        // copy the gameMatrix in aux
        BoardCell[][] aux = new BoardCell[NUM_ROWS][];
        for (int i = 0; i < NUM_ROWS; ++i)
            aux[i] = new BoardCell[NUM_COLUMNS];
        CopyMatrix(gameMatrix, aux);
        int i, ii, j, jj;
        // eliminate the shape from the gameMatrix
        for (ii = nowShape.x, i = 1; i <= 4; ++i, ++ii) {
            for (jj = nowShape.y, j = 1; j <= 4; ++j, ++jj) {
                if (nowShape.mat[i][j].getState() == 1) {
                    gameMatrix[ii][jj] = new BoardCell();
                }
            }
        }
        // rotate the shape to left
        nowShape.RotateLeft();
        // ... and try to put it again on the table
        for (ii = nowShape.x, i = 1; i <= 4; ++i, ++ii) {
            for (jj = nowShape.y, j = 1; j <= 4; ++j, ++jj) {
                gameMatrix[ii][jj].setState(gameMatrix[ii][jj].getState() + nowShape.mat[i][j].getState());
                if (nowShape.mat[i][j].getState() == 1) {
                    gameMatrix[ii][jj].setColor(nowShape.mat[i][j].getColor());
                }
                // if we can't put the rotated shape on the table
                if (gameMatrix[ii][jj].getState() > 1) {
                    // then recreate the initial state of the table
                    CopyMatrix(aux, gameMatrix);
                    // ... and rotate the shape to right, to obtain its initial state
                    nowShape.RotateRight();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean RotateRight(Shape nowShape) {
        // copy the gameMatrix in aux
        BoardCell[][] aux = new BoardCell[NUM_ROWS][];
        for (int i = 0; i < NUM_ROWS; ++i)
            aux[i] = new BoardCell[NUM_COLUMNS];
        CopyMatrix(gameMatrix, aux);
        int i, ii, j, jj;
        // eliminate the shape from the gameMatrix
        for (ii = nowShape.x, i = 1; i <= 4; ++i, ++ii) {
            for (jj = nowShape.y, j = 1; j <= 4; ++j, ++jj) {
                if (nowShape.mat[i][j].getState() == 1) {
                    gameMatrix[ii][jj] = new BoardCell();
                }
            }
        }
        // rotate the shape to right
        nowShape.RotateRight();
        // ... and try to put it again on the table
        for (ii = nowShape.x, i = 1; i <= 4; ++i, ++ii) {
            for (jj = nowShape.y, j = 1; j <= 4; ++j, ++jj) {
                gameMatrix[ii][jj].setState(gameMatrix[ii][jj].getState() + nowShape.mat[i][j].getState());
                if (nowShape.mat[i][j].getState() == 1) {
                    gameMatrix[ii][jj].setColor(nowShape.mat[i][j].getColor());
                }
                // if we can't put the rotated shape on the table
                if (gameMatrix[ii][jj].getState() > 1) {
                    // then recreate the initial state of the table
                    CopyMatrix(aux, gameMatrix);
                    // ... and rotate the shape to left, to obtain its initial state
                    nowShape.RotateLeft();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean CreateShape() {
        // generate random shape to put on the gameMatrix
        currentShape = shapes[random.nextInt(shapes.length)];
        // generate random number of rotations
        int number_of_rotations = random.nextInt(4);
        for (int i = 1; i <= number_of_rotations; ++i) {
            currentShape.RotateRight();
        }
        currentShape.x = 0;
        currentShape.y = 6;
        // put the new generated shape adjacent to the top side of the table if possible
        for (int offset = 0; offset <= 3; ++offset) {
            int i, ii, j, jj;
            boolean ok = true;
            for (ii = currentShape.x + offset, i = 1; i <= 4; ++i, ++ii) {
                for (jj = currentShape.y, j = 1; j <= 4; ++j, ++jj) {
                    gameMatrix[ii][jj].setState(gameMatrix[ii][jj].getState() + currentShape.mat[i][j].getState());
                    if (gameMatrix[ii][jj].getState() > 1) {
                        ok = false;
                    }
                }
            }
            if (ok) {
                for (i = 1, ii = currentShape.x + offset; i <= 4; ++i, ++ii) {
                    for (j = 1, jj = currentShape.y; j <= 4; ++j, ++jj) {
                        if (currentShape.mat[i][j].getState() == 1) {
                            gameMatrix[ii][jj].setColor(currentShape.mat[i][j].getColor());
                        }
                    }
                }
                currentShape.x += offset;
                return true;
            } else {
                for (ii = currentShape.x + offset, i = 1; i <= 4; ++i, ++ii) {
                    for (jj = currentShape.y, j = 1; j <= 4; ++j, ++jj) {
                        gameMatrix[ii][jj].setState(gameMatrix[ii][jj].getState() - currentShape.mat[i][j].getState());
                    }
                }
            }
        }
        return false;
    }

    private boolean Check() {
        int k = 0;
        boolean found = false;
        for (int i = NUM_ROWS - 4; i >= 3; --i) {
            boolean ok = true;
            for (int j = 3; j < NUM_COLUMNS - 3; ++j) {
                if (gameMatrix[i][j].getState() == 0) {
                    ok = false;
                }
            }
            if (ok) {
                ++k;
                found = true;
            } else {
                for (int j = 3; j < NUM_COLUMNS - 3; ++j) {
                    int state = gameMatrix[i][j].getState();
                    int color = gameMatrix[i][j].getColor();
                    gameMatrix[i + k][j] = new BoardCell(state, color);
                }
            }
        }
        for (int pas = 0; pas < k; ++pas) {
            for (int j = 3; j < NUM_COLUMNS - 3; ++j) {
                gameMatrix[3 + pas][j] = new BoardCell();
            }
        }
        // Update the score
        score += k * (k + 1) / 2;
        return found;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        bitmap = Bitmap.createBitmap(BOARD_WIDTH, BOARD_HEIGHT, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        linearLayout = (LinearLayout) findViewById(R.id.game_board);
        score = 0;

        gestureDetector = new GestureDetectorCompat(this, this);
        gestureDetector.setOnDoubleTapListener(this);

        timer = new Timer();

        ShapesInit();

        GameInit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gameInProgress) {
            gamePaused = true;
            PaintMatrix();
        }
    }

    void PaintMatrix() {

        // Paint the game board background
        paint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT, paint);

        // Paint the grid on the game board
        paint.setColor(Color.WHITE);
        for (int i = 0; i <= (NUM_ROWS - 6); ++i) {
            canvas.drawLine(0, i * (BOARD_HEIGHT / (NUM_ROWS - 6)), BOARD_WIDTH,
                    i * (BOARD_HEIGHT / (NUM_ROWS - 6)), paint);
        }
        for (int i = 0; i <= (NUM_COLUMNS - 6); ++i) {
            canvas.drawLine(i * (BOARD_WIDTH / (NUM_COLUMNS - 6)), 0,
                    i * (BOARD_WIDTH / (NUM_COLUMNS - 6)), BOARD_HEIGHT, paint);
        }

        // Paint the tetris blocks
        for (int i = 3; i < NUM_ROWS - 3; ++i) {
            for (int j = 3; j < NUM_COLUMNS - 3; ++j) {
                if (gameMatrix[i][j].getState() == 1) {
                    paint.setColor(gameMatrix[i][j].getColor());
                    canvas.drawRect((j - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)),
                            (i - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)),
                            (j + 1 - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)),
                            (i + 1 - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)),
                            paint);
                }
            }
        }

        // Paint borders to the tetris blocks
        for (int i = 3; i < NUM_ROWS - 3; ++i) {
            for (int j = 3; j < NUM_COLUMNS - 3; ++j) {
                if (gameMatrix[i][j].getState() == 1) {
                    paint.setColor(Color.BLACK);
                    canvas.drawLine((j - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)),
                            (i - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)),
                            (j - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)),
                            (i + 1 - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)),
                            paint);
                    canvas.drawLine((j - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)),
                            (i - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)),
                            (j + 1 - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)),
                            (i - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)),
                            paint);
                    canvas.drawLine((j + 1 - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)),
                            (i - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)),
                            (j + 1 - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)),
                            (i + 1 - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)),
                            paint);
                    canvas.drawLine((j - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)),
                            (i + 1 - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)),
                            (j + 1 - 3) * (BOARD_WIDTH / (NUM_COLUMNS - 6)),
                            (i + 1 - 3) * (BOARD_HEIGHT / (NUM_ROWS - 6)),
                            paint);
                }
            }
        }

        if (!gameInProgress) {
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(60);
            canvas.drawText("GAME OVER", (float) (BOARD_WIDTH / 2.0), (float) (BOARD_HEIGHT / 2.0), paint);
        } else if (gamePaused) {
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(60);
            canvas.drawText("GAME PAUSED", (float) (BOARD_WIDTH / 2.0), (float) (BOARD_HEIGHT / 2.0), paint);
        }

        // Display the current painting
        linearLayout.setBackgroundDrawable(new BitmapDrawable(bitmap));

        // Update the score textview
        TextView textView = (TextView) findViewById(R.id.game_score_textview);
        textView.setText("Score: " + score);
    }

    void TimerInit(int mFastSpeedState) {
        // fastSpeedState = 0 for normal speed
        // fastSpeedState = 2 for fast speed
        // fastSpeedState = 1 for fast speed to be changed in normal speed
        timer.cancel();
        timer = new Timer();
        fastSpeedState = mFastSpeedState;
        timerTask = new TimerTask() {
            @Override
            public void run() {

                //Perform background work here
                if (gameInProgress && !gamePaused) {
                    boolean moved = MoveShape(DOWN_DIRECTION, currentShape);
                    if (!moved) {
                        if (fastSpeedState == 2) // fast speed
                        {
                            fastSpeedState = 1; // to be changed to normal speed
                            return;
                        }
                        Check();
                        boolean created = CreateShape();
                        if (!created)
                            gameInProgress = false;
                    }
                }
                handler.post(new Runnable() {
                    public void run() {
                        //Perform GUI updation work here
                        if (gameInProgress && gamePaused)
                            return;
                        PaintMatrix();
                        if (!gameInProgress) {
                            cancel();
                            return;
                        }
                        if (fastSpeedState == 1) {
                            TimerInit(0);
                            timer.schedule(timerTask, 0, 500);
                        }
                    }
                });
            }
        };
    }

    void GameInit() {

        // Create the game board (backend)
        gameMatrix = new BoardCell[NUM_ROWS][];
        for (int i = 0; i < NUM_ROWS; ++i) {
            gameMatrix[i] = new BoardCell[NUM_COLUMNS];
            for (int j = 0; j < NUM_COLUMNS; ++j) {
                gameMatrix[i][j] = new BoardCell();
            }
        }

        // Marking the first and the last 3 lines and columns as occupied.

        for (int j = 0; j < NUM_COLUMNS; ++j) {
            for (int i = 0; i <= 2; ++i) {
                gameMatrix[i][j] = new BoardCell(1, Color.BLACK);
            }
            for (int i = NUM_ROWS - 3; i < NUM_ROWS; ++i) {
                gameMatrix[i][j] = new BoardCell(1, Color.BLACK);
            }
        }

        for (int i = 0; i < NUM_ROWS; ++i) {
            for (int j = 0; j <= 2; ++j) {
                gameMatrix[i][j] = new BoardCell(1, Color.BLACK);
            }
            for (int j = NUM_COLUMNS - 3; j < NUM_COLUMNS; ++j) {
                gameMatrix[i][j] = new BoardCell(1, Color.BLACK);
            }
        }

        // Create an initial tetris block
        CreateShape();

        // Start the game
        gameInProgress = true;
        gamePaused = false;

        // Paint the initial matrix (frontend)
        PaintMatrix();

        // Set a timer
        TimerInit(0);
        timer.schedule(timerTask, 500, 500); // after x ms, it runs every y ms
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (!gameInProgress)
            return false;
        if (gamePaused)
            gamePaused = false;
        else {
            gamePaused = true;
            PaintMatrix();
        }
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (!gameInProgress || gamePaused)
            return false;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float width = size.x;
        float x = e.getX();
        if (x <= width / 2.0) {
            // rotate left
            RotateLeft(currentShape);
            PaintMatrix();
        } else {
            // rotate right
            RotateRight(currentShape);
            PaintMatrix();
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!gameInProgress || gamePaused)
            return false;
        try {
            float x1 = e1.getX();
            float y1 = e1.getY();

            float x2 = e2.getX();
            float y2 = e2.getY();

            double angle = getAngle(x1, y1, x2, y2);

            if (inRange(angle, 45, 135)) {
                // UP
                // cancel the fast down movement
                if (fastSpeedState == 2) {
                    TimerInit(1);
                    timer.schedule(timerTask, 0, 50);
                }
            } else if (inRange(angle, 0, 45) || inRange(angle, 315, 360)) {
                // RIGHT
                // move right
                MoveShape(RIGHT_DIRECTION, currentShape);
                PaintMatrix();
            } else if (inRange(angle, 225, 315)) {
                // DOWN
                // move fast down
                TimerInit(2);
                timer.schedule(timerTask, 0, 50);
            } else {
                // LEFT
                // move left
                MoveShape(LEFT_DIRECTION, currentShape);
                PaintMatrix();
            }

        } catch (Exception e) {
            // nothing
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public double getAngle(float x1, float y1, float x2, float y2) {

        double rad = Math.atan2(y1 - y2, x2 - x1) + Math.PI;
        return (rad * 180 / Math.PI + 180) % 360;
    }

    private boolean inRange(double angle, float init, float end) {
        return (angle >= init) && (angle < end);
    }

    public class BoardCell {

        private int state, color;
        // state = 0 means free cell, state = 1 means occupied cell

        public BoardCell() {
            state = 0;
            color = Color.BLACK;
        }

        public BoardCell(int state, int color) {
            this.state = state;
            this.color = color;
        }

        public int getState() {
            return state;
        }

        public int getColor() {
            return color;
        }

        public void setState(int state) {
            this.state = state;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }

    public class Shape {
        public int x, y;
        public BoardCell[][] mat = new BoardCell[5][5];

        Shape() {
            for (int i = 0; i < 5; ++i) {
                for (int j = 0; j < 5; ++j) {
                    mat[i][j] = new BoardCell();
                }
            }
            x = y = 0;
        }

        Shape(int[][] _mat, int _color) {
            for (int i = 0; i < 5; ++i) {
                for (int j = 0; j < 5; ++j) {
                    if (_mat[i][j] == 1) {
                        mat[i][j] = new BoardCell(_mat[i][j], _color);
                    } else {
                        mat[i][j] = new BoardCell();
                    }

                }
            }
            x = y = 0;
        }

        Shape(int[][] _mat, int _color, int _x, int _y) {
            for (int i = 0; i < 5; ++i) {
                for (int j = 0; j < 5; ++j) {
                    if (_mat[i][j] == 1) {
                        mat[i][j] = new BoardCell(_mat[i][j], _color);
                    } else {
                        mat[i][j] = new BoardCell();
                    }

                }
            }
            x = _x;
            y = _y;
        }

        void RotateLeft() {
            BoardCell[][] aux = new BoardCell[5][5];
            for (int i = 1; i < 5; ++i) {
                for (int j = 1; j < 5; ++j) {
                    aux[4 - j + 1][i] = mat[i][j];
                }
            }
            for (int i = 1; i < 5; ++i) {
                for (int j = 1; j < 5; ++j) {
                    mat[i][j] = aux[i][j];
                }
            }
        }

        void RotateRight() {
            BoardCell[][] aux = new BoardCell[5][5];
            for (int i = 1; i < 5; ++i) {
                for (int j = 1; j < 5; ++j) {
                    aux[j][4 - i + 1] = mat[i][j];
                }
            }
            for (int i = 1; i < 5; ++i) {
                for (int j = 1; j < 5; ++j) {
                    mat[i][j] = aux[i][j];
                }
            }
        }
    }
}

