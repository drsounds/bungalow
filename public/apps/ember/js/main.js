function createParticleSystem (scene, options) {
    var particleSystem = new BABYLON.ParticleSystem("particles", 2000, scene);

    //Texture of each particle
    particleSystem.particleTexture = new BABYLON.Texture(options.texture, scene);

    // Where the particles come from
    particleSystem.minEmitBox = new BABYLON.Vector3(-1, 0, 0); // Starting all from
    particleSystem.maxEmitBox = new BABYLON.Vector3(2, 0, 0); // To...

// Colors of all particles

    for (var i = 0; i < options.colors.length; i++) {
        var color = options.colors[i];
        particleSystem['color' + (i + 1)] = new BABYLON.Color4(color.r, color.g, color.b, color.a);
    }
    particleSystem.colorDead = new BABYLON.Color4(0, 0, 0.0, 0.0);
    
    // Size of each particle (random between...
    particleSystem.minSize = 0.1;
    particleSystem.maxSize = 11.5;

    // Life time of each particle (random between...
    particleSystem.minLifeTime = 0.3;
    particleSystem.maxLifeTime = 1005;

    // Emission rate
    particleSystem.emitRate = options.emitRate;

    // Blend mode : BLENDMODE_ONEONE, or BLENDMODE_STANDARD
    particleSystem.blendMode = BABYLON.ParticleSystem.BLENDMODE_ONEONE;

    // Set the gravity of all particles
    particleSystem.gravity = new BABYLON.Vector3(0, -9.81, 0);

    // Direction of each particle after it has been emitted
    particleSystem.direction1 = new BABYLON.Vector3(-7, 8, 3);
    particleSystem.direction2 = new BABYLON.Vector3(7, 8, -3);

    // Angular speed, in radians
    particleSystem.minAngularSpeed = 1;
    particleSystem.maxAngularSpeed = Math.PI;

    // Speed
    particleSystem.minEmitPower = options.minEmitPower;
    particleSystem.maxEmitPower = options.maxEmitPower;
    particleSystem.updateSpeed = options.updateSpeed;

    // Start the particle system
    return particleSystem;

    
}

var profiles = {
    'sky': {
        name: 'sky',
        texture: 'textures/flare5.png',
        minEmitPower: 0.5,
        maxEmitPower: 31.5,
        updateSpeed: 0.001,
        emitRate: 11500,
        minEmitBox: 0,
        maxEmitBox: 100,
        colors: [
            {
                r: 0.5,
                g: 0.2,
                b: 0.1,
                a: 1
            },
            {
                r: 1.0,
                g: 0.3,
                b: 0.0,
                a: 1.0
            },
            {
                r: 1.0,
                g: 0.3,
                b: 0.0,
                a: 1.0
            },
            {
                r: 1.0,
                g: 0.3,
                b: 0.0,
                a: 1.0
            }
        ]
    },

    'sea': {
        name: 'sea',
        texture: 'textures/flare2.png',
        minEmitPower: 0.5,
        maxEmitPower: 13.5,
        updateSpeed: 0.005,
        minEmitBox: 0,
        emitRate: 11500,
        maxEmitBox: 100,
        colors: [
            {
                r: 0.0,
                g: 0.5,
                b: 1.0,
                a: 1.0
            },
            {
                r: 1,
                g: 0.5,
                b: 0.0,
                a: 1
            },
            {
                r: 1,
                g: 0,
                b: 0,
                a: 1
            },
            {
                r: 1.0,
                g: 0.3,
                b: 0.0,
                a: 1.0
            },
            {
                r: 1.0,
                g: 0.3,
                b: 0.0,
                a: 1.0
            }
        ]
    },
    'default': {
        name: 'default',
        texture: 'textures/flare5.png',
        minEmitPower: 0.5,
        maxEmitPower: 51.5,
        updateSpeed: 0.001,
        emitRate: 11150,
        minEmitBox: 0,
        maxEmitBox: 100,
        colors: [
            {
                r: 0.6,
                g: 0.5,
                b: 0.1,
                a: 1
            },
            {
                r: 0.2,
                g: 0.77,
                b: 1.0,
                a: 1.0
            },
            {
                r: 0.2,
                g: 0.3,
                b: 0.2,
                a: 1.0
            },
            {
                r: 0.2,
                g: 0.3,
                b: 0.2,
                a: 1.0
            }
        ]
    },
}

window.addEventListener('message', function (event) {
    if (event.data.action === 'navigate') {
        var scheme = event.data.arguments[0];
    // Get the canvas element from our HTML above
        var canvas = document.querySelector("#renderCanvas");

        // Load the BABYLON 3D engine
        var engine = new BABYLON.Engine(canvas, true);
        var createScene = function () {
            var scene = new BABYLON.Scene(engine);
            scene.clearColor = new BABYLON.Color3(0, 0.0, 0.0);
            if (scheme == 'sky') {
                scene.clearColor = new BABYLON.Color3(0.15, 0.15, 0.18); 
            }
            // Setup environment
            var light0 = new BABYLON.PointLight("Omni", new BABYLON.Vector3(0, 2, 8), scene);
            var camera = new BABYLON.ArcRotateCamera("ArcRotateCamera", 1, 0.8, 50, new BABYLON.Vector3(0, 0, -120), scene);
            camera.attachControl(canvas, true);

            // Fountain object
            var fountain = BABYLON.Mesh.CreateBox("foutain", 1.0, scene);
            fountain.material = new BABYLON.StandardMaterial("texture1", scene);
            fountain.material.alpha = 0.0;

            /*// Ground
            var ground = BABYLON.Mesh.CreatePlane("ground", 50.0, scene);
            ground.position = new BABYLON.Vector3(0, -10, 0);
            ground.rotation = new BABYLON.Vector3(Math.PI / 2, 0, 0);

            ground.material = new BABYLON.StandardMaterial("groundMat", scene);
            ground.material.backFaceCulling = false;
            ground.material.diffuseColor = new BABYLON.Color3(0.3, 0.3, 1);
    */
            // Create a particle system

            var profile = profiles['default'];
            if (scheme in profiles) {
                profile = profiles[scheme];
                console.log(profile);
            }

            var particleSystem = createParticleSystem(scene, profile);
            var particleSystem2 = createParticleSystem(scene, profiles['sky']);
            particleSystem.emitter = fountain; // the starting object, the emitter
            particleSystem2.emitter = fountain; // the starting object, the emitter
            // Fountain's animation
            var keys = [];
            var animation = new BABYLON.Animation("animation", "rotation.x", 30, BABYLON.Animation.ANIMATIONTYPE_FLOAT,
                                                                            BABYLON.Animation.ANIMATIONLOOPMODE_CYCLE);
            particleSystem.start();
            particleSystem2.start();
            // At the animation key 0, the value of scaling is "1"
            keys.push({
                frame: 0,
                value: 0
            });

            // At the animation key 50, the value of scaling is "0.2"
            keys.push({
                frame: 50,
                value: Math.PI
            });

            // At the animation key 100, the value of scaling is "1"
            keys.push({
                frame: 100,
                value: 0
            });

            // Launch animation
            animation.setKeys(keys);
            fountain.animations.push(animation);
            scene.beginAnimation(fountain, 0, 100, true);

            return scene;
        }

        var scene = createScene();

        var camera = new BABYLON.ArcRotateCamera("Camera", 0, Math.PI / 2, 13, BABYLON.Vector3.Zero(), scene);

        camera.attachControl(canvas, false);

      

        var renderLoop = function () {
            scene.render();
        };
        engine.runRenderLoop(renderLoop);
    }
});