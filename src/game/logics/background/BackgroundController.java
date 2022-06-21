package game.logics.background;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import game.frame.GameWindow;
import game.logics.interactions.SpeedHandler;
import game.utility.debug.Debugger;
import game.utility.other.Pair;

/**
 * This class is a {@link Background} handler.
 */
public class BackgroundController implements Background {

    /**
     * Specifies the path within the sprite folder [specified in
     * {@link game.utility.sprites.AbstractSprite AbstractSprite} class]
     * where {@link BackgroundSprite} sprites can be found.
     */
    private static final String SPRITE_PATH = "background" + System.getProperty("file.separator");

    private static final String KEY_SPRITE1 = "background1";
    private static final String KEY_SPRITE2 = "background2";

    /**
     * If sprites are missing, they will be replaced by a rectangle of the color specified here.
     *
     *   HSV: 240° 33% 14.1%
     *   RGB: 24 24 36
     */
    private static final Color PLACE_HOLDER = Color.getHSBColor((float) 0.666, (float) 0.333, (float) 0.141);

    private static final double LADDER_GENERATION = 0.15;

    private static final int SCREEN_WIDTH = GameWindow.GAME_SCREEN.getWidth();
    private static final Pair<Double, Double> START_POS = new Pair<>(0.0, 0.0);

    private final Pair<Double, Double> position = START_POS.copy();

    private final BackgroundDrawer drawMgr = new BackgroundDrawManager();
    private final Random rand = new Random();
    private final SpeedHandler movement;

    /// FLAGS ///
    private boolean visible;
    private boolean onScreen;
    private boolean onClearArea;
    private final Map<BoxPos, Boolean> boxVisible;
    private final Map<BoxPos, Optional<String>> boxSprite;

    /**
     * @param speed the {@link SpeedHandler} to use for the pickup
     */
    public BackgroundController(final SpeedHandler speed) {

        this.movement = speed;
        this.drawMgr.setPlaceH(PLACE_HOLDER);
        this.drawMgr.addSprite(KEY_SPRITE1, SPRITE_PATH + "background_1.png");
        this.drawMgr.addSprite(KEY_SPRITE2, SPRITE_PATH + "background_2.png");

        this.boxVisible = new HashMap<>(Map.of(
                BoxPos.LEFT, false,
                BoxPos.CENTRAL, true,
                BoxPos.RIGHT, false));
        this.boxSprite = new HashMap<>(3);

        Set.of(BoxPos.values()).stream()
                .forEach(key -> this.boxSprite.put(key, Optional.empty()));
        this.boxSprite.put(BoxPos.CENTRAL, Optional.of(KEY_SPRITE1));

        this.setVisibility(true);
    }

    /**
     * Allows to set the background visibility.
     * 
     * @param v <code>true</code> if has to be shown, <code>false</code> if has to be hidden
     */
    private void setVisibility(final boolean v) {
        visible = v;
    }

    private boolean isVisible() {
        return visible;
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        this.position.set(START_POS.getX(), START_POS.getY());
        this.movement.resetSpeed();
    }

    /**
     * {@inheritDoc}
     */
    public void update() {
        this.updateFlags();
        if (this.isVisible()) {
            if (this.onScreen) {
                this.boxSprite.put(BoxPos.RIGHT,
                        rand.nextDouble() > BackgroundController.LADDER_GENERATION
                        ? Optional.of(BackgroundController.KEY_SPRITE1)
                        : Optional.of(BackgroundController.KEY_SPRITE2));
            }
            if (!this.onClearArea) {
                this.shiftBox();
                this.onClearArea = true;
            }
        }
        if (this.position.getX() > -SCREEN_WIDTH * 2) {
            this.position.setX(this.position.getX() - this.movement.getXSpeed() / GameWindow.FPS_LIMIT);
        }/* else if (this.onClearArea) {
            //final double tempRight = this.leftPosition.getX();
            //this.leftPosition.setX(this.rightPosition.getX());
            //this.rightPosition.setX(tempRight + SCREEN_WIDTH);
        }*/
    }

    /**
     * Draws the background if visible.
     * 
     * @param g the graphics drawer
     */
    public void draw(final Graphics2D g) {
        if (this.isVisible()) {

            this.boxSprite.entrySet().stream()
                    .filter(box -> this.boxVisible.get(box.getKey()))
                    .forEach(box -> this.drawMgr.drawSprite(g,
                            box.getValue().orElse(BackgroundDrawer.PLACEHOLDER_KEY),
                            this.calculate(box.getKey()),
                            GameWindow.GAME_SCREEN.getHeight(),
                            GameWindow.GAME_SCREEN.getWidth()));
        }
    }

    private void shiftBox() {

        this.boxVisible.putAll(new HashMap<>(Map.of(
                BoxPos.LEFT, true,
                BoxPos.CENTRAL, true,
                BoxPos.RIGHT, false)));

        this.boxSprite.put(BoxPos.LEFT, this.boxSprite.get(BoxPos.CENTRAL));
        this.boxSprite.put(BoxPos.CENTRAL, this.boxSprite.get(BoxPos.RIGHT));

        final Pair<Double, Double> temp = this.calculate(BoxPos.RIGHT);
        this.position.set(temp.getX(), temp.getY());
    }

    private Pair<Double, Double> calculate(final BoxPos box) {
        final Pair<Double, Double> newPos;
        switch (box) {
            case LEFT:
                newPos = new Pair<>(this.position.getX() - SCREEN_WIDTH, this.position.getY());
                break;
            default:
                 //newPos = new Pair<>(this.position);
                 newPos = this.position.copy();
                 break;
            case RIGHT:
                newPos = new Pair<>(this.position.getX() + SCREEN_WIDTH, this.position.getY());
                break;
        }
        return newPos;
    }

    /**
     * {@inheritDoc}
     */
    public void drawCoordinates(final Graphics2D g) {
        final int xShift = (int) Math.round(position.getX())
                + (int) Math.round(GameWindow.GAME_SCREEN.getTileSize() * 0.88);
        final int yShiftDrawnX = (int) Math.round(position.getY())
                + GameWindow.GAME_SCREEN.getTileSize();
        final int yShiftDrawnY = yShiftDrawnX + 10;

        if (GameWindow.GAME_DEBUGGER.isFeatureEnabled(Debugger.Option.ENTITY_COORDINATES) && this.isVisible()) {
            g.setColor(Debugger.DEBUG_COLOR);
            g.setFont(Debugger.DEBUG_FONT);

            g.drawString("X:" + Math.round(position.getX()), xShift, yShiftDrawnX);
            g.drawString("Y:" + Math.round(position.getY()), xShift, yShiftDrawnY);
        }
    }

    /**
     * Updates the entity's flags.
     */
    private void updateFlags() {
      /*  if (position.getX() <= Math.abs(GameWindow.GAME_SCREEN.getWidth() - GameWindow.GAME_SCREEN.getTileSize())
                && position.getY() >= 0 && position.getY() <= GameWindow.GAME_SCREEN.getHeight()) {
      */  if (position.getX() <= -GameWindow.GAME_SCREEN.getTileSize()
                && position.getY() >= 0 && position.getY() <= GameWindow.GAME_SCREEN.getHeight()) {
            onScreen = true;
            onClearArea = false;
        } else {
         /*   if (position.getX() < -GameWindow.GAME_SCREEN.getTileSize() - GameWindow.GAME_SCREEN.getWidth()) {
                onClearArea = true;
            } else if (position.getX() >= GameWindow.GAME_SCREEN.getWidth()) {
                onClearArea = false;
            } else {
                onClearArea = false;
            }*/
            onScreen = false;
        }
    }

    /**
     * @return a string representing the type of entity with his coordinates in the environment
     */
    @Override
    public String toString() {
        return "Background"
                + "[L: X:" + Math.round(this.calculate(BoxPos.LEFT).getX())
                +  " - Y:" + Math.round(this.calculate(BoxPos.LEFT).getY()) + "]\n" + "           "
                + "[C: X:" + Math.round(position.getX())
                +  " - Y:" + Math.round(position.getY()) + "]\n" + "           "
                + "[R: X:" + Math.round(this.calculate(BoxPos.RIGHT).getX())
                +  " - Y:" + Math.round(this.calculate(BoxPos.RIGHT).getY()) + "]";
    }

    private enum BoxPos {
        LEFT, CENTRAL, RIGHT;
    }
}