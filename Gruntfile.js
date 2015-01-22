/*
 * grunt
 * http://gruntjs.com/
 *
 * Copyright (c) 2013 "Cowboy" Ben Alman
 * Licensed under the MIT license.
 * https://github.com/gruntjs/grunt/blob/master/LICENSE-MIT
 */

'use strict';

module.exports = function(grunt) {
grunt.loadNpmTasks('grunt-node-webkit-builder');
    grunt.initConfig({
      nodewebkit: {
        options: {
            platforms: ['osx'],
            buildDir: './webkitbuilds', // Where the build version of my node-webkit app is saved
        },
        src: ['./public/**/*'] // Your node-webkit app
      },
    })
}