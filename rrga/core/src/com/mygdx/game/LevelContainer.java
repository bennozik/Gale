package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.mygdx.game.model.*;
import com.mygdx.game.model.hazard.NestHazard;
import com.mygdx.game.model.hazard.StaticHazard;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.model.hazard.BirdHazard;
import com.mygdx.game.model.hazard.LightningHazard;
import com.mygdx.game.utility.obstacle.BoxObstacle;
import com.mygdx.game.utility.obstacle.Obstacle;
import com.mygdx.game.utility.obstacle.PolygonObstacle;
import com.mygdx.game.utility.util.PooledList;

public class LevelContainer{
    /**
     * The default value of gravity (going down)
     */
    protected static final float DEFAULT_GRAVITY = -4.9f;

    /**
     * The Box2D world
     */
    protected World world;
    /**
     * The boundary of the world
     */
    protected Rectangle bounds;
    /**
     * The world scale
     */
    protected Vector2 scale;

    /**
     * All the objects in the world.
     */
    protected PooledList<Obstacle> objects;
    /**
     * Queue for adding objects
     */
    protected PooledList<Obstacle> addQueue;
    /**
     * Mark set to handle more sophisticated collision callbacks
     */
    protected ObjectSet<Fixture> sensorFixtures;

    /**
     * The set of all birds currently in the level
     */
    private ObjectSet<BirdHazard> birds;

    /**
     * The set of all moving platforms currently in the level
     */
    private final ObjectSet<MovingPlatformModel> movingPlats;

    /**
     * The set of all lightning currently in the level
     */
    private ObjectSet<LightningHazard> lightnings;

    /**
     * The set of all nests currently in the level
     */
    private ObjectSet<NestHazard> nests;


    /**
     * The texture for walls and platforms
     */
    protected TextureRegion platformTile;

    /**
     * The textures for movable cloud platforms
     */
    private TextureRegion[] cloudPlatformTextures;

    /**
     * Texture asset for character front avatar
     */
    private TextureRegion avatarFrontTexture;
    /**
     * Texture asset for character side avatar
     */
    private TextureRegion avatarSideTexture;
    /**
     * Texture asset for character idle animation
     */
    private TextureRegion avatarIdleTexture;
    /**
     * Texture asset for the wind gust
     */
    private TextureRegion windTexture;
    /**
     * Texture assets for the wind animation
     */
    private TextureRegion[] windAnimation = new TextureRegion[9];
    /**
     * Texture asset for opened umbrella
     */
    private TextureRegion umbrellaOpenTexture;

    /** Texture asset for closed umbrella */
    private TextureRegion umbrellaClosedTexture;

    /** Texture asset for red bird animation */
    private Texture redBirdAnimationTexture;

    /** Texture asset for blue bird animation */
    private Texture blueBirdAnimationTexture;

    /** Texture asset for green bird animation */
    private Texture greenBirdAnimationTexture;

    /** Texture asset for brown bird animation */

    private Texture brownBirdAnimationTexture;

    /** Texture asset for goal */
    private TextureRegion goalTexture;

    /**
     * Texture asset for hp
     */
    private Texture hpTexture;
    /**
     * Texture asset for boost timer
     */
    private Texture boostTexture;
    /**
     * Texture asset for lightning
     */
    private TextureRegion lightningTexture;

    // Start of animation texture
    /**
     * Texture asset for avatar walking animation
     */
    private Texture avatarWalkAnimationTexture;
    /**
     * Texture asset for avatar falling animation
     */
    private Texture avatarFallingAnimationTexture;
    /**
     * Texture asset for umbrella open animation
     */
    private Texture umbrellaOpenAnimationTexture;

    /**
     * Texture asset for umbrella boost animation
     */
    private Texture umbrellaBoostAnimationTexture;
    /**

     * Texture asset for a bird warning
     */
    private Texture warningTexture;

    /**
     * Texture asset for goal animation
     */
    private Texture goalAnimationTexture;
    /**
     * Texture asset for wind animation
     */
    private Texture windAnimationTexture;

    //font for writing player health. temporary solution until a proper health asset is added
    private BitmapFont avatarHealthFont;


    /** Global Physics constants */
    private JsonValue globalConstants;

    /**
     * Reference to the character avatar
     */
    private PlayerModel avatar;
    /**
     * Reference to the umbrella
     */
    private UmbrellaModel umbrella;
    /**
     * Reference to the goalDoor (for collision detection)
     */
    private GoalDoor goalDoor;

    /** reference to the JSON parser */
    private LevelParser parser;


    /**
     * Creates and initialize a new instance of Level Container
     * <p>
     * The game has default gravity and other settings
     */
    public LevelContainer(World world, Rectangle bounds, Vector2 scale) {
        this.world = world;
        this.bounds = bounds;
        this.scale = scale;

        sensorFixtures = new ObjectSet<Fixture>();
        birds = new ObjectSet<>();
        lightnings = new ObjectSet<>();
        movingPlats = new ObjectSet<>();
        nests = new ObjectSet<>();

        objects = new PooledList<Obstacle>();
        addQueue = new PooledList<Obstacle>();
    }

    public void setScale(Vector2 scale) {
        this.scale = scale;
    }

    /**
     * Note: Null texture is returned when color is invalid.
     * @param color the color of the bird
     * @return texture of bird for the given value color.
     */
    private Texture getFlapAnimationTexture(String color){
        switch(color){
            case "red": return redBirdAnimationTexture;
            case "blue": return blueBirdAnimationTexture;
            case "green": return greenBirdAnimationTexture;
            case "brown": return brownBirdAnimationTexture;
            default: return null;
        }
    }

    /**
     * Gather the assets for this controller.
     * <p>
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        globalConstants = directory.getEntry( "global:constants", JsonValue.class);

        // Player Component Textures
        platformTile = new TextureRegion(directory.getEntry("game:newplatform", Texture.class));
        avatarSideTexture = new TextureRegion(directory.getEntry("game:player", Texture.class));
        avatarFrontTexture = new TextureRegion(directory.getEntry("game:front", Texture.class));
        umbrellaOpenTexture = new TextureRegion(directory.getEntry("game:umbrella", Texture.class));
        umbrellaClosedTexture = new TextureRegion(directory.getEntry("game:closed", Texture.class));
        windTexture = new TextureRegion(directory.getEntry("game:wind", Texture.class));
        goalTexture = new TextureRegion(directory.getEntry("game:goal", Texture.class));
        hpTexture = directory.getEntry("game:hp_indicator", Texture.class);
        boostTexture = directory.getEntry("game:boost", Texture.class);
        avatarIdleTexture = new TextureRegion(directory.getEntry("game:player_idle_animation", Texture.class));

        // Hazard Textures
        redBirdAnimationTexture = directory.getEntry("game:red_bird_flapping", Texture.class);
        blueBirdAnimationTexture = directory.getEntry("game:blue_bird_flapping", Texture.class);
        greenBirdAnimationTexture = directory.getEntry("game:green_bird_flapping", Texture.class);
        brownBirdAnimationTexture = directory.getEntry("game:brown_bird_flapping", Texture.class);
        
        warningTexture = directory.getEntry("game:bird_warning", Texture.class);

        lightningTexture = new TextureRegion(directory.getEntry("game:lightning", Texture.class));

        // Animation Textures
        avatarWalkAnimationTexture = directory.getEntry("game:player_walk_animation", Texture.class);
        avatarFallingAnimationTexture = directory.getEntry("game:player_falling_animation", Texture.class);
        umbrellaOpenAnimationTexture = directory.getEntry("game:umbrella_open_animation", Texture.class);
        umbrellaBoostAnimationTexture =  directory.getEntry("game:umbrella_dodge_animation", Texture.class);
        goalAnimationTexture = directory.getEntry("game:goal_animation", Texture.class);
        for(int i = 0; i < 9; i++){
            windAnimation[i] = new TextureRegion(directory.getEntry("game:wind_frame"+i, Texture.class));
        }


        // Fonts
        avatarHealthFont = directory.getEntry("shared:retro", BitmapFont.class);

        // Movable Platforms (clouds)
        cloudPlatformTextures = new TextureRegion[]{
                new TextureRegion(directory.getEntry("platform:cloud0", Texture.class)),
                new TextureRegion(directory.getEntry("platform:cloud1", Texture.class)),
                new TextureRegion(directory.getEntry("platform:cloud2", Texture.class)),
                new TextureRegion(directory.getEntry("platform:cloud3", Texture.class))
        };
    }
    /**
     * Resets the level container (emptying the container)
     */
    public void reset() {
        objects.clear();
        addQueue.clear();
        birds.clear();
        lightnings.clear();
        movingPlats.clear();
        nests.clear();
    }

    /**
     * Lays out the game geography.
     */
    public void populateLevel() {
        // Add level goal
        JsonValue goalconst = globalConstants.get("goal");

        Vector2 goalPos = parser.getGoalPos();
        float dwidth = goalconst.getFloat("width");
        float dheight = goalconst.getFloat("height");
        goalDoor = new GoalDoor(goalconst, goalPos.x, goalPos.y,dwidth, dheight);
        goalDoor.setDrawScale(scale);
        goalDoor.setTexture(goalTexture);
        // doing so fits the texture onto the specified size of the object
        goalDoor.setTextureScale(
                dwidth * scale.x/goalTexture.getRegionWidth(),
                dheight * scale.y/goalTexture.getRegionHeight());
        goalDoor.setAnimation(goalAnimationTexture);
        addObject(goalDoor);

        // Setting Gravity on World
        JsonValue defaults = globalConstants.get("defaults");
        world.setGravity(new Vector2(0, defaults.getFloat("gravity", DEFAULT_GRAVITY)));

        String pname = "platform";
        JsonValue[] plats = parser.getPlatformData();
        JsonValue cur;
        for (int ii = 0; ii < plats.length; ii++) {
            cur = plats[ii];
            PolygonObstacle obj = new PolygonObstacle(cur.get("points").asFloatArray(),
                    cur.getFloat("x"), cur.getFloat("y"));
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(defaults.getFloat("density", 0.0f));
            obj.setFriction(defaults.getFloat("friction", 0.0f));
            obj.setRestitution(defaults.getFloat("restitution", 0.0f));
            obj.setDrawScale(scale);
            obj.setTexture(platformTile);
            obj.setName(pname + ii);
            addObject(obj);
        }

        String mpname = "moving_platform";
        JsonValue[] mPlats = parser.getMovingPlatformData();
        for (int ii = 0; ii < mPlats.length; ii++) {
            cur = mPlats[ii];
            MovingPlatformModel obj = new MovingPlatformModel( cur, cur.get("points").asFloatArray(),
                    cur.getFloat("x"), cur.getFloat("y")
            );
            obj.setBodyType(BodyDef.BodyType.KinematicBody);

            obj.setDensity(defaults.getFloat("density", 0.0f));
            obj.setFriction(defaults.getFloat("friction", 0.0f));
            obj.setRestitution(defaults.getFloat("restitution", 0.0f));
            obj.setDrawScale(scale);
            obj.setTexture(cloudPlatformTextures[cur.getInt("tileIndex")]);
            obj.setName(mpname + ii);
            addObject(obj);
            movingPlats.add(obj);
        }

        // Create wind gusts
        String windName = "wind";
        JsonValue[] windjv = parser.getWindData();
        for (int ii = 0; ii < windjv.length; ii++) {
            WindModel obj;
            obj = new WindModel(windjv[ii]);
            obj.setDrawScale(scale);
            obj.setTexture(windTexture);
            obj.setAnimation(windAnimation);
            obj.setName(windName + ii);
            addObject(obj);
        }

        JsonValue hazardsjv = globalConstants.get("hazards");

        //create hazards
        JsonValue[] hazardData = parser.getStaticHazardData();
        for(int ii = 0; ii < hazardData.length; ii++){
            StaticHazard obj;
            JsonValue jv = hazardData[ii];
            obj = new StaticHazard(jv);
            obj.setDrawScale(scale);
            //temporary texture - just like with platforms, we will have to get this from parsing
            // TODO: get texture for static hazards
            obj.setTexture(lightningTexture);
            obj.setName("static_hazard"+ii);
            addObject(obj);
        }

        //create birds
        String birdName = "bird";
        JsonValue[] birdData = parser.getBirdData();
        int birdDamage = hazardsjv.getInt("birdDamage");
        int birdSensorRadius = hazardsjv.getInt("birdSensorRadius");
        float birdKnockback = hazardsjv.getInt("birdKnockback");
        for (int ii = 0; ii < birdData.length; ii++) {
            BirdHazard obj;
            JsonValue jv = birdData[ii];
            obj = new BirdHazard(jv, birdDamage, birdSensorRadius, birdKnockback, warningTexture);
            obj.setDrawScale(scale);
            obj.setFlapAnimation(getFlapAnimationTexture(jv.getString("color", "red")));
            obj.setWarningAnimation(warningTexture);
            obj.setName(birdName + ii);
            addObject(obj);
            birds.add(obj);
        }

        //TODO
        //create nests




        //create lightning
        String lightningName = "lightning";
        JsonValue[] lightningData = parser.getLightningData();
        for (int ii = 0; ii < lightningData.length; ii++) {
            LightningHazard obj;
            obj = new LightningHazard(lightningData[ii]);
            obj.setDrawScale(scale);
            obj.setTexture(lightningTexture);
            obj.setName(lightningName + ii);
            addObject(obj);
            lightnings.add(obj);
        }

        // Create invisible |_| shaped world boundaries so player is within bounds.
        dwidth = bounds.width;
        dheight = bounds.height;
        String wallName = "barrier";

        // TODO: create some loop, too much duplication.
        // Create the left wall
        BoxObstacle wall = new BoxObstacle(-0.5f, dheight/2f, 1, 2*dheight);
        wall.setDensity(0);
        wall.setFriction(0);
        wall.setBodyType(BodyDef.BodyType.StaticBody);
        wall.setName(wallName);
        wall.setDrawScale(scale);
        addObject(wall);

        // Create the right wall
        wall = new BoxObstacle(dwidth-0.5f, dheight/2f, 1, 2*dheight);
        wall.setDensity(0);
        wall.setFriction(0);
        wall.setBodyType(BodyDef.BodyType.StaticBody);
        wall.setName(wallName);
        wall.setDrawScale(scale);
        addObject(wall);

        // Create the bottom wall
        // TODO: if ground is y-level 0, the wall's y-position should be around [-0.5, -2].
        wall = new BoxObstacle(dwidth/2f, -dheight/2f, dwidth, 1);
        wall.setDensity(0);
        wall.setFriction(0);
        wall.setBodyType(BodyDef.BodyType.StaticBody);
        wall.setName(wallName);
        wall.setDrawScale(scale);
        addObject(wall);

        // Create player
        dwidth = globalConstants.get("player").get("size").getFloat(0);
        dheight = globalConstants.get("player").get("size").getFloat(1);
        avatar = new PlayerModel(globalConstants.get("player"), new Vector2(parser.getPlayerPos()), dwidth, dheight, globalConstants.get("player").getInt("maxhealth"));
        avatar.setDrawScale(scale);
        avatar.setFrontTexture(avatarFrontTexture);
        avatar.setSideTexture(avatarSideTexture);
        avatar.useSideTexture();
        // TODO: (technical) load an HP texture and set texture here
        avatar.setHpTexture(hpTexture);
        avatar.setBoostTexture(boostTexture);
        avatar.setWalkAnimation(avatarWalkAnimationTexture);
        avatar.setFallingAnimation(avatarFallingAnimationTexture);
        avatar.setIdleAnimation(avatarIdleTexture);

        avatar.healthFont = avatarHealthFont;
        addObject(avatar);

        // Create the umbrella
        dwidth = globalConstants.get("umbrella").get("size").getFloat(0);
        dheight = globalConstants.get("umbrella").get("size").getFloat(1);
        umbrella = new UmbrellaModel(
                globalConstants.get("umbrella"),
                new Vector2(parser.getPlayerPos().x, parser.getPlayerPos().y), dwidth, dheight
        );
        umbrella.setDrawScale(scale);
        umbrella.setOpenTexture(umbrellaOpenTexture);
        umbrella.setClosedTexture(umbrellaClosedTexture);
        umbrella.useClosedTexture();
        umbrella.setOpenAnimation(umbrellaOpenAnimationTexture);
        umbrella.setBoostAnimation(umbrellaBoostAnimationTexture);
        umbrella.setClosedMomentum(globalConstants.get("umbrella").getFloat("closedmomentum"));
        addObject(umbrella);
    }

    /**
     * Immediately adds the object to the physics world
     * <p>
     * param obj The object to add
     */
    protected void addObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        objects.add(obj);
        obj.activatePhysics(world);
    }

    /**
     * Adds a physics object in to the insertion queue.
     * <p>
     * Objects on the queue are added just before collision processing.  We do this to
     * control object creation.
     * <p>
     * param obj The object to add
     */
    public void addQueuedObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        addQueue.add(obj);
    }

    /**
     * Returns true if the object is in bounds.
     * <p>
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     * @return true if the object is in bounds.
     */
    public boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x + bounds.width);
        boolean vert = (bounds.y <= obj.getY() && obj.getY() <= bounds.y + bounds.height);
        return horiz && vert;
    }

    /**
     * Dispose of all (non-static) resources allocated to this container
     *
     * empties all game objects.
     */
    public void dispose() {
        objects.clear();
        addQueue.clear();
        birds.clear();
        lightnings.clear();
        nests.clear();


        objects = null;
        addQueue = null;
        bounds = null;
        scale = null;
        world = null;
        birds = null;
        lightnings = null;
        nests = null;
    }
    /**
     * Get world object
     * @return world
     */
    public World getWorld() {
        return world;
    }
    /**
     * Get player object
     * @return avatar
     */
    public PlayerModel getAvatar() {
        return avatar;
    }
    /**
     * Get umbrella object
     * @return umbrella
     */
    public UmbrellaModel getUmbrella() {
        return umbrella;
    }
    /**
     * Get goalDoor object
     * @return goalDoor
     */
    public BoxObstacle getGoalDoor() {
        return goalDoor;
    }
    /**
     * Get objects
     * @return objects
     */
    public PooledList<Obstacle> getObjects() {
        return objects;
    }
    /**
     * Get birds
     * @return birds
     */
    public ObjectSet<BirdHazard> getBirds() {
        return birds;
    }
    /**
     * Get lightnings
     * @return lightnings
     */
    public ObjectSet<LightningHazard> getLightnings() {
        return lightnings;
    }
    /**
     * Get nests
     * @return nests
     */
    public ObjectSet<NestHazard> getNests() {
        return nests;
    }
    /**
     * Get moving platforms
     * @return movingPlats
     */
    public ObjectSet<MovingPlatformModel> getMovingPlats(){return movingPlats;}

    public void setParser(LevelParser parser) { this.parser = parser; }

    /**
     * Set world
     */
    public void setWorld(World worldObj) { world = worldObj; }
    /**
     * Set player object
     */
    public void setAvatar(PlayerModel avatarObj) {
        avatar = avatarObj;
    }
    /**
     * Set umbrella object
     */
    public void setUmbrella(UmbrellaModel umbrellaObj) {
        umbrella = umbrellaObj;
    }
    /**
     * Set objects
     */
    public void setObjects(PooledList<Obstacle> allObjects) {
        objects = allObjects;
    }
    /**
     * Set birds
     */
    public void setBirds(ObjectSet<BirdHazard> birdsObj) {
        birds = birdsObj;
    }
    /**
     * Set lightnings
     */
    public void setLightnings(ObjectSet<LightningHazard> lightningsObj) {
        lightnings = lightningsObj;
    }



}
