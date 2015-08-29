// Get the canvas element from our HTML above
    var canvas = document.querySelector("#renderCanvas");

    // Load the BABYLON 3D engine
    var engine = new BABYLON.Engine(canvas, true);
    var createScene = function () {
    var particleSystem = new BABYLON.ParticleSystem("particles", 2000, scene, customEffect);
        particleSystem.particleTexture = new BABYLON.Texture("Flare.png", scene);
    }