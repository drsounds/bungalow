(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
'use strict';

var Cosmos = require('@bungalow/cosmos');
exports2.requireUri = function (uri) {
    return new Promise(function (resolve, fail) {});
};

},{"@bungalow/cosmos":2}],2:[function(require,module,exports){
"use strict";

var Cosmos = function Cosmos() {};

Cosmos.request = function (method, url, params, data) {
    return new Promise(function (resolve, fail) {
        var xhr = new XMLHttpRequest();
        console.log("Sending request");
        xhr.onreadystatechange = function () {
            if (xhr.readyState == 4 && xhr.status == 200) {
                var data = xhr.response;
                console.log("Resolved url " + url);
                resolve(data);
            }
        };
        xhr.responseType = 'json';
        console.log(method, url);
        xhr.open(method, '/api' + url, true);
        xhr.send(data);
        console.log(data);
    });
};

module.exports = Cosmos;

},{}]},{},[1]);
