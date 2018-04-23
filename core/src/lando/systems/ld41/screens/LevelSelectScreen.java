package lando.systems.ld41.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import lando.systems.ld41.LudumDare41;
import lando.systems.ld41.gameobjects.Level;
import lando.systems.ld41.utils.Assets;
import lando.systems.ld41.utils.accessors.RectangleAccessor;

import java.util.Iterator;

public class LevelSelectScreen extends BaseScreen {
    EarClippingTriangulator triangulator;
    PolygonSpriteBatch polyBatch = new PolygonSpriteBatch();
    Array<Level> levels;
    Array<TextureRegion> thumbnails;
    int currentLevelIdx;
    int currentThumbnailIdx;

    TextureRegion arrow;
    float arrowSize = 64f;
    Rectangle arrowLeftClickTarget;
    Rectangle arrowRightClickTarget;

    NinePatch signPatch;
    float signSize;
    float padBetween = 32f;
    float currentWidth;
    Rectangle currentClickTarget;

    Rectangle leftSignPosition;
    Rectangle currentSignPosition;
    Rectangle rightSignPosition;
    Rectangle leftSign;
    Rectangle currentSign;
    Rectangle currentMap;
    Rectangle rightSign;
    Rectangle incomingSign;
    Rectangle incomingSignLeft;
    Rectangle incomingSignRight;
    int movingDirection;
    boolean isCycling = false;

    public LevelSelectScreen() {
        Gdx.input.setInputProcessor(this);
        triangulator = new EarClippingTriangulator();
        arrow = LudumDare41.game.assets.arrow;

        levels = new Array<Level>();
        thumbnails = new Array<TextureRegion>();

        Iterator<IntMap.Entry<String>> iterator = game.assets.levelNumberToFileNameMap.iterator();
        while (iterator.hasNext()) {
            IntMap.Entry<String> el = iterator.next();
            levels.add(new Level(el.value));
        }

        for (int i = 0; i < levels.size; i++) {
            thumbnails.add(getLevelThumbnail(levels.get(i)));
        }

        currentLevelIdx = 0;
        currentThumbnailIdx = 0;
        arrowLeftClickTarget = new Rectangle(10, (hudCamera.viewportHeight/2) - (arrowSize/2), arrowSize, arrowSize);
        arrowRightClickTarget = new Rectangle(hudCamera.viewportWidth - 10f - arrowSize, (hudCamera.viewportHeight/2) - (arrowSize/2), arrowSize, arrowSize);

        signPatch = LudumDare41.game.assets.backplateNinePatch;

        signSize = (hudCamera.viewportWidth - (arrowLeftClickTarget.width + 20f + (padBetween * 2))) / 4;
        float x = 10f + arrowSize + signSize + padBetween;
        float y = hudCamera.viewportHeight/2;
        currentWidth = hudCamera.viewportWidth - (x * 2);
        currentClickTarget = new Rectangle(x, (hudCamera.viewportHeight/2) - currentWidth, currentWidth, currentWidth);
        currentMap = new Rectangle(currentClickTarget.x, currentClickTarget.y, currentClickTarget.width, currentClickTarget.height);

        leftSignPosition = new Rectangle(10f + arrowSize, y - (signSize/2), signSize, signSize);
        leftSign = new Rectangle();
        leftSign.set(leftSignPosition);
        currentSignPosition = new Rectangle((hudCamera.viewportWidth/2) - (signSize/2), y, signSize, signSize);
        currentSign = new Rectangle();
        currentSign.set(currentSignPosition);
        rightSignPosition = new Rectangle(hudCamera.viewportWidth - (10f + arrowSize + signSize), y - (signSize/2), signSize, signSize);
        rightSign = new Rectangle();
        rightSign.set(rightSignPosition);
        incomingSign = new Rectangle();
        incomingSignLeft = new Rectangle(-32f - signSize, y - (signSize/2), signSize, signSize);
        incomingSignRight = new Rectangle(hudCamera.viewportWidth + 32f, y - (signSize/2), signSize, signSize);
    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        Assets.drawString(batch, "Tee Off", 10f, hudCamera.viewportHeight - 20f, Color.CORAL, 1.25f, game.assets.font);
        Assets.drawString(batch, "Select a hole", 10f, hudCamera.viewportHeight - 100f, Color.CORAL, .5f, game.assets.font);

        if (
            isCycling && (
                (movingDirection == 1 && currentLevelIdx + 2 <= levels.size - 1) ||
                    (movingDirection == -1 && currentLevelIdx - 2 >= 0)
            )) {
            // Draw incoming box
            renderSign(batch, currentLevelIdx + (movingDirection * 2), incomingSign);
        }

        if (currentLevelIdx > 0) {
            // Draw left box
            renderSign(batch, currentLevelIdx - 1, leftSign);

            // Draw left arrow (reversed)
            batch.draw(arrow, arrowLeftClickTarget.x + arrowSize, arrowLeftClickTarget.y, -arrowSize, arrowSize);
        }

        // Draw current
        renderSign(batch, currentLevelIdx, currentSign);
        batch.draw(thumbnails.get(currentThumbnailIdx), currentMap.x, currentMap.y, currentWidth, currentWidth);

        if (currentLevelIdx != levels.size - 1) {
            // Draw right box
            renderSign(batch, currentLevelIdx + 1, rightSign);

            // Draw right arrow
            batch.draw(arrow, arrowRightClickTarget.x, arrowRightClickTarget.y, arrowSize, arrowSize);
        }

        batch.end();
    }

    public void renderSign(SpriteBatch batch, int holeIdx, Rectangle rect) {
        Level level = levels.get(holeIdx);

        float top = rect.y + rect.height;

        game.assets.layout.setText(
            game.assets.font,
            "Hole: " + (holeIdx + 1) + "\n" +
                level.name + "\n" +
                "Par: " + level.par,
            Color.CORAL,
            rect.width,
            1,
            false
        );

        signPatch.draw(batch, rect.x, rect.y, rect.width, rect.height);
        game.assets.font.draw(batch, game.assets.layout, rect.x, top - 6f);
        game.assets.layout.reset();
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (isCycling) {
            return true;
        }

        Vector3 unproj = hudCamera.unproject(new Vector3(screenX, screenY, 0));
        if (button == 0) {
            if (currentLevelIdx > 0 && (arrowLeftClickTarget.contains(unproj.x, unproj.y) || leftSignPosition.contains(unproj.x, unproj.y))) {
                tweenCycle(-1);
            } else if (currentLevelIdx != levels.size - 1 && (arrowRightClickTarget.contains(unproj.x, unproj.y) || rightSignPosition.contains(unproj.x, unproj.y))) {
                tweenCycle(1);
            } else if (currentClickTarget.contains(unproj.x, unproj.y) || currentSignPosition.contains(unproj.x, unproj.y)) {
                game.setScreen(new GameScreen(currentLevelIdx), LudumDare41.game.assets.circleCropShader, 1.4f);
            }
        }

        return true;
    }

    void tweenCycle(final int dir) {
        isCycling = true;
        movingDirection = dir;
        float duration = .3f;

        Array<Rectangle> cycle = new Array<Rectangle>();
        cycle.add(
            rightSign,
            currentSign,
            leftSign
        );

        if (dir > 0) {
            incomingSign.set(incomingSignRight);
            cycle.insert(0, incomingSign);
            cycle.add(incomingSignLeft);
        } else if (dir < 0) {
            incomingSign.set(incomingSignLeft);
            cycle.insert(0, incomingSignRight);
            cycle.add(incomingSign);
            cycle.reverse();
        }

        Timeline timeline = Timeline.createParallel();

        for (int i = 0; i < cycle.size - 1; i++) {
            Rectangle t = cycle.get(i + 1);
            timeline.push(Tween.to(cycle.get(i), RectangleAccessor.XYWH, duration)
                .target(t.x, t.y, t.width, t.height)
                .ease(Quad.OUT));
        }

        timeline.setCallback(new TweenCallback() {
            @Override
            public void onEvent(int i, BaseTween<?> baseTween) {
                currentLevelIdx = currentLevelIdx + dir;
                isCycling = false;
                rightSign.set(rightSignPosition);
                leftSign.set(leftSignPosition);
                currentSign.set(currentSignPosition);
            }
        })
        .start(game.tween);

        Timeline.createSequence()
            .push(Tween.to(currentMap, RectangleAccessor.Y, duration/2)
                .target(-currentWidth)
                .ease(Quad.OUT)
                .setCallback(new TweenCallback() {
                    @Override
                    public void onEvent(int i, BaseTween<?> baseTween) {
                        currentThumbnailIdx = currentThumbnailIdx + dir;
                    }
                }))
            .push(Tween.to(currentMap, RectangleAccessor.Y, duration/2)
                .target(currentClickTarget.y)
                .ease(Quad.IN))
            .start(game.tween);
    }

    TextureRegion getLevelThumbnail(Level level) {
        float holeSize = 20f;
        int levelWidth = level.groundLayer.getWidth() * (int)level.groundLayer.getTileWidth();
        int levelHeight = level.groundLayer.getHeight() * (int)level.groundLayer.getTileHeight();
        int frameSize = Math.max(levelWidth, levelHeight);

        float xOffset = 0;
        if (frameSize > levelWidth) {
            xOffset = (frameSize/2) - (levelWidth/2);
        }

        FrameBuffer fb = new FrameBuffer(Pixmap.Format.RGBA8888, frameSize, frameSize, false);
        OrthographicCamera camera = new OrthographicCamera(frameSize, frameSize);
        camera.setToOrtho(false, fb.getWidth(), fb.getHeight());
        TextureRegion fbRegion = new TextureRegion(fb.getColorBufferTexture());
        fbRegion.flip(false, true);

        fb.begin();
        polyBatch.setProjectionMatrix(camera.combined);
        polyBatch.begin();

        Polyline exteriorPolyline = level.exteriorBoundry.getPolyline();
        PolygonRegion exteriorPolyReg = new PolygonRegion(
                LudumDare41.game.assets.thumbnailBg,
                exteriorPolyline.getTransformedVertices(),
                triangulator.computeTriangles(exteriorPolyline.getTransformedVertices()).toArray()
        );
        PolygonSprite exteriorPoly = new PolygonSprite(exteriorPolyReg);
        exteriorPoly.setOrigin(0, 0);
        exteriorPoly.setX(xOffset);
        exteriorPoly.draw(polyBatch);

        for (PolylineMapObject boundry : level.boundaries) {
            if (boundry == level.exteriorBoundry) {
                continue;
            }

            Polyline polyline = boundry.getPolyline();
            PolygonRegion polyReg = new PolygonRegion(
                    LudumDare41.game.assets.thumbnailBoundries,
                    polyline.getTransformedVertices(),
                    triangulator.computeTriangles(polyline.getTransformedVertices()).toArray()
            );
            PolygonSprite poly = new PolygonSprite(polyReg);
            poly.setOrigin(0, 0);
            poly.setX(xOffset);
            poly.draw(polyBatch);
        }

        polyBatch.end();

        ShapeRenderer shapes = LudumDare41.game.assets.shapes;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.RED);
        shapes.setProjectionMatrix(camera.combined);
        {
            for (int i = 0; i < level.circles.size; i++){
                Ellipse circle = level.circles.get(i).getEllipse();
                shapes.circle(circle.x + xOffset, circle.y, circle.height / 2);
            }
        }

        shapes.setColor(Color.BLACK);
        shapes.circle(level.hole.position.x - (holeSize/2) + xOffset, level.hole.position.y - (holeSize/2), holeSize);

        shapes.setColor(Color.MAGENTA);
        shapes.triangle(
            level.tee.pos.x + xOffset,
            level.tee.pos.y,
            level.tee.pos.x + xOffset - holeSize,
            level.tee.pos.y + holeSize,
            level.tee.pos.x + xOffset + holeSize,
            level.tee.pos.y + holeSize
        );
        shapes.end();

        fb.end();

        return fbRegion;
    }
}
