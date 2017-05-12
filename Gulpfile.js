// gulpfile.js
var gulp = require('gulp');
var browserify = require('browserify');
var babelify = require('babelify');
var source = require('vinyl-source-stream');
//var sass = require('gulp-sass');
/*
gulp.task('sass', function () {
    gulp.src(__dirname + '/resources/sass/application.scss')
    .pipe(sass().on('error', sass.logError))
    .pipe(gulp.dest(__dirname + '/static/css'));
});
*/
gulp.task('js', function () {
    browserify({
        entries: ['./src/js/main.js'],
        debug: true
    })
    .transform(babelify.configure({
        presets: ['es2017', 'react'],
	plugins: ["transform-es2015-modules-commonjs"]
    }))
    .require('./src/js/main.js', {entry: true})
    .bundle()
    .pipe(source('bundle.js'))
    .pipe(gulp.dest(__dirname + '/public/js'));
    console.log("Task js");
});

gulp.task('default', ['js']);
gulp.task('watch', function () {
  //  gulp.watch(__dirname + '/resources/sass/**/*.scss', ['sass']);
    gulp.watch(__dirname + '/src/js/**/*.js', ['js']);
});