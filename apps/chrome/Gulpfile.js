var gulp = require('gulp');

var browserify = require('browserify');
var source = require('vinyl-source-stream');
var babelify = require('babelify');
gulp.task('js', function () {
    return browserify('./assets/scripts/app.js')
        .transform(babelify)
        .bundle()
        .pipe(source('bundle.js'))
        .pipe(gulp.dest('./'))
});

gulp.task('default', ['js']);