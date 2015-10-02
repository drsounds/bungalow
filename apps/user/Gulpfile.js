var gulp = require('gulp');

var browserify = require('browserify');
var source = require('vinyl-source-stream');
var babelify = require('babelify');
gulp.task('js', function () {
    return browserify('./src/js/app.js')
        .transform(babelify)
        .bundle()
        .pipe(source('bundle.js'))
        .pipe(gulp.dest('./'))
});

gulp.task('resolver', function () {
    return browserify('./src/js/resolver.js')
        .transform(babelify)
        .bundle()
        .pipe(source('resolver.js'))
        .pipe(gulp.dest('./'))
});

gulp.task('default', ['js', 'resolver']);